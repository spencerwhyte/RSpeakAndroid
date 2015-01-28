package com.example.rspeak_android.activities;

import com.example.rspeak_android.HTTPRequests.GetCreditScoreRequest;
import com.example.rspeak_android.HTTPRequests.RegisterDeviceRequest;
import com.example.rspeak_android.HTTPRequests.RegisterPushNotificationRequest;
import com.example.rspeak_android.R;
import com.example.rspeak_android.adapters.QuestionsAnswersPagerAdapter;
import com.example.rspeak_android.model.HTTPRequest;
import com.example.rspeak_android.gcm.GCMManager;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BrowseQuestionsAnswersActivity extends ActionBarActivity implements ActionBar.TabListener 
{
	QuestionsAnswersPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    GCMManager gcmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_browse_questions_answers );
        
    	// check if this device's version matches the Play requirements
        gcmManager = new GCMManager( this );
        
    	if ( gcmManager.checkPlayServices() )
    	{
    		// now check if there already exists a push_notification ID
    		String push_notification_id = gcmManager.getRegistrationId( this );
    		if ( push_notification_id == null ) // register a new ID
    		{
    			gcmManager.registerInBackground();
    		}
    	}
    	else
    	{
    		finish();
    	}
    	
    	ensureIDsAreRegisteredWithServer();

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new QuestionsAnswersPagerAdapter( getSupportFragmentManager(), this );

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        
        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        
        // add event handler for click on 'ask question' button
        Button ask_button = (Button) findViewById( R.id.ask_button );
        ask_button.setOnClickListener( getAskClickListener() );
    }
    
    // check if push notification id and device id are registered with server
	// and register them if not
    public void ensureIDsAreRegisteredWithServer()
    {
    	SharedPreferences device_properties = getSharedPreferences( "DEVICE_PROPERTIES", 0 );
    	boolean registeredDeviceId = device_properties.getBoolean( HTTPRequest.DATA_DEVICE_ID_SET, false );
    	boolean registeredPushNotificaitonId = device_properties.getBoolean( HTTPRequest.DATA_PUSH_NOTIFICATION_ID_SET, false );
    	
    	if ( !registeredDeviceId )
    	{
    		RegisterDeviceRequest registerDeviceRequest = new RegisterDeviceRequest( this );
    		registerDeviceRequest.send();
    	}
    	if ( !registeredPushNotificaitonId )
    	{
    		RegisterPushNotificationRequest registerPushNotificationRequest = new RegisterPushNotificationRequest( this );
    		registerPushNotificationRequest.send();
    	}
    } 
    
    // Go to ask question activity when ask quesiton is clicked
    public View.OnClickListener getAskClickListener()
    {
    	return new View.OnClickListener() 
        {
            
			@Override
			public void onClick(View v) 
			{			
				// start the ask question activity: it has the text field for the question
				Intent intent = new Intent( BrowseQuestionsAnswersActivity.this, AskQuestionActivity.class );
			    startActivity(intent); 
			}
        };
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	gcmManager.checkPlayServices();
    	
    	updateCreditScore();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {    
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.browse_questions_answers, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
    
    public void updateCreditScore()
    {
    	// update the credit points
    	GetCreditScoreRequest getCreditScoreRequest = new GetCreditScoreRequest( this );
    	getCreditScoreRequest.send();
    	
    	// modify credit score in view
    	SharedPreferences device_properties = getSharedPreferences( "DEVICE_PROPERTIES", 0 );
    	int credit_points = device_properties.getInt( HTTPRequest.DATA_CREDIT_POINTS, -1 );
    	
    	TextView credit = (TextView) findViewById( R.id.credit );
    	credit.setText( "Credit Score: " + ( credit_points == -1 ? "" : credit_points ) );
    }
    
}
