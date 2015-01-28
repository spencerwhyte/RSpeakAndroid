package com.example.rspeak_android;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;


public class RSpeakApplication extends Application
{
	public static final String PROPERTY_DEVICE_ID = "device_id";
	public static final String PROPERTY_PUSH_NOTIFICATION_ID = "push_notification_id";
	public static final String PROPERTY_APP_VERSION = "app_version";
	public static RequestQueue requestQueue;

    @Override
    public void onCreate() 
    {
        super.onCreate();
        requestQueue = Volley.newRequestQueue( getApplicationContext() );
    }
    
    public static RequestQueue getRequestQueue()
    {
    	return requestQueue;
    }
    
    public SharedPreferences getGCMPreferences() 
    {
        return getSharedPreferences( RSpeakApplication.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
}
