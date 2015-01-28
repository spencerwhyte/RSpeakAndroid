package com.example.rspeak_android.HTTPRequests;

import java.util.HashMap;

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
import com.example.rspeak_android.RSpeakApplication;
import com.example.rspeak_android.model.HTTPRequest;

public class GetCreditScoreRequest extends HTTPRequest
{
    private Context context;

	public GetCreditScoreRequest(Context context)
	{
		super();
        this.context = context;
		this.setType(HTTPRequest.Type.POST);
		this.setURL(HTTPRequest.BASE_URL + HTTPRequest.URL_RETRIEVE_CREDIT_SCORE);
	}
	
	public void send()
	{
		// get the device id
		SharedPreferences device_properties = context.getSharedPreferences( "DEVICE_PROPERTIES", 0 );
		String device_id = device_properties.getString( RSpeakApplication.PROPERTY_DEVICE_ID, null );
		
		// then create the JSON object for the http request
		HashMap<String, String> params = new HashMap<String, String>();
	    params.put( HTTPRequest.DATA_DEVICE_ID, device_id );
	    JSONObject requestData = new JSONObject( params );
	    this.setData( requestData.toString() );

		// then try to send the request to the server
        super.send();
	}
	
	// if the request is successful
	public Response.Listener<JSONObject> getSuccessHandler()
	{
		return new Response.Listener<JSONObject>() 
		{
			@TargetApi(Build.VERSION_CODES.GINGERBREAD)
			@Override
			public void onResponse( JSONObject response )
			{
				// get the question id and the thread id from the json response
				int credit_points = -1;
				try
				{
					credit_points = response.getInt( HTTPRequest.DATA_CREDIT_POINTS );
				}
				catch ( JSONException e )
				{
					Log.e( "GetCreditScoreRequest.getSuccessHandler", "Error: couldn't read credit points after sending device id to server: " + e.toString() );
					return;
				}
				
				if ( credit_points != -1 ) // -1 could indicate the server didn't find the device id
				{
					SharedPreferences device_properties = context.getSharedPreferences( "DEVICE_PROPERTIES", 0 );
					Editor editor = device_properties.edit().putInt( HTTPRequest.DATA_CREDIT_POINTS, credit_points );
					if ( Build.VERSION.SDK_INT < 9 )
					{
						editor.commit();
					} 
					else 
					{
						editor.apply();
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
			@Override
			public void onErrorResponse( VolleyError error )
			{
				Log.e( "GetCreditScoreRequest.getErrorHandler", "failed to retrieve the credit score from the server:\n" + error.toString() );
			}
		};
	}
}
