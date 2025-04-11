
WORKSPACE=$(cd "$(dirname "$0")"; pwd)

# java jar包发布
cd ${WORKSPACE}

mvn clean package -pl galileo-auto-server -am -Dmaven.test.skip=true

REMOTE_SERVER=168.168.33.206

REMOTE_SERVER_PATH=/data/workspace/galileo-auto-server

ssh vot@${REMOTE_SERVER} rm -rf ${REMOTE_SERVER_PATH}/*.jar

scp ${WORKSPACE}/galileo-auto-server/target/galileo-auto-server.jar vot@${REMOTE_SERVER}:${REMOTE_SERVER_PATH}

ssh vot@${REMOTE_SERVER} docker-compose -f ${REMOTE_SERVER_PATH}/docker-compose.yml down

ssh vot@${REMOTE_SERVER} docker-compose -f ${REMOTE_SERVER_PATH}/docker-compose.yml up -d

