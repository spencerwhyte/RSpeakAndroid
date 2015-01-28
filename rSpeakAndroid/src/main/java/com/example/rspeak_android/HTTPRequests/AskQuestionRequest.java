/* 
 * This class handles data relating to a question when it is submitted.
 * 
 * The tasks of this class are:
 * 1. Add the question to the local questions database.
 * 2. Send the question to the server to update the backend data model.
 * 
 */

package com.example.rspeak_android.HTTPRequests;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.rspeak_android.RSpeakApplication;
import com.example.rspeak_android.model.HTTPRequest;
import com.example.rspeak_android.model.HTTPRequestsDataSource;
import com.example.rspeak_android.model.Question;
import com.example.rspeak_android.model.QuestionsDataSource;
import com.example.rspeak_android.model.ThreadsDataSource;

public class AskQuestionRequest extends HTTPRequest
{
    private Context context;
	private String questionString;
	
	public AskQuestionRequest(Context context, String questionString)
	{
        super();
		this.context = context;
		this.questionString = questionString;
        this.setType(HTTPRequest.Type.POST);
        this.setURL(HTTPRequest.BASE_URL + HTTPRequest.URL_ASK);
	}
	
	public void send()
	{
        // first add the question to the database
        QuestionsDataSource questionSource = new QuestionsDataSource( context );
        questionSource.open();
        Question question = questionSource.createQuestion( questionString, System.currentTimeMillis(), true );
        questionSource.close();

        // get the device id
        SharedPreferences device_properties = context.getSharedPreferences( "DEVICE_PROPERTIES", 0 );
        String device_id = device_properties.getString( RSpeakApplication.PROPERTY_DEVICE_ID, null );

        // then create the JSON object for the http request
        HashMap<String, String> params = new HashMap<String, String>();
        params.put( HTTPRequest.DATA_DEVICE_ID, device_id );
        params.put( HTTPRequest.DATA_QUESTION_ID, String.valueOf( question.getID() ));
        params.put( HTTPRequest.DATA_CONTENT, questionString );
        JSONObject request_data = new JSONObject(params);
        this.setData(request_data.toString());

        // then add the question request to the database
        HTTPRequestsDataSource requestSource = new HTTPRequestsDataSource( context );
        requestSource.open();
        this.setID(requestSource.storeRequest(this));
        requestSource.close();
		
		// then try to send the request to the server
        super.send();
	}
	
	// if the request is successful
	public Response.Listener<JSONObject> getSuccessHandler()
	{
        final int id = this.getID();
		return new Response.Listener<JSONObject>() 
		{
			@Override
			public void onResponse( JSONObject response )
			{
				// Remove the request from the database
				HTTPRequestsDataSource requestSource = new HTTPRequestsDataSource( context );
				requestSource.open();
				requestSource.deleteRequest( id );
				requestSource.close();
				
				// get the question id and the thread id from the json response
				String question_id;
				String thread_id;
				try
				{
					question_id = response.getString( HTTPRequest.DATA_QUESTION_ID );
					thread_id = response.getString( HTTPRequest.DATA_THREAD_ID );
				}
				catch ( JSONException e )
				{
					Log.e( "AskQuestionTransaction.getSuccessHandler", "Error: couldn't read either question or thread id after sending question to server: " + e.toString() );
					return;
				}
				
				if ( question_id != null && thread_id != null )
				{
					ThreadsDataSource tSource = new ThreadsDataSource( context );
					tSource.open();
					
					tSource.createThread( thread_id,
							Long.parseLong( question_id ),
							false );

					tSource.close();
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
				Log.e( "AskQuestionTransaction.getErrorHandler", "failed to send the question to the server:\n" + error.toString() );
			}
		};
	}
}
