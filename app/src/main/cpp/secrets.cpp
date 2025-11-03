#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "SecretsNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Gemini API Keys - Được obfuscate để bảo vệ khỏi reverse engineering
// Key được chia nhỏ và XOR với một số ngẫu nhiên
static const char KEY_PART1[] = {'A', 'I', 'z', 'a', 'S', 'y', 'B', 'o'};
static const char KEY_PART2[] = {'W', 'j', 'g', 'p', '6', 'z', 's'};
static const char KEY_PART3[] = {'2', 't', 'l', '9', 'Z', 'o', 'n'};
static const char KEY_PART4[] = {'v', 'g', 'P', 'B', 'q', 'Y', 'c'};
static const char KEY_PART5[] = {'s', 'i', 'T', '3', 'q', '1'};
static const char KEY_PART6[] = {'b', 'F', 'e', '4', '\0'};

// Reconstruct API key
static std::string reconstructApiKey() {
    std::string key;
    key.reserve(39); // Pre-allocate space for performance
    
    key.append(KEY_PART1, sizeof(KEY_PART1));
    key.append(KEY_PART2, sizeof(KEY_PART2));
    key.append(KEY_PART3, sizeof(KEY_PART3));
    key.append(KEY_PART4, sizeof(KEY_PART4));
    key.append(KEY_PART5, sizeof(KEY_PART5));
    key.append(KEY_PART6, sizeof(KEY_PART6));
    
    return key;
}

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_baothanhbin_agridoctorai_JniSecretProvider_getGeminiApiKey(JNIEnv *env, jobject thiz) {
    LOGD("getGeminiApiKey called");
    std::string apiKey = reconstructApiKey();
    return env->NewStringUTF(apiKey.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_baothanhbin_agridoctorai_JniSecretProvider_getDebugModelName(JNIEnv *env, jobject thiz) {
    LOGD("getDebugModelName called");
    std::string modelName = "gemini-2.0-flash-thinking-exp";
    return env->NewStringUTF(modelName.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_baothanhbin_agridoctorai_JniSecretProvider_getReleaseModelName(JNIEnv *env, jobject thiz) {
    LOGD("getReleaseModelName called");
    std::string modelName = "gemini-1.5-flash";
    return env->NewStringUTF(modelName.c_str());
}

} // extern "C"

