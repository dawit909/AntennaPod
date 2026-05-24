package de.danoeh.antennapod.playback.service;

import android.util.Log;

public class AdSkipper {

    // Load the C++ library compiled by CMake
    static {
        System.loadLibrary("fingerprinter");
    }

    /**
     * The native method declaration. The JVM looks for the matching C++ function
     * using that massive Java_de_danoeh... naming convention we used above.
     */
    public native String generateFingerprint(short[] audioData, int sampleRate, int numChannels);


    public String hashAudioSegment(short[] pcmData, int sampleRate, int channels) {
        try {
            return generateFingerprint(pcmData, sampleRate, channels);
        } catch (Exception e) {
            Log.e("AdSkipper", "Failed to generate fingerprint via NDK", e);
            return null;
        }
    }
}