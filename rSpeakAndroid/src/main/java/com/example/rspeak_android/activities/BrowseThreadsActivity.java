package com.example.rspeak_android.activities;

import com.example.rspeak_android.R;
import com.example.rspeak_android.model.Question;
import com.example.rspeak_android.model.QuestionsDataSource;
import com.example.rspeak_android.model.RSpeakSQLiteHelper;
import com.example.rspeak_android.fragments.BrowseThreadsFragment;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class BrowseThreadsActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse_threads);
		
		Intent intent = getIntent();
	    long question_id = intent.getLongExtra( RSpeakSQLiteHelper.QUESTIONS_COLUMN_ID, 1 );
	    
	    // set up the question atop the threads list
	    QuestionsDataSource qSource = new QuestionsDataSource( this );
	    qSource.open();
	    Question question = qSource.getQuestionById( question_id );
	    
	    TextView verse = (TextView) findViewById( R.id.verse );
	    verse.setText( question.getQuestionContent() );
	    qSource.close();

		if (savedInstanceState == null) 
		{
			Fragment browseThreadsFragment = BrowseThreadsFragment.newInstance( question_id );
			
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, browseThreadsFragment).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.browse_threads, menu);
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

}
