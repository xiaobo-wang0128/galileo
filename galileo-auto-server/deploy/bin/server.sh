
APP_PATH=$(cd "$(dirname "$0")"; pwd)
APP_PATH=${APP_PATH%%bin*}

HTTP_PORT=8101

JAR_NAME=galileo-auto-server.jar

SPRING_PROFILE=online

JVM_HEAD="-Xms1g -Xmx1g"

JVM_END="-XX:NewRatio=1 -XX:SurvivorRatio=6 -XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=512m -XX:MaxDirectMemorySize=256m -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:CMSMaxAbortablePrecleanTime=5000 -XX:+CMSClassUnloadingEnabled -XX:CMSInitiatingOccupancyFraction=80 -XX:+UseCMSInitiatingOccupancyOnly -XX:+ExplicitGCInvokesConcurrent -Dsun.rmi.dgc.client.gcInterval=72000000 -XX:ParallelGCThreads=4 -verbose:gc -Xloggc:${APP_PATH}logs/java_gc.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+PrintHeapAtGC -XX:HeapDumpPath=${APP_PATH}logs/java_heapdump.hprof -XX:+HeapDumpOnOutOfMemoryError -XX:NativeMemoryTracking=detail -Dfile.encoding=UTF-8 -Djava.awt.headless=true -Dsun.net.client.defaultConnectTimeout=10000 -Dsun.net.client.defaultReadTimeout=30000 -Dtomcat.monitor.http.binding.host=127.0.0.1  -Dspring.profiles.active=${SPRING_PROFILE} -Dhaiq.app.home=${APP_PATH}  -Xbootclasspath/a:${APP_PATH}conf -jar ${APP_PATH}lib/${JAR_NAME} --server.port=${HTTP_PORT} --server.tomcat.uri-encoding=UTF-8 --server.tomcat.max-threads=400"

JAVA_OPTION=${JVM_HEAD}" "${JVM_END}

PROJECT_NAME_DIR=$(basename $APP_PATH)

cd ${APP_PATH}

function startServer {
	
	#HTTP_PORT_cmd="netstat -tln | grep $HTTP_PORT "
	#dubbo_port_cmd="netstat -tln | grep $dubbo_port "
	linux_core=`uname`
	if [ "$linux_core" == "Darwin" ]; then
		#判断 api rest 端口是否被占用
		if [ -n "$HTTP_PORT" ]; then
		    SERVER_PORT_COUNT=`lsof -i tcp:$HTTP_PORT | wc -l`
		    if [ $SERVER_PORT_COUNT -gt 0 ]; then
		        echo "ERROR: port $HTTP_PORT already used!"
		        exit 1
		    fi
		fi

	else
		#判断 api rest 端口是否被占用
		if [ -n "$HTTP_PORT" ]; then
		    SERVER_PORT_COUNT=`netstat -tln | grep $HTTP_PORT | wc -l`
		    if [ $SERVER_PORT_COUNT -gt 0 ]; then
		        echo "ERROR: port $HTTP_PORT already used!"
		        exit 1
		    fi
		fi
	
	fi
	
	#执行java命令启动服务
	echo -e "Starting the ${PROJECT_NAME_DIR} ...\c"
	
	nohup java ${JAVA_OPTION} > ${APP_PATH}logs/app.log 2>&1  &
		
	#监听启动过程

	COUNT=0
	SLEEPTIME=0
	while [ $COUNT -lt 1 ]; do    
	    echo -e ".\c"
	    sleep 1


	    if [ -n "$HTTP_PORT" ]; then
	    	if [ "$linux_core" == "Darwin" ]; then
	    		COUNT=`lsof -i tcp:$HTTP_PORT | wc -l`
	    	else
	    		COUNT=`netstat -tln | grep $HTTP_PORT | wc -l`
	    	fi
	    else
	    	COUNT=`ps -f | grep java | grep "$APP_PATH" | awk '{print $2}' | wc -l`
	    fi
	    if [ $COUNT -gt 0 ]; then
	        break
	    fi
	    if [ $SLEEPTIME -gt 60 ]; then
	    	echo "fail"
	    	echo "ERROR: start timeout"
	    	
	        break
	    fi
	    ((SLEEPTIME=SLEEPTIME+1));
	done

	if [ $COUNT -gt 0 ]; then
		echo "OK!"
		echo "${PROJECT_NAME_DIR} started success."
    fi

 
}


function startQuick {
	java ${JAVA_OPTION} > ${APP_PATH}logs/app.log &
}


function stopServer {
	
	echo -e "Shutdown ${PROJECT_NAME_DIR} ...\c"

	PROCESS=`ps -ef|grep java|grep ${APP_PATH}|grep -v grep|grep -v PID|grep -v $$|awk '{ print $2}'`

	# length=${#PROCESS[@]}
	# echo $length
	for i in $PROCESS
	do
		kill $i
	done

	SLEEPTIME=0
	COUNT=1
	while [ $COUNT -gt 0 ]; do    
	    echo -e ".\c"
	    sleep 1 

	    COUNT=`ps -f | grep java | grep classpath | grep "$APP_PATH" | awk '{print $2}' | wc -l`
	    
	    if [ $COUNT == 0 ]; then
	        break
	    fi

	    if [ $SLEEPTIME -gt 60 ]; then
	    	echo "进程kill失败，尝试 kill -9 命令"

	    	PROCESS=`ps -ef|grep java|grep ${APP_PATH}|grep -v grep|grep -v PID|grep -v $$|awk '{ print $2}'`
			for i in $PROCESS
			do
				kill -9 $i
			done

	    	((SLEEPTIME=0));
	        break
	    fi
	    ((SLEEPTIME=SLEEPTIME+1));
	done

	echo "DONE!"
	sleep 1 
	echo ${PROJECT_NAME_DIR}" has stoped."
}


##################################
########### 脚本入口###############
##################################

if [ "$1" == "start" ]; then
	startServer
	exit;
elif [ "$1" == "stop" ]; then
	stopServer
	exit;

elif [ "$1" == "quick" ]; then
	startQuick
	exit;
elif [ "$1" == "restart" ]; then
	stopServer
	startServer
	exit;
elif [ "$1" == "debug" ]; then
	if [ "$2" == "" ]; then
		echo "ERROR: need debug socket_port, like: ./server.sh debug 9901"
		exit 1
	fi

	DEBUG_PORT=$2
	JAVA_OPTION=${JVM_HEAD}" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address="${DEBUG_PORT}" "${JVM_END}
	startServer
	exit;

else
	echo "Usage: ./server.sh { start | restart | stop | debug port }"
	exit 1;
fi


