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
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

public class WhatifActivity extends Activity
		implements ViewFactory {

	// LogCat用のタグを定数で定義する
	public static final String TAG = "Test";

	private TrumpView[] trumpView = new TrumpView[6];
	private RoundedImageView[] trumpBackView = new RoundedImageView[6];

	private Deck deck;
	private Deck record;

	private int statusbarHeight;

	private ArrayList<Point> trumpBackView_location = new ArrayList<Point>();

	private boolean flipAnimFlag = true;// アニメーション中はfalseとなり画像をクリックできない
	private boolean moveAnimFlag = true;// アニメーション中はfalseとなり画像をクリックできない
	private boolean dealAnimFlag = true;// アニメーション中はfalseとなり画像をクリックできない

	private float centerX;// Y軸回転の中心点(X座標)を設定
	private float centerY;// Y軸回転の中心点(Y座標)を設定

	private boolean replaceFlag = true;// onCreate()後にonWindowFocusChanged()で一度だけ処理を実行するためのフラグ

	private int animCount = 0;// 5枚配るアニメ処理で何枚目であるかカウントする変数
	private int counter = 0;// 

	private TextView guideView;// ガイド
	private TextSwitcher count;// カウンターViewのインスタンス
	private int width = 0;// 端末の横幅
	private int height = 0;// 端末の高さ
	private int txtSwitchFontSize = 20; //カウンターのフォントサイズ

	private int scrollHeight;//スクロールViewの移動量を格納する変数

	private TextView wagerView;
	private TextView winView;
	private TextView paidView;
	private TextView creditView;

	private enum DPI {
		ldpi, mdpi, hdpi, xhdpi, xxhdpi
	}

	private DPI dpi;
	private boolean coinFlag = false;// trueでコインの加算処理中のため入力を受け付けないようにする
	private boolean skipFlag = false;// trueでコインの加算処理をスキップ

	private int timerCounter;// タイマー処理のカウントアップに使用する変数

	private int[] rate52 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3,
			3, 3, 4, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 10, 10, 12, 12, 14, 14, 16,
			18, 20, 22, 25, 30, 35, 40, 45, 50, 55, 60, 80, 100 };

	private TextView msg;

	private Coin coin;

	private Resources res;

	private ScrollView bonusScroll1;
	private ScrollView bonusScroll2;
	private ScrollView paysScroll1;
	private ScrollView paysScroll2;
	private ScrollView hitsScroll1;

	private int scrollLines = 3;// スクロールViewで表示させ行数

	private int timeTranslate = 200;//200
	private int timeDeal = 50;//50
	private int timeFlip = 90;//90

	private int scrollSleepTime = 250;//250
	private int msgSleepTime = 500;//500

	Handler handler = new Handler();

	private int margin;

	/* ********** ********** ********** ********** */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // タイトルバーを非表示にする
		//		setContentView(R.layout.activity_whatif);
		setContentView(R.layout.vertical_layout);

		findViewById(R.id.black).bringToFront();

		// トランプ(52枚)を管理するDeckクラスのインスタンスの取得
		deck = new Deck(this.getApplicationContext());
		// トランプ(52枚)の生成とシャッフル
		deck.shuffle(this.getApplicationContext());
		// 場札に置いた順番を記録するインスタンスを取得する
		record = new Deck(this.getApplicationContext());

		// コイン処理のインスタンスを取得する
		coin = new Coin();

		// ldpi : 120 dpi
		// mdpi : 160 dpi
		// hdpi : 240 dpi
		// xhdpi : 320 dpi
		// xxhdpi : 480 dpi
		// txdpi : 213 dpi (Nexus 7)

		// ldpi : mdpi : hdpi : xdhpi : xxhdpi : tvdpi = 0.75 : 1 : 1.5 : 2 : 3 : 1.33125

		// Activityを継承していないため、getWindowManager()メソッドは利用できない
		// Displayクラスのインスタンスを取得するため
		// 引数のContextを使用してWindowManagerを取得する
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		// Displayインスタンスを取得する
		Display display = wm.getDefaultDisplay();

		// DPI を取得する
		int _dpi = getResources().getDisplayMetrics().densityDpi;

		// dpi を元に比率を計算する ( dpi ÷ 基準値(mdpi) )
		float ratio = _dpi / 160f;

		Log.v(TAG, "dpi=" + _dpi + " density=" + ratio);

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

		/* ********** ********** ********** ********** */

		res = getResources();

		for (int i = 0; i <= 5; i++) {
			int id = res.getIdentifier("trumpView" + i, "id", getPackageName());
			trumpView[i] = (TrumpView) findViewById(id);

		}
		for (int i = 0; i <= 5; i++) {
			int id = res.getIdentifier("roundedImageView" + i, "id", getPackageName());
			trumpBackView[i] = (RoundedImageView) findViewById(id);
		}

		// リスナー登録
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

		findViewById(R.id.msgLayout).setOnClickListener(msgListener);

		wagerView = (TextView) findViewById(R.id.wager);//
		winView = (TextView) findViewById(R.id.win);//
		paidView = (TextView) findViewById(R.id.paid);//
		creditView = (TextView) findViewById(R.id.credit);//

		// 非表示
		findViewById(R.id.msgLayout).setVisibility(View.INVISIBLE);

		//カウンター処理
		txtSwitchOn();

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

			// trumpViewの縦横を画像に合わせる
			for (int i = 0; i <= 5; i++) {
				trumpView[i].getLayoutParams().width = trumpBackView[0].getWidth();
				trumpView[i].getLayoutParams().height = trumpBackView[0].getHeight();
				trumpView[i].requestLayout();
			}
			// trumpViewのフォントサイズを変更する

			int fontSize = 20;
			switch (dpi) {
			case ldpi:
				fontSize = 20;
				break;
			case mdpi:
				fontSize = 20;
				break;
			case hdpi:
				fontSize = 20;
				break;
			case xhdpi:
				fontSize = 20;
				break;
			case xxhdpi:
				fontSize = 20;
				break;
			}

			for (int i = 0; i <= 5; i++) {
				trumpView[i].setFontSize(fontSize, fontSize * 2);
			}

			// 山札から5枚トランプの情報を読み込む
			for (int i = 1; i <= 5; i++) {
				trumpView[i].setTrump(deck.trump.get(i - 1).getNumber(),
						deck.trump.get(i - 1).getSuit(),
						deck.trump.get(i - 1).getSerial(),
						deck.trump.get(i - 1).getColor());
			}

			// トランプ画像(×5)のwidthと端末のwidthから余白を計算する
			int trumpWidth = trumpBackView[0].getWidth();
			margin = (Math.max(trumpWidth * 5, width) - Math.min(trumpWidth * 5, width)) / 6;
			Log.v(TAG, "margin=" + margin);

			trumpBackView[2].getLayoutParams();
			// trumpBackViewからマージンを取得
			MarginLayoutParams marginParms2 = (MarginLayoutParams) trumpBackView[2].getLayoutParams();
			// 移動させたい距離に変更
			marginParms2.rightMargin += margin;
			// trumpBackViewへ反映
			trumpBackView[2].setLayoutParams(marginParms2);

			trumpBackView[1].getLayoutParams();
			MarginLayoutParams marginParms1 = (MarginLayoutParams) trumpBackView[1].getLayoutParams();
			marginParms1.rightMargin += margin;
			trumpBackView[1].setLayoutParams(marginParms1);

			trumpBackView[4].getLayoutParams();
			MarginLayoutParams marginParms4 = (MarginLayoutParams) trumpBackView[4].getLayoutParams();
			marginParms4.leftMargin += margin;
			trumpBackView[4].setLayoutParams(marginParms4);

			trumpBackView[5].getLayoutParams();
			MarginLayoutParams marginParms5 = (MarginLayoutParams) trumpBackView[5].getLayoutParams();
			marginParms5.leftMargin += margin;
			trumpBackView[5].setLayoutParams(marginParms5);

			// Y軸回転用の変数を取得する
			centerX = trumpBackView[0].getWidth() / 2;
			centerY = trumpBackView[0].getLayoutParams().height;

			// トランプ画像の座標を取得
			getTrumpBackViewPoint();
			//スクロールViewの設定をする
			setScrollView();
			// ボーナスにテキストを設定する
			setTxtBounus();
			// コインの初期値を設定する
			redrawCoin();

			// トランプ画像を非表示にする
			for (int i = 0; i <= 5; i++) {
				trumpView[i].setVisibility(View.INVISIBLE);
				trumpBackView[i].setVisibility(View.INVISIBLE);
			}

			// 最前面の黒画像をフェードアウトする
			new Thread((new Runnable() {

				@Override
				public void run() {
					handler.post(new Runnable() {

						@Override
						public void run() {

							AlphaAnimation alpha = new AlphaAnimation(
									1.0f, // 開始時の透明度（1は全く透過しない）
									0.0f); // 終了時の透明度（0は完全に透過）

							alpha.setDuration(1000);

							// アニメーション終了時の表示状態を維持する
							alpha.setFillEnabled(true);
							alpha.setFillAfter(true);

							// アニメーションを開始
							findViewById(R.id.black).startAnimation(alpha);
						}
					});

				}
			})).start();

			// 起動時のみ必要な処理が終了したのでフラグを変更する
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
		//		rotation.setDuration(90);
		rotation.setDuration(timeFlip);
		trumpBackView[index].startAnimation(rotation);
		rotation.setAnimationListener(new TrumpAnimationListener(index) {
			// 裏面が回転し終わり表面が回転し始める
			@Override
			public void onAnimationEnd(Animation animation) {

				// Y軸回転(270～360度)
				Rotate3dAnimation rotation = new Rotate3dAnimation(270, 360, centerX, centerY, 0f, false);
				//				rotation.setDuration(90);
				rotation.setDuration(timeFlip);
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
		//		rotation.setDuration(50);
		rotation.setDuration(timeDeal);
		trumpBackView[index].startAnimation(rotation);
		rotation.setAnimationListener(new TrumpAnimationListener(index) {
			// 裏面が回転し終わり表面が回転し始める
			@Override
			public void onAnimationEnd(Animation animation) {

				// Y軸回転(270～360度)
				Rotate3dAnimation rotation = new Rotate3dAnimation(270, 360, centerX, centerY, 0f, false);
				//				rotation.setDuration(50);
				rotation.setDuration(timeDeal);
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

	private void setScrollView() {
		scrollHeight = findViewById(R.id.cChain1).getHeight();

		bonusScroll1 = (ScrollView) findViewById(R.id.bonusScroll1);

		// スクロールViewの高さを変更
		bonusScroll1.getLayoutParams().height = scrollHeight * scrollLines;
		bonusScroll1.requestLayout();

		bonusScroll1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;//trueでタッチスクロールを禁止する
			}
		});
		bonusScroll1.post(new Runnable() {
			@Override
			public void run() {
				bonusScroll1.fullScroll(ScrollView.FOCUS_DOWN);// 一番下までスクロールする
			}
		});

		paysScroll1 = (ScrollView) findViewById(R.id.paysScroll1);
		// スクロールViewの高さを変更
		paysScroll1.getLayoutParams().height = scrollHeight * scrollLines;
		paysScroll1.requestLayout();
		paysScroll1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;//trueでタッチスクロールを禁止する
			}
		});
		paysScroll1.post(new Runnable() {
			@Override
			public void run() {
				paysScroll1.fullScroll(ScrollView.FOCUS_DOWN);// 一番下までスクロールする
			}
		});

		bonusScroll2 = (ScrollView) findViewById(R.id.bonusScroll2);
		// スクロールViewの高さを変更
		bonusScroll2.getLayoutParams().height = scrollHeight;
		bonusScroll2.requestLayout();
		bonusScroll2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;//trueでタッチスクロールを禁止する
			}
		});

		paysScroll2 = (ScrollView) findViewById(R.id.paysScroll2);
		// スクロールViewの高さを変更
		paysScroll2.getLayoutParams().height = scrollHeight;
		paysScroll2.requestLayout();
		paysScroll2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;//trueでタッチスクロールを禁止する
			}
		});

		hitsScroll1 = (ScrollView) findViewById(R.id.hitsScroll1);
		// スクロールViewの高さを変更
		hitsScroll1.getLayoutParams().height = scrollHeight;
		hitsScroll1.requestLayout();
		hitsScroll1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;//trueでタッチスクロールを禁止する
			}
		});

	}

	// ArrayListにclearViewのXY座標を格納する処理
	private void getTrumpBackViewPoint() {
		int[] location = new int[2];
		for (int i = 0; i < 6; i++) {

			trumpBackView[i].getLocationInWindow(location);
			int x = location[0];
			int y = location[1];
			Point point = new Point(x, y);
			trumpBackView_location.add(point);

		}
	}

	// 手札から場札へトランプが移動する処理
	private void moveTrump(final int index) {

		// アニメーション中にクリックできないようfalseに変更する
		moveAnimFlag = false;

		// アニメーションするViewを最前面に移動する
		trumpView[index].bringToFront();

		TranslateAnimation translate = new TranslateAnimation(
				0, trumpBackView_location.get(0).x - trumpBackView_location.get(index).x,
				0, (trumpBackView_location.get(0).y - statusbarHeight) - ((trumpBackView_location.get(index).y - statusbarHeight)));

		//		translate.setDuration(175);
		translate.setDuration(timeTranslate);
		translate.setFillAfter(true);

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
				trumpView[index].setAnimation(null);//チラつき防止、移動後のアニメViewが消える前に次のトランプが読み込まれてしまうため

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

				// アニメーションのため一時非表示
				trumpView[index].setVisibility(View.INVISIBLE);

				// 手札に新しい札を配る

				if (counter < 48) {
					// 手札のビュー(trumpView[index])に新しい札の情報を移す
					trumpView[index].setTrump(deck.trump.get(counter + 4).getNumber(),
							deck.trump.get(counter + 4).getSuit(),
							deck.trump.get(counter + 4).getSerial(),
							deck.trump.get(counter + 4).getColor());

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

	private void txtSwitchOn() {

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

				new Thread(new Runnable() {

					@Override
					public void run() {

						handler.post(new Runnable() {

							@Override
							public void run() {

								bonusScroll1.smoothScrollBy(0, -scrollHeight);
								paysScroll1.smoothScrollBy(0, -scrollHeight);

							}

						});

					}

				}).start();

				new Thread(new Runnable() {

					@Override
					public void run() {

						try {
							Thread.sleep(scrollSleepTime);

							handler.post(new Runnable() {

								@Override
								public void run() {

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

							});

						} catch (InterruptedException e1) {
							// TODO 自動生成された catch ブロック
							e1.printStackTrace();
						}

					}

				}).start();

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
		if (coinFlag == false && (x != 0)) {

			coinFlag = true;// コイン増加表示の処理中というフラグを立てる
			//			beforeCredit = coin.getCredit();// 増加前のコインの枚数を格納
			//			beforeWager = coin.getBeforeWager();
			final Timer timer = new Timer();

			//			Log.v(TAG, "timer_start x=" + x);

			winView.setText(String.valueOf(x));

			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					// handlerを通じてUI Threadへ処理をキューイング
					handler.post(new Runnable() {
						public void run() {
							paidView.setText(String.valueOf(timerCounter));
							creditView.setText(String.valueOf(coin.getCredit() + timerCounter));
							timerCounter++;

							if (x == coin.getWager()) {
								//								Log.v(TAG, "timer_stop_MIN");
								coin.setCredit(coin.getCredit() + coin.getWager());
								coin.setWager(0);

								redrawCoin();

								coinFlag = false;
								timerCounter = 0;
								timer.cancel();

							} else if (coin.getWager() > 0 && x == timerCounter) {
								//								Log.v(TAG, "timer_stop_WIN");

								coin.setCredit(coin.getCredit() + x);
								coin.setWager(0);

								redrawCoin();

								coinFlag = false;
								timerCounter = 0;
								timer.cancel();
							} else if (skipFlag == true) {
								//								Log.v(TAG, "timer_stop_SKIP");

								coin.setCredit(coin.getCredit() + x);
								coin.setWager(0);

								redrawCoin();

								coinFlag = false;
								skipFlag = false;
								timerCounter = 0;
								timer.cancel();

							}

						}
					});

				}
			}, 0, 50);

		} else if (x == 0) {
			coin.setWager(0);
			redrawCoin();
		}

	}

	private void refundCoin() {

		cuCoin(rate52[counter - 1] * coin.getWager());
		//		Log.v(TAG, "RATE=" + rate52[counter - 1] + "WAGER" + coin.getWager());
		//		Log.v(TAG, "WIN=" + rate52[counter - 1] * coin.getWager());
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

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					Thread.sleep(msgSleepTime);

					handler.post(new Runnable() {

						@Override
						public void run() {

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
							findViewById(R.id.msgLayout).setVisibility(View.VISIBLE);

							refundCoin();
							//Toast.makeText(WhatifActivity.this, "ＧＡＭＥ ＯＶＥＲ", Toast.LENGTH_SHORT).show();

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
			findViewById(R.id.msgLayout).setVisibility(View.VISIBLE);

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

			if (coinFlag) {
				skipFlag = true;
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

			if (!coinFlag) {

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
			if (!coinFlag) {
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

			if (!coinFlag) {
				// 最小BET数を満たしていたらゲーム開始
				if (coin.getWager() >= coin.getMinbet() && counter == 0) {

					setTxtBounus(coin.getWager());

					// コイン操作画面を非表示にして、手札を表示する				
					findViewById(R.id.coinLayout).setVisibility(View.INVISIBLE);

					dealFlipTrump(1);
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

					setScrollView();

					setTxtBounus(coin.getWager());

					redrawGuide();

					for (int i = 1; i < 6; i++) {
						// 手札のビュー(trumpView[index])に新しい札の情報を移す
						trumpView[i].setTrump(deck.trump.get(i - 1).getNumber(),
								deck.trump.get(i - 1).getSuit(),
								deck.trump.get(i - 1).getSerial(),
								deck.trump.get(i - 1).getColor());

					}

					// コイン操作画面を非表示にして、手札を表示する				
					findViewById(R.id.coinLayout).setVisibility(View.INVISIBLE);
					trumpView[0].setVisibility(View.GONE);
					dealFlipTrump(1);
				}

			}
		}
	};

	// プレイアウトボタンをクリックした時の処理
	OnClickListener msgListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (coinFlag) {
				skipFlag = true;
			}

			findViewById(R.id.msgLayout).setVisibility(View.INVISIBLE);
			findViewById(R.id.coinLayout).setVisibility(View.VISIBLE);

		}
	};

}
