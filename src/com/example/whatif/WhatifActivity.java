package com.example.whatif;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

public class WhatifActivity extends Activity
		implements ViewFactory {

	private TrumpView[] clearView = new TrumpView[6];
	private TrumpView[] trumpView = new TrumpView[6];
	private TrumpView[] trumpBackView = new TrumpView[6];

	private FrameLayout mainFrame;

	private FrameLayout[] layoutFrame = new FrameLayout[6];

	private int[] frameId = { R.id.layoutLocation,
			R.id.Hand1Field,
			R.id.Hand2Field,
			R.id.Hand3Field,
			R.id.Hand4Field,
			R.id.Hand5Field };

	private Deck deck;

	private Deck record;

	private int statusbarHeight;

	private ArrayList<Point> clearView_location = new ArrayList<Point>();
	private int[] layout_location = new int[2];
	//	private ArrayList<Point> trumpView_location = new ArrayList<Point>();

	private boolean animFlag = true;// アニメーション中はfalseとなり画像をクリックできない
	private boolean flipAnimFlag = true;
	private boolean moveAnimFlag = true;

	private float centerX;// Y軸回転の中心点(X座標)を設定
	private float centerY;// Y軸回転の中心点(Y座標)を設定
	private long time = 500;// Y軸回転のアニメーションスピード

	private boolean replaceFlag = true;

	// LogCat用のタグを定数で定義する
	public static final String TAG = "Test";

	public int animCount = 0;
	private int counter = 0;

	TextView guideView; // ガイド表示
	private TextSwitcher count;
	private int width = 0;
	private int height = 0;
	private int txtSwitchFontSize = 20;
	private boolean dealAnimFlag = true;
	private ScrollView chainScroll;
	private ScrollView bonusScroll;

	private int scrollHeight;
	private com.example.whatif.Coin coin;
	private TextView wagerView;
	private TextView winView;
	private TextView paidView;
	private TextView creditView;

	private enum DPI {
		ldpi, mdpi, hdpi, xhdpi, xxhdpi
	}

	private DPI dpi;
	private boolean coin_flag;
	private boolean skip_flag;
	private Handler handler = new Handler();
	private int beforeCredit;
	private int timerCount;

	private int[] rate52 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3,
			3, 3, 4, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 10, 10, 12, 12, 14, 14, 16,
			18, 20, 22, 25, 30, 35, 40, 45, 50, 55, 60, 80, 100 };
	private int beforeWager;
	private TextView msg;

	private boolean scrollFlag = true;
	private int scrollCount = 0;

	/* ********** ********** ********** ********** */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // タイトルバーを非表示にする
		//		setContentView(R.layout.activity_whatif);
		setContentView(R.layout.activity_whatif);

		// トランプ(52枚)を管理するDeckクラスのインスタンスの取得
		deck = new Deck(this.getApplicationContext());
		// トランプ(52枚)の生成とシャッフル
		deck.shuffle(this.getApplicationContext());
		// 場札に置いた順番を記録するインスタンスを取得する
		record = new Deck(this.getApplicationContext());

		coin = new Coin();

		// Activityを継承していないため、getWindowManager()メソッドは利用できない
		// Displayクラスのインスタンスを取得するため
		// 引数のContextを使用してWindowManagerを取得する
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		// Displayインスタンスを取得する
		Display display = wm.getDefaultDisplay();

		width = display.getWidth();
		height = display.getHeight();

		if (width <= 240) {//ldpi（120dpi）240×320px
			dpi = DPI.ldpi;
		} else if (240 < +width && +width <= 320) {//mdpi（160dpi）320×480px
			dpi = DPI.mdpi;
		} else if (320 < +width && +width <= 480) {//hdpi（240dpi）480×800px
			dpi = DPI.hdpi;
		} else if (480 < +width && +width <= 640) {//xhdpi（320dpi）640×960px
			dpi = DPI.xhdpi;
		} else if (640 < +width) {//xxhdpi（480dpi）960×1440px
			dpi = DPI.xxhdpi;
		}
		Log.v(TAG, "> " + dpi + " w=" + width + " h=" + height);

		// コイン操作画面を非表示にする(アニメーションの座標計算のため)
		findViewById(R.id.CoinLayout).setVisibility(View.GONE);
		findViewById(R.id.TextLayout).setVisibility(View.GONE);

		// 画像配置用に最前面のレイアウトのインスタンスを取得
		mainFrame = (FrameLayout) findViewById(R.id.FrameLayout);

		// clearViewのインスタンス取得と配置
		for (int i = 0; i < 6; i++) {
			// 場札と手札1～5を配置するレイアウトのインスタンスを取得
			layoutFrame[i] = (FrameLayout) findViewById(frameId[i]);

			// 場札と手札1～5のViewインスタンスを取得
			clearView[i] = new TrumpView(this.getApplicationContext());
			clearView[i].addClearView(this.getApplicationContext());

			// FrameLayoutにトランプ画像を配置するため、画像の幅・高さ・重力を設定しておく
			// このパラメーターは手札1～5にも使用する
			FrameLayout.LayoutParams params =
					new FrameLayout.LayoutParams(clearView[i].getTrumpWidth(), clearView[i].getTrumpHeight(), Gravity.CENTER);

			// レイアウトに場札と手札1～5の画像を配置する
			layoutFrame[i].addView(clearView[i], params);
		}

		// clearViewのインスタンス取得
		for (int i = 0; i < 6; i++) {
			// 場札と手札1～5のViewインスタンスを取得
			trumpView[i] = new TrumpView(this.getApplicationContext());

		}

		trumpView[0].addLayoutView(this.getApplicationContext());
		trumpView[0].setVisibility(View.INVISIBLE);

		// trumpBackViewのインスタンス取得と配置
		// onWindowFocusChanged()で場札の位置を微調整するので
		// 場札の裏面は後で配置する
		for (int i = 0; i < 6; i++) {
			// 場札と手札1～5のViewインスタンスを取得
			trumpBackView[i] = new TrumpView(this.getApplicationContext());
			trumpBackView[i].addBackView(this.getApplicationContext());

			if (i > 0) {
				FrameLayout.LayoutParams params =
						new FrameLayout.LayoutParams(trumpBackView[i].getTrumpWidth(), trumpBackView[i].getTrumpHeight(), Gravity.CENTER);

				layoutFrame[i].addView(trumpBackView[i], params);

			}
			trumpBackView[i].setVisibility(View.INVISIBLE);

		}

		centerX = trumpBackView[0].getTrumpWidth() / 2;
		centerY = trumpBackView[0].getTrumpHeight();

		trumpView[0].setOnClickListener(layoutListener);// Y軸回転テスト用のクリックリスナー
		trumpView[1].setOnClickListener(hand1Listener);
		trumpView[2].setOnClickListener(hand2Listener);
		trumpView[3].setOnClickListener(hand3Listener);
		trumpView[4].setOnClickListener(hand4Listener);
		trumpView[5].setOnClickListener(hand5Listener);

		findViewById(R.id.collectBtn).setOnClickListener(collectBtnListener);
		findViewById(R.id.hdBtn).setOnClickListener(hbBtnListener);
		findViewById(R.id.betBtn).setOnClickListener(betBtnListener);
		findViewById(R.id.repeatBtn).setOnClickListener(repeatBtnListener);
		findViewById(R.id.payoutBtn).setOnClickListener(payoutBtnListener);
		findViewById(R.id.dealBtn).setOnClickListener(dealBtnListener);

		findViewById(R.id.TextLayout).setOnClickListener(textListener);

		wagerView = (TextView) findViewById(R.id.wager);//
		winView = (TextView) findViewById(R.id.win);//
		paidView = (TextView) findViewById(R.id.paid);//
		creditView = (TextView) findViewById(R.id.credit);//

		// todo
		//		コイン処理の移植
		//		ゲームオーバー処理
		//		ListViewの処理

		//カウンター処理
		countChain();

		replaceFlag = true;
		Log.v(TAG, "onCreate()");
	}// onCreate()

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		// 機種によって複数回onWindowFocusChanged()が呼ばれる場合があるので
		// FlagでonCreate()の実行後に一度だけ実行されるように記述する
		if (replaceFlag) {
			// ステータスバーの高さを取得
			Rect rect = new Rect();
			getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
			statusbarHeight = rect.top;
			Log.v(TAG, "ステータスバー height=" + statusbarHeight);

			// ClearViewの座標を取得する
			setClearViewPoint();

			layout_location[0] = Math.max(clearView_location.get(0).x, clearView_location.get(3).x);
			layout_location[1] = clearView_location.get(0).y - statusbarHeight;

			FrameLayout.LayoutParams params =
					new FrameLayout.LayoutParams(trumpView[0].getTrumpWidth(), trumpView[0].getTrumpHeight());

			params.leftMargin = layout_location[0];
			params.topMargin = layout_location[1];
			params.gravity = Gravity.NO_GRAVITY;// この記載がないとマージンが有効にならない

			// レイアウトに場札画像を配置する
			mainFrame.addView(trumpView[0], params);

			// レイアウトに裏面の画像を配置する(場札用)
			mainFrame.addView(trumpBackView[0], params);
			trumpBackView[0].setVisibility(View.INVISIBLE);

			int x = Math.max(clearView_location.get(0).x, clearView_location.get(3).x)
					- Math.min(clearView_location.get(0).x, clearView_location.get(3).x);

			clearView[0].layout(clearView[0].getLeft() + x, 0,
					clearView[0].getRight() + x, clearView[0].getHeight());

			// 手札1から5にトランプを配置し非表示にする
			//			for (int i = 1; i < 6; i++) {
			//
			//				trumpView[i].addTrumpView(deck, i - 1, this.getApplicationContext());
			//
			//				FrameLayout.LayoutParams handParams =
			//						new FrameLayout.LayoutParams(trumpView[i].getTrumpWidth(), trumpView[i].getTrumpHeight());
			//
			//				handParams.leftMargin = clearView_location.get(i).x;
			//				handParams.topMargin = clearView_location.get(i).y - statusbarHeight;
			//				handParams.gravity = Gravity.NO_GRAVITY;// この記載がないとマージンが有効にならない
			//				// レイアウトに場札画像を配置する
			//				mainFrame.addView(trumpView[i], handParams);
			//				trumpView[i].setVisibility(View.INVISIBLE);
			//
			//			}

			trumpView[3].addTrumpView(deck, 2, this.getApplicationContext());
			FrameLayout.LayoutParams handParams =
					new FrameLayout.LayoutParams(trumpView[3].getTrumpWidth(), trumpView[3].getTrumpHeight());
			handParams.leftMargin = clearView_location.get(3).x;
			handParams.topMargin = clearView_location.get(3).y - statusbarHeight;
			handParams.gravity = Gravity.NO_GRAVITY;// この記載がないとマージンが有効にならない
			// レイアウトに場札画像を配置する
			mainFrame.addView(trumpView[3], handParams);
			trumpView[3].setVisibility(View.INVISIBLE);

			trumpView[2].addTrumpView(deck, 1, this.getApplicationContext());
			handParams =
					new FrameLayout.LayoutParams(trumpView[2].getTrumpWidth(), trumpView[2].getTrumpHeight());
			handParams.leftMargin = clearView_location.get(2).x;
			handParams.topMargin = clearView_location.get(2).y - statusbarHeight;
			handParams.gravity = Gravity.NO_GRAVITY;// この記載がないとマージンが有効にならない
			// レイアウトに場札画像を配置する
			mainFrame.addView(trumpView[2], handParams);
			trumpView[2].setVisibility(View.INVISIBLE);

			trumpView[4].addTrumpView(deck, 3, this.getApplicationContext());
			handParams =
					new FrameLayout.LayoutParams(trumpView[4].getTrumpWidth(), trumpView[4].getTrumpHeight());
			handParams.leftMargin = clearView_location.get(4).x;
			handParams.topMargin = clearView_location.get(4).y - statusbarHeight;
			handParams.gravity = Gravity.NO_GRAVITY;// この記載がないとマージンが有効にならない
			// レイアウトに場札画像を配置する
			mainFrame.addView(trumpView[4], handParams);
			trumpView[4].setVisibility(View.INVISIBLE);

			trumpView[1].addTrumpView(deck, 0, this.getApplicationContext());
			handParams =
					new FrameLayout.LayoutParams(trumpView[1].getTrumpWidth(), trumpView[1].getTrumpHeight());
			handParams.leftMargin = clearView_location.get(1).x;
			handParams.topMargin = clearView_location.get(1).y - statusbarHeight;
			handParams.gravity = Gravity.NO_GRAVITY;// この記載がないとマージンが有効にならない
			// レイアウトに場札画像を配置する
			mainFrame.addView(trumpView[1], handParams);
			trumpView[1].setVisibility(View.INVISIBLE);

			trumpView[5].addTrumpView(deck, 4, this.getApplicationContext());
			handParams =
					new FrameLayout.LayoutParams(trumpView[5].getTrumpWidth(), trumpView[5].getTrumpHeight());
			handParams.leftMargin = clearView_location.get(5).x;
			handParams.topMargin = clearView_location.get(5).y - statusbarHeight;
			handParams.gravity = Gravity.NO_GRAVITY;// この記載がないとマージンが有効にならない
			// レイアウトに場札画像を配置する
			mainFrame.addView(trumpView[5], handParams);
			trumpView[5].setVisibility(View.INVISIBLE);

			scrollHeight = findViewById(R.id.cChain1).getHeight();

			chainScroll = (ScrollView) findViewById(R.id.chainScroll);

			chainScroll.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});

			chainScroll.post(new Runnable() {

				@Override
				public void run() {
					chainScroll.fullScroll(ScrollView.FOCUS_DOWN);

				}
			});

			bonusScroll = (ScrollView) findViewById(R.id.bonusScroll);
			bonusScroll.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});

			bonusScroll.post(new Runnable() {

				@Override
				public void run() {
					bonusScroll.fullScroll(ScrollView.FOCUS_DOWN);

				}
			});

			setTxtBounus();

			findViewById(R.id.HandLayout).setVisibility(View.GONE);
			findViewById(R.id.CoinLayout).setVisibility(View.VISIBLE);

			redrawCoin();
			replaceFlag = false;
		}
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

	// トランプ1枚をアニメーションの処理
	public void FlipTrump(final int index) {

		// アニメーション中にクリックできないようfalseに変更する
		flipAnimFlag = false;
		// 現在表示されているトランプ画像を非表示にする
		trumpBackView[index].setVisibility(View.INVISIBLE);

		// Y軸回転(0～90度)
		Rotate3dAnimation rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, true);
		rotation.setDuration(90);
		trumpBackView[index].startAnimation(rotation);
		rotation.setAnimationListener(new TrumpAnimationListener(index) {
			// 裏面が回転し終わり表面が回転し始める
			@Override
			public void onAnimationEnd(Animation animation) {

				// Y軸回転(270～360度)
				Rotate3dAnimation rotation = new Rotate3dAnimation(270, 360, centerX, centerY, 0f, false);
				rotation.setDuration(90);
				trumpView[index].startAnimation(rotation);
				rotation.setAnimationListener(new TrumpAnimationListener(index) {
					@Override
					public void onAnimationEnd(Animation animation) {
						// アニメーションの終了
						trumpView[index].setVisibility(View.VISIBLE);

						// ゲームオーバーの判定
						GameOver();
						// ゲームクリアの判定
						selectAble();
						flipAnimFlag = true;
					}
				});
			}
		});

	}

	// トランプ5枚を順番に捲るアニメーションの処理
	public void dealFlipTrump(final int index) {

		// アニメーション中にクリックできないようfalseに変更する
		dealAnimFlag = false;
		// 現在表示されているトランプ画像を非表示にする
		trumpBackView[index].setVisibility(View.INVISIBLE);

		// Y軸回転(0～90度)
		Rotate3dAnimation rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, true);
		rotation.setDuration(50);
		trumpBackView[index].startAnimation(rotation);
		rotation.setAnimationListener(new TrumpAnimationListener(index) {
			// 裏面が回転し終わり表面が回転し始める
			@Override
			public void onAnimationEnd(Animation animation) {

				// Y軸回転(270～360度)
				Rotate3dAnimation rotation = new Rotate3dAnimation(270, 360, centerX, centerY, 0f, false);
				rotation.setDuration(50);
				trumpView[index].startAnimation(rotation);
				rotation.setAnimationListener(new TrumpAnimationListener(index) {
					@Override
					public void onAnimationEnd(Animation animation) {
						// アニメーションの終了
						trumpView[index].setVisibility(View.VISIBLE);
						yellowNum(trumpView[index].getSerial());
						animCount++;
						dealAnimFlag = true;
						// 手札2～5までのY軸回転処理						
						if (animCount < 5) {
							dealFlipTrump(index + 1);
						} else if (animCount == 5) {
							animCount = 0;
							selectAble();
						}

					}
				});
			}
		});

	}

	// ArrayListにclearViewのXY座標を格納する処理
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

	// 手札1～5を配る処理
	private void dealTrump() {

		trumpView[0].setVisibility(View.INVISIBLE);
		for (int i = 1; i < 6; i++) {
			trumpView[i].setVisibility(View.INVISIBLE);
			trumpBackView[i].setVisibility(View.INVISIBLE);
		}

		dealFlipTrump(1);

	}

	// 手札から場札へトランプが移動する処理
	private synchronized void moveTrump(final int index) {

		// アニメーション中にクリックできないようfalseに変更する
		moveAnimFlag = false;

		TranslateAnimation translate = new TranslateAnimation(
				0, layout_location[0] - clearView_location.get(index).x,
				0, layout_location[1] - (clearView_location.get(index).y - statusbarHeight));

		translate.setDuration(175);
		trumpView[index].startAnimation(translate);
		translate.setAnimationListener(new TrumpAnimationListener(index) {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// ////////////////////////////////////////////////
				// トランプクリック後の処理
				// ////////////////////////////////////////////////

				counter++;
				count.setText(String.valueOf(counter));

				// 場札のビュー(trumpView[0])に手札の情報を移す
				trumpView[0].setTrump(trumpView[index].getNumber(),
						trumpView[index].getSuit(),
						trumpView[index].getSerial(),
						trumpView[index].getColor());

				if (counter == 1) {
					trumpView[0].setVisibility(View.VISIBLE);
				}

				// recordに場札に置く札を記録していく
				record.trump.add(new Trump(
						trumpView[0].getNumber(),
						trumpView[0].getSuit(),
						trumpView[0].getSerial(),
						trumpView[0].getColor()
						));

				// 場札に置いた札を強調表示する
				boldNum(trumpView[0].getSerial());

				// 2枚目以降に強調表示した札の番号を消す
				if (counter > 1) {
					deleteNum(record.trump.get(counter - 2).getSerial());
				}

				// 手札に新しい札を配る
				final Handler handler = new Handler();

				new Thread(new Runnable() {

					@Override
					public void run() {

						handler.post(new Runnable() {

							@Override
							public void run() {
								if (counter < 48) {
									// 手札のビュー(trumpView[index])に新しい札の情報を移す
									trumpView[index].setTrump(deck.trump.get(counter + 4).getNumber(),
											deck.trump.get(counter + 4).getSuit(),
											deck.trump.get(counter + 4).getSerial(),
											deck.trump.get(counter + 4).getColor());
									// アニメーションのため一時非表示
									trumpView[index].setVisibility(View.INVISIBLE);
									FlipTrump(index);
									yellowNum(trumpView[index].getSerial());

								} else {
									trumpView[index].setVisibility(View.INVISIBLE);
									
									Resources res = getResources();
									int id = res.getIdentifier("selectAble" + index, "id", getPackageName());
									TextView view = (TextView) findViewById(id);
									view.setTextColor(0x00FFFF00);
									
									GameClear();
								}

								scrollUp();

								moveAnimFlag = true;

							}

						});

					}

				}).start();

			}

		});

	}

	// 場札と同じ数字かスート(図柄)ならtrueを返す処理
	private boolean agreeTrump(int index) {
		if (counter == 0) {
			// 一枚目の場合(どのカードも場札に置ける)
			//			Log.v(TAG, "一枚目");

			return true;
		} else if (trumpView[0].getSuit().equals(trumpView[index].getSuit())) {
			// スート(図柄)が一致した場合
			//			Log.v(TAG, "スート(図柄)が一致");

			return true;
		} else if (trumpView[0].getNumber() == trumpView[index].getNumber()) {
			// 数字が一致した場合
			//			Log.v(TAG, "数字が一致");

			return true;
		}
		return false;
	}

	// boldNum関数…場札に置いたトランプの数字をガイド上で太字・シアンにする処理
	public void boldNum(int x) {
		Resources res = getResources();
		int guideId = res.getIdentifier("card" + x, "id", getPackageName());
		guideView = (TextView) findViewById(guideId);
		// TextViewの文字色を変更する（16進数で頭の2bitがアルファ値、00で透過率100%）
		guideView.setTextColor(0xFF00BFFF);
		// フォントのスタイル（太字、斜線など）を変更する
		guideView.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
	}// Card.BoldNum_**********

	// yellowNum関数…場札に置いたトランプの数字をガイド上で黄色にする処理
	public void yellowNum(int x) {
		Resources res = getResources();
		int guideId = res.getIdentifier("card" + x, "id", getPackageName());
		guideView = (TextView) findViewById(guideId);
		// TextViewの文字色を変更する（16進数で頭の2bitがアルファ値、00で透過率100%）
		guideView.setTextColor(0xFFFFD700);
		// フォントのスタイル（太字、斜線など）を変更する
		guideView.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
		// 背景色を変更する
		guideView.setBackgroundColor(0xFF7777FF);
	}// Card.yellowNum_**********

	// deleteNum関数…場札に置いたトランプの数字をガイド上から消す処理
	public void deleteNum(int x) {
		// getResources()でリソースオブジェクトを
		Resources res = getResources();
		// guideIdという変数にリソースの場所を格納する
		// ("card" + x , "id" , getPackageName())
		// ↓
		// (R.id.card***)
		int guideId = res.getIdentifier("card" + x, "id", getPackageName());
		guideView = (TextView) findViewById(guideId);
		guideView.setTextColor(0x00FFFFFF);
		// 背景色を元の青色に戻す
		guideView.setBackgroundColor(0xFF0000FF);
	}// Card.DeleteNum_**********

	public void redrawGuide() {
		Resources res = getResources();
		int guideId;
		for (int i = 0; i < 52; i++) {
			guideId = res.getIdentifier("card" + i, "id", getPackageName());
			guideView = (TextView) findViewById(guideId);
			// 文字色を白に戻す
			guideView.setTextColor(0xFFFFFFFF);
			// 背景色を元の青色に戻す
			guideView.setBackgroundColor(0xFF0000FF);
		}
	}

	private void countChain() {

		if (width <= 240) {//ldpi（120dpi）240×320px
			txtSwitchFontSize = 40;
		} else if (240 < +width && +width <= 320) {//mdpi（160dpi）320×480px
			txtSwitchFontSize = 44;
		} else if (320 < +width && +width <= 480) {//hdpi（240dpi）480×800px
			txtSwitchFontSize = 48;
		} else if (480 < +width && +width <= 640) {//xhdpi（320dpi）640×960px
			txtSwitchFontSize = 52;
		} else if (640 < +width) {//xxhdpi（480dpi）960×1440px
			txtSwitchFontSize = 64;

		}

		final Animation in = AnimationUtils.loadAnimation(this, R.anim.down_in);
		final Animation out = AnimationUtils.loadAnimation(this, R.anim.down_out);

		count = (TextSwitcher) findViewById(R.id.countView);
		count.setFactory(this);

		count.setInAnimation(in);
		count.setOutAnimation(out);

		count.setText(String.valueOf(counter));

	}

	private void setTxtBounus() {

		Resources res = getResources();
		int id;
		TextView view;
		for (int i = 1; i <= 52; i++) {
			id = res.getIdentifier("cChain" + i, "id", getPackageName());
			view = (TextView) findViewById(id);
			view.setText(String.valueOf(i) + " CARDS");
			view.setBackgroundColor(0xFF0000FF);
		}

		for (int i = 1; i <= 52; i++) {
			id = res.getIdentifier("cBonus" + i, "id", getPackageName());
			view = (TextView) findViewById(id);
			view.setText(String.valueOf(rate52[i - 1]));

		}
	}

	private void setTxtBounus(int wager) {

		Resources res = getResources();
		int id;
		TextView view;

		for (int i = 1; i <= 52; i++) {
			id = res.getIdentifier("cChain" + i, "id", getPackageName());
			view = (TextView) findViewById(id);
			view.setBackgroundColor(0xFF0000FF);
		}

		for (int i = 1; i <= 52; i++) {
			id = res.getIdentifier("cBonus" + i, "id", getPackageName());
			view = (TextView) findViewById(id);
			view.setText(String.valueOf(rate52[i - 1] * wager));
			view.setTextColor(0xFFFFFFFF);
			view.setBackgroundColor(0xFFFF0000);
		}
	}

	private void scrollUp() {

		final Resources res = getResources();

		final int cChainId = res.getIdentifier("cChain" + counter, "id", getPackageName());
		final int cBonusId = res.getIdentifier("cBonus" + counter, "id", getPackageName());
		if (counter == 1) {

			((TextView) findViewById(cChainId)).setBackgroundColor(0xFFFF0000);

			((TextView) findViewById(cBonusId)).setTextColor(0xFFFF0000);
			((TextView) findViewById(cBonusId)).setBackgroundColor(0xFFFFFFFF);

		} else {
			if (counter <= 50) {

				chainScroll.scrollBy(0, -scrollHeight);
				bonusScroll.scrollBy(0, -scrollHeight);

				// 1つ前のViewの状態を元に戻す
				((TextView) findViewById(res.getIdentifier("cChain" + (counter - 1), "id", getPackageName())))
						.setBackgroundColor(0xFF0000FF);

				((TextView) findViewById(res.getIdentifier("cBonus" + (counter - 1), "id", getPackageName())))
						.setTextColor(0xFFFFFFFF);
				((TextView) findViewById(res.getIdentifier("cBonus" + (counter - 1), "id", getPackageName())))
						.setBackgroundColor(0xFFFF0000);

				((TextView) findViewById(cChainId)).setBackgroundColor(0xFFFF0000);

				((TextView) findViewById(cBonusId)).setTextColor(0xFFFF0000);
				((TextView) findViewById(cBonusId)).setBackgroundColor(0xFFFFFFFF);

			} else {

				// 1つ前のViewの状態を元に戻す
				((TextView) findViewById(res.getIdentifier("cChain" + (counter - 1), "id", getPackageName())))
						.setBackgroundColor(0xFF0000FF);

				((TextView) findViewById(res.getIdentifier("cBonus" + (counter - 1), "id", getPackageName())))
						.setTextColor(0xFFFFFFFF);
				((TextView) findViewById(res.getIdentifier("cBonus" + (counter - 1), "id", getPackageName())))
						.setBackgroundColor(0xFFFF0000);

				((TextView) findViewById(cChainId)).setBackgroundColor(0xFFFF0000);

				((TextView) findViewById(cBonusId)).setTextColor(0xFFFF0000);
				((TextView) findViewById(cBonusId)).setBackgroundColor(0xFFFFFFFF);
			}

		}

	}

	public void redrawCoin() {
		wagerView.setText(String.valueOf(coin.getWager()));
		winView.setText(String.valueOf(0));
		paidView.setText(String.valueOf(0));
		creditView.setText(String.valueOf(coin.getCredit()));
	}

	// cuCoin関数…cu = Count Upの略称、払い戻し時にコインの枚数が1枚ずつ
	// 増減する様子を表示する処理、引数は増減する枚数を渡す
	public void cuCoin(final int x) {
		// コイン増加表示の処理中かフラグ判定
		if (coin_flag == false && (x != 0)) {

			coin_flag = true;// コイン増加表示の処理中というフラグを立てる
			beforeCredit = coin.getCredit();// 増加前のコインの枚数を格納
			beforeWager = coin.getBeforeWager();
			final Timer timer = new Timer();

			//			Log.v(TAG, "timer_start x=" + x);

			winView.setText(String.valueOf(x));

			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					// handlerを通じてUI Threadへ処理をキューイング
					handler.post(new Runnable() {
						public void run() {
							paidView.setText(String.valueOf(timerCount));
							creditView.setText(String.valueOf(beforeCredit + timerCount));
							timerCount++;

							// Timer終了処理　
							if (x == beforeWager && timerCount == x) {

								creditView.setText(String.valueOf(beforeCredit
										+ timerCount));
								coin.setCredit((beforeCredit + beforeWager));
								coin.setWager(0);

								wagerView.setText("0");
								winView.setText("0");
								paidView.setText("0");

								//								Log.v(TAG, "timer_stop x=" + x + "counter=" + timerCount);
								coin_flag = false;
								timerCount = 0;
								timer.cancel();

							} else if (timerCount == (x - beforeWager)
									&& x != beforeWager) {

								creditView.setText(String.valueOf(beforeCredit + x));

								coin.setCredit((beforeCredit + x));
								coin.setWager(0);

								wagerView.setText("0");
								winView.setText("0");
								paidView.setText("0");

								//								Log.v(TAG, "timer_stop x=" + x + "counter=" + timerCount);
								coin_flag = false;
								timerCount = 0;
								timer.cancel();
							}

							else if (skip_flag == true) {

								creditView.setText(String.valueOf(beforeCredit + x));

								coin.setCredit((beforeCredit + x));
								coin.setWager(0);

								wagerView.setText("0");
								winView.setText("0");
								paidView.setText("0");

								//								Log.v(TAG, "timer_stop_skip x=" + x + "counter=" + timerCount);
								skip_flag = false;
								coin_flag = false;
								timerCount = 0;
								timer.cancel();
							}

						}
					});

				}
			}, 0, 50);

		}
		//		else if ((0 < timerCount) && (timerCount < (x - coin.getWager()))
		//				&& (x != 0)) {
		//			// コイン増加表示の処理中に再度ボタンを押した時に
		//			// 増加表示をスキップする処理
		//			timerCount = x - 1;
		//		} 
		else if (x == 0) {
			coin.setWager(0);
			redrawCoin();
		}

	}

	private void refundCoin() {

		cuCoin(rate52[counter - 1] * coin.getWager());

	}

	// 場札と同じ数字かスート(図柄)ならSELECT ABLEを表示する処理
	private void selectAble() {

		Resources res = getResources();
		int id;
		TextView view;

		if (counter == 0) {
			// 一枚目の場合(どのカードも場札に置ける)

			for (int i = 1; i <= 5; i++) {
				id = res.getIdentifier("selectAble" + i, "id", getPackageName());
				view = (TextView) findViewById(id);
				view.setTextColor(0xFFFFFF00);
			}

		} else {
			// スート(図柄)が一致した場合
			for (int i = 1; i <= 5; i++) {
				id = res.getIdentifier("selectAble" + i, "id", getPackageName());
				view = (TextView) findViewById(id);
				if (trumpView[0].getSuit().equals(trumpView[i].getSuit())) {
					view.setTextColor(0xFFFFFF00);
				}
				else if (trumpView[0].getNumber() == trumpView[i].getNumber()) {
					view.setTextColor(0xFFFFFF00);

				}
				else {
					view.setTextColor(0x00FFFF00);
				}

			}
		}

	}

	// ////////////////////////////////////////////////
	// GameFlag
	// ////////////////////////////////////////////////

	// GameOver関数…ゲームフラグの管理
	public void GameOver() {
		for (int i = 1; i < 6; i++) {

			if (trumpView[0].getSuit().equals(trumpView[i].getSuit()) ||
					trumpView[0].getNumber() == trumpView[i].getNumber()) {
				// スート(図柄)と数字の両方が一致した場合
				return;
			}

		}

		final Handler handler = new Handler();

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					Thread.sleep(250);

					handler.post(new Runnable() {

						@Override
						public void run() {

							try {
								Thread.sleep(1000);

								coin.setBeforeWager(coin.getWager());

								for (int i = 1; i < 6; i++) {
									trumpView[i].setVisibility(View.INVISIBLE);
								}

								msg = (TextView) findViewById(R.id.msgView1);

								if (counter >= 14) {

									msg.setText("WINNER!");
									msg.setTextColor(Color.RED);
								}
								else if (10 <= counter && counter <= 13) {
									msg.setText("DRAW!");
									msg.setTextColor(Color.GREEN);
								}
								else {
									msg.setText("LOSER!");
									msg.setTextColor(Color.BLUE);
								}
								// 手札を非表示にして、メッセージ画面手札を表示する
								findViewById(R.id.HandLayout).setVisibility(View.GONE);
								findViewById(R.id.TextLayout).setVisibility(View.VISIBLE);

								refundCoin();
								//Toast.makeText(WhatifActivity.this, "ＧＡＭＥ ＯＶＥＲ", Toast.LENGTH_SHORT).show();

							} catch (InterruptedException e) {
								e.printStackTrace();
							}

						}

					});

				} catch (InterruptedException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				}

			}

		}).start();

	}// GameOver_**********

	public void GameClear() {

		if (counter == 52) {

			for (int i = 1; i < 6; i++) {
				trumpView[i].setVisibility(View.INVISIBLE);
			}

			coin.setBeforeWager(coin.getWager());
			msg = (TextView) findViewById(R.id.msgView1);
			msg.setText("CONGRATULATION!");
			msg.setTextColor(Color.YELLOW);

			// 手札を非表示にして、メッセージ画面手札を表示する
			findViewById(R.id.HandLayout).setVisibility(View.GONE);
			findViewById(R.id.TextLayout).setVisibility(View.VISIBLE);

			refundCoin();
			//Toast.makeText(WhatifActivity.this, "ＧＡＭＥ ＣＬＥＡＲ", Toast.LENGTH_SHORT).show();
		}

	}

	// ////////////////////////////////////////////////
	// ViewFactory
	// ////////////////////////////////////////////////
	@Override
	public View makeView() {
		TextView txt = new TextView(this);
		//		txt.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		txt.setGravity(Gravity.CENTER);
		txt.setTextColor(0xFFFFFFFF);
		txt.setTextSize(txtSwitchFontSize);
		return txt;
	}

	// ////////////////////////////////////////////////
	// ボタンクリック時の処理
	// ////////////////////////////////////////////////

	// レイアウトViewをクリックした時の処理
	OnClickListener layoutListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

		}
	};

	// 手札1Viewをクリックした時の処理
	OnClickListener hand1Listener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (dealAnimFlag && flipAnimFlag && moveAnimFlag && agreeTrump(1)) {
				moveTrump(1);
			}

		}
	};

	// 手札2Viewをクリックした時の処理
	OnClickListener hand2Listener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (dealAnimFlag && flipAnimFlag && moveAnimFlag && agreeTrump(2)) {
				moveTrump(2);
			}

		}
	};
	// 手札3Viewをクリックした時の処理
	OnClickListener hand3Listener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (dealAnimFlag && flipAnimFlag && moveAnimFlag && agreeTrump(3)) {
				moveTrump(3);
			}
		}

	};
	// 手札4Viewをクリックした時の処理
	OnClickListener hand4Listener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (dealAnimFlag && flipAnimFlag && moveAnimFlag && agreeTrump(4)) {
				moveTrump(4);
			}

		}
	};
	// 手札5Viewをクリックした時の処理
	OnClickListener hand5Listener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (dealAnimFlag && flipAnimFlag && moveAnimFlag && agreeTrump(5)) {
				moveTrump(5);
			}
		}

	};

	// コレクトボタンをクリックした時の処理
	OnClickListener collectBtnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (coin_flag) {
				skip_flag = true;
			}

			if (0 < coin.getWager()) {
				coin.cancelBet();
				wagerView.setText(String.valueOf(coin.getWager()));
				creditView.setText(String.valueOf(coin.getCredit()));
			}

		}
	};

	// ハーフダブルボタンをクリックした時の処理
	OnClickListener hbBtnListener = new OnClickListener() {
		// 当たり枚数の半分を賭けて行うダブルダウンのこと。負けても半分は返ってくる。
		@Override
		public void onClick(View v) {

		}
	};
	// ベットボタンをクリックした時の処理
	OnClickListener betBtnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (!coin_flag) {

				coin.minBet();

				wagerView.setText(String.valueOf(coin.getWager()));
				creditView.setText(String.valueOf(coin.getCredit()));
			}
		}
	};
	// リピートボタンをクリックした時の処理
	OnClickListener repeatBtnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!coin_flag) {
				coin.repBet();
				wagerView.setText(String.valueOf(coin.getWager()));
				creditView.setText(String.valueOf(coin.getCredit()));
			}
		}
	};
	// プレイアウトボタンをクリックした時の処理
	OnClickListener payoutBtnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

		}
	};

	// ディールボタンをクリックした時の処理
	OnClickListener dealBtnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (!coin_flag) {
				// 最小BET数を満たしていたらゲーム開始
				if (coin.getWager() >= coin.getMinbet() && counter == 0) {

					setTxtBounus(coin.getWager());

					// コイン操作画面を非表示にして、手札を表示する				
					findViewById(R.id.CoinLayout).setVisibility(View.GONE);
					findViewById(R.id.HandLayout).setVisibility(View.VISIBLE);

					dealTrump();
				} else if (coin.getWager() >= coin.getMinbet() && counter > 0) {

					counter = 0;
					count.setText(String.valueOf(0));

					trumpView[0].setTrump(0, "", 0, Color.WHITE);

					// トランプ(52枚)を管理するDeckクラスのインスタンスの取得
					deck = new Deck(getApplicationContext());
					// トランプ(52枚)の生成とシャッフル
					deck.shuffle(getApplicationContext());
					// 場札に置いた順番を記録するインスタンスを取得する
					record = new Deck(getApplicationContext());

					setTxtBounus(coin.getWager());

					chainScroll.post(new Runnable() {

						@Override
						public void run() {
							chainScroll.fullScroll(ScrollView.FOCUS_DOWN);

						}
					});

					bonusScroll.post(new Runnable() {

						@Override
						public void run() {
							bonusScroll.fullScroll(ScrollView.FOCUS_DOWN);

						}
					});

					redrawGuide();

					for (int i = 1; i < 6; i++) {
						// 手札のビュー(trumpView[index])に新しい札の情報を移す
						trumpView[i].setTrump(deck.trump.get(i - 1).getNumber(),
								deck.trump.get(i - 1).getSuit(),
								deck.trump.get(i - 1).getSerial(),
								deck.trump.get(i - 1).getColor());

					}

					// コイン操作画面を非表示にして、手札を表示する				
					findViewById(R.id.CoinLayout).setVisibility(View.GONE);
					findViewById(R.id.HandLayout).setVisibility(View.VISIBLE);

					dealTrump();
				}

			}
		}
	};

	// プレイアウトボタンをクリックした時の処理
	OnClickListener textListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (coin_flag) {
				skip_flag = true;
			}

			findViewById(R.id.TextLayout).setVisibility(View.GONE);
			findViewById(R.id.CoinLayout).setVisibility(View.VISIBLE);

		}
	};

}
