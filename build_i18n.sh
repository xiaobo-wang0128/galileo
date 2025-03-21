
WORKSPACE=$(cd "$(dirname "$0")"; pwd)

cd ${WORKSPACE}/galileo-vue/galileo-i18n-vue

mkdir -p ${WORKSPACE}/galileo-vue/galileo-i18n-vue/static

npm i

rm -rf ./dist/*

npm run build

mkdir -p ${WORKSPACE}/galileo-i18n-server/src/main/resources/views/screen

rm -rf ${WORKSPACE}/galileo-i18n-server/src/main/resources/views/screen/index.vm

cp ${WORKSPACE}/galileo-vue/galileo-i18n-vue/dist/index.html ${WORKSPACE}/galileo-i18n-server/src/main/resources/views/screen/index.vm

rm -rf ${WORKSPACE}/galileo-i18n-server/src/main/resources/statics

cp -r ${WORKSPACE}/galileo-vue/galileo-i18n-vue/dist/statics ${WORKSPACE}/galileo-i18n-server/src/main/resources/

# java jar包发布

cd ${WORKSPACE}

mvn clean package -pl galileo-i18n-server -am -Dmaven.test.skip=true

REMOTE_SERVER=168.168.33.208

ssh vot@${REMOTE_SERVER} rm -rf /home/data/workspace/i18n-server/*.jar

scp ${WORKSPACE}/galileo-i18n-server/target/i18n-server.jar vot@${REMOTE_SERVER}:/home/data/workspace/i18n-server

ssh vot@${REMOTE_SERVER} docker-compose -f /home/data/workspace/i18n-server/docker-compose.yml down

ssh vot@${REMOTE_SERVER} docker-compose -f /home/data/workspace/i18n-server/docker-compose.yml up -d

