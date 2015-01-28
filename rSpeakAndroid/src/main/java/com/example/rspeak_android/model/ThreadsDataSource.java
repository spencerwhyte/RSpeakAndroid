package com.example.rspeak_android.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


public class ThreadsDataSource {

    // Database Fields
    private SQLiteDatabase database;
    private RSpeakSQLiteHelper dbHelper;
    private String[] allColumns = {
            RSpeakSQLiteHelper.THREADS_COLUMN_ID,
            RSpeakSQLiteHelper.THREADS_COLUMN_QUESTION_ID,
            RSpeakSQLiteHelper.THREADS_COLUMN_IS_STOPPED};
    private ResponsesDataSource responsesDataSource;

    public ThreadsDataSource(Context context) {
        dbHelper = new RSpeakSQLiteHelper(context);

        responsesDataSource = new ResponsesDataSource(context);
        responsesDataSource.open();
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
        responsesDataSource.close();
    }

    public Thread createThread(String thread_id,
                               long question_id,
                               boolean is_stopped) {
        ContentValues values = new ContentValues();

        values.put(RSpeakSQLiteHelper.THREADS_COLUMN_ID, thread_id);
        values.put(RSpeakSQLiteHelper.THREADS_COLUMN_QUESTION_ID, question_id);
        values.put(RSpeakSQLiteHelper.THREADS_COLUMN_IS_STOPPED, is_stopped);

        database.insert(RSpeakSQLiteHelper.TABLE_THREADS, null, values);
        Cursor cursor = database.query(RSpeakSQLiteHelper.TABLE_THREADS,
                allColumns,
                RSpeakSQLiteHelper.THREADS_COLUMN_ID + " = '" + thread_id + "'",
                null,
                null,
                null,
                null);
        cursor.moveToFirst();
        Thread newThread = cursorToThread(cursor);
        cursor.close();
        return newThread;
    }

    public void deleteThread(Thread thread) {
        String thread_id = thread.getThreadID();
        System.out.println("Thread with id " + thread_id + " and is being deleted.");

        // First delete all responses with that thread id then delete the actual thread
        database.delete(RSpeakSQLiteHelper.TABLE_RESPONSES,
                RSpeakSQLiteHelper.RESPONSES_COLUMN_THREAD_ID + " = " + thread_id,
                null);

        database.delete(RSpeakSQLiteHelper.TABLE_THREADS,
                RSpeakSQLiteHelper.THREADS_COLUMN_ID + " = " + thread_id,
                null);
    }


    private List<Thread> queryAllThreads(String conditions) {
        List<Thread> threads = new ArrayList<Thread>();

        Cursor cursor = database.query(RSpeakSQLiteHelper.TABLE_THREADS,
                allColumns,
                conditions,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Thread thread = cursorToThread(cursor);
            threads.add(thread);
            cursor.moveToNext();
        }

        cursor.close();
        return threads;
    }

    public List<Thread> getAllThreads() {
        return queryAllThreads(null);
    }

    public Thread getThreadById(String thread_id) {
        return queryAllThreads(
                RSpeakSQLiteHelper.THREADS_COLUMN_ID +
                        " = '" +
                        thread_id +
                        "'").get(0);
    }

    public List<Thread> getThreadsByQuestionID(long question_id) {
        return queryAllThreads(
                RSpeakSQLiteHelper.THREADS_COLUMN_QUESTION_ID +
                        " = '" +
                        question_id +
                        "'");
    }

    private Thread cursorToThread(Cursor cursor) {
        Thread thread = new Thread();

        thread.setThreadID(
                cursor.getString(
                        cursor.getColumnIndex(RSpeakSQLiteHelper.THREADS_COLUMN_ID)));
        thread.setQuestionID(
                cursor.getLong(
                        cursor.getColumnIndex(RSpeakSQLiteHelper.THREADS_COLUMN_QUESTION_ID)));
        thread.setIsStopped(
                cursor.getInt(
                        cursor.getColumnIndex(RSpeakSQLiteHelper.THREADS_COLUMN_IS_STOPPED)) > 0);
        thread.setResponses(
                responsesDataSource.queryAllResponses(
                        RSpeakSQLiteHelper.RESPONSES_COLUMN_THREAD_ID + " = '" + thread.getThreadID() + "'"));

        return thread;
    }
}
