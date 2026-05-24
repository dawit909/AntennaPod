#include <jni.h>
#include <string>
#include <chromaprint.h>

extern "C" JNIEXPORT jstring JNICALL
Java_de_danoeh_antennapod_playback_service_AdSkipper_generateFingerprint(
        JNIEnv* env,
        jobject /* this */,
        jshortArray audio_data,
        jint sample_rate,
        jint num_channels) {

    // 1. Initialize Chromaprint
    ChromaprintContext *ctx = chromaprint_new(CHROMAPRINT_ALGORITHM_DEFAULT);
    chromaprint_start(ctx, sample_rate, num_channels);

    // 2. Extract the raw PCM audio bytes from the Android Java array
    jsize len = env->GetArrayLength(audio_data);
    jshort *body = env->GetShortArrayElements(audio_data, 0);

    // 3. Feed the audio to the mathematical hasher
    chromaprint_feed(ctx, body, len);
    chromaprint_finish(ctx);

    // 4. Get the resulting fingerprint string
    char *fingerprint;
    chromaprint_get_fingerprint(ctx, &fingerprint);

    // Clean up memory to prevent leaks
    env->ReleaseShortArrayElements(audio_data, body, 0);
    chromaprint_free(ctx);

    // Convert the C-string back to a Java string and return it
    jstring result = env->NewStringUTF(fingerprint);
    chromaprint_dealloc(fingerprint);

    return result;
}