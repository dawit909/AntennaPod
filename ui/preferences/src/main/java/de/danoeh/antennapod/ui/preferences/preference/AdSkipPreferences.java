package de.danoeh.antennapod.ui.preferences.preference;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import de.danoeh.antennapod.ui.preferences.R;

public class AdSkipPreferences {
    public static boolean isEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_ad_skip_enabled_key);
        // Default to false for privacy
        return prefs.getBoolean(key, false);
    }

    public static String getServerUrl(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_ad_skip_server_key);
        String defaultUrl = context.getString(R.string.pref_ad_skip_server_default);

        String url = prefs.getString(key, defaultUrl);

        // Strip trailing slashes to prevent malformed API calls like "https://url.com//api/skips"
        if (url != null && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
