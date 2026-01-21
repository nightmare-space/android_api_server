#!/bin/bash

# filepath: /Users/lori/Desktop/nightmare-space/android_api_server/aas/build.sh
LOCAL_DIR=$(
    cd $(dirname $0)
    pwd
)
# 设置 Android NDK 路径
NDK_PATH=/Volumes/Mac/Android/sdk/ndk/26.3.11579264
if [ -z "$NDK_PATH" ]; then
    echo "请设置 NDK_PATH 为 Android NDK 的路径"
    exit 1
fi
cd aas/src/main/cpp
# 创建构建目录
BUILD_DIR=build
mkdir -p $BUILD_DIR
cd $BUILD_DIR

# 配置 CMake
cmake -DCMAKE_TOOLCHAIN_FILE=$NDK_PATH/build/cmake/android.toolchain.cmake \
    -DANDROID_ABI=arm64-v8a \
    -DANDROID_PLATFORM=android-21 \
    ..

# 编译项目
cmake --build .

echo "编译完成，输出文件位于: $(pwd)/libaas.so"
SO=$(pwd)/libaas.so
cd $LOCAL_DIR

cp $SO build/libaas.so