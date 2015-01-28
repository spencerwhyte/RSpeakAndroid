package com.example.rspeak_android.HTTPRequests;

import java.util.HashMap;
import java.util.Random;

import org.json.JSONException;
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

public class RegisterDeviceRequest extends HTTPRequest
{
    private Context context;

	private static char[] symbols;
	static 
	{
		StringBuilder tmp = new StringBuilder();
		
		for ( char ch = '0'; ch <= '9'; ++ch )
		{
			tmp.append( ch );
		}
		for ( char ch = 'A'; ch <= 'Z'; ++ch )
		{
			tmp.append( ch );
		}
		for ( char ch = 'a'; ch <= 'z'; ++ch )
		{
			tmp.append( ch );
		}
		
		symbols = tmp.toString().toCharArray();
	}
	
	public RegisterDeviceRequest(Context context) {
        super();
        this.context = context;
        this.setType(HTTPRequest.Type.POST);
        this.setURL(HTTPRequest.BASE_URL + HTTPRequest.URL_REGISTER_DEVICE);
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void send() {
        SharedPreferences device_properties = context.getSharedPreferences( "DEVICE_PROPERTIES", 0 );
        boolean deviceIDIsSet = device_properties.getBoolean( HTTPRequest.DATA_DEVICE_ID_SET, false );
        String deviceID = device_properties.getString(HTTPRequest.DATA_DEVICE_ID, null);

        // only go through with the transaction if there is no device id already registered
        if (deviceIDIsSet){
            assert deviceID != null : Log.e("RegisterDeviceRequest.send", "Expected deviceID to be a non-empty string, found null.");

            // then create the JSON object for the http request
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(HTTPRequest.DATA_DEVICE_ID, deviceID);
            params.put(HTTPRequest.DATA_DEVICE_TYPE, "ANDROID");
            JSONObject requestData = new JSONObject(params);
            this.setData(requestData.toString());
        }
        else {
            Random random = new Random();

            // first create a random 16 character String
            char[] id = new char[16];
            for (int i = 0; i < 16; i++) {
                id[i] = symbols[random.nextInt(symbols.length)];
            }
            deviceID = new String(id);

            // put the id in the shared preferences
            Editor editor = device_properties.edit().putString(HTTPRequest.DATA_DEVICE_ID, deviceID);
            editor.putBoolean(HTTPRequest.DATA_DEVICE_ID_SET, true);
            editor.apply();

            // then create the JSON object for the http request
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(HTTPRequest.DATA_DEVICE_ID, deviceID);
            params.put(HTTPRequest.DATA_DEVICE_TYPE, "ANDROID");
            JSONObject requestData = new JSONObject(params);
            this.setData(requestData.toString());

            // then add the question request to the database
            HTTPRequestsDataSource requestSource = new HTTPRequestsDataSource(context);
            requestSource.open();
            this.setID(requestSource.storeRequest(this));
            requestSource.close();
        }

        // then try to send the request to the server
        super.send();
	}
	
	// if the request is successful
	public Response.Listener<JSONObject> getSuccessHandler()
	{
		return new Response.Listener<JSONObject>() 
		{
			@Override
			public void onResponse( JSONObject response )
			{
				boolean valid_id;
				
				try
				{
					valid_id = response.getBoolean(HTTPRequest.DATA_VALID_ID);
				}
				catch( JSONException e )
				{
					Log.e( "RegisterDeviceRequest.getSuccessHandler", "The JSON response received after registering the device couldn't be parsed" );
					return;
				}
				
				// Remove the request from the database
				HTTPRequestsDataSource requestSource = new HTTPRequestsDataSource( context );
				requestSource.open();
				requestSource.deleteRequest( RegisterDeviceRequest.this.getID() );
				requestSource.close();

                // If the server declares the ID invalid then it is assumed
                // to be already in use.
                // In that case, delete the ID stored in the preferences and
                // create a new ID/request.
				if ( !valid_id )
				{
                    SharedPreferences deviceProperties = context.getSharedPreferences( "DEVICE_PROPERTIES", 0 );
                    Editor devicePropertiesEditor = deviceProperties.edit().remove(HTTPRequest.DATA_DEVICE_ID);
                    devicePropertiesEditor.putBoolean(HTTPRequest.DATA_DEVICE_ID_SET, false);
                    devicePropertiesEditor.apply();

                    RegisterDeviceRequest.this.send();
				}
				else // check if there's a push notification id, if not then try to create one 
				{	
					GCMManager gcmManager = new GCMManager( context );
					String pushNotificationId = gcmManager.getRegistrationId( context );
					if ( pushNotificationId == null )
					{
						RegisterPushNotificationRequest registerPushNotificationRequest = new RegisterPushNotificationRequest( context );
                        registerPushNotificationRequest.send();
					}
				}
			}
		};
	}

	// if the request is not successful
	public Response.ErrorListener getErrorHandler()
	{
		return new Response.ErrorListener()
		{
			@TargetApi(Build.VERSION_CODES.GINGERBREAD)
			@Override
			public void onErrorResponse( VolleyError error )
			{
				// unset device id in shared preferences
				SharedPreferences device_properties = context.getSharedPreferences( "DEVICE_PROPERTIES", 0 );
				Editor editor = device_properties.edit().putString( HTTPRequest.DATA_DEVICE_ID, null );
				editor.putBoolean( HTTPRequest.DATA_DEVICE_ID_SET, false );
				editor.apply();
				
				Log.e( "RegisterDeviceRequest.getErrorHandler", "failed to register the device with the server" );
			}
		};
	}
}
