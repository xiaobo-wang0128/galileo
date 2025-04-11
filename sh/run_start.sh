
WORKSPACE=$(cd "$(dirname "$0")"; pwd)

cd ${WORKSPACE}

mvn clean package -Dmaven.test.skip=true

java -jar -javaagent:/Users/wangxiaobo/work/app/skywalking/skywalking-agent.jar -Dskywalking.agent.service_name=iwms-core -Dskywalking.collector.backend_service=skywalking-oap:11800 -Dskywalking.agent.span_limit_per_segment=2000 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=27017 -Djava.rmi.server.hostname=jvm.dev.com -Dcom.sun.management.jmxremote.rmi.port=27017 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar ./galileo-start/target/galileo-start.jar