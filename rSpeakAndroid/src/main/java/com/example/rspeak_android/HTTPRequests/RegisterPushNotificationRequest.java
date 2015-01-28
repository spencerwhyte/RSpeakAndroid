package com.example.rspeak_android.HTTPRequests;

import java.util.HashMap;

import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.rspeak_android.model.HTTPRequest;
import com.example.rspeak_android.model.HTTPRequestsDataSource;
import com.example.rspeak_android.gcm.GCMManager;

public class RegisterPushNotificationRequest extends HTTPRequest
{
    private Context context;
	private GCMManager gcmManager;
	
	public RegisterPushNotificationRequest(Context context)
	{
		super();
        this.context = context;
		this.gcmManager = new GCMManager( context );
        this.setType(HTTPRequest.Type.POST);
        this.setURL(HTTPRequest.BASE_URL + HTTPRequest.URL_REGISTER_PUSH_NOTIFICATION_ID);
	}
	
	public void send()
	{
        SharedPreferences device_properties = context.getSharedPreferences( "DEVICE_PROPERTIES", 0 );
        String device_id = device_properties.getString( HTTPRequest.DATA_DEVICE_ID, null );
        String push_notification_id = gcmManager.getRegistrationId( context );
        boolean device_id_is_set = device_properties.getBoolean( HTTPRequest.DATA_DEVICE_ID_SET, false );

        // if there is no device id then register a new one
        if ( !device_id_is_set )
        {
            RegisterDeviceRequest registerDeviceRequest = new RegisterDeviceRequest( context );
            registerDeviceRequest.send();
        }
        else if ( push_notification_id != null )
        {
            // then create the JSON object for the http request
            HashMap<String, String> params = new HashMap<String, String>();
            params.put( HTTPRequest.DATA_DEVICE_ID, device_id );
            params.put( HTTPRequest.DATA_PUSH_NOTIFICATION_ID, push_notification_id );
            JSONObject requestData = new JSONObject(params);
            this.setData(requestData.toString());

            // then add the question request to the database
            HTTPRequestsDataSource requestSource = new HTTPRequestsDataSource( context );
            requestSource.open();
            this.setID(requestSource.storeRequest(this));
            requestSource.close();

            // then try to send the request to the server
            super.send();
        }
	}
	
	// if the request is successful remove the HTTP request
	// from the database
	public Response.Listener<JSONObject> getSuccessHandler()
	{
		return new Response.Listener<JSONObject>() 
		{
			@TargetApi(Build.VERSION_CODES.GINGERBREAD)
			@Override
			public void onResponse( JSONObject response )
			{
				// Remove the request from the database
				HTTPRequestsDataSource requestSource = new HTTPRequestsDataSource( context );
				requestSource.open();
				requestSource.deleteRequest( RegisterPushNotificationRequest.this.getID() );
				requestSource.close();
				
				SharedPreferences device_properties = context.getSharedPreferences( "DEVICE_PROPERTIES", 0 );
				Editor editor = device_properties.edit().putBoolean( HTTPRequest.DATA_PUSH_NOTIFICATION_ID_SET, true );
				editor.apply();
			}
		};
	}

	// if the request is not successful
	public Response.ErrorListener getErrorHandler()
	{
		return new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse( VolleyError error )
			{
				Log.e( "RegisterPushNotificationRequest.getErrorHandler", "failed to register GCM push notification id" );
			}
		};
	}
}
