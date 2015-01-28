package com.example.rspeak_android.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


public class ResponsesDataSource {

    // Database Fields
    private SQLiteDatabase database;
    private RSpeakSQLiteHelper dbHelper;
    private String[] allColumns = {RSpeakSQLiteHelper.RESPONSES_COLUMN_RESPONSE_ID,
            RSpeakSQLiteHelper.RESPONSES_COLUMN_THREAD_ID,
            RSpeakSQLiteHelper.RESPONSES_COLUMN_RESPONSE_CONTENT,
            RSpeakSQLiteHelper.RESPONSES_COLUMN_ON_RESPONDER_DEVICE,
            RSpeakSQLiteHelper.RESPONSES_COLUMN_TIME_POSTED};

    public ResponsesDataSource(Context context) {
        dbHelper = new RSpeakSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Response createResponse(String thread_id,
                                   String response_content,
                                   boolean currently_on_responder_device,
                                   long time_posted) {
        ContentValues values = new ContentValues();

        values.put(RSpeakSQLiteHelper.RESPONSES_COLUMN_THREAD_ID, thread_id);
        values.put(RSpeakSQLiteHelper.RESPONSES_COLUMN_RESPONSE_CONTENT, response_content);
        values.put(RSpeakSQLiteHelper.RESPONSES_COLUMN_ON_RESPONDER_DEVICE, currently_on_responder_device);
        values.put(RSpeakSQLiteHelper.RESPONSES_COLUMN_TIME_POSTED, time_posted);

        long response_id = database.insert(RSpeakSQLiteHelper.TABLE_RESPONSES, null, values);
        Cursor cursor = database.query(RSpeakSQLiteHelper.TABLE_RESPONSES,
                allColumns,
                RSpeakSQLiteHelper.RESPONSES_COLUMN_RESPONSE_ID + " = '" + response_id + "'",
                null,
                null,
                null,
                null);
        cursor.moveToFirst();
        Response newResponse = cursorToResponse(cursor);
        cursor.close();
        return newResponse;
    }

    public List<Response> queryAllResponses(String conditions) {
        List<Response> responses = new ArrayList<Response>();

        Cursor cursor = database.query(RSpeakSQLiteHelper.TABLE_RESPONSES,
                allColumns,
                conditions,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Response response = cursorToResponse(cursor);
            responses.add(response);
            cursor.moveToNext();
        }

        cursor.close();
        return responses;
    }


    // no delete response method because all responses within a thread
    // are deleted once the parent thread is deleted
    // response deletion is implemented in ThreadsDataSource.deleteThread

    public List<Response> getAllResponses() {
        return queryAllResponses(null);
    }

    public List<Response> getResponsesFromThreadId(String id) {
        return queryAllResponses(
                RSpeakSQLiteHelper.RESPONSES_COLUMN_THREAD_ID +
                        " = '" +
                        id +
                        "'");
    }

    private Response cursorToResponse(Cursor cursor) {
        Response response = new Response();

        response.setResponseID(
                cursor.getLong(
                        cursor.getColumnIndex(RSpeakSQLiteHelper.RESPONSES_COLUMN_RESPONSE_ID)));
        response.setThreadID(
                cursor.getString(
                        cursor.getColumnIndex(RSpeakSQLiteHelper.RESPONSES_COLUMN_THREAD_ID)));
        response.setResponseContent(
                cursor.getString(
                        cursor.getColumnIndex(RSpeakSQLiteHelper.RESPONSES_COLUMN_RESPONSE_CONTENT)));
        response.setCurrentlyOnResponderDevice(
                cursor.getInt(
                        cursor.getColumnIndex(RSpeakSQLiteHelper.RESPONSES_COLUMN_ON_RESPONDER_DEVICE)) > 0);
        response.setTimePosted(
                cursor.getLong(
                        cursor.getColumnIndex(RSpeakSQLiteHelper.RESPONSES_COLUMN_TIME_POSTED)));

        return response;
    }
}
