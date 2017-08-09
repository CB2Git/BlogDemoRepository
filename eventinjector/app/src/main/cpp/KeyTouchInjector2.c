#include <stdlib.h>
#include <sys/wait.h>
#include <android/log.h>
#include <jni.h>
#include <fcntl.h>
#include <linux/uinput.h>
#include <linux/input.h>
#include <stdio.h>

#define TAG "KeyTouchInjector2::JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

unsigned char execCmd(const char *cmd) {
    int status = system(cmd);
    if (-1 == status) {
        status = 0;
    } else {
        if (WIFEXITED(status)) {
            if (0 == WEXITSTATUS(status)) {
                status = 1;
            } else {
                status = 0;
            }
        }
    }
    LOGI("cmd=%s,result=%s", cmd, status == 1 ? "ok" : "error");
    return (unsigned char) status;
}


void send_event(int fd, uint16_t type, uint16_t code, int32_t value) {
    LOGI("SendEvent call (%04X,%04X,%04X)", type, code, value);
    if (fd <= fileno(stderr)) return;

    struct input_event event;

    memset(&event, 0, sizeof(event));
    gettimeofday(&event.time, NULL);

    // event (type, code, value)
    event.type = type;
    event.code = code;
    event.value = value;
    if (write(fd, &event, sizeof(event)) < 0) {
        LOGE("send_event error,error = %s", strerror(stderr));
    }
}

int fd;

JNIEXPORT jboolean JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector2_initInjector(JNIEnv *env, jclass type,
                                                                jint version) {

    if (version >= 18) {
        execCmd("su -c 'chmod 666 /dev/uinput;echo 0 > /sys/fs/selinux/enforce'");
    } else {
        execCmd("su -c 'chmod 666 /dev/uinput'");
    }

    fd = open("/dev/uinput", O_WRONLY | O_NONBLOCK);

    struct uinput_user_dev upnp;
    memset(&upnp, 0, sizeof(upnp));
    upnp.id.bustype = BUS_VIRTUAL;
    upnp.id.vendor = 0x1234; /* sample vendor */
    upnp.id.product = 0x5678; /* sample product */
    //设置支持按键消息
    ioctl(fd, UI_SET_EVBIT, EV_KEY);
    //支持的按键
    ioctl(fd, UI_SET_KEYBIT, KEY_VOLUMEDOWN);
    ioctl(fd, UI_SET_KEYBIT, BTN_TOUCH);

    //设置支持触屏
    ioctl(fd, UI_SET_EVBIT, EV_ABS);
    //触屏的消息类型
    ioctl(fd, UI_SET_ABSBIT, ABS_X);
    ioctl(fd, UI_SET_ABSBIT, ABS_Y);
    ioctl(fd, UI_SET_ABSBIT, ABS_PRESSURE);
    ioctl(fd, UI_SET_ABSBIT, ABS_MT_POSITION_X);
    ioctl(fd, UI_SET_ABSBIT, ABS_MT_POSITION_Y);
    ioctl(fd, UI_SET_ABSBIT, ABS_MT_PRESSURE);
    ioctl(fd, UI_SET_ABSBIT, ABS_MT_TRACKING_ID);

    upnp.absmin[ABS_X] = 0;
    upnp.absmax[ABS_X] = 768; //sam 把屏幕设为768*1280
    upnp.absmin[ABS_Y] = 0;
    upnp.absmax[ABS_Y] = 1280;


    upnp.absmin[ABS_MT_POSITION_X] = 0;
    upnp.absmax[ABS_MT_POSITION_X] = 768; //sam 768*1280
    upnp.absmin[ABS_MT_POSITION_Y] = 0;
    upnp.absmax[ABS_MT_POSITION_Y] = 1280;

    strcpy(upnp.name, "Example device");
    write(fd, &upnp, sizeof(upnp));
    //ioctl(fd, UI_DEV_SETUP, &upnp);
    ioctl(fd, UI_DEV_CREATE);

}

JNIEXPORT jboolean JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector2_sendKey(JNIEnv *env, jclass type) {

    send_event(fd, EV_KEY, KEY_VOLUMEDOWN, 1);
    send_event(fd, EV_SYN, SYN_REPORT, 0);
    send_event(fd, EV_KEY, KEY_VOLUMEDOWN, 0);
    send_event(fd, EV_SYN, SYN_REPORT, 0);

}

JNIEXPORT jboolean JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector2_sendTouch(JNIEnv *env, jclass type) {

    send_event(fd, EV_ABS, ABS_MT_TRACKING_ID, 0x123);
    send_event(fd, EV_ABS, ABS_MT_POSITION_X, 200);
    send_event(fd, EV_ABS, ABS_MT_POSITION_Y, 700);
    send_event(fd, EV_KEY, BTN_TOUCH, 1);
    send_event(fd, EV_SYN, SYN_REPORT, 0);

    send_event(fd, EV_ABS, ABS_MT_TRACKING_ID, -1);
    send_event(fd, EV_KEY, BTN_TOUCH, 0);
    send_event(fd, EV_SYN, SYN_REPORT, 1);
}

JNIEXPORT jboolean JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector2_destroy(JNIEnv *env, jclass type) {

    ioctl(fd, UI_DEV_DESTROY);
    close(fd);
}