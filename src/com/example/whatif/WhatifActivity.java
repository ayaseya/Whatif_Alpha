package com.example.whatif;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;

public class WhatifActivity extends Activity {
	private TrumpView trumpView;
	private FrameLayout frameLayout;
	private FrameLayout layoutLocation;

	private FrameLayout alphaLayout;

	private CheckDevice cd;
	private Deck deck;

	/* ********** ********** ********** ********** */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // タイトルバーを非表示にする
		//		setContentView(R.layout.activity_whatif);
		setContentView(R.layout.activity_main);

		cd = new CheckDevice(getApplicationContext());

		deck = new Deck(this.getApplicationContext());
		trumpView = new TrumpView(this.getApplicationContext());
		trumpView = (TrumpView) trumpView.addLayoutView(this.getApplicationContext());

		layoutLocation = (FrameLayout) findViewById(R.id.layoutLocation);
		FrameLayout.LayoutParams params =
				new FrameLayout.LayoutParams(trumpView.getTrumpWidth(), trumpView.getTrumpHeight(),Gravity.CENTER);

		layoutLocation.addView(trumpView, params);

		trumpView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Log.v("Test", "w=" + v.getWidth() + " h=" + v.getHeight());
				Log.v("Test", "w=" + ((cd.getWidth() - 60) / 5) + " h=" + ((cd.getWidth() - 60) / 5) * 1.5);

			}
		});

	}// onCreate()

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.whatif, menu);

		return true;
	}// onCreateOptionsMenu()

	/* ********** ********** ********** ********** */

}
