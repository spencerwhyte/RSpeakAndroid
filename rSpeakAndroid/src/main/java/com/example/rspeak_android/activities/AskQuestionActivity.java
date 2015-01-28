package com.example.rspeak_android.activities;

import com.example.rspeak_android.HTTPRequests.AskQuestionRequest;
import com.example.rspeak_android.HTTPRequests.GetCreditScoreRequest;
import com.example.rspeak_android.R;
import com.example.rspeak_android.model.HTTPRequest;
import com.example.rspeak_android.fragments.AskQuestionFragment;

import android.support.v7.app.ActionBarActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class AskQuestionActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ask_question);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, AskQuestionFragment.newInstance() ).commit();
		}
		
		ImageButton send_question =  (ImageButton) this.findViewById( R.id.send_question );
		
		// set the focus on the edit text box by default
		EditText question = (EditText) this.findViewById( R.id.question );
		question.requestFocus();
		
		send_question.setOnClickListener( getSendButtonListener() );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ask_question, menu);
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
    public void onResume()
    {
    	super.onResume();
    	
    	updateCreditScore();
    }
	
	// When send is clicked create an http request with their question
	// then go back to previous activity
	public OnClickListener getSendButtonListener()
	{
		return new OnClickListener() 
		{
			@Override
			public void onClick( final View v )
			{
				EditText question = (EditText) findViewById( R.id.question );
				String questionString = question.getText().toString();
				
				if ( questionString != null )
				{
					AskQuestionRequest askQuestionRequest = new AskQuestionRequest( getApplicationContext(), questionString );
					askQuestionRequest.send();
					
					finish();
				}
			}
		};
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
