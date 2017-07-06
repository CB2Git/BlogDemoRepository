#include <jni.h>

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
    jfieldID fieldID1 = env->GetFieldID(jclass1,"mString","Ljava/lang/String;");
    env->SetObjectField(obj,fieldID1,env->NewStringUTF("native modify it"));
}
}



