package de.danoeh.antennapod.playback.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class LocalSkipDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "local_pioneer_skips.db";
    private static final int DATABASE_VERSION = 1;

    public LocalSkipDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE local_skips (episode_id TEXT, timestamp_ms INTEGER, duration_ms INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS local_skips");
        onCreate(db);
    }

    public void addLocalSkip(String episodeId, long timestamp, long duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("episode_id", episodeId);
        values.put("timestamp_ms", timestamp);
        values.put("duration_ms", duration);
        db.insert("local_skips", null, values);
        db.close();
    }

    public List<AdSubmitter.AdSkip> getLocalSkips(String episodeId) {
        List<AdSubmitter.AdSkip> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT timestamp_ms, duration_ms FROM local_skips WHERE episode_id = ?", new String[]{episodeId});

        if (cursor.moveToFirst()) {
            do {
                AdSubmitter.AdSkip skip = new AdSubmitter.AdSkip();
                skip.timestampMs = cursor.getLong(0);
                skip.durationMs = cursor.getLong(1);
                skip.fingerprint = "local_override"; // Identifier for diagnostic purposes
                list.add(skip);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}