package com.example.rspeak_android.fragments;

import java.util.List;

import com.example.rspeak_android.activities.BrowseConversationActivity;
import com.example.rspeak_android.activities.BrowseThreadsActivity;
import com.example.rspeak_android.adapters.QuestionsAnswersArrayAdapter;
import com.example.rspeak_android.model.Question;
import com.example.rspeak_android.model.QuestionsDataSource;
import com.example.rspeak_android.model.RSpeakSQLiteHelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class QuestionsAnswersListFragment extends ListFragment 
{	
	public enum QuestionOrigin { LOCAL, FOREIGN };
	QuestionOrigin questionOrigin;
	Question[] questionsArray;
	
	private static final String QUESTION_ORIGIN = "question_origin";
	
	public static final QuestionsAnswersListFragment newInstance( QuestionOrigin origin )
	{
		QuestionsAnswersListFragment qa_fragment = new QuestionsAnswersListFragment();
		Bundle bundle = new Bundle(1);
		bundle.putInt( QUESTION_ORIGIN, origin.ordinal() );
		qa_fragment.setArguments( bundle );
		return qa_fragment;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		List<Question> questions;
		questionOrigin = QuestionOrigin.values()[ getArguments().getInt(QUESTION_ORIGIN) ];
			
        QuestionsDataSource questionsDataSource = new QuestionsDataSource( getActivity() );
        questionsDataSource.open();
		
		if ( questionOrigin == QuestionOrigin.LOCAL )
		{
			questions = questionsDataSource.getLocallyAskedQuestions();
		}
		else // if ( questionOrigin == QuestionOrigin.FOREIGN )
		{
			questions = questionsDataSource.getForeignAskedQuestions();
		}
				
		questionsArray = questions.toArray( new Question[ questions.size() ] );
		     
		QuestionsAnswersArrayAdapter qa_adapter = new QuestionsAnswersArrayAdapter( getActivity(), questionsArray );
  		
        setListAdapter( qa_adapter );
        questionsDataSource.close();
     }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) 
	{
		// if the question is running one thread at the moment then jump directly to said thread
		if ( questionsArray[ position ].getThreads().size() == 1 )
		{
			String thread_id = questionsArray[ position ].getThreads().get( 0 ).getThreadID();
			
			Intent intent = new Intent( getActivity(), BrowseConversationActivity.class );
		    intent.putExtra( RSpeakSQLiteHelper.THREADS_COLUMN_ID, thread_id );
		    startActivity( intent );
		}
		else // show a list of the available threads
		{
			long question_id = questionsArray[ position ].getID();
			
			Intent intent = new Intent( getActivity(), BrowseThreadsActivity.class );
		    intent.putExtra( RSpeakSQLiteHelper.QUESTIONS_COLUMN_ID, question_id );
		    startActivity( intent );
		}
	}
	
}
