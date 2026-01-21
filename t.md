因为目前我还没想好Sunshine Android的存在形式，是apk或是由adb启动的二进制，或是二者都可

区别在于，如果是apk，则apk需要向用户申请录屏权限，此时在Android上的可读目录`/data/data/com.xxx/files`

如果是二进制由adb启动，则不需要任何权限，此时在Android上的可读目录为`/data/local/tmp`

二者的可读目录是不同的，所以这里我想等我时间充裕一点再来实现

关于CI，由上面的回复，如果是二进制，则需要用NDK编译整个Sunshine，这比较复杂，有很多额外的代码需要编写，如果是Apk，需要引入gradle来编译，但就目前来看，还是很初步的阶段，我只是将一部分兼容android的代码合并到了Sunshine，但是目前是无法依靠这些编译出apk或者二进制的，所以CI需要等整个兼容Android的代码完成后才能编写，最初步的想法是，兼容Android的同时不影响Sunshine原有的任何代码
