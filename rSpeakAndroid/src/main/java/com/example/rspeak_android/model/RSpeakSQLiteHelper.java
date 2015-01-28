package com.example.rspeak_android.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class RSpeakSQLiteHelper extends SQLiteOpenHelper {

    // threads database keyterms
    public static final String TABLE_QUESTIONS = "questions";
    public static final String QUESTIONS_COLUMN_ID = "question_id";
    public static final String QUESTIONS_COLUMN_CONTENT = "question_content";
    public static final String QUESTIONS_COLUMN_TIME_POSTED = "time_posted";
    public static final String QUESTIONS_COLUMN_ON_ASKER_DEVICE = "currently_on_asker_device";

    public static final String TABLE_THREADS = "threads";
    public static final String THREADS_COLUMN_ID = "thread_id";
    public static final String THREADS_COLUMN_QUESTION_ID = "question_id";
    public static final String THREADS_COLUMN_IS_STOPPED = "is_stopped";

    // responses database keyterms
    public static final String TABLE_RESPONSES = "responses";
    public static final String RESPONSES_COLUMN_RESPONSE_ID = "response_id";
    public static final String RESPONSES_COLUMN_THREAD_ID = "thread_id";
    public static final String RESPONSES_COLUMN_RESPONSE_CONTENT = "response_content";
    public static final String RESPONSES_COLUMN_ON_RESPONDER_DEVICE = "currently_on_responder_device";
    public static final String RESPONSES_COLUMN_TIME_POSTED = "time_posted";

    // HTTPRequests database keyterms
    public static final String TABLE_HTTPREQUESTS = "http_requests";
    public static final String HTTPREQUESTS_COLUMN_REQUEST_ID = "request_id";
    public static final String HTTPREQUESTS_COLUMN_TYPE = "type";
    public static final String HTTPREQUESTS_COLUMN_URL = "url";
    public static final String HTTPREQUESTS_COLUMN_DATA = "data";
    public static final String HTTPREQUESTS_COLUMN_TIME = "time";

    // further helper keyterms
    private static final String RSPEAK_DATABASE_NAME = "rspeak.db";
    private static final int RSPEAK_DATABASE_VERSION = 1;

    // database creation statements
    private static final String CREATE_QUESTIONS_DATABASE = "create table "
            + TABLE_QUESTIONS
            + "(" + QUESTIONS_COLUMN_ID + " integer primary key autoincrement, "
            + QUESTIONS_COLUMN_CONTENT + " text not null, "
            + QUESTIONS_COLUMN_TIME_POSTED + " integer, "
            + QUESTIONS_COLUMN_ON_ASKER_DEVICE + " numeric);";

    private static final String CREATE_THREADS_DATABASE = "create table "
            + TABLE_THREADS
            + "(" + THREADS_COLUMN_ID + " text primary key not null, "
            + THREADS_COLUMN_QUESTION_ID + " integer not null, "
            + THREADS_COLUMN_IS_STOPPED + " numeric);";

    private static final String CREATE_RESPONSES_DATABASE = "create table "
            + TABLE_RESPONSES
            + "(" + RESPONSES_COLUMN_RESPONSE_ID + " integer primary key autoincrement, "
            + RESPONSES_COLUMN_THREAD_ID + " text not null, "
            + RESPONSES_COLUMN_RESPONSE_CONTENT + " text not null, "
            + RESPONSES_COLUMN_ON_RESPONDER_DEVICE + " numeric, "
            + RESPONSES_COLUMN_TIME_POSTED + " integer);";

    private static final String CREATE_HTTPREQUESTS_DATABASE = "create table "
            + TABLE_HTTPREQUESTS
            + "(" + HTTPREQUESTS_COLUMN_REQUEST_ID + " integer primary key autoincrement, "
            + HTTPREQUESTS_COLUMN_TYPE + " integer not null, "
            + HTTPREQUESTS_COLUMN_URL + " text not null, "
            + HTTPREQUESTS_COLUMN_DATA + " text not null,"
            + HTTPREQUESTS_COLUMN_TIME + " integer);";

    private Context context;

    public RSpeakSQLiteHelper(Context context) {
        super(context, RSPEAK_DATABASE_NAME, null, RSPEAK_DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_QUESTIONS_DATABASE);
        database.execSQL(CREATE_THREADS_DATABASE);
        database.execSQL(CREATE_RESPONSES_DATABASE);
        database.execSQL(CREATE_HTTPREQUESTS_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(RSpeakSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_THREADS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_RESPONSES);
        onCreate(database);
    }

}
