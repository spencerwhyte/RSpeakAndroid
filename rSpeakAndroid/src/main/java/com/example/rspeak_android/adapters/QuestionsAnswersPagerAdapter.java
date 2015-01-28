package com.example.rspeak_android.adapters;

import java.util.Locale;

import com.example.rspeak_android.R;
import com.example.rspeak_android.fragments.QuestionsAnswersListFragment;
import com.example.rspeak_android.fragments.QuestionsAnswersListFragment.QuestionOrigin;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class QuestionsAnswersPagerAdapter extends FragmentPagerAdapter
{
	Context context;
	
	public QuestionsAnswersPagerAdapter( FragmentManager fm, Context context ) 
	{
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) 
    {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).

    	if ( position == 0 ) // ASKED
    	{
    		return QuestionsAnswersListFragment.newInstance( QuestionOrigin.LOCAL );
    	}
    	else // if ( position == 1 ) // ANSWERED
    	{
    		return QuestionsAnswersListFragment.newInstance( QuestionOrigin.FOREIGN );
    	}
    }

    @Override
    public int getCount() 
    {
        // Show 2 total pages.
        return 2;
    }

    @Override
    public CharSequence getPageTitle( int position ) 
    {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return context.getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return context.getString(R.string.title_section2).toUpperCase(l);
        }
        return null;
    }
}
