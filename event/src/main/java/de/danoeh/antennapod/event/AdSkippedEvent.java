package de.danoeh.antennapod.event;

public class AdSkippedEvent {
    public final long originalTimestampMs;
    public final String episodeId;

    public AdSkippedEvent(String episodeId, long originalTimestampMs) {
        this.episodeId = episodeId;
        this.originalTimestampMs = originalTimestampMs;
    }
}
