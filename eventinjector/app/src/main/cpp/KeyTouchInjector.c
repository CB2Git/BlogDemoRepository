#include <jni.h>
#include <android/log.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <dirent.h>
#include <fcntl.h>
#include <linux/input.h>
#include <stdio.h>

#define TAG "KeyTouchInjector::JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define test_bit(bit, array)    (array[bit/8] & (1<<(bit%8)))

const char *dev_path = "/dev/input";

typedef struct _DeviceMask_ {
    char dev_path[PATH_MAX];
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

void openDeviceLocked(char *devicePath) {
    LOGW("Opening device: %s", devicePath);
    int fd = open(devicePath, O_RDWR | O_CLOEXEC);
    if (fd < 0) {
        LOGE("could not open %s, %s\n", devicePath, strerror(errno));
        return;
    }
    strcpy(devices[device_Count].dev_path, devicePath);
    ioctl(fd, EVIOCGBIT(0, sizeof(devices[device_Count].evbit)), devices[device_Count].evbit);
    ioctl(fd, EVIOCGBIT(EV_KEY, sizeof(devices[device_Count].keyBitmask)),
          devices[device_Count].keyBitmask);
    ioctl(fd, EVIOCGBIT(EV_REL, sizeof(devices[device_Count].relBitmask)),
          devices[device_Count].relBitmask);
    ioctl(fd, EVIOCGBIT(EV_ABS, sizeof(devices[device_Count].absBitmask)),
          devices[device_Count].absBitmask);
    ioctl(fd, EVIOCGBIT(EV_SW, sizeof(devices[device_Count].swBitmask)),
          devices[device_Count].swBitmask);
    ioctl(fd, EVIOCGBIT(EV_LED, sizeof(devices[device_Count].ledBitmask)),
          devices[device_Count].ledBitmask);
    ioctl(fd, EVIOCGBIT(EV_FF, sizeof(devices[device_Count].ffBitmask)),
          devices[device_Count].ffBitmask);
    ioctl(fd, EVIOCGPROP(sizeof(devices[device_Count].propBitmask)),
          devices[device_Count].propBitmask);
    close(fd);
}

/**
 * 扫描所有设备
 */
void scanDevDir() {
    char devname[PATH_MAX];
    char *filename;
    DIR *dir = opendir(dev_path);
    struct dirent *de;
    if (dir == NULL) {
        LOGE("open %s error,error=%s", dev_path, strerror(errno));
        return;
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
        openDeviceLocked(devname);
        device_Count++;
    }
    closedir(dir);
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
    int init_state = 0;
    if (version >= 18) {
        init_state = execCmd("su -c 'chmod 666 /dev/input/*;echo 0 > /sys/fs/selinux/enforce'");
    } else {
        init_state = execCmd("su -c 'chmod 666 /dev/input/*'");
    }
    if (init_state) {
        scanDevDir();
    }
    return init_state;
}


JNIEXPORT jboolean JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector_recoveryInjector(JNIEnv *env, jclass type,
                                                                   jint version) {
    device_Count = 0;
    if (version >= 18) {
        return execCmd("su -c 'chmod 600 /dev/input/*;echo 1 > /sys/fs/selinux/enforce'");
    }
    return execCmd("su -c 'chmod 600 /dev/input/*'");
}


JNIEXPORT jboolean JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector_nativeInjectKeyEvent(JNIEnv *env, jclass type,
                                                                       jint keyCode) {
    char *devPath = NULL;
    for (int i = 0; i < device_Count; i++) {
        if (test_bit(EV_KEY, devices[i].evbit) && test_bit(keyCode, devices[i].keyBitmask)) {
            devPath = devices[i].dev_path;
            break;
        }
    }
    if (devPath == NULL) {
        LOGE("can not found Support key = %d", keyCode);
        return JNI_FALSE;
    }
    LOGE("open %s for write key event", devPath);
    int fd = open(devPath, O_RDWR | O_NDELAY);
    if (fd < 0) {
        LOGE("open %s error,error=%s", devPath, strerror(errno));
        return JNI_FALSE;
    }
    send_event(fd, EV_KEY, keyCode, 1);  // send  key down event
    send_event(fd, EV_SYN, SYN_REPORT, 1);
    send_event(fd, EV_KEY, keyCode, 0);  // send key up event
    send_event(fd, EV_SYN, SYN_REPORT, 1);
    close(fd);
    return JNI_TRUE;
}


JNIEXPORT jboolean JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector_injectTouchEvent(JNIEnv *env, jclass type, jint x,
                                                                   jint y) {
    char *devPath = NULL;
    for (int i = 0; i < device_Count; i++) {
        if (test_bit(EV_ABS, devices[i].evbit) &&
            test_bit(ABS_MT_POSITION_X, devices[i].absBitmask) &&
            test_bit(ABS_MT_POSITION_Y, devices[i].absBitmask)) {
            devPath = devices[i].dev_path;
            break;
        }
    }
    if (devPath == NULL) {
        LOGE("can not found Support abs");
        return JNI_FALSE;
    }
    LOGE("open %s for write key event", devPath);
    int fd = open(devPath, O_RDWR | O_NDELAY);
    if (fd < 0) {
        LOGE("open %s error,error=%s", devPath, strerror(errno));
        return JNI_FALSE;
    }

    //down
    send_event(fd, EV_KEY, BTN_TOUCH, 1);
    send_event(fd, EV_ABS, ABS_MT_PRESSURE, 1);
    send_event(fd, EV_ABS, ABS_MT_POSITION_X, x);
    send_event(fd, EV_ABS, ABS_MT_POSITION_Y, y);
    send_event(fd, EV_SYN, SYN_MT_REPORT, SYN_REPORT);
    send_event(fd, EV_SYN, SYN_REPORT, SYN_REPORT);

    //up
    send_event(fd, EV_KEY, BTN_TOUCH, 0);
    send_event(fd, EV_ABS, ABS_MT_PRESSURE, 0);
    send_event(fd, EV_ABS, ABS_MT_POSITION_X, x);
    send_event(fd, EV_ABS, ABS_MT_POSITION_Y, y);
    send_event(fd, EV_SYN, SYN_MT_REPORT, SYN_REPORT);
    send_event(fd, EV_SYN, SYN_REPORT, SYN_REPORT);
    close(fd);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_org_ndk_eventinjector_utils_KeyTouchInjector_injectSwipeEvent(JNIEnv *env, jclass type,
                                                                   jintArray points_) {
    int pointSize = (*env)->GetArrayLength(env, points_);
    LOGI("point size = %d", pointSize);
    if (pointSize % 2) {
        --pointSize;
        LOGE("%s", "point size is Odd,so i will ignore the last point");
    }

    char *devPath = NULL;
    for (int i = 0; i < device_Count; i++) {
        if (test_bit(EV_ABS, devices[i].evbit) &&
            test_bit(ABS_MT_POSITION_X, devices[i].absBitmask) &&
            test_bit(ABS_MT_POSITION_Y, devices[i].absBitmask)) {
            devPath = devices[i].dev_path;
            break;
        }
    }
    if (devPath == NULL) {
        LOGE("can not found Support swipe");
        return JNI_FALSE;
    }
    LOGE("open %s for write swipe event", devPath);
    int fd = open(devPath, O_RDWR | O_NDELAY);
    if (fd < 0) {
        LOGE("open %s error,error=%s", devPath, strerror(errno));
        return JNI_FALSE;
    }
    jint *points = (*env)->GetIntArrayElements(env, points_, NULL);

    for (int i = 0; i < pointSize; i += 2) {
        if (i == 0) {
            //down
            send_event(fd, EV_KEY, BTN_TOUCH, 1);
            send_event(fd, EV_ABS, ABS_MT_PRESSURE, 1);
            send_event(fd, EV_ABS, ABS_MT_POSITION_X, points[i]);
            send_event(fd, EV_ABS, ABS_MT_POSITION_Y, points[i + 1]);
            send_event(fd, EV_SYN, SYN_MT_REPORT, SYN_REPORT);
            send_event(fd, EV_SYN, SYN_REPORT, SYN_REPORT);
        }
        if (i == pointSize - 2) {
            //up
            send_event(fd, EV_KEY, BTN_TOUCH, 0);
            send_event(fd, EV_ABS, ABS_MT_PRESSURE, 0);
            send_event(fd, EV_ABS, ABS_MT_POSITION_X, points[i]);
            send_event(fd, EV_ABS, ABS_MT_POSITION_Y, points[i + 1]);
            send_event(fd, EV_SYN, SYN_MT_REPORT, SYN_REPORT);
            send_event(fd, EV_SYN, SYN_REPORT, SYN_REPORT);
        } else {
            //move
            send_event(fd, EV_ABS, ABS_MT_POSITION_X, points[i]);
            send_event(fd, EV_ABS, ABS_MT_POSITION_Y, points[i + 1]);
            send_event(fd, EV_SYN, SYN_MT_REPORT, SYN_REPORT);
            send_event(fd, EV_SYN, SYN_REPORT, SYN_REPORT);
        }
        //系统响应时间大约为33ms
        usleep(5500);
    }
    close(fd);
    (*env)->ReleaseIntArrayElements(env, points_, points, 0);
}