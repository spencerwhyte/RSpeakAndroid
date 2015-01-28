package com.example.rspeak_android.adapters;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.rspeak_android.R;
import com.example.rspeak_android.model.Question;
import com.example.rspeak_android.model.Response;
import com.example.rspeak_android.model.Thread;

public class QuestionsAnswersArrayAdapter extends ArrayAdapter<Question>
{
	private final Context context;
	private final Question[] values;
	
	public QuestionsAnswersArrayAdapter(Context context, Question[] values)
	{
		super(context, R.layout.qa_list_layout, values);
		this.context = context;
		this.values = values;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.qa_list_layout, parent, false);
	    
	    TextView questionLine = (TextView) rowView.findViewById(R.id.question_line);
	    TextView latestResponseLine = (TextView) rowView.findViewById(R.id.latest_response_line);
	    TextView dateLine = (TextView) rowView.findViewById(R.id.date_line);
	    
	    if ( values != null )
	    {
		    Question questionInstance = values[position];
		    List<Thread> threads = questionInstance.getThreads();
		    
		    DateFormat df = DateFormat.getDateInstance();
		    
		    questionLine.setText( questionInstance.getQuestionContent() );
		    
		    Date questionDate = new Date( questionInstance.getTimePosted() );
	    	dateLine.setText( df.format( questionDate ) );
		    
	    	// if there are no threads then the server hasn't registered your question yet
	    	if ( threads.size() == 0 )
	    	{
		    	latestResponseLine.setText( "Question Yet to Reach Server" );
		    	latestResponseLine.setTextAppearance( context, R.style.response_hasnt_reached_server_style );
		    	latestResponseLine.setGravity( Gravity.CENTER );
	    	}
	    	// if there is only one thread we'll show the information from that thread
	    	else if ( threads.size() == 1 )
		    {
		    	Thread threadInstance = threads.get( 0 );
		    	List<Response> responses = threadInstance.getResponses();
			    
			    if ( responses != null && responses.size() > 0 )
			    {
				    Response lastResponse = responses.get( responses.size() - 1 );
				    Date responseDate = new Date( lastResponse.getTimePosted() );
				    
				    latestResponseLine.setText( lastResponse.getResponseContent() );
				    dateLine.setText( df.format( responseDate ) );
			    }
			    else
			    {	
			    	// put no response message and style
			    	latestResponseLine.setText( "No Answer Yet" );
			    	latestResponseLine.setTextAppearance( context, R.style.no_response_line_style );
			    	latestResponseLine.setGravity( Gravity.CENTER );
			    }	
		    }
		    else // show how many threads we have
		    {
		    	latestResponseLine.setText( threads.size() + " Threads" );
		    	latestResponseLine.setTextAppearance( context, R.style.no_response_line_style );
		    	latestResponseLine.setGravity( Gravity.CENTER );
		    }
	    }
	    
	    return rowView;
	}
}
