package com.alp2app.smsspamdetection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SpamDB";
    private static final int DATABASE_VERSION = 1;

    // Tablo ve kolon isimleri
    public static final String TABLE_SPAM = "spam_messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_IS_BLOCKED = "is_blocked";

    private static final String CREATE_TABLE_SPAM = 
        "CREATE TABLE " + TABLE_SPAM + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_SENDER + " TEXT, " +
        COLUMN_MESSAGE + " TEXT, " +
        COLUMN_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
        COLUMN_IS_BLOCKED + " INTEGER DEFAULT 0);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SPAM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPAM);
        onCreate(db);
    }

    public long addSpamMessage(String sender, String message, boolean isBlocked) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_MESSAGE, message);
        values.put(COLUMN_IS_BLOCKED, isBlocked ? 1 : 0);
        return db.insert(TABLE_SPAM, null, values);
    }

    public Cursor getAllSpamMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SPAM, null, null, null, null, null, 
                       COLUMN_DATE + " DESC");
    }
} 