
WORKSPACE=$(cd "$(dirname "$0")"; pwd)

cd ${WORKSPACE}/galileo-vue/galileo-portal-vue

npm i

rm -rf ./dist/*

npm run build


rm -rf ${WORKSPACE}/galileo-portal/src/main/resources/views/screen/*.vm

cp ${WORKSPACE}/galileo-vue/galileo-portal-vue/dist/index.html ${WORKSPACE}/galileo-portal/src/main/resources/views/screen/galileo_portal_index.vm

rm -rf ${WORKSPACE}/galileo-portal/src/main/resources/statics

cp -r ${WORKSPACE}/galileo-vue/galileo-portal-vue/dist/statics ${WORKSPACE}/galileo-portal/src/main/resources/

#sed -i 's/\/armada_galileo/${portalContextPath}/' ${WORKSPACE}/galileo-portal/src/main/resources/views/screen/index.vm