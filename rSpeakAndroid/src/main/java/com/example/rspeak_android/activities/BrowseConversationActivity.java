package com.example.rspeak_android.activities;

import com.example.rspeak_android.R;
import com.example.rspeak_android.model.RSpeakSQLiteHelper;
import com.example.rspeak_android.fragments.BrowseConversationFragment;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class BrowseConversationActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.activity_browse_conversation );
		
		Intent intent = getIntent();
	    String thread_id = intent.getStringExtra( RSpeakSQLiteHelper.THREADS_COLUMN_ID );

		if (savedInstanceState == null) 
		{
			Fragment conversation_fragment = BrowseConversationFragment.newInstance( thread_id );
			
			getSupportFragmentManager().beginTransaction()
					.add( R.id.container,  conversation_fragment ).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.browse_conversation, menu);
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
