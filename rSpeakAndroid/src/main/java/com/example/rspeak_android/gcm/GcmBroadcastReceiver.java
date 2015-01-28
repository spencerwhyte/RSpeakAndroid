/* This class listens to network events such as loss or
 * gain of connection.
 * 
 */

package com.example.rspeak_android.gcm;

import java.util.List;

import com.example.rspeak_android.HTTPRequests.GetUpdatesRequest;
import com.example.rspeak_android.model.HTTPRequest;
import com.example.rspeak_android.model.HTTPRequestsDataSource;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class GcmBroadcastReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive( Context context, Intent intent )
	{
		ConnectivityManager connectivityManager = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
	    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
	    NetworkInfo mobileNetInfo = connectivityManager.getNetworkInfo( ConnectivityManager.TYPE_MOBILE );
	    
	    boolean wifi_connected = (activeNetInfo != null && activeNetInfo.isConnected());
	    boolean mobile_data_connected = (mobileNetInfo != null && mobileNetInfo.isConnected());
	    
	    // Use available network connection to send questions / replies, if any
	    // 1. get the requests from the database.
	    // 2. send requests to the server.
	    // 3. delete the requests.
	    // 4. request Updates from server.
	    if ( wifi_connected || mobile_data_connected )
	    {
	    	HTTPRequestsDataSource httpSource = new HTTPRequestsDataSource( context );
	    	List<HTTPRequest> httpRequests = httpSource.getAllRequests();
	    	
	    	for ( HTTPRequest request : httpRequests )
	    	{
                request.send();
	    	}
	    	
	    	GetUpdatesRequest updatesRequest = new GetUpdatesRequest( context );
            updatesRequest.send();
	    }
	    
	    
	}

}
