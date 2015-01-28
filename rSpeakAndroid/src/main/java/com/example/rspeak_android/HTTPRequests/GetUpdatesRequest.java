package com.example.rspeak_android.HTTPRequests;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.rspeak_android.RSpeakApplication;
import com.example.rspeak_android.model.HTTPRequest;
import com.example.rspeak_android.model.Question;
import com.example.rspeak_android.model.QuestionsDataSource;
import com.example.rspeak_android.model.ResponsesDataSource;
import com.example.rspeak_android.model.ThreadsDataSource;

public class GetUpdatesRequest extends HTTPRequest
{
	private Context context;
	
	public GetUpdatesRequest(Context context)
	{
		super();
        this.context = context;
		this.setType(HTTPRequest.Type.POST);
		this.setURL(HTTPRequest.BASE_URL + HTTPRequest.URL_RETRIEVE_UPDATES);
	}
	
	public void send()
	{
		// get the device id
		SharedPreferences device_properties = context.getSharedPreferences( "DEVICE_PROPERTIES", 0 );
		String device_id = device_properties.getString( RSpeakApplication.PROPERTY_DEVICE_ID, null );
		
		// then create the JSON object for the http request
		HashMap<String, String> params = new HashMap<String, String>();
	    params.put( HTTPRequest.DATA_DEVICE_ID, device_id );
	    JSONObject requestData = new JSONObject(params);
        this.setData(requestData.toString());
		
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
				// get the question and response updates
				JSONArray question_updates = null;
				JSONArray response_updates = null;
				try
				{
					question_updates = response.getJSONArray( HTTPRequest.DATA_QUESTION_UPDATES );
					response_updates = response.getJSONArray( HTTPRequest.DATA_RESPONSE_UPDATES );
					
					// add foreign question/thread pairs to the database
					if ( question_updates != null )
					{
						QuestionsDataSource qSource = new QuestionsDataSource( context );
						ThreadsDataSource tSource = new ThreadsDataSource( context );
						qSource.open();
						tSource.open();
						
						for ( int i = 0; i < question_updates.length(); i++ )
						{
							JSONObject question_update = question_updates.getJSONObject( i );
							String thread_id = question_update.getString( HTTPRequest.DATA_THREAD_ID );
							String question_content = question_update.getString( HTTPRequest.DATA_CONTENT );
							int time_posted = question_update.getInt( HTTPRequest.DATA_TIME );
							
							// add the question to the database							
							Question question = qSource.createQuestion( question_content, time_posted, false );
							
							// add the thread to the database
							tSource.createThread( thread_id, question.getID(), false );
						}
						
						qSource.close();
						tSource.close();
					}
					
					// add foreign responses to current threads
					if ( response_updates != null )
					{
						ResponsesDataSource rSource = new ResponsesDataSource( context );
						rSource.open();
						
						for ( int i = 0; i < response_updates.length(); i++ )
						{
							JSONObject response_update = response_updates.getJSONObject( i );
							String thread_id = response_update.getString( HTTPRequest.DATA_THREAD_ID );
							String response_content = response_update.getString( HTTPRequest.DATA_CONTENT );
							int time_posted = response_update.getInt( HTTPRequest.DATA_TIME );
							
							// add the response to the database							
							rSource.createResponse( thread_id, response_content, false, time_posted );
						}
						
						rSource.close();
					}
				}
				catch ( JSONException e )
				{
					Log.e( "GetUpdatesRequest.getSuccessHandler", "Error: couldn't read question/response arrays after sending device id to server: " + e.toString() );
					return;
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
				Log.e( "GetUpdatesRequest.getErrorHandler", "failed to retrieve the question/response updates from the server:\n" + error.toString() );
			}
		};
	}
}
