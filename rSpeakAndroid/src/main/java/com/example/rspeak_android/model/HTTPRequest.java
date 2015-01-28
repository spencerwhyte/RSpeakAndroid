package com.example.rspeak_android.model;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.rspeak_android.RSpeakApplication;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class HTTPRequest {
    // URL constants
    public static final String BASE_URL = "http://192.168.0.10:8000/v1";
    public static final String URL_REGISTER_DEVICE = "/register/device/";
    public static final String URL_REGISTER_PUSH_NOTIFICATION_ID = "/register/push_notification_id/";
    public static final String URL_ASK = "/ask/";
    public static final String URL_RETRIEVE_CREDIT_SCORE = "/retrieve/credit_score/";
    public static final String URL_RESPOND = "/respond/";
    public static final String URL_RETRIEVE_UPDATES = "/retrieve/updates/";

    // JSON data constants
    public static final String DATA_DEVICE_ID = "device_id";
    public static final String DATA_PUSH_NOTIFICATION_ID = "push_notification_id";
    public static final String DATA_DEVICE_ID_SET = "device_id_set";
    public static final String DATA_PUSH_NOTIFICATION_ID_SET = "push_notification_id_set";
    public static final String DATA_DEVICE_TYPE = "device_type";
    public static final String DATA_QUESTION_ID = "question_id";
    public static final String DATA_THREAD_ID = "thread_id";
    public static final String DATA_CONTENT = "content";
    public static final String DATA_TIME = "time";
    public static final String DATA_VALID_ID = "valid_id";
    public static final String DATA_CREDIT_POINTS = "credit_points";
    public static final String DATA_QUESTION_UPDATES = "question_updates";
    public static final String DATA_RESPONSE_UPDATES = "response_updates";

    public enum Type {GET, POST}

    // variables that change form instance to instance
    private int request_id;
    private Type type;
    private String url;
    private String data;

    public HTTPRequest() {
        this.data = "";
    }

    public int getID() {
        return request_id;
    }

    public void setID(int request_id) {
        this.request_id = request_id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getURL() {
        return this.url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    // Returns the event handler when receiving a successful response (i.e.
    // any server response).
    abstract public Response.Listener<JSONObject> getSuccessHandler();

    // Returns the event handler when receiving an error occurs
    abstract public Response.ErrorListener getErrorHandler();

    public void send() {
        JSONObject data_object = null;
        JsonObjectRequest request = null;

        if (type == Type.POST) {
            try {
                data_object = new JSONObject(this.data);
            } catch (JSONException e) {
                Log.e("HTTPRequest.startRequest", "The data string from the database couldn't be converted to a JSON Object.", e);
            }

            request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    data_object,
                    this.getSuccessHandler(),
                    this.getErrorHandler());
        }

        RequestQueue requestQueue = RSpeakApplication.getRequestQueue();
        requestQueue.add(request);
    }
}
