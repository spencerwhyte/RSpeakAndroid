package com.example.rspeak_android.fragments;


import com.example.rspeak_android.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AskQuestionFragment extends Fragment
{
	View fragmentView;
	
	public static AskQuestionFragment newInstance()
	{
		AskQuestionFragment fragment = new AskQuestionFragment();
				
		return fragment;
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedBundle) 
	{
		fragmentView = inflater.inflate( R.layout.fragment_ask_question, container, false );
		
	    return fragmentView;
	}
}
