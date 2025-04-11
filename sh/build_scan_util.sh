mvn clean package -pl galileo-scan -am -Dmaven.test.skip=true

mkdir -p ~/galileo/

REMOTE_SERVER=root@jenkins.okios.cn

ssh ${REMOTE_SERVER} rm -rf /home/vot/galileo/galileo-scan-util.jar

scp ./galileo-scan/target/galileo-scan-util.jar ${REMOTE_SERVER}:/home/vot/galileo/
