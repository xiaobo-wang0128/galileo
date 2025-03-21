

APP_PATH=$(cd "$(dirname "$0")"; pwd)

cd ${APP_PATH}

npm run build

cd ./dist

zip -r upload.zip ./* 

scp ./upload.zip hairou@172.20.8.210:/home/hairou/ess_front

ssh hairou@172.20.8.210 unzip -o /home/hairou/ess_front/upload.zip -d /home/hairou/ess_front/  