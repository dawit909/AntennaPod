package de.danoeh.antennapod.playback.service;

import androidx.media3.common.audio.BaseAudioProcessor;
import androidx.media3.common.audio.AudioProcessor.AudioFormat;
import androidx.media3.common.audio.AudioProcessor.UnhandledAudioFormatException;
import androidx.media3.common.util.UnstableApi;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.util.Log;
import org.greenrobot.eventbus.EventBus;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.danoeh.antennapod.event.playback.AdDetectedEvent;

@UnstableApi
public class FingerprintAudioProcessor extends BaseAudioProcessor {

    private static final String TAG = "FingerprintProcessor";
    private static final int HASH_WINDOW_SECONDS = 3;

    private short[] accumulationBuffer;
    private int accumulationIndex = 0;

    private final AdSkipper adSkipper;
    private final ExecutorService hashingExecutor = Executors.newSingleThreadExecutor();

    private final Map<String, Integer> knownAdHashes = new ConcurrentHashMap<>();


    // Add a method to load the hashes from the database
    public void setKnownAdHashes(Map<String, Integer> hashes) {
        this.knownAdHashes.clear();
        if (hashes != null) {
            this.knownAdHashes.putAll(hashes);
        }
    }

    private static volatile String latestGeneratedHash = null;

    public static String getLatestHash() {
        return latestGeneratedHash;
    }


    public FingerprintAudioProcessor() {
        this.adSkipper = new AdSkipper();
    }

    @Override
    public AudioFormat onConfigure(AudioFormat inputAudioFormat) throws UnhandledAudioFormatException {
        if (inputAudioFormat.encoding != androidx.media3.common.C.ENCODING_PCM_16BIT) {
            throw new UnhandledAudioFormatException(inputAudioFormat);
        }

        // Calculate bucket size based on the audio track's specific sample rate
        int samplesPerSecond = inputAudioFormat.sampleRate * inputAudioFormat.channelCount;
        accumulationBuffer = new short[samplesPerSecond * HASH_WINDOW_SECONDS];
        accumulationIndex = 0;

        return inputAudioFormat;
    }

    @Override
    public void queueInput(ByteBuffer inputBuffer) {
        int remainingBytes = inputBuffer.remaining();
        if (remainingBytes == 0) {
            return;
        }

        // 1. Grab a properly sized, REUSABLE output buffer from the Base class
        // This completely eliminates the memory leak!
        ByteBuffer buffer = replaceOutputBuffer(remainingBytes);

        // 2. Read the audio data for our hashing bucket
        int originalPosition = inputBuffer.position();
        while (inputBuffer.remaining() >= 2 && accumulationIndex < accumulationBuffer.length) {
            accumulationBuffer[accumulationIndex++] = inputBuffer.getShort();
        }

        // 3. Reset the input buffer position so we can copy it to the output
        inputBuffer.position(originalPosition);

        // 4. Pass the audio straight through to the speakers
        buffer.put(inputBuffer);
        buffer.flip();

        // 5. If our 3-second bucket is full, hash it!
        if (accumulationIndex >= accumulationBuffer.length) {
            final short[] bucketToHash = accumulationBuffer.clone();
            final int sampleRate = inputAudioFormat.sampleRate;
            final int channels = inputAudioFormat.channelCount;

            hashingExecutor.submit(() -> {
                String liveHash = adSkipper.hashAudioSegment(bucketToHash, sampleRate, channels);

                // Save it for the UI
                if (liveHash != null) {
                    latestGeneratedHash = liveHash;
                }

                // THE CONNECTION LOGIC
                if (liveHash != null && knownAdHashes.containsKey(liveHash)) {
                    Log.d(TAG, "AdSkipper: MATCH FOUND! Triggering skip.");

                    int skipDuration = knownAdHashes.get(liveHash);

                    // Fire the event to the main thread
                    EventBus.getDefault().post(new AdDetectedEvent(skipDuration));

                    // Remove the hash so we don't accidentally skip twice for the same ad
                    knownAdHashes.remove(liveHash);
                }
            });
            accumulationIndex = 0;
        }
    }

    @Override
    protected void onFlush() {
        accumulationIndex = 0;
    }

    @Override
    protected void onReset() {
        accumulationBuffer = null;
    }
}