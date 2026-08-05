#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_james_imagereader_NativeApi_getPassword(JNIEnv *env, jobject thiz) {
    std::string password = "zcr@20200102";
    return env->NewStringUTF(password.c_str());
}