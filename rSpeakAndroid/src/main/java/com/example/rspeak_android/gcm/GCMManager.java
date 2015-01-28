package com.example.rspeak_android.gcm;

import java.io.IOException;

import com.example.rspeak_android.HTTPRequests.RegisterPushNotificationRequest;
import com.example.rspeak_android.RSpeakApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

public class GCMManager 
{
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private final static String SENDER_ID = "172880790543";
	private Context context;
	private GoogleCloudMessaging gcm;
	private String regid;
	
	public GCMManager( Context context )
	{
		this.context = context;
		gcm = GoogleCloudMessaging.getInstance( context );
	}
	
	public boolean checkPlayServices() 
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable( context );
        if ( resultCode != ConnectionResult.SUCCESS ) 
        {
            if ( GooglePlayServicesUtil.isUserRecoverableError( resultCode )) 
            {
                GooglePlayServicesUtil.getErrorDialog( resultCode, (Activity) context,
                        PLAY_SERVICES_RESOLUTION_REQUEST ).show();
            } 
            else 
            {
                Log.i( "notification", "This device is not supported." );
            }
            return false;
        }
        return true;
    }
    
    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public String getRegistrationId( Context context ) 
    {
        final SharedPreferences prefs = ((RSpeakApplication) context.getApplicationContext() ).getGCMPreferences();
        String registrationId = prefs.getString( RSpeakApplication.PROPERTY_PUSH_NOTIFICATION_ID, null );
        
        if ( registrationId == null ) 
        {
            Log.i( "info", "Registration not found." );
            return null;
        }
        
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt( RSpeakApplication.PROPERTY_APP_VERSION, Integer.MIN_VALUE );
        int currentVersion = getAppVersion( context );
        if ( registeredVersion != currentVersion ) 
        {
            Log.i( "info", "App version changed." );
            return null;
        }
        
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion( Context context ) 
    {
        try 
        {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo( context.getPackageName(), 0 );
            return packageInfo.versionCode;
        } 
        catch ( NameNotFoundException e ) 
        {
            // should never happen
            throw new RuntimeException( "Could not get package name: " + e );
        }
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    public void registerInBackground() 
    {
        new AsyncTask<Void, Void, String>() 
        {
            @Override
            protected String doInBackground( Void... params ) 
            {
                String msg;
                try 
                {
                    if ( gcm == null ) 
                    {
                        gcm = GoogleCloudMessaging.getInstance( context );
                    }
                    regid = gcm.register( SENDER_ID );
                    msg = "Device registered, registration ID=" + regid;

                    storeRegistrationId( regid );
                    sendRegistrationIdToBackend();
                } 
                catch ( IOException ex ) 
                {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                
                return msg;
            }

            @Override
            protected void onPostExecute( String msg ) 
            {
            	Log.i( "info", "completed gcm task with message:\n" + msg );
            	
            	// register the push_notification_id with the server
            	RegisterPushNotificationRequest registerPushNotificationClientRequest = new RegisterPushNotificationRequest( context );
                registerPushNotificationClientRequest.send();
            }
        }.execute( null, null, null );
    }
    
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() 
    {
        // Your implementation here.
    }
    
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regId registration ID
     */
    private void storeRegistrationId( String regId ) 
    {
        final SharedPreferences prefs = ((RSpeakApplication) context.getApplicationContext()).getGCMPreferences();
        int appVersion = getAppVersion( context );
        Log.i( "info", "Saving regId on app version " + appVersion );
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString( RSpeakApplication.PROPERTY_PUSH_NOTIFICATION_ID, regId );
        editor.putInt( RSpeakApplication.PROPERTY_APP_VERSION, appVersion );
        editor.apply();
    }
}
