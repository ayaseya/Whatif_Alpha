package com.example.whatif;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;

public class WhatifActivity extends Activity {

	/* ********** ********** ********** ********** */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // タイトルバーを非表示にする
		setContentView(R.layout.activity_whatif);

		Deck deck = new Deck(this.getApplicationContext());
		
		for(int i=0;i<deck.trump.size();i++){
		Log.v("Test", "deck="+ deck.trump.get(i).getSuit() + deck.trump.get(i).getNumber()	);
		}
		
		TrumpView tv =(TrumpView)findViewById(R.id.spade1);
		tv.setNumber(1);
		tv.setSuit(R.string.spade);
		

		
		
	}// onCreate()

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.whatif, menu);

		return true;
	}// onCreateOptionsMenu()

	/* ********** ********** ********** ********** */

}
