#include <string.h>
#include <stdint.h>
#include <jni.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <dirent.h>
#include <time.h>
#include <errno.h>

#include <sys/ioctl.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/inotify.h>
#include <sys/limits.h>
#include <sys/poll.h>

#include <linux/fb.h>
#include <linux/kd.h>
#include <linux/input.h>

#include <android/log.h>
#include <assert.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <inttypes.h>
#include <memory.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/epoll.h>
#include <sys/limits.h>
#include <sys/inotify.h>
#include <sys/ioctl.h>
#include <sys/utsname.h>
#include <unistd.h>

#define LOG_TAG "EventHub"

// #define LOG_NDEBUG 0

#define TAG "EventInjector::JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define ALOG(_ALOG_) LOGI("%s",_ALOG_)


#include "EventInjector.h"


/* Debug tools
 */
int g_debug = 0;


void debug(char *szFormat, ...) {
    if (g_debug == 0) return;
    //if (strlen(szDbgfile) == 0) return;

    char szBuffer[4096]; //in this buffer we form the message
    const size_t NUMCHARS = sizeof(szBuffer) / sizeof(szBuffer[0]);
    const int LASTCHAR = NUMCHARS - 1;
    //format the input string
    va_list pArgs;
    va_start(pArgs, szFormat);
    // use a bounded buffer size to prevent buffer overruns.  Limit count to
    // character size minus one to allow for a NULL terminating character.
    vsnprintf(szBuffer, NUMCHARS - 1, szFormat, pArgs);
    va_end(pArgs);
    //ensure that the formatted string is NULL-terminated
    szBuffer[LASTCHAR] = '\0';

    LOGD("%s", szBuffer);
    //TextCallback(szBuffer);
}


jint Java_org_ndk_eventinjector_Events_intEnableDebug(JNIEnv *env, jobject thiz, jint enable) {

    g_debug = enable;
    return g_debug;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    debug("eventinterceptor native lib loaded.");
    return JNI_VERSION_1_2; //1_2 1_4
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
    debug("eventinterceptor native lib unloaded.");
}

static struct typedev {
    struct pollfd ufds;
    char *device_path;
    char *device_name;
} *pDevs = NULL;
struct pollfd *ufds;
static int nDevsCount;

const char *device_path = "/dev/input";

int g_Polling = 0;
struct input_event event;
int c;
int i;
int pollres;
int get_time = 0;
char *newline = "\n";
uint16_t get_switch = 0;
struct input_event event;
int version;

int dont_block = -1;
int event_count = 0;
int sync_rate = 0;
int64_t last_sync_time = 0;
const char *device = NULL;


static int open_device(int index) {
    if (index >= nDevsCount || pDevs == NULL) return -1;
    debug("open_device prep to open");
    char *device = pDevs[index].device_path;

    debug("open_device call %s", device);
    int version;
    int fd;

    char name[80];
    char location[80];
    char idstr[80];
    struct input_id id;

    fd = open(device, O_RDWR);
    if (fd < 0) {
        pDevs[index].ufds.fd = -1;

        pDevs[index].device_name = NULL;
        debug("could not open %s, %s", device, strerror(errno));
        return -1;
    }

    pDevs[index].ufds.fd = fd;
    ufds[index].fd = fd;

    name[sizeof(name) - 1] = '\0';
    if (ioctl(fd, EVIOCGNAME(sizeof(name) - 1), &name) < 1) {
        debug("could not get device name for %s, %s", device, strerror(errno));
        name[0] = '\0';
    }
    debug("Device %d: %s: %s", nDevsCount, device, name);

    pDevs[index].device_name = strdup(name);


    return 0;
}

int remove_device(int index) {
    if (index >= nDevsCount || pDevs == NULL) return -1;

    int count = nDevsCount - index - 1;
    debug("remove device %d", index);
    free(pDevs[index].device_path);
    free(pDevs[index].device_name);

    memmove(&pDevs[index], &pDevs[index + 1], sizeof(pDevs[0]) * count);
    nDevsCount--;
    return 0;
}


static int scan_dir(const char *dirname) {
    nDevsCount = 0;
    char devname[PATH_MAX];
    char *filename;
    DIR *dir;
    struct dirent *de;
    dir = opendir(dirname);
    if (dir == NULL)
        return -1;
    strcpy(devname, dirname);
    filename = devname + strlen(devname);
    *filename++ = '/';
    while ((de = readdir(dir))) {
        if (de->d_name[0] == '.' &&
            (de->d_name[1] == '\0' ||
             (de->d_name[1] == '.' && de->d_name[2] == '\0')))
            continue;
        strcpy(filename, de->d_name);
        debug("scan_dir:prepare to open:%s", devname);
        // add new filename to our structure: devname
        struct typedev *new_pDevs = realloc(pDevs, sizeof(pDevs[0]) * (nDevsCount + 1));
        if (new_pDevs == NULL) {
            debug("out of memory");
            return -1;
        }
        pDevs = new_pDevs;

        struct pollfd *new_ufds = realloc(ufds, sizeof(ufds[0]) * (nDevsCount + 1));
        if (new_ufds == NULL) {
            debug("out of memory");
            return -1;
        }
        ufds = new_ufds;
        ufds[nDevsCount].events = POLLIN;

        pDevs[nDevsCount].ufds.events = POLLIN;
        pDevs[nDevsCount].device_path = strdup(devname);

        nDevsCount++;
    }
    closedir(dir);
    return 0;
}

jint
Java_org_ndk_eventinjector_Events_intSendEvent(JNIEnv *env, jobject thiz, jint index, uint16_t type,
                                               uint16_t code, int32_t value) {
    if (index >= nDevsCount || pDevs[index].ufds.fd == -1) return -1;
    int fd = pDevs[index].ufds.fd;
    debug("SendEvent call (%d,%d,%d,%d)", fd, type, code, value);
    struct uinput_event event;
    struct input_event e;
    int len;

    if (fd <= fileno(stderr)) return -1;

    memset(&event, 0, sizeof(event));
    event.type = type;
    event.code = code;
    event.value = value;

    len = write(fd, &event, sizeof(event));
    debug("SendEvent done:%d", len);



    /********************/

    struct input_event event2;

    memset(&event2, 0, sizeof(event2));
    gettimeofday(&event2.time, NULL);



// sync (0,0,0)
    event2.type = EV_SYN;
    event2.code = SYN_REPORT;
    event2.value = 0;
    if (write(fd, &event, sizeof(event)) < 0) {
        debug("send_event error");
    }


    return 0;
}


jint Java_org_ndk_eventinjector_Events_ScanFiles(JNIEnv *env, jobject thiz) {
    int res = scan_dir(device_path);
    if (res < 0) {
        debug("scan dir failed for %s:", device_path);
        return -1;
    }

    return nDevsCount;
}

jstring Java_org_ndk_eventinjector_Events_getDevPath(JNIEnv *env, jobject thiz, jint index) {
    return (*env)->NewStringUTF(env, pDevs[index].device_path);
}

jstring Java_org_ndk_eventinjector_Events_getDevName(JNIEnv *env, jobject thiz, jint index) {
    if (pDevs[index].device_name == NULL) return NULL;
    else return (*env)->NewStringUTF(env, pDevs[index].device_name);
}

jint Java_org_ndk_eventinjector_Events_OpenDev(JNIEnv *env, jobject thiz, jint index) {
    return open_device(index);
}

jint Java_org_ndk_eventinjector_Events_RemoveDev(JNIEnv *env, jobject thiz, jint index) {
    return remove_device(index);
}

jint Java_org_ndk_eventinjector_Events_PollDev(JNIEnv *env, jobject thiz, jint index) {
    if (index >= nDevsCount || pDevs[index].ufds.fd == -1) return -1;
    int pollres = poll(ufds, nDevsCount, -1);
    if (ufds[index].revents) {
        if (ufds[index].revents & POLLIN) {
            int res = read(ufds[index].fd, &event, sizeof(event));
            if (res < (int) sizeof(event)) {
                return 1;
            } else return 0;
        }
    }
    return -1;
}

jint Java_org_ndk_eventinjector_Events_getType(JNIEnv *env, jobject thiz) {
    return event.type;
}

jint Java_org_ndk_eventinjector_Events_getCode(JNIEnv *env, jobject thiz) {
    return event.code;
}

jint Java_org_ndk_eventinjector_Events_getValue(JNIEnv *env, jobject thiz) {
    return event.value;
}

void send_event(int fd, uint16_t type, uint16_t code, int32_t value) {
    debug("SendEvent call (%d,%d,%d,%d)", fd, type, code, value);
    if (fd <= fileno(stderr)) return;

    struct input_event event;
    int len;

    memset(&event, 0, sizeof(event));
    gettimeofday(&event.time, NULL);

    // event (type, code, value)
    event.type = type;
    event.code = code;
    event.value = value;
    if (write(fd, &event, sizeof(event)) < 0) {
        debug("send_event error");
    }

    // sync (0,0,0)
    event.type = EV_SYN;
    event.code = SYN_REPORT;
    event.value = 0;
    if (write(fd, &event, sizeof(event)) < 0) {
        debug("send_event error");
    }
}
