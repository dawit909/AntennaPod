package de.danoeh.antennapod.event.playback;

public class AdDetectedEvent {
    public final int skipDurationMs;

    public AdDetectedEvent(int skipDurationMs) {
        this.skipDurationMs = skipDurationMs;
    }
}