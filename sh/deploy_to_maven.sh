
WORKSPACE=$(cd "$(dirname "$0")"; pwd)

# 前端 vue 打包

#cd ${WORKSPACE}/galileo-vue/galileo-portal-vue
#
#mkdir -p ${WORKSPACE}/galileo-vue/galileo-portal-vue/static
#
#npm i
#
#rm -rf ./dist/*
#
#npm run build
#
#rm -rf ${WORKSPACE}/galileo-portal/src/main/resources/views/screen/index.vm
#
#cp ${WORKSPACE}/galileo-vue/galileo-portal-vue/dist/index.html ${WORKSPACE}/galileo-portal/src/main/resources/views/screen/index.vm
#
#rm -rf ${WORKSPACE}/galileo-portal/src/main/resources/statics
#
#cp -r ${WORKSPACE}/galileo-vue/galileo-portal-vue/dist/statics ${WORKSPACE}/galileo-portal/src/main/resources/

# java jar包发布

cd ${WORKSPACE}

mvn clean deploy -pl ./pom.xml -am

mvn clean deploy -pl galileo-itextpdf,galileo-miniwebx,galileo-flow,galileo-rainbow,galileo-tool,galileo-open-sdk,galileo-es-sdk,galileo-scan,galileo-scan-model -am -Dmaven.test.skip=true

#mvn clean deploy -pl galileo-autoconfig -am -Dmaven.test.skip=true
#mvn clean deploy -pl galileo-flow -am -Dmaven.test.skip=true
#mvn clean deploy -pl galileo-miniwebx -am -Dmaven.test.skip=true
#mvn clean deploy -pl galileo-mybatis -am -Dmaven.test.skip=true
#mvn clean deploy -pl galileo-plugin -am -Dmaven.test.skip=true
#mvn clean deploy -pl galileo-rainbow -am -Dmaven.test.skip=true
#mvn clean deploy -pl galileo-tool -am -Dmaven.test.skip=true
#mvn clean deploy -pl galileo-sdk -am -Dmaven.test.skip=true
#mvn clean deploy -pl galileo-portal -am -Dmaven.test.skip=true
#mvn clean deploy -pl galileo-i18n -am -Dmaven.test.skip=true
# mvn clean deploy -pl galileo-user -am -Dmaven.test.skip=true

