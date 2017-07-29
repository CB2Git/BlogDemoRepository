#include <jni.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/inotify.h>
#include "android/log.h"


#define TAG "uninstall-tool" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型


/***
 * 思路参考链接:
 * http://blog.csdn.net/jiangwei0910410003/article/details/42177117
 * 文件监听参考链接:
 * http://www.jiangmiao.org/blog/2179.html
 */
extern "C"
{

int sdk_int = 0;

bool has_listener = false;

void initTool(JNIEnv *env) {
    jclass version = env->FindClass("android/os/Build$VERSION");
    jfieldID sdk_int_id = env->GetStaticFieldID(version, "SDK_INT", "I");
    sdk_int = env->GetStaticIntField(version, sdk_int_id);
    LOGI("sdk version:%d", sdk_int);
}

JNIEXPORT void JNICALL
Java_org_ndk_ndkfirst_UninstallTool_setUninstallAction(JNIEnv *env, jclass jcls, jstring path,
                                                       jstring url) {
    initTool(env);
    LOGI("target:%s", env->GetStringUTFChars(path, NULL));
    //5.0及以上native进程无法使用am等命令。
    if (sdk_int >= 21 || has_listener) {
        LOGE("stop uninstall");
        return;
    }
    pid_t pid = fork();
    LOGF("pid = %d", pid);
    if (pid == -1) {
        LOGE("%s", "fork error!!!");
    } else if (pid == 0) {
        //子进程
        // 创建一个inotify实例
        int inotify = inotify_init();
        if (inotify < 0) {
            LOGE("%s", "inotify_init() error!!!");
            _exit(-1);
        }
        //监听/data/data/package-name/是否被删除
        int watch_id = inotify_add_watch(inotify, env->GetStringUTFChars(path, NULL), IN_DELETE);
        if (watch_id < 0) {
            LOGE("%s", "inotify_add_watch error!!!");
            _exit(-1);
        }
        LOGI("begin listener ==》 %s", env->GetStringUTFChars(path, NULL));
        has_listener = true;
        inotify_event event;
        //读取事件，这里会阻塞掉，一直等待
        read(inotify, &event, sizeof(event));
        //读取到了，说明被应用被卸载了
        LOGI("%s", "package-uninstall");
        //停止监听
        inotify_rm_watch(inotify, watch_id);
        const char *targetUrl = env->GetStringUTFChars(url, NULL);
        //执行命令
        if (sdk_int <= 16) {
            execlp(
                    "am", "am", "start", "-a", "android.intent.action.VIEW", "-d",
                    targetUrl, (char *) NULL);
        } else {
            //4.2以上的系统由于用户权限管理更严格，需要加上 --user 0
            execlp("am", "am", "start", "--user", "0", "-a",
                   "android.intent.action.VIEW", "-d", targetUrl, (char *) NULL);
        }
    } else {
        //父进程直接退出，使子进程被init进程领养，以避免子进程僵死
    }
}
}

