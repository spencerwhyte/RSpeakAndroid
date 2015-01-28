package com.example.rspeak_android.HTTPRequests;

import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.rspeak_android.RSpeakApplication;
import com.example.rspeak_android.model.HTTPRequest;
import com.example.rspeak_android.model.HTTPRequestsDataSource;
import com.example.rspeak_android.model.ResponsesDataSource;

public class RespondToQuestionRequest extends HTTPRequest
{
    private Context context;
	private String threadId;
	private String responseString;
	
	public RespondToQuestionRequest(Context context, String threadId, String response)
	{
		super();
        this.context = context;
		this.threadId = threadId;
		this.responseString = response;
        this.setType(HTTPRequest.Type.POST);
        this.setURL(HTTPRequest.BASE_URL + HTTPRequest.URL_RESPOND);
	}
	
	public void beginTransaction()
	{
        // first add the response to the database
        ResponsesDataSource responsesSource = new ResponsesDataSource( context );
        responsesSource.open();
        responsesSource.createResponse(threadId, responseString, true, System.currentTimeMillis() );
        responsesSource.close();

        // get the device id
        SharedPreferences device_properties = context.getSharedPreferences( "DEVICE_PROPERTIES", 0 );
        String device_id = device_properties.getString( RSpeakApplication.PROPERTY_DEVICE_ID, null );

        // then create the JSON object for the http request
        HashMap<String, String> params = new HashMap<String, String>();
        params.put( HTTPRequest.DATA_DEVICE_ID, device_id );
        params.put( HTTPRequest.DATA_THREAD_ID, threadId);
        params.put( HTTPRequest.DATA_CONTENT, responseString);
        JSONObject requestData = new JSONObject(params);
        this.setData(requestData.toString());

        // then add the question request to the database
        HTTPRequestsDataSource requestSource = new HTTPRequestsDataSource( context );
        requestSource.open();
        this.setID(requestSource.storeRequest(this));
        requestSource.close();

		// then try to send the request to the server
		this.send();
	}
	
	// if the request is successful
	public Response.Listener<JSONObject> getSuccessHandler()
	{
		return new Response.Listener<JSONObject>() 
		{
			@Override
			public void onResponse( JSONObject response )
			{
				// Remove the request from the database
				HTTPRequestsDataSource requestSource = new HTTPRequestsDataSource( context );
				requestSource.open();
				requestSource.deleteRequest( RespondToQuestionRequest.this.getID() );
				requestSource.close();
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
				Log.e( "RespondToQuestionRequest.getErrorHandler", "failed to send the response to the server:\n" + error.toString() );
			}
		};
	}
}
