package com.example.rspeak_android.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.rspeak_android.HTTPRequests.AskQuestionRequest;
import com.example.rspeak_android.HTTPRequests.RegisterDeviceRequest;
import com.example.rspeak_android.HTTPRequests.RegisterPushNotificationRequest;
import com.example.rspeak_android.HTTPRequests.RespondToQuestionRequest;

import java.util.ArrayList;
import java.util.List;


public class HTTPRequestsDataSource {

    // Database Fields
    private Context context;
    private SQLiteDatabase database;
    private RSpeakSQLiteHelper dbHelper;
    private String[] allColumns = {
            RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_REQUEST_ID,
            RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_TYPE,
            RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_URL,
            RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_DATA};

    public HTTPRequestsDataSource(Context context) {
        this.context = context;
        this.dbHelper = new RSpeakSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Creates a new database request and returns its id.
    public int storeRequest(HTTPRequest request) {
        ContentValues values = new ContentValues();

        values.put(RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_TYPE, request.getType().ordinal());
        values.put(RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_URL, request.getURL());
        values.put(RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_DATA, request.getData());
        values.put(RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_TIME, System.currentTimeMillis());


        int requestId = (int) database.insert(RSpeakSQLiteHelper.TABLE_HTTPREQUESTS, null, values);

        return requestId;
    }

    public void deleteRequest(int request_id) {
        database.delete(RSpeakSQLiteHelper.TABLE_HTTPREQUESTS,
                RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_REQUEST_ID + " = " + request_id,
                null);
    }

    // Requests should be retrieved and sent in the order they were created
    // so sort by oldest first
    private List<HTTPRequest> queryAllRequests(String conditions) {
        List<HTTPRequest> requests = new ArrayList<HTTPRequest>();

        Cursor cursor = database.query(RSpeakSQLiteHelper.TABLE_HTTPREQUESTS,
                allColumns,
                conditions,
                null,
                null,
                RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_TIME + " ASC",
                null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HTTPRequest request = cursorToRequest(cursor);
            if (request != null) {
                requests.add(request);
            }
            cursor.moveToNext();
        }

        cursor.close();
        return requests;
    }

    public List<HTTPRequest> getAllRequests() {
        return queryAllRequests(null);
    }

    private HTTPRequest cursorToRequest(Cursor cursor) {
        int requestId = cursor.getInt(cursor.getColumnIndex(RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_REQUEST_ID));
        String url = cursor.getString(cursor.getColumnIndex(RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_URL));
        String dataString = cursor.getString(cursor.getColumnIndex(RSpeakSQLiteHelper.HTTPREQUESTS_COLUMN_DATA));

        JSONObject data;
        try {
             data = new JSONObject(dataString);
        } catch (JSONException e) {
            Log.e("HTTPRequestsDataSource.cursorToRequest", "Couldn't convert data string to json object.", e);
            this.deleteRequest(requestId);
            return null;
        }

        // The returned request should replace the need for the previous request
        this.deleteRequest(requestId);

        return createRequestFromURL(url, data);
    }

    // Returns the request object corresponding to the provided URL, and using
    // the provided data.
    // The requests retrieved from the database table do not have the
    // appropriate success/error handlers, therefore this method is needed in
    // order to return usable requests.
    public HTTPRequest createRequestFromURL(String url, JSONObject data)
    {
        try {
            if (url.equals(HTTPRequest.URL_ASK)) {
                String question = data.getString(HTTPRequest.DATA_CONTENT);
                return new AskQuestionRequest(context, question);
            } else if (url.equals(HTTPRequest.URL_REGISTER_DEVICE)) {
                return new RegisterDeviceRequest(context);
            } else if (url.equals(HTTPRequest.URL_REGISTER_PUSH_NOTIFICATION_ID)) {
                return new RegisterPushNotificationRequest(context);
            } else if (url.equals(HTTPRequest.URL_RESPOND)) {
                String threadId = data.getString(HTTPRequest.DATA_THREAD_ID);
                String response = data.getString(HTTPRequest.DATA_CONTENT);
                return new RespondToQuestionRequest(context, threadId, response);
            }
        } catch(JSONException e) {
            Log.e("HTTPRequestsDataSource.createRequestFromURL", "Failed to retrieve properties from JSON object.", e);
            return null;
        }

        assert false : "HTTPRequestsDataSource.createRequestFromURL: Unknown URL found while trying to create request.";
        return null;
    }
}
