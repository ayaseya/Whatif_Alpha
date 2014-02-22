package com.example.whatif;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;

public class WhatifActivity extends Activity {

	private TrumpView[] clearView = new TrumpView[6];
	private TrumpView[] trumpView = new TrumpView[6];
	private TrumpView trumpBackView;

	private FrameLayout mainFrame;

	private FrameLayout[] layoutFrame = new FrameLayout[6];

	private int[] frameId = { R.id.layoutLocation,
			R.id.Hand1Field,
			R.id.Hand2Field,
			R.id.Hand3Field,
			R.id.Hand4Field,
			R.id.Hand5Field };

	private CheckDevice cd;
	private Deck deck;

	private int statusbarHeight;

	private ArrayList<Point> clearView_location = new ArrayList<Point>();
	private int[] layout_location = new int[2];
	//	private ArrayList<Point> trumpView_location = new ArrayList<Point>();

	private boolean flag = true;// 表と裏のどちらが表示されているか判断
	private boolean animFlag = true;// アニメーション中はfalseとなり画像をクリックできない
	private float centerX;// Y軸回転の中心点(X座標)を設定
	private float centerY;// Y軸回転の中心点(Y座標)を設定
	private long time = 500;// Y軸回転のアニメーションスピード

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

		for (int i = 0; i < 6; i++) {
			// 場札と手札1～5を配置するレイアウトのインスタンスを取得
			layoutFrame[i] = (FrameLayout) findViewById(frameId[i]);

			// 場札と手札1～5のViewインスタンスを取得
			clearView[i] = new TrumpView(this.getApplicationContext());
			clearView[i] = (TrumpView) clearView[i].addClearView(this.getApplicationContext());

			// FrameLayoutにトランプ画像を配置するため、画像の幅・高さ・重力を設定しておく
			// このパラメーターは手札1～5にも使用する
			FrameLayout.LayoutParams params =
					new FrameLayout.LayoutParams(clearView[i].getTrumpWidth(), clearView[i].getTrumpHeight(), Gravity.CENTER);

			// レイアウトに場札と手札1～5の画像を配置する
			layoutFrame[i].addView(clearView[i], params);
		}

		trumpView[0] = new TrumpView(this.getApplicationContext());
		trumpView[0] = (TrumpView) trumpView[0].addLayoutView(this.getApplicationContext());

		trumpBackView = new TrumpView(this.getApplicationContext());
		trumpBackView = (TrumpView) trumpBackView.addBackView(this.getApplicationContext());

		centerX = trumpBackView.getTrumpWidth() / 2;
		centerY = trumpBackView.getTrumpHeight();

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
		statusbarHeight = rect.top;
		Log.v(TAG, "ステータスバー height=" + statusbarHeight);
		setClearViewPoint();

		layout_location[0] = Math.max(clearView_location.get(0).x, clearView_location.get(3).x);
		layout_location[1] = clearView_location.get(0).y - statusbarHeight;

		// 画像配置用に最前面のレイアウトのインスタンスを取得
		mainFrame = (FrameLayout) findViewById(R.id.FrameLayout);

		FrameLayout.LayoutParams params =
				new FrameLayout.LayoutParams(trumpView[0].getTrumpWidth(), trumpView[0].getTrumpHeight());

		params.leftMargin = layout_location[0];
		params.topMargin = layout_location[1];
		params.gravity = Gravity.NO_GRAVITY;// この記載がないとマージンが有効にならない

		// レイアウトに場札画像を配置する
		mainFrame.addView(trumpView[0], params);
		trumpView[0].setVisibility(View.INVISIBLE);

		// レイアウトの一時配置用の画像を非表示にする(削除するとレイアウトが崩れるため非表示を選択)
		clearView[0].setVisibility(View.INVISIBLE);

		mainFrame.addView(trumpBackView, params);
		// trumpBackView.setVisibility(View.INVISIBLE);

		trumpView[0].setOnClickListener(layoutClick);// Y軸回転テスト用のクリックリスナー
		trumpBackView.setOnClickListener(trumpBackViewClick);// Y軸回転テスト用のクリックリスナー

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

	public void FlipTrump(View v, int index) {

		if (animFlag) {
			// アニメーション中にクリックできないようfalseに変更する
			animFlag = false;

			// 現在表示されているトランプ画像をレイアウトから削除する
			// mainFrame.removeView(v);

			// Y軸回転(0～90度)
			Rotate3dAnimation rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, true);
			rotation.setDuration(time);
			trumpBackView.startAnimation(rotation);
			rotation.setAnimationListener(startFlip);
			Log.v(TAG, "anime");
		}
	}

	// http://kinsentansa.blogspot.jp/2012/04/android_13.html
	
	public AnimationListener startFlip = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {

			trumpBackView.setVisibility(View.INVISIBLE);

			// Y軸回転(270～360度)
			Rotate3dAnimation rotation = new Rotate3dAnimation(270, 360, centerX, centerY, 0f, false);
			rotation.setDuration(time);
			trumpView[0].startAnimation(rotation);
			rotation.setAnimationListener(endFlip);
			Log.v(TAG, "startFlip");
		}
	};

	public AnimationListener endFlip = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			// アニメーションの終了
			trumpView[0].setVisibility(View.VISIBLE);
			animFlag = true;
			Log.v(TAG, "endFlip");
		}
	};

	// ArrayListにclearViewのXY座標を格納する
	private void setClearViewPoint() {
		int[] location = new int[2];
		for (int i = 0; i < 6; i++) {

			clearView[i].getLocationInWindow(location);
			int x = location[0];
			int y = location[1];
			Point point = new Point(x, y);
			clearView_location.add(point);

		}
	}

	// ArrayListに格納したclearViewのXY座標を取得する

	// レイアウトViewをクリックした時の処理
	private OnClickListener layoutClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.v(TAG, "layout...CLICK");
			FlipTrump(v, 0);
		}
	};

	// レイアウトViewをクリックした時の処理
	private OnClickListener trumpBackViewClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.v(TAG, "layout...CLICK");
			FlipTrump(v, 0);
		}
	};

}
