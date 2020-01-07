// +build !windows
#include <iostream>
#define WEBVIEW_IMPLEMENTATION
#include "webview.h"
#include "ca_weblite_webview_WebViewNative.h"



typedef  void (*callback_t)(const char *, void *);
WEBVIEW_API void webview_bind(webview_t w, const char *name,
                              callback_t fn, void *arg) {
     webview::webview *wv = (webview::webview*)w;


     wv->bind(name,  [w, fn](std::string s) -> std::string {

        if (fn != NULL) {
            fn(s.c_str(), (void*)w);
        }
        //
        return "";
      });            
}



/*
 * Class:     ca_weblite_webview_WebViewNative
 * Method:    webview_create
 * Signature: (IJ)J
 */
JNIEXPORT jlong JNICALL Java_ca_weblite_webview_WebViewNative_webview_1create
  (JNIEnv *env, jclass clazz, jint debug, jlong win) {
    return (jlong)webview_create((int)debug, (void*)win);
}

/*
 * Class:     ca_weblite_webview_WebViewNative
 * Method:    webview_destroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_ca_weblite_webview_WebViewNative_webview_1destroy
  (JNIEnv *env, jclass clazz, jlong wv) {
    webview_destroy((webview_t)wv);
}

/*
 * Class:     ca_weblite_webview_WebViewNative
 * Method:    webview_run
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_ca_weblite_webview_WebViewNative_webview_1run
  (JNIEnv *env, jclass clazz, jlong wv) {
    webview_run((webview_t)wv);
}

/*
 * Class:     ca_weblite_webview_WebViewNative
 * Method:    webview_terminate
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_ca_weblite_webview_WebViewNative_webview_1terminate
  (JNIEnv *env, jclass clazz, jlong wv) {
    webview_terminate((webview_t)wv);
}


/*
 * Class:     ca_weblite_webview_WebViewNative
 * Method:    webview_dispatch
 * Signature: (JLjava/lang/Runnable;J)V
 */
JNIEXPORT void JNICALL Java_ca_weblite_webview_WebViewNative_webview_1dispatch
  (JNIEnv *env, jclass clazz, jlong w, jobject callback, jlong arg) {
    webview::webview *wv = (webview::webview*)w;
    
    JavaVM *jvm;
    (env)->GetJavaVM(&jvm);
    jclass objClass = env->GetObjectClass(callback);
    jclass callbacksClass;
    if (objClass)
	{
		callbacksClass = reinterpret_cast<jclass>(env->NewGlobalRef(objClass));
		env->DeleteLocalRef(objClass);
	}
    jobject callbackInstance = env->NewGlobalRef(callback);

    wv->dispatch([env, callback,jvm,callbacksClass,callbackInstance]() {
        (jvm)->AttachCurrentThread((void**)&env, NULL);
        jmethodID run = env->GetMethodID(callbacksClass, "run", "()V");
        (env)->CallVoidMethod(callbackInstance, run);
        env->DeleteGlobalRef(callbackInstance);
        env->DeleteGlobalRef(callbacksClass);
        (jvm)->DetachCurrentThread();
    });
}

/*
 * Class:     ca_weblite_webview_WebViewNative
 * Method:    webview_get_window
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_ca_weblite_webview_WebViewNative_webview_1get_1window
  (JNIEnv *env, jclass clazz, jlong w) {
    return (jlong)webview_get_window((webview_t)w);
}

/*
 * Class:     ca_weblite_webview_WebViewNative
 * Method:    webview_set_title
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_ca_weblite_webview_WebViewNative_webview_1set_1title
  (JNIEnv *env, jclass clazz, jlong w, jstring title) {
    const char* utf = (env)->GetStringUTFChars(title, 0);
    webview_set_title((webview_t)w, utf);
    (env)->ReleaseStringUTFChars(title, utf);
}

/*
 * Class:     ca_weblite_webview_WebViewNative
 * Method:    webview_set_bounds
 * Signature: (JIIIII)V
 */
JNIEXPORT void JNICALL Java_ca_weblite_webview_WebViewNative_webview_1set_1bounds
  (JNIEnv *env, jclass clazz, jlong wv, jint x, jint y, jint w, jint h, jint flags) {
	//cout << "Setting size" << w << ", " << h << std::flush;
    webview_set_size((webview_t)wv, w, h, true);
}

/*
 * Class:     ca_weblite_webview_WebViewNative
 * Method:    webview_navigate
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_ca_weblite_webview_WebViewNative_webview_1navigate
  (JNIEnv *env, jclass clazz, jlong w, jstring url) {
     const char* utf = (env)->GetStringUTFChars(url, 0);
    webview_navigate((webview_t)w, utf);
    (env)->ReleaseStringUTFChars(url, utf);
}

/*
 * Class:     ca_weblite_webview_WebViewNative
 * Method:    webview_init
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_ca_weblite_webview_WebViewNative_webview_1init
  (JNIEnv *env, jclass clazz, jlong w, jstring js) {
    const char* utf = (env)->GetStringUTFChars(js, 0);
    webview_init((webview_t)w, utf);
    (env)->ReleaseStringUTFChars(js, utf);
}

/*
 * Class:     ca_weblite_webview_WebViewNative
 * Method:    webview_eval
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_ca_weblite_webview_WebViewNative_webview_1eval
  (JNIEnv *env, jclass clazz, jlong w, jstring js) {
    const char* utf = (env)->GetStringUTFChars(js, 0);
    webview_eval((webview_t)w, utf);
    (env)->ReleaseStringUTFChars(js, utf);
}

/*
 * Class:     ca_weblite_webview_WebViewNative
 * Method:    webview_bind
 * Signature: (JLjava/lang/String;Lca/weblite/webview/WebViewNativeCallback;J)V
 */
JNIEXPORT void JNICALL Java_ca_weblite_webview_WebViewNative_webview_1bind
  (JNIEnv *env, jclass clazz, jlong wv, jstring name, jobject fn, jlong arg) {
    webview::webview *w = (webview::webview*)wv;
    const char* nameUtf = (env)->GetStringUTFChars(name, 0);
    JavaVM *jvm;
    (env)->GetJavaVM(&jvm);
    jobject fnInstance = env->NewGlobalRef(fn);
    jclass objClass = env->GetObjectClass(fn);
    jclass callbacksClass;
    if (objClass)
    {
            callbacksClass = reinterpret_cast<jclass>(env->NewGlobalRef(objClass));
            env->DeleteLocalRef(objClass);
    }
    w->bind(nameUtf, [fnInstance,env,jvm,callbacksClass](std::string s) -> std::string {
        (jvm)->AttachCurrentThread((void**)&env, NULL);
        const char* strArg = s.c_str();
        
        jmethodID invoke = (env)->GetMethodID(callbacksClass, "invoke", "(Ljava/lang/String;J)V"); 
        
        (env)->CallVoidMethod(fnInstance, invoke, (env)->NewStringUTF(strArg));
        //env->DeleteGlobalRef(fnInstance);
        (jvm)->DetachCurrentThread();
        
        return s;
    });
    (env)->ReleaseStringUTFChars(name, nameUtf);
}



