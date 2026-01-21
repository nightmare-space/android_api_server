#include <jni.h>
#include <stdlib.h>
#include <unistd.h>

// 必须加声明！否则 C++ 找不到符号
extern "C" {
int virtual_mouse_open();
void virtual_mouse_move(int dx, int dy);
void virtual_mouse_left_click();
void virtual_mouse_close();
}

#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <linux/uinput.h>
#include <sys/ioctl.h>
#include <sys/time.h>

// ==============================
// JNI 示例函数（无关本问题）
// ==============================
extern "C"
JNIEXPORT jlong JNICALL
Java_com_nightmare_aas_JNIBridge_sumSquaresNative(JNIEnv *env, jclass clazz, jint n) {
    long long sum = 0;
    for (int i = 1; i <= n; i++) sum += (long long)i * i;
    return sum;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_nightmare_aas_JNIBridge_monteCarloNative(JNIEnv *env, jclass clazz, jint samples) {
    int insideCircle = 0;
    for (int i = 0; i < samples; i++) {
        double x = (double) rand() / RAND_MAX;
        double y = (double) rand() / RAND_MAX;
        if (x * x + y * y <= 1.0) insideCircle++;
    }
    return 4.0 * insideCircle / samples;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_nightmare_aas_JNIBridge_fibNative(JNIEnv *env, jclass clazz, jint n) {
    if (n <= 1) return n;
    long long a = 0, b = 1;
    for (int i = 2; i <= n; i++) {
        long long t = a + b;
        a = b;
        b = t;
    }
    return b;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_nightmare_aas_JNIBridge_setUid(JNIEnv *env, jclass clazz, jint uid) {
    return (setuid(uid) == 0) ? JNI_TRUE : JNI_FALSE;
}

// ==============================
// JNI 调用虚拟鼠标
// ==============================
extern "C"
JNIEXPORT jint JNICALL
Java_com_nightmare_aas_JNIBridge_nativeOpen(JNIEnv *env, jobject thiz) {
    return virtual_mouse_open();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_nightmare_aas_JNIBridge_nativeMove(JNIEnv *env, jobject thiz, jint dx, jint dy) {
    virtual_mouse_move(dx, dy);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_nightmare_aas_JNIBridge_nativeClickLeft(JNIEnv *env, jobject thiz) {
    virtual_mouse_left_click();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_nightmare_aas_JNIBridge_nativeClose(JNIEnv *env, jobject thiz) {
    virtual_mouse_close();
}

// ==============================
// C 层虚拟鼠标实现
// ==============================
#define EV_SYN 0x00
#define EV_KEY 0x01
#define EV_REL 0x02

#define REL_X  0x00
#define REL_Y  0x01

#define BTN_LEFT  0x110
#define BTN_RIGHT 0x111

static int uinput_fd = -1;

static void emit(int fd, int type, int code, int value) {
    struct input_event ev {};
    struct timeval tv;
    gettimeofday(&tv, NULL);

    ev.time = tv;
    ev.type = type;
    ev.code = code;
    ev.value = value;

    write(fd, &ev, sizeof(ev));
}

static void sync_event(int fd) {
    emit(fd, EV_SYN, 0, 0);
}

extern "C"
int virtual_mouse_open() {
    uinput_fd = open("/dev/uinput", O_WRONLY | O_NONBLOCK);
    if (uinput_fd < 0) {
        perror("open /dev/uinput");
        return -1;
    }

    ioctl(uinput_fd, UI_SET_EVBIT, EV_KEY);
    ioctl(uinput_fd, UI_SET_KEYBIT, BTN_LEFT);

    ioctl(uinput_fd, UI_SET_EVBIT, EV_REL);
    ioctl(uinput_fd, UI_SET_RELBIT, REL_X);
    ioctl(uinput_fd, UI_SET_RELBIT, REL_Y);

    struct uinput_setup usetup {};
    snprintf(usetup.name, UINPUT_MAX_NAME_SIZE, "c_virtual_mouse");
    usetup.id.bustype = BUS_USB;
    usetup.id.vendor  = 0x1234;
    usetup.id.product = 0x5678;

    ioctl(uinput_fd, UI_DEV_SETUP, &usetup);
    ioctl(uinput_fd, UI_DEV_CREATE);

    usleep(100000);
    return 0;
}

extern "C"
void virtual_mouse_move(int dx, int dy) {
    emit(uinput_fd, EV_REL, REL_X, dx);
    emit(uinput_fd, EV_REL, REL_Y, dy);
    sync_event(uinput_fd);
}

extern "C"
void virtual_mouse_left_click() {
    emit(uinput_fd, EV_KEY, BTN_LEFT, 1);
    sync_event(uinput_fd);
    emit(uinput_fd, EV_KEY, BTN_LEFT, 0);
    sync_event(uinput_fd);
}

extern "C"
void virtual_mouse_close() {
    if (uinput_fd >= 0) {
        ioctl(uinput_fd, UI_DEV_DESTROY);
        close(uinput_fd);
        uinput_fd = -1;
    }
}


static const char *EVENT_PATH = "/dev/input/event12";

static void send_event(int fd, __u16 type, __u16 code, __s32 value) {
    struct input_event ev;
    gettimeofday(&ev.time, NULL);
    ev.type = type;
    ev.code = code;
    ev.value = value;
    write(fd, &ev, sizeof(ev));
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_nightmare_aas_JNIBridge_nativeMoveMouse(JNIEnv *env, jclass clazz, jint dx, jint dy) {
    int fd = open(EVENT_PATH, O_WRONLY);
    if (fd < 0) {
        perror("open event");
        return -1;
    }

    send_event(fd, EV_REL, REL_X, dx);
    send_event(fd, EV_REL, REL_Y, dy);
    send_event(fd, EV_SYN, SYN_REPORT, 0);

    close(fd);
    return 0;
}