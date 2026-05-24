package de.danoeh.antennapod.event;

public class AdSkippedEvent {
    public final long originalTimestampMs;

    public AdSkippedEvent(long originalTimestampMs) {
        this.originalTimestampMs = originalTimestampMs;
    }
}
