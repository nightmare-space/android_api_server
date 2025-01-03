LOCAL_DIR=$(
    cd $(dirname $0)
    pwd
)
PROJ_DIR=$LOCAL_DIR/../
cd $PROJ_DIR
$LOCAL_DIR/build_without_gradle.sh
result=$?
# if [ $result -ne 0 ]; then
#     echo "编译失败"
#     exit 1
# fi
cd $LOCAL_DIR
lsof_result=$(adb shell 'lsof')
kill_server(){
    port=$1
    # 安卓低版本没有awk
    awk_result=$(echo "$lsof_result" | awk -v uid=shell '$3 == uid' | grep $port | awk "{print \$2}")
    if [ -z "$awk_result" ]; then
        echo "服务未启动，启动中"
    else
        echo "服务已启动，PID:$awk_result 重启中"
        echo "$awk_result" | adb shell "xargs kill -9"
    fi
}

for port in $(seq 15000 15040); do
    kill_server $port
done

# echo "$lsof_result"
# adb shell 'lsof | awk -v uid=shell "\$3 == uid" | grep 15000 | awk "{print \$2}" | xargs kill '
# # 取MD5的前8位
SERVER_PATH=$LOCAL_DIR/build/app_server
MD5=$(md5sum $SERVER_PATH | cut -d ' ' -f1 | cut -c 1-8)
ADB_KIT_ASSETS="/Users/nightmare/Desktop/nightmare-core/adb_kit/assets/app_server"
UNCON_ASSETS="/Users/nightmare/Desktop/nightmare-core/uncon/assets/app_server"
SULA_ASSETS="/Users/nightmare/Desktop/nightmare-space/super_launcher/assets/sula_app_server"
cp $SERVER_PATH $ADB_KIT_ASSETS
cp $SERVER_PATH $UNCON_ASSETS
cp $SERVER_PATH $SULA_ASSETS
echo MD5:$MD5
devices=`adb devices | grep -v List | grep device | wc -l`
echo devices:$devices
adb shell 'rm -rf /data/local/tmp/app_server*'
adb push "build/app_server" /data/local/tmp/app_server$MD5
adb forward tcp:15000 tcp:15000
adb shell app_process -Djava.net.preferIPv4Stack=true -Djava.class.path=/data/local/tmp/app_server$MD5 /system/bin --nice-name=com.nightmare.aas com.nightmare.aas_integrated.AASIntegrate .



