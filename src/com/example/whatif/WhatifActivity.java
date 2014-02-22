package com.example.whatif;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

public class WhatifActivity extends Activity {
	private TrumpView trumpView;
	private TrumpView layoutView;
	private TrumpView clearView0;
	private TrumpView clearView1;
	private TrumpView clearView2;
	private TrumpView clearView3;
	private TrumpView clearView4;
	private TrumpView clearView5;

	private FrameLayout mainFrame;

	private FrameLayout layoutFrame;
	private FrameLayout hand1Frame;
	private FrameLayout hand2Frame;
	private FrameLayout hand3Frame;
	private FrameLayout hand4Frame;
	private FrameLayout hand5Frame;

	private CheckDevice cd;
	private Deck deck;

	private int[] layoutView_location = new int[2];
	private int StatusBarHeight;

	// LogCat用のタグを定数で定義する
	public static final String TAG = "Test";
	
	/* ********** ********** ********** ********** */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // タイトルバーを非表示にする
		//		setContentView(R.layout.activity_whatif);
		setContentView(R.layout.activity_whatif);

		// 端末の画像解像度を計測するためのインスタンスの取得
		cd = new CheckDevice(getApplicationContext());

		// トランプ(52枚)を管理するDeckクラスのインスタンスの取得
		deck = new Deck(this.getApplicationContext());

		// 画像配置用に最前面のレイアウトのインスタンスを取得
		mainFrame = (FrameLayout) findViewById(R.id.FrameLayout);

		// トランプ画像のインスタンスを取得
		trumpView = new TrumpView(this.getApplicationContext());
		trumpView = (TrumpView) trumpView.addLayoutView(this.getApplicationContext());

		// 場札のインスタンスを取得
		clearView0 = new TrumpView(this.getApplicationContext());
		clearView0 = (TrumpView) clearView0.addClearView(this.getApplicationContext());

		// 場札を配置するレイアウトのインスタンスを取得
		layoutFrame = (FrameLayout) findViewById(R.id.layoutLocation);

		// FrameLayoutにトランプ画像を配置するため、画像の幅・高さ・重力を設定しておく
		// このパラメーターは手札1～5にも使用する
		FrameLayout.LayoutParams params =
				new FrameLayout.LayoutParams(trumpView.getTrumpWidth(), trumpView.getTrumpHeight(), Gravity.CENTER);

		// レイアウトに場札画像を一時的に配置する
		layoutFrame.addView(clearView0, params);

		// 手札1～5を配置するレイアウトのインスタンスを取得
		hand1Frame = (FrameLayout) findViewById(R.id.Hand1Field);
		hand2Frame = (FrameLayout) findViewById(R.id.Hand2Field);
		hand3Frame = (FrameLayout) findViewById(R.id.Hand3Field);
		hand4Frame = (FrameLayout) findViewById(R.id.Hand4Field);
		hand5Frame = (FrameLayout) findViewById(R.id.Hand5Field);

		// 手札1～5の配置座標を計測するためのビューの取得
		clearView1 = new TrumpView(this.getApplicationContext());
		clearView1 = (TrumpView) clearView1.addClearView(this.getApplicationContext());

		clearView2 = new TrumpView(this.getApplicationContext());
		clearView2 = (TrumpView) clearView2.addClearView(this.getApplicationContext());

		clearView3 = new TrumpView(this.getApplicationContext());
		clearView3 = (TrumpView) clearView3.addClearView(this.getApplicationContext());

		clearView4 = new TrumpView(this.getApplicationContext());
		clearView4 = (TrumpView) clearView4.addClearView(this.getApplicationContext());

		clearView5 = new TrumpView(this.getApplicationContext());
//		clearView5 = (TrumpView) clearView5.addClearView(this.getApplicationContext());
		clearView5 = (TrumpView) clearView5.addBackView(this.getApplicationContext());

		
		// レイアウトに手札1～5の画像を配置する
		hand1Frame.addView(clearView1, params);
		hand2Frame.addView(clearView2, params);
		hand3Frame.addView(clearView3, params);
		hand4Frame.addView(clearView4, params);
		hand5Frame.addView(clearView5, params);

		//		トランプの裏面作成
		//		トランプが配られたときの演出（上から下へ表示される）
		//		トランプが裏返る表現
		//		トランプが移動するアニメ
		Log.v(TAG, "onCreate()");
	}// onCreate()

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		// ステータスバーの高さを取得
		Rect rect = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
		StatusBarHeight = rect.top;
		Log.v(TAG, "ステータスバー height=" + StatusBarHeight);

		int[] clearView0_location = new int[2];
		clearView0.getLocationInWindow(clearView0_location);
		int[] clearView3_location = new int[2];
		clearView3.getLocationInWindow(clearView3_location);

		layoutView_location[0] = Math.max(clearView0_location[0], clearView3_location[0]);
		layoutView_location[1] = clearView0_location[1]-StatusBarHeight;

//		Log.v(TAG, "0 x=" + clearView0_location[0] + " y=" + clearView0_location[1]);
//		Log.v(TAG, "3 x=" + clearView3_location[0] + " y=" + clearView3_location[1]);
//
//		Log.v(TAG, "layout x=" + layoutView_location[0] + " y=" + layoutView_location[1]);

		// 画像配置用に最前面のレイアウトのインスタンスを取得
		mainFrame = (FrameLayout) findViewById(R.id.FrameLayout);

		FrameLayout.LayoutParams params =
				new FrameLayout.LayoutParams(trumpView.getTrumpWidth(), trumpView.getTrumpHeight());

		layoutView = new TrumpView(this.getApplicationContext());
		layoutView = (TrumpView) layoutView.addLayoutView(this.getApplicationContext());

		params.leftMargin = layoutView_location[0];
		params.topMargin = layoutView_location[1];
		params.gravity = Gravity.NO_GRAVITY;// この記載がないとマージンが有効にならない

		// レイアウトに場札画像を配置する
		mainFrame.addView(layoutView, params);
		
		// レイアウトの一時配置用の画像を非表示にする(削除するとレイアウトが崩れるため非表示を選択)
		clearView0.setVisibility(View.INVISIBLE);

		Log.v(TAG, "onWindowFocusChanged()");

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.v(TAG, "onRestart()");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.v(TAG, "onStart()");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "onResume()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.v(TAG, "onPause()");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.v(TAG, "onStop()");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.whatif, menu);

		return true;
	}// onCreateOptionsMenu()

	/* ********** ********** ********** ********** */


}
