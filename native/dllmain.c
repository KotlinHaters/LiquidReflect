#include <windows.h>
#include "jni.h"
#include "jvmti.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

JavaVM* jvm;
JNIEnv* jniEnv;
jvmtiEnv* jvmti;
jclass envClass;
jmethodID methodID;

jbyteArray asByteArray(JNIEnv* env, const unsigned char* buf, int len) {
    jbyteArray array = (*env)->NewByteArray(env, len);
    (*env)->SetByteArrayRegion(env, array, 0, len, (const jbyte*)buf);
    return array;
}

unsigned char* asUnsignedCharArray(JNIEnv* env, jbyteArray array) {
    int len = (*env)->GetArrayLength(env, array);
    unsigned char* buf = (unsigned char*)malloc(len);
    (*env)->GetByteArrayRegion(env, array, 0, len, (jbyte*)buf);
    return buf;
}

void JNICALL classFileLoadHook(jvmtiEnv* jvmti,
    JNIEnv* env,
    jclass class_being_redefined,
    jobject loader,
    const char* name,
    jobject protection_domain,
    jint data_len,
    const unsigned char* data,
    jint* new_data_len,
    unsigned char** new_data) {

    if (name == NULL) {
        return;
    }

    (*jvmti)->Allocate(jvmti, data_len, new_data);
    *new_data_len = data_len;
    memcpy(*new_data, data, data_len);

    const jbyteArray plainBytes = asByteArray(env, *new_data, *new_data_len);
    jbyteArray newByteArray = (jbyteArray)(*env)->CallStaticObjectMethod(env, envClass, methodID, (*env)->NewStringUTF(env, name), plainBytes);

    unsigned char* newChars = asUnsignedCharArray(env, newByteArray);
    const jint newLength = (jint)(*env)->GetArrayLength(env, newByteArray);

    (*jvmti)->Allocate(jvmti, newLength, new_data);
    *new_data_len = newLength;
    memcpy(*new_data, newChars, newLength);
}

JNIEXPORT void JNICALL retransformClass(JNIEnv* env, jclass caller, jclass target) {
    jclass classes[1];
    classes[0] = target;
    (*jvmti)->RetransformClasses(jvmti, 1, classes);
}

jclass DefineClass(JNIEnv* env, jobject obj, jobject classLoader, jbyteArray bytes)
{
    jclass clClass = (*env)->FindClass(env, "java/lang/ClassLoader");
    jmethodID defineClass = (*env)->GetMethodID(env, clClass, "defineClass", "([BII)Ljava/lang/Class;");
    jobject classDefined = (*env)->CallObjectMethod(env, classLoader, defineClass, bytes, 0, (*env)->GetArrayLength(env, bytes));
    return (jclass)classDefined;
}

const char* getLoaderJarPath() {
    char userProfile[MAX_PATH];
    char modsFolderPath[MAX_PATH];
    DWORD result = GetEnvironmentVariableA("USERPROFILE", userProfile, MAX_PATH);
    if (result != 0 && result < MAX_PATH) {
        sprintf_s(modsFolderPath, MAX_PATH, "%s\\.liquidreflect\\release.jar", userProfile);
        return modsFolderPath;
    }
    else {
        return NULL;
    }
}

DWORD WINAPI Inject(LPVOID parm) {
    HMODULE jvmHandle = GetModuleHandle(L"jvm.dll");
    if (!jvmHandle) return 0;
    typedef jint(JNICALL* fnJNI_GetCreatedJavaVMs)(JavaVM**, jsize, jsize*);
    fnJNI_GetCreatedJavaVMs JNI_GetCreatedJavaVMs = (fnJNI_GetCreatedJavaVMs)GetProcAddress(jvmHandle, "JNI_GetCreatedJavaVMs");
    if (!JNI_GetCreatedJavaVMs) return 0;
    if (JNI_GetCreatedJavaVMs(&jvm, 1, NULL) != JNI_OK || (*jvm)->AttachCurrentThread(jvm, (void**)&jniEnv, NULL) != JNI_OK) return 0;
    (*jvm)->GetEnv(jvm, (void**)&jvmti, JVMTI_VERSION_1_2);
    if (!jvmti) return 0;
    jclass threadClass = (*jniEnv)->FindClass(jniEnv, "java/lang/Thread");
    jmethodID getAllStackTraces = (*jniEnv)->GetStaticMethodID(jniEnv, threadClass, "getAllStackTraces", "()Ljava/util/Map;");
    if (!getAllStackTraces) return 0;
    jobjectArray threads = (jobjectArray)(*jniEnv)->CallObjectMethod(jniEnv, (*jniEnv)->CallObjectMethod(jniEnv, (*jniEnv)->CallStaticObjectMethod(jniEnv, threadClass, getAllStackTraces), (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->FindClass(jniEnv, "java/util/Map"), "keySet", "()Ljava/util/Set;")), (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->FindClass(jniEnv, "java/util/Set"), "toArray", "()[Ljava/lang/Object;"));
    if (!threads) return 0;
    jsize arrlength = (*jniEnv)->GetArrayLength(jniEnv, threads);
    jobject clientThread = NULL;
    for (int i = 0; i < arrlength; i++) {
        jobject thread = (*jniEnv)->GetObjectArrayElement(jniEnv, threads, i);
        if (thread == NULL) continue;
        jclass threadClass = (*jniEnv)->GetObjectClass(jniEnv, thread);
        jstring name = (*jniEnv)->CallObjectMethod(jniEnv, thread, (*jniEnv)->GetMethodID(jniEnv, threadClass, "getName", "()Ljava/lang/String;"));
        const char* str = (*jniEnv)->GetStringUTFChars(jniEnv, name, FALSE);
        if (!strcmp(str, "Client thread")) {
            clientThread = thread;
            (*jniEnv)->ReleaseStringUTFChars(jniEnv, name, str);
            break;
        }
        (*jniEnv)->ReleaseStringUTFChars(jniEnv, name, str);
    }
    if (!clientThread) return 0;
    jclass urlClassLoader = (*jniEnv)->FindClass(jniEnv, "java/net/URLClassLoader");
    jmethodID findClass = (*jniEnv)->GetMethodID(jniEnv, urlClassLoader, "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    jmethodID addURL = (*jniEnv)->GetMethodID(jniEnv, urlClassLoader, "addURL", "(Ljava/net/URL;)V");
    jclass fileClass = (*jniEnv)->FindClass(jniEnv, "java/io/File");
    jmethodID init = (*jniEnv)->GetMethodID(jniEnv, fileClass, "<init>", "(Ljava/lang/String;)V");
    jstring filePath = (*jniEnv)->NewStringUTF(jniEnv, getLoaderJarPath());
    jobject file = (*jniEnv)->NewObject(jniEnv, fileClass, init, filePath);
    jmethodID toURI = (*jniEnv)->GetMethodID(jniEnv, fileClass, "toURI", "()Ljava/net/URI;");
    jobject uri = (*jniEnv)->CallObjectMethod(jniEnv, file, toURI);
    jclass URIClass = (*jniEnv)->FindClass(jniEnv, "java/net/URI");
    jmethodID toURL = (*jniEnv)->GetMethodID(jniEnv, URIClass, "toURL", "()Ljava/net/URL;");
    jobject url = (*jniEnv)->CallObjectMethod(jniEnv, uri, toURL);
    (*jniEnv)->CallVoidMethod(jniEnv, (*jniEnv)->CallObjectMethod(jniEnv, clientThread, (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->GetObjectClass(jniEnv, clientThread), "getContextClassLoader", "()Ljava/lang/ClassLoader;")), addURL, url);
    jstring entryClass = (*jniEnv)->NewStringUTF(jniEnv, "net/ccbluex/liquidbounce/injection/Loader");
    jclass clazz = (jclass)(*jniEnv)->CallObjectMethod(jniEnv, (*jniEnv)->CallObjectMethod(jniEnv, clientThread, (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->GetObjectClass(jniEnv, clientThread), "getContextClassLoader", "()Ljava/lang/ClassLoader;")), findClass, entryClass);

    jstring envClazz = (*jniEnv)->NewStringUTF(jniEnv, "net/ccbluex/liquidbounce/injection/Environment");
    envClass = (jclass)(*jniEnv)->CallObjectMethod(jniEnv, (*jniEnv)->CallObjectMethod(jniEnv, clientThread, (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->GetObjectClass(jniEnv, clientThread), "getContextClassLoader", "()Ljava/lang/ClassLoader;")), findClass, envClazz);

    methodID = (*jniEnv)->GetStaticMethodID(jniEnv,envClass, "processClass", "(Ljava/lang/String;[B)[B");

    jvmtiCapabilities capabilities = { 0 };
    memset(&capabilities, 0, sizeof(jvmtiCapabilities));

    capabilities.can_get_bytecodes = 1;
    capabilities.can_redefine_classes = 1;
    capabilities.can_redefine_any_class = 1;
    capabilities.can_generate_all_class_hook_events = 1;
    capabilities.can_retransform_classes = 1;
    capabilities.can_retransform_any_class = 1;

    (*jvmti)->AddCapabilities((jvmtiEnv*)jvmti, &capabilities);

    jvmtiEventCallbacks callbacks = { 0 };
    memset(&callbacks, 0, sizeof(jvmtiEventCallbacks));

    callbacks.ClassFileLoadHook = &classFileLoadHook;

    (*jvmti)->SetEventCallbacks((jvmtiEnv*)jvmti, &callbacks, sizeof(jvmtiEventCallbacks));
    (*jvmti)->SetEventNotificationMode((jvmtiEnv*)jvmti, JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, NULL);

    jstring wrapper = (*jniEnv)->NewStringUTF(jniEnv, "net/ccbluex/liquidbounce/injection/NativeWrapper");
    jclass wrapperClass = (jclass)(*jniEnv)->CallObjectMethod(jniEnv, (*jniEnv)->CallObjectMethod(jniEnv, clientThread, (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->GetObjectClass(jniEnv, clientThread), "getContextClassLoader", "()Ljava/lang/ClassLoader;")), findClass, wrapper);

    JNINativeMethod methods[] =
    {
        {"retransformClass", "(Ljava/lang/Class;[B)I", (void*)&retransformClass},
        {"defineClass", "(Ljava/lang/ClassLoader;[B)Ljava/lang/Class;", (void*)&DefineClass}
    };

    (*jniEnv)->RegisterNatives(jniEnv, wrapperClass, methods, 3);

    jmethodID loaderid = NULL;
    loaderid = (*jniEnv)->GetMethodID(jniEnv,clazz, "<init>", "()V");
    jobject LoadClent = (*jniEnv)->NewObject(jniEnv, clazz, loaderid);
    (*jvm)->DetachCurrentThread(jvm);
    return 0;
}

BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD fdwReason, LPVOID lpvReserved)
{
    switch (fdwReason)
    {
    case DLL_PROCESS_ATTACH:
    {
        CreateThread(NULL, 4096, &Inject, NULL, 0, NULL);
        break;
    }
    }
    return TRUE;
}