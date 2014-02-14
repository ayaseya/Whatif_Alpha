package com.example.whatif;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

public class WhatifActivity extends Activity {
	TrumpView trumpView;
	View v;
	LinearLayout lLayout;

	/* ********** ********** ********** ********** */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // タイトルバーを非表示にする
		setContentView(R.layout.activity_whatif);

		Deck deck = new Deck(this.getApplicationContext());
		trumpView = new TrumpView(this.getApplicationContext());
		v = trumpView.addTrumpView(deck, 0, this.getApplicationContext());

		lLayout = (LinearLayout) findViewById(R.id.MainLayout);
		lLayout.addView(v);

		//		for(int i=0;i<deck.trump.size();i++){
		//		Log.v("Test", "deck="+ deck.trump.get(i).getSuit() + deck.trump.get(i).getNumber()	);
		//		}

		//		TrumpView tv =(TrumpView)findViewById(R.id.spade1);
		//		tv.setNumber(1);
		//		tv.setSuit(R.string.spade);

	}// onCreate()

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.whatif, menu);

		return true;
	}// onCreateOptionsMenu()

	/* ********** ********** ********** ********** */

}
