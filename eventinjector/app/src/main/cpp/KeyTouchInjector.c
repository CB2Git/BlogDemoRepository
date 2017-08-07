#include <jni.h>
#include <android/log.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <dirent.h>
#include <fcntl.h>
#include <linux/input.h>
#include <stdio.h>
#include "KeyTouchInjector.h"

#define TAG "KeyTouchInjector::JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define DLOG(_ALOG_) LOGI("%s",_ALOG_)
#define test_bit(bit, array)    (array[bit/8] & (1<<(bit%8)))

const char *dev_path = "/dev/input";

unsigned char execCmd(char *cmd) {
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

typedef struct _DeviceMask_ {
    char *dev_path;
    uint8_t evbit[(KEY_MAX + 1) / 8];
    uint8_t keyBitmask[(KEY_MAX + 1) / 8];
    uint8_t absBitmask[(KEY_MAX + 1) / 8];
    uint8_t relBitmask[(KEY_MAX + 1) / 8];
    uint8_t swBitmask[(KEY_MAX + 1) / 8];
    uint8_t ledBitmask[(KEY_MAX + 1) / 8];
    uint8_t ffBitmask[(KEY_MAX + 1) / 8];
    uint8_t propBitmask[(KEY_MAX + 1) / 8];
} DeviceMask;

DeviceMask devices[100] = {0};

int device_Count = 0;

void openDeviceLocked(char *devicePath) {

    LOGW("Opening device: %s", devicePath);
    int fd = open(devicePath, O_RDWR | O_CLOEXEC);
    if (fd < 0) {
        LOGE("could not open %s, %s\n", devicePath, strerror(errno));
        return;
    }
//     #define EV_SYN     0x00   /*表示设备支持所有的事件*/
//     #define EV_KEY     0x01  /*键盘或者按键,表示一个键码*/
//     #define EV_REL     0x02  /*鼠标设备,表示一个相对的光标位置结果*/
//     #define EV_ABS     0x03  /*手写板产生的值,其是一个绝对整数值*/
//     #define EV_MSC     0x04  /*其他类型*/
//     #define EV_LED     0x11   /*LED 灯设备*/
//     #define EV_SND     0x12  /*蜂鸣器,输入声音*/
//     #define EV_REP     0x14   /*允许重复按键类型*/
//     #define EV_PWR     0x16   /*电源管理事件*/


    uint8_t evbit[(KEY_MAX + 1) / 8] = {0};
    uint8_t keyBitmask[(KEY_MAX + 1) / 8];
//    uint8_t absBitmask[(KEY_MAX + 1) / 8];
//    uint8_t relBitmask[(KEY_MAX + 1) / 8];
//    uint8_t swBitmask[(KEY_MAX + 1) / 8];
//    uint8_t ledBitmask[(KEY_MAX + 1) / 8];
//    uint8_t ffBitmask[(KEY_MAX + 1) / 8];
//    uint8_t propBitmask[(KEY_MAX + 1) / 8];
    ioctl(fd, EVIOCGBIT(0, sizeof(evbit)), evbit);
    ioctl(fd, EVIOCGBIT(EV_KEY, sizeof(keyBitmask)), keyBitmask);
//    ioctl(fd, EVIOCGBIT(EV_ABS, sizeof(absBitmask)), absBitmask);
//    ioctl(fd, EVIOCGBIT(EV_REL, sizeof(relBitmask)), relBitmask);
//    ioctl(fd, EVIOCGBIT(EV_SW, sizeof(swBitmask)), swBitmask);
//    ioctl(fd, EVIOCGBIT(EV_LED, sizeof(ledBitmask)), ledBitmask);
//    ioctl(fd, EVIOCGBIT(EV_FF, sizeof(ffBitmask)), ffBitmask);
//    ioctl(fd, EVIOCGPROP(sizeof(propBitmask)), propBitmask);
    close(fd);

    if (test_bit(EV_KEY, evbit)) {
        LOGI("key");
    }
    if (test_bit(EV_ABS, evbit)) {
        LOGI("abs");
    }

    if (test_bit(KEY_0, keyBitmask) && test_bit(KEY_1, keyBitmask) &&
        test_bit(KEY_2, keyBitmask) && test_bit(KEY_3, keyBitmask) &&
        test_bit(KEY_4, keyBitmask) && test_bit(KEY_5, keyBitmask) &&
        test_bit(KEY_6, keyBitmask) && test_bit(KEY_7, keyBitmask) &&
        test_bit(KEY_8, keyBitmask) && test_bit(KEY_9, keyBitmask) ||
        test_bit(KEY_POWER, keyBitmask)) {
        return devicePath;
    }
    // }
    return NULL;
}

/**
 * 扫描所有设备
 */
char *scanDevDir() {
    char devname[PATH_MAX];
    char *filename;
    char *target = NULL;
    DIR *dir = opendir(dev_path);
    struct dirent *de;
    if (dir == NULL) {
        LOGE("open %s error,error=%s", dev_path, strerror(errno));
        return NULL;
    }
    strcpy(devname, dev_path);
    filename = devname + strlen(dev_path);
    *filename++ = '/';
    while (de = readdir(dir)) {
        if (de->d_name[0] == '.' &&
            (de->d_name[1] == '\0' ||
             (de->d_name[1] == '.' && de->d_name[2] == '\0')))
            continue;
        strcpy(filename, de->d_name);
        target = openDeviceLocked(devname);
        if (target)
            LOGI("target = %s", target);
        else
            LOGI("target = null");
        //    break;
    }
    closedir(dir);
    return target;
}

void send_event(int fd, uint16_t type, uint16_t code, int32_t value) {
    LOGI("SendEvent call (%d,%d,%d,%d)", fd, type, code, value);
    if (fd <= fileno(stderr)) return;

    struct input_event event;

    memset(&event, 0, sizeof(event));
    gettimeofday(&event.time, NULL);

    // event (type, code, value)
    event.type = type;
    event.code = code;
    event.value = value;
    if (write(fd, &event, sizeof(event)) < 0) {
        LOGE("send_event error");
    }

    // sync (0,0,0)
    event.type = EV_SYN;
    event.code = SYN_REPORT;
    event.value = 0;
    if (write(fd, &event, sizeof(event)) < 0) {
        LOGI("send_event error");
    }
}

JNIEXPORT jboolean JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector_checkSU(JNIEnv *env, jclass type) {
    return execCmd("su -c 'echo hello world'");
}

/**
 * 从Android4.3起，android加入了SELinux对所有进程强制执行强制访问控制 (MAC)
 * 参考链接:https://source.android.com/security/selinux/?hl=zh-cn
 */
JNIEXPORT jboolean JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector_initInjector(JNIEnv *env, jclass type,
                                                               jint version) {
    if (version >= 18) {
        return execCmd("su -c 'chmod 666 /dev/input/*;echo 0 > /sys/fs/selinux/enforce'");
    }
    return execCmd("su -c 'chmod 666 /dev/input/*'");
}


JNIEXPORT jboolean JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector_recoveryInjector(JNIEnv *env, jclass type,
                                                                   jint version) {
    if (version >= 18) {
        return execCmd("su -c 'chmod 600 /dev/input/*;echo 1 > /sys/fs/selinux/enforce'");
    }
    return execCmd("su -c 'chmod 600 /dev/input/*'");
}


JNIEXPORT jstring JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector_findKeyTouchDevice(JNIEnv *env, jclass type) {
    return (*env)->NewStringUTF(env, scanDevDir());
}

JNIEXPORT jboolean JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector_nativeInjectKeyEvent(JNIEnv *env, jclass type,
                                                                       jstring devPath_,
                                                                       jint keyCode) {
    const char *devPath = (*env)->GetStringUTFChars(env, devPath_, NULL);
    int fd = open(devPath, O_RDWR | O_NDELAY);
    (*env)->ReleaseStringUTFChars(env, devPath_, devPath);
    if (fd < 0) {
        LOGE("open %s error,error=%s", devPath, strerror(errno));
        return JNI_FALSE;
    }
    send_event(fd, EV_KEY, keyCode, 1);  // send  key down event
    send_event(fd, EV_KEY, keyCode, 0);  // send key up event
    close(fd);
    return JNI_TRUE;
}