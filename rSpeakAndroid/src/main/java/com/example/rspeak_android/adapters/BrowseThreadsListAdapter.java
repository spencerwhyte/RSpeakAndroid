package com.example.rspeak_android.adapters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.example.rspeak_android.R;
import com.example.rspeak_android.model.Response;
import com.example.rspeak_android.model.Thread;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BrowseThreadsListAdapter extends ArrayAdapter<Thread>
{
	private final Context context;
	private final Thread[] values;
	
	public BrowseThreadsListAdapter(Context context, Thread[] values)
	{
		super(context, R.layout.thread_list_layout, values);
		this.context = context;
		this.values = values;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.thread_list_layout, parent, false);
	    
	    TextView index_line = (TextView) rowView.findViewById( R.id.index_line );
	    TextView latestResponseLine = (TextView) rowView.findViewById( R.id.latest_response_line );
	    TextView dateLine = (TextView) rowView.findViewById( R.id.date_line );
	    
	    if ( values != null )
	    {
		    Thread threadInstance = values[position];
		    List<Response> responses = threadInstance.getResponses();
		    
		    index_line.setText( String.valueOf( position + 1 ) );

		    if ( responses != null && responses.size() > 0 )
		    {
		    	DateFormat df = new SimpleDateFormat("dd/MM/yy");
			    Response lastResponse = responses.get( responses.size() - 1 );
			    Date responseDate = new Date( lastResponse.getTimePosted() );
			    
			    latestResponseLine.setText( lastResponse.getResponseContent() );
			    dateLine.setText( df.format( responseDate ) );
		    }
		    else
		    {	
		    	// put no response message and style
		    	latestResponseLine.setText( "No Response Yet" );
		    	latestResponseLine.setTextAppearance( context, R.style.no_response_line_style );
		    	latestResponseLine.setGravity( Gravity.CENTER );
		    }	
	    }
	    
	    return rowView;
	}
}
