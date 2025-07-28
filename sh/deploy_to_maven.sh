
WORKSPACE=$(cd "$(dirname "$0")"; pwd)

echo ${WORKSPACE}

cd ${WORKSPACE}

cd ..

mvn clean deploy -pl ./pom.xml -am

mvn clean deploy -pl galileo-framework/galileo-autoconfig,galileo-framework/galileo-docs,galileo-framework/galileo-es-sdk,galileo-framework/galileo-flow,galileo-framework/galileo-itextpdf,galileo-framework/galileo-miniwebx,galileo-framework/galileo-mybatis,galileo-framework/galileo-open-sdk,galileo-framework/galileo-plugin,galileo-framework/galileo-rainbow,galileo-framework/galileo-tool -am -Dmaven.test.skip=true

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

