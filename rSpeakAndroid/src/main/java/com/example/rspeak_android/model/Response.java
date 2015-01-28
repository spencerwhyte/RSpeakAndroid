package com.example.rspeak_android.model;

public class Response {
    private long response_id;
    private String thread_id;
    private String response_content;
    private boolean currently_on_responder_device;
    private long time_posted;

    public long getResponseID() {
        return response_id;
    }

    public void setResponseID(long new_response_id) {
        response_id = new_response_id;
    }

    public String getThreadID() {
        return thread_id;
    }

    public void setThreadID(String new_thread_id) {
        thread_id = new_thread_id;
    }

    public String getResponseContent() {
        return response_content;
    }

    public void setResponseContent(String new_response_content) {
        response_content = new_response_content;
    }

    public boolean isCurrentlyOnResponderDevice() {
        return currently_on_responder_device;
    }

    public void setCurrentlyOnResponderDevice(boolean new_currently_on_responder_device) {
        currently_on_responder_device = new_currently_on_responder_device;
    }

    public long getTimePosted() {
        return time_posted;
    }

    public void setTimePosted(long new_time_posted) {
        time_posted = new_time_posted;
    }
}
