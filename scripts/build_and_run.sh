LOCAL_DIR=$(
    cd $(dirname $0)
    pwd
)
SERIAL=$1
if [ -n "$SERIAL" ]; then
    export ANDROID_SERIAL=$SERIAL
    echo "使用设备: $SERIAL"
fi
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
        echo
    else
        echo "服务已启动，PID:$awk_result 停止中"
        KILL_CMD="echo $awk_result | xargs kill -9"
        echo "$awk_result" | adb shell "xargs kill -9"
    fi
}
kill_all_server(){
    #  use adb shell pidof com.nightmare.aas get pid
    echo "尝试停止所有 aas 相关进程"
    pidof_result=$(adb shell pidof com.nightmare.aas)
    if [ -z "$pidof_result" ]; then
        echo "没有找到 aas 相关进程"
    else
        echo "找到 aas 相关进程 PID:$pidof_result 停止中"
        echo $pidof_result | adb shell xargs kill -9
        # use su -c to kill
        echo $pidof_result | adb shell su -c "xargs kill -9"
    fi
}
kill_all_server



# echo "$lsof_result"
# adb shell 'lsof | awk -v uid=shell "\$3 == uid" | grep 15000 | awk "{print \$2}" | xargs kill '
# # 取MD5的前8位
SERVER_PATH=$LOCAL_DIR/build/app_server
MD5=$(md5sum $SERVER_PATH | cut -d ' ' -f1 | cut -c 1-8)
NAP="$HOME/Desktop/nightmare-app"
ADB_KIT_ASSETS="$NAP/adb_kit/assets/app_server"
UNCON_ASSETS="$NAP/uncon/assets/app_server"
SULA_ASSETS="$NAP/neo_desktop"
cp $SERVER_PATH $ADB_KIT_ASSETS
cp $SERVER_PATH $UNCON_ASSETS
cp $SERVER_PATH $SULA_ASSETS
echo MD5:$MD5
devices=`adb devices | grep -v List | grep device | wc -l`
echo devices:$devices
adb shell 'rm -rf /data/local/tmp/app_server*'
adb push "build/app_server" /data/local/tmp/app_server$MD5
adb push "build/libaas.so" /data/local/tmp/libaas.so
$LOCAL_DIR/forward_port.sh
TARGET_SERVER_PATH=/data/local/tmp/app_server$MD5
echo TARGET_SERVER_PATH:$TARGET_SERVER_PATH
# adb shell app_process -Djava.net.preferIPv4Stack=true -Djava.library.path=/data/local/tmp/ -Djava.class.path=/data/local/tmp/app_server$MD5 /system/bin --nice-name=com.nightmare.aas com.nightmare.aas_integrated.AASIntegrate sula
# adb shell app_process -Djava.library.path=/data/local/tmp/ -Djava.class.path=$TARGET_SERVER_PATH /system/bin --nice-name=com.nightmare.aas com.nightmare.aas_integrated.AASIntegrate .
CMD="app_process -Djava.library.path=/data/local/tmp/ -Djava.class.path=$TARGET_SERVER_PATH /system/bin --nice-name=com.nightmare.aas com.nightmare.aas_integrated.AASIntegrate ."
echo CMD: $CMD
# adb shell su -c "$CMD"
adb shell "$CMD"
# app_process -Djava.class.path=/data/app/~~IUxvJryjtXzdl9oNx4Y4lw==/com.nightmare.sula-Dgfc-gIAFHr5s17sRvIVXg==/base.apk /system/bin --nice-name=com.nightmare.aas com.nightmare.aas_integrated.AASIntegrate sula


# 哦 我记得在root下不切换到 shell 执行一些代码会有问题


# 尝试反编译修改
# https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/services/core/java/com/android/server/display/mode/DisplayModeDirector.java;l=382;drc=61197364367c9e404c7da6900658f1b16c42d0da;bpv=0;bpt=1