package com.example.rspeak_android.adapters;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.example.rspeak_android.R;
import com.example.rspeak_android.model.Question;
import com.example.rspeak_android.model.Response;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BrowseConversationListAdapter extends BaseAdapter
{
	private final Context context;
	private List<Response> responses;
	private final Question question;
	
	private static final int INITIAL_QUESTION = 0;
	private static final int LOCAL_RESPONSE = 1;
	private static final int FOREIGN_RESPONSE = 2;

	public BrowseConversationListAdapter( Context context, List<Response> responses, Question question )
	{
		this.context = context;
		this.responses = responses;
		this.question = question;
	}
	
	public void updateResponses( List<Response> responses )
	{
		this.responses = responses;
	}
	
	@Override
	public int getCount()
	{
		return responses.size() + 1;
	}
	
	@Override 
	public Object getItem( int position )
	{
		return ( position == 0 ? question : responses.get( position - 1 ) );
	}
	
	@Override
	public int getItemViewType( int position )
	{
		// if the item originates from local device then return 0
		// return 1 for response from foreign device
		if ( position == 0 )
		{
			return INITIAL_QUESTION;
		}
		boolean on_responder_device = ( (Response) getItem( position ) ).isCurrentlyOnResponderDevice();
				
		return ( on_responder_device ? LOCAL_RESPONSE : FOREIGN_RESPONSE );
	}
	
	@Override
	public long getItemId( int position )
	{
		return position;
	}
	
	@Override 
	public View getView( int position, View convertView, ViewGroup parent )
	{
		int viewType = getItemViewType( position );
	
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if ( viewType == INITIAL_QUESTION )
		{
			convertView = inflater.inflate( R.layout.fragment_conversation_list_item_question, null );
		}
		else if ( viewType == LOCAL_RESPONSE )
		{
			convertView = inflater.inflate( R.layout.fragment_conversation_list_item_local, null );
		}
		else if ( viewType == FOREIGN_RESPONSE )
		{
			convertView = inflater.inflate( R.layout.fragment_conversation_list_item_foreign, null );
		}
		
		TextView verse = (TextView) convertView.findViewById( R.id.verse );
		
		if ( viewType == INITIAL_QUESTION )
		{
			verse.setText( question.getQuestionContent() );
			
			TextView date = (TextView) convertView.findViewById( R.id.date );
			DateFormat df = DateFormat.getDateInstance();
			Date postDate = new Date( question.getTimePosted() );
			date.setText( df.format( postDate ) );
			
			View divider = convertView.findViewById( R.id.divider );
			int color = ( question.isCurrentlyOnAskerDevice() ? R.color.teal_light : R.color.ash_light );
			divider.setBackgroundResource( color );
			
			verse.setTextColor( context.getResources().getColor( color ) );
		}
		else
		{
			Response response = (Response) getItem( position );
			verse.setText( response.getResponseContent() );
			
			int color = ( response.isCurrentlyOnResponderDevice() ? R.color.teal_light : R.color.ash_light );
			verse.setTextColor( context.getResources().getColor( color ) );
		}
		
		return convertView;
	}
}
