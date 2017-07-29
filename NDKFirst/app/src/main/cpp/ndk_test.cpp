#include <jni.h>
#include <stddef.h>
#include "android/log.h"


#define TAG "ndk-jni" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型


extern "C"
{
JNIEXPORT jstring JNICALL
Java_org_ndk_ndkfirst_NDKTest_getStringFromNative(JNIEnv *env, jobject obj) {
    return env->NewStringUTF("hello world from native~~~");
}

JNIEXPORT jint JNICALL
Java_org_ndk_ndkfirst_NDKTest_calcInNative(JNIEnv *env, jobject obj, jint num1, jint num2) {
    return num1 + num2;
}

JNIEXPORT void JNICALL Java_org_ndk_ndkfirst_NDKTest_callJavaMetood(JNIEnv *env, jobject obj) {
    jclass jclass1 = env->GetObjectClass(obj);
    jmethodID methodID = env->GetMethodID(jclass1, "toString", "()Ljava/lang/String;");
    env->CallObjectMethod(obj, methodID);
}

JNIEXPORT void JNICALL Java_org_ndk_ndkfirst_NDKTest_modifyFiled(JNIEnv *env, jobject obj) {
    jclass jclass1 = env->GetObjectClass(obj);
    jfieldID fieldID1 = env->GetFieldID(jclass1, "mString", "Ljava/lang/String;");
    env->SetObjectField(obj, fieldID1, env->NewStringUTF("native modify it"));
}

JNIEXPORT jobjectArray JNICALL
Java_org_ndk_ndkfirst_NDKTest_getIntArray(JNIEnv *env, jclass cls, jint row, jint column) {
    jclass intArrayCls = env->FindClass("[I");
    //创建一个数组，数组里面的元素为int[]
    jobjectArray inArray = env->NewObjectArray(row, intArrayCls, NULL);
    int length = env->GetArrayLength(inArray);
    for (int i = 0; i < length; ++i) {
        //创建一个int[]
        jintArray jintArray1 = env->NewIntArray(column);
        int temp[column];
        for (int ii = 0; ii < sizeof(temp) / sizeof(temp[0]); ++ii) {
            temp[ii] = i + ii;
        }
        //初始化int[]
        env->SetIntArrayRegion(jintArray1, 0, sizeof(temp) / sizeof(temp[0]), temp);
        //将int[]放到最初简历的数组中
        env->SetObjectArrayElement(inArray, i, jintArray1);
        //清除临时的引用
        env->DeleteLocalRef(jintArray1);
    }
    return inArray;
}


JNIEXPORT jobject JNICALL
Java_org_ndk_ndkfirst_NDKTest_getStudentFromNative(JNIEnv *env, jobject obj) {
    jclass stuClass = env->FindClass("org/ndk/ndkfirst/Student");
    //构造函数的名字为<init> 返回值为void
    jmethodID stuconstrocID = env->GetMethodID(stuClass, "<init>", "()V");
    //调用无参构造函数
    jobject student = env->NewObject(stuClass, stuconstrocID);
    return student;
}

JNIEXPORT jobject JNICALL
Java_org_ndk_ndkfirst_NDKTest_getStudentFromNative2(JNIEnv *env, jobject obj) {
    //获取Student的jclass对象
    jclass stuCls = env->FindClass("org/ndk/ndkfirst/Student");
    //调用有参数的构造函数，参数为int,String
    jmethodID stuid = env->GetMethodID(stuCls, "<init>", "(ILjava/lang/String;)V");
    //参数为10，native student
    return env->NewObject(stuCls, stuid, 10, env->NewStringUTF("native student"));
}

JNIEXPORT void JNICALL
Java_org_ndk_ndkfirst_NDKTest_outputStudentInNative(JNIEnv *env, jobject obj, jobject stu) {
    //获取类对象
    jclass stuCls = env->GetObjectClass(stu);
    //调用toString方法
    jmethodID toStringid = env->GetMethodID(stuCls, "toString", "()Ljava/lang/String;");
    //调用方法
    jstring str = (jstring) env->CallObjectMethod(stu, toStringid);
    //这里需要使用GetStringUTFChars而不是GetStringChars,因为jstring为宽字符
    LOGI("native output : %s", env->GetStringUTFChars(str, NULL));
}
}



