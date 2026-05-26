package de.danoeh.antennapod.playback.service;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import de.danoeh.antennapod.playback.service.BuildConfig;

import android.util.Log;

public class AdSubmitter {
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static volatile boolean isMarkingAd = false;

    // Data structure to hold downloaded skips
    public static class AdSkip {
        public long timestampMs;
        public long durationMs;
        public String fingerprint;
    }

    public interface AdFetchCallback {
        void onSuccess(List<AdSkip> skips);
        void onFailure(String error);
    }

    public static void submitAd(String clientId, String episodeId, long startTimestampMs, String hash, long skipDurationMs) {
        try {
            JSONObject json = new JSONObject();
            json.put("client_id", clientId);
            json.put("episode_id", episodeId);
            json.put("timestamp_ms", startTimestampMs); // NEW
            json.put("fingerprint", hash);
            json.put("skip_duration_ms", skipDurationMs);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(BuildConfig.BASE_URL + "/api/submit")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("AdSubmitter", "Failed to submit ad", e);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("AdSubmitter", "Ad submitted: " + response.code());
                    response.close();
                }
            });
        } catch (Exception e) {
            Log.e("AdSubmitter", "JSON Error", e);
        }
    }

    public static void fetchAdSkips(String episodeId, AdFetchCallback callback) {
        Request request = new Request.Builder()
                .url(BuildConfig.BASE_URL + "/api/skips?episode_id=" + episodeId)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("Server returned " + response.code());
                    return;
                }
                try {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    List<AdSkip> episodeSkips = new ArrayList<>();

                    if (jsonResponse.has("fingerprints") && !jsonResponse.isNull("fingerprints")) {
                        JSONArray jsonArray = jsonResponse.getJSONArray("fingerprints");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject skipJson = jsonArray.getJSONObject(i);
                            AdSkip skip = new AdSkip();
                            skip.timestampMs = skipJson.getLong("timestamp_ms");
                            skip.durationMs = skipJson.getLong("skip_duration_ms");
                            skip.fingerprint = skipJson.getString("fingerprint");
                            episodeSkips.add(skip);
                        }
                    }
                    callback.onSuccess(episodeSkips);
                } catch (Exception e) {
                    callback.onFailure("JSON Parse error");
                } finally {
                    response.close();
                }
            }
        });
    }
    public static void reportAd(String clientId, String episodeId, long timestampMs) {
        try {
            JSONObject json = new JSONObject();
            json.put("client_id", clientId);
            json.put("episode_id", episodeId);
            json.put("timestamp_ms", timestampMs);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BuildConfig.BASE_URL + "/api/report")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("AdSubmitter", "Failed to send report", e);
                }
                @Override
                public void onResponse(Call call, Response response) {
                    response.close();
                }
            });
        } catch (Exception e) {
            Log.e("AdSubmitter", "JSON Error in report", e);
        }
    }

    public static void upvoteAd(String clientId, String episodeId, long timestampMs) {
        try {
            JSONObject json = new JSONObject();
            json.put("client_id", clientId);
            json.put("episode_id", episodeId);
            json.put("timestamp_ms", timestampMs);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BuildConfig.BASE_URL + "/api/upvote")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("AdSubmitter", "Failed to send upvote", e);
                }
                @Override
                public void onResponse(Call call, Response response) {
                    response.close();
                }
            });
        } catch (Exception e) {
            Log.e("AdSubmitter", "JSON Error in upvote", e);
        }
    }
}