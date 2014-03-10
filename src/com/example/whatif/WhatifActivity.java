package com.example.whatif;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
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
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
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

	private Handler handler = new Handler();

	private int counterWidth = 0;
	private int counterHeight = 0;

	private int margin = 10;

	private Configuration config;

	private TextView counterMsg;

	private TextView txt;

	private int[] idList;

	private int trumpViewWidth;

	private int trumpViewHeight;

	private float ratio;

	private float scale;

	private int fontSize = 16;

	/* ********** ********** ********** ********** */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v(TAG, "/* ********** ********** ********** ********** */");

		requestWindowFeature(Window.FEATURE_NO_TITLE); // タイトルバーを非表示にする
		//		setContentView(R.layout.activity_whatif);
		setContentView(R.layout.whatif_layout);

		findViewById(R.id.black).bringToFront();

		//端末の画面の向きを取得するためリソースを取得する
		config = getResources().getConfiguration();

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
		ratio = _dpi / 160f;

		scale = getResources().getDisplayMetrics().scaledDensity;

		width = display.getWidth();
		height = display.getHeight();

		if (ratio == 0.75) {
			dpi = DPI.ldpi;
		} else if (ratio == 1.0) {
			dpi = DPI.mdpi;
		} else if (ratio == 1.5) {
			dpi = DPI.hdpi;
		} else if (ratio == 2.0) {
			dpi = DPI.xhdpi;
		} else if (ratio >= 3.0) {
			dpi = DPI.xxhdpi;
		}
		Log.v(TAG, "> " + dpi + "(" + _dpi + "dpi)" + " density=" + ratio + " w=" + width + " h=" + height + " s=" + scale);

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
		findViewById(R.id.dealBtn).setOnClickListener(dealBtnListener);

		findViewById(R.id.msgLayout).setOnClickListener(msgListener);

		wagerView = (TextView) findViewById(R.id.wager);//
		winView = (TextView) findViewById(R.id.win);//
		paidView = (TextView) findViewById(R.id.paid);//
		creditView = (TextView) findViewById(R.id.credit);//

		// 非表示
		findViewById(R.id.msgLayout).setVisibility(View.INVISIBLE);

		counterMsg = (TextView) findViewById(R.id.counterMsg);

		trumpViewWidth = 60;
		trumpViewHeight = 90;

		fixFont();

		//		Log.v(TAG, "onCreate()");
	}// onCreate()

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		// Portrait(縦長)
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			vertical();

		}
		// Landscape(横長)
		else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			horizontal();
		}

		//		fixFont();
		//		Log.v(TAG, "onWindowFocusChanged()");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		//		Log.v(TAG, "onRestart()");
	}

	@Override
	protected void onStart() {
		super.onStart();
		//		Log.v(TAG, "onStart()");
	}

	@Override
	protected void onResume() {
		super.onResume();
		//		Log.v(TAG, "onResume()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		//		Log.v(TAG, "onPause()");
	}

	@Override
	protected void onStop() {
		super.onStop();
		//		Log.v(TAG, "onStop()");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//		Log.v(TAG, "onDestroy()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.whatif, menu);

		return true;
	}// onCreateOptionsMenu()

	/* ********** ********** ********** ********** */

	private void resizeTrump() {

		for (int i = 0; i <= 5; i++) {
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(trumpViewWidth, trumpViewHeight);
			int[] rules = ((LayoutParams) trumpBackView[i].getLayoutParams()).getRules();

			//		Log.v(TAG, "rules[]="
			//				+ rules[0] + ","
			//				+ rules[1] + ","
			//				+ rules[2] + ","
			//				+ rules[3] + ","
			//				+ rules[4] + ","
			//				+ rules[5] + ","
			//				+ rules[6] + ","
			//				+ rules[7] + ","
			//				+ rules[8] + ","
			//				+ rules[9] + ","
			//				+ rules[10] + ","
			//				+ rules[11] + ","
			//				+ rules[12] + ","
			//				+ rules[13] + ","
			//				+ rules[14] + ","
			//				+ rules[15]);

			if (rules[0] != 0) {// -1ならtrue
				lp.addRule(RelativeLayout.LEFT_OF, rules[0]);
			}
			if (rules[1] != 0) {
				lp.addRule(RelativeLayout.RIGHT_OF, rules[1]);
			}
			if (rules[2] != 0) {
				lp.addRule(RelativeLayout.ABOVE, rules[2]);
			}
			if (rules[3] != 0) {
				lp.addRule(RelativeLayout.BELOW, rules[3]);
			}
			if (rules[4] != 0) {
				lp.addRule(RelativeLayout.ALIGN_BASELINE, rules[4]);
			}
			if (rules[5] != 0) {
				lp.addRule(RelativeLayout.ALIGN_LEFT, rules[5]);
			}
			if (rules[6] != 0) {
				lp.addRule(RelativeLayout.ALIGN_TOP, rules[6]);
			}
			if (rules[7] != 0) {
				lp.addRule(RelativeLayout.ALIGN_RIGHT, rules[7]);
			}
			if (rules[8] != 0) {
				lp.addRule(RelativeLayout.ALIGN_BOTTOM, rules[8]);
			}
			if (rules[9] != 0) {
				lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, rules[9]);
			}
			if (rules[10] != 0) {
				lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, rules[10]);
			}
			if (rules[11] != 0) {
				lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, rules[11]);
			}
			if (rules[12] != 0) {
				lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, rules[12]);
			}
			if (rules[13] != 0) {
				lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
			}
			if (rules[14] != 0) {
				lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
			}
			if (rules[15] != 0) {
				lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			}

			if (config.orientation == Configuration.ORIENTATION_PORTRAIT) { // 縦画面でのマージンを設定する
				if (i == 0) {
					lp.topMargin = 15;
					TextView selectAble = (TextView) findViewById(R.id.selectAble1);
					if ((trumpViewHeight + 20 + selectAble.getHeight())
					< findViewById(R.id.ruler).getHeight()) {
						lp.topMargin = (findViewById(R.id.ruler).getHeight() -
								(trumpViewHeight + 20 + selectAble.getHeight())) / 4;
					}
				} else if (i == 3) {
					lp.topMargin = 15;

					TextView selectAble = (TextView) findViewById(R.id.selectAble1);
					if ((trumpViewHeight + 20 + selectAble.getHeight())
					< findViewById(R.id.ruler).getHeight()) {
						lp.topMargin = (findViewById(R.id.ruler).getHeight() -
								(trumpViewHeight + 20 + selectAble.getHeight())) / 4;
					}

					lp.leftMargin = margin;
					lp.rightMargin = margin;
				} else if (i == 1) {
					lp.rightMargin = margin;
				} else if (i == 5) {
					lp.leftMargin = margin;
				}
			} else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横画面でのマージンを設定する
				margin = (width - (trumpViewWidth * 5)) / 6;
				TextView selectAble = (TextView) findViewById(R.id.selectAble1);

				if (i == 0) {
					lp.topMargin = 5;
				} else if (i == 3) {
					lp.topMargin = 10;

					if ((height / 2) < findViewById(R.id.ruler).getHeight()) {
						lp.topMargin = (findViewById(R.id.ruler).getHeight() -
								(trumpViewHeight + 20 + selectAble.getHeight())) / 2;
					}

				} else if (i == 2) {
					lp.rightMargin = margin;
				} else if (i == 1) {
					lp.rightMargin = margin;
				} else if (i == 4) {
					lp.leftMargin = margin;
				} else if (i == 5) {
					lp.leftMargin = margin;
				}

			}
			trumpBackView[i].setLayoutParams(lp);

		}

	}

	private void fixFont() {

		// Portrait(縦長)
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			fontSize = (int) ((width / scale) / 20);

		}
		// Landscape(横長)
		else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			fontSize = (int) ((width / scale) / 40);
		}

		Log.v(TAG, "fontSize=" + fontSize);

		String[] itemList = res.getStringArray(R.array.textView);

		TextView[] textView = new TextView[itemList.length];

		idList = new int[itemList.length];
		//配列データからIDの取得
		for (int i = 0; i < itemList.length; i++) {
			idList[i] = res.getIdentifier(itemList[i], "id", getPackageName());
			textView[i] = (TextView) findViewById(idList[i]);
			if (textView[i] != null) {
				//				textView[i].setTextSize(TypedValue.COMPLEX_UNIT_PX ,fontSize);
				textView[i].setTextSize(fontSize);
			}

		}
	}

	// トランプ1枚をアニメーションの処理
	private void FlipTrump(final int index) {

		// アニメーション中にクリックできないようfalseに変更する
		flipAnimFlag = false;
		// 現在表示されているトランプ画像を非表示にする
		trumpBackView[index].setVisibility(View.INVISIBLE);

		// Y軸回転(0～90度)
		Rotate3dAnimation rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, true);
		rotation.setDuration(timeFlip);
		trumpBackView[index].startAnimation(rotation);
		rotation.setAnimationListener(new TrumpAnimationListener(index) {
			// 裏面が回転し終わり表面が回転し始める
			@Override
			public void onAnimationEnd(Animation animation) {

				// Y軸回転(270～360度)
				Rotate3dAnimation rotation = new Rotate3dAnimation(270, 360, centerX, centerY, 0f, false);

				rotation.setDuration(timeFlip);
				trumpView[index].startAnimation(rotation);
				rotation.setAnimationListener(new TrumpAnimationListener(index) {
					@Override
					public void onAnimationEnd(Animation animation) {
						// アニメーションの終了
						trumpView[index].setVisibility(View.VISIBLE);
						flipAnimFlag = true;
					}
				});
			}
		});

	}

	// トランプ5枚を順番に捲るアニメーションの処理
	private void dealFlipTrump(final int index) {

		// アニメーション中にクリックできないようfalseに変更する
		dealAnimFlag = false;
		// 現在表示されているトランプ画像を非表示にする
		trumpBackView[index].setVisibility(View.INVISIBLE);

		// Y軸回転(0～90度)
		Rotate3dAnimation rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, true);

		rotation.setDuration(timeDeal);
		trumpBackView[index].startAnimation(rotation);
		rotation.setAnimationListener(new TrumpAnimationListener(index) {
			// 裏面が回転し終わり表面が回転し始める
			@Override
			public void onAnimationEnd(Animation animation) {

				// Y軸回転(270～360度)
				Rotate3dAnimation rotation = new Rotate3dAnimation(270, 360, centerX, centerY, 0f, false);

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
				if (counter == 0) {
					bonusScroll1.fullScroll(ScrollView.FOCUS_DOWN);// 一番下までスクロールする
				} else {
					bonusScroll1.scrollBy(0, scrollHeight * (50 - counter));

					int cChainId = res.getIdentifier("cChain" + counter, "id", getPackageName());

					((TextView) findViewById(cChainId)).setBackgroundColor(0xFFFF0000);

				}
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

				if (counter == 0) {
					paysScroll1.fullScroll(ScrollView.FOCUS_DOWN);// 一番下までスクロールする
				} else {
					paysScroll1.scrollBy(0, scrollHeight * (50 - counter));

					int cBonusId = res.getIdentifier("cBonus" + counter, "id", getPackageName());
					((TextView) findViewById(cBonusId)).setTextColor(0xFFFF0000);
					((TextView) findViewById(cBonusId)).setBackgroundColor(0xFFFFFFFF);
				}
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

	// 手札から場札へトランプが移動する処理
	private void moveTrump(final int index) {

		// アニメーション中にクリックできないようfalseに変更する
		moveAnimFlag = false;

		// アニメーションするViewを最前面に移動する
		trumpView[index].bringToFront();

		TranslateAnimation translate = new TranslateAnimation(
				0, trumpView[0].getLeft() - trumpView[index].getLeft(),
				0, trumpView[0].getTop() - trumpView[index].getTop());

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
					selectAble();
					GameOver();

				} else {
					trumpView[index].setVisibility(View.INVISIBLE);
					trumpView[index].setTrump(0, null, 0, 0);// 非表示になった手札には空の情報を入れておく(selectAble()に反応しないよう)

					Resources res = getResources();
					int id = res.getIdentifier("selectAble" + index, "id", getPackageName());
					TextView view = (TextView) findViewById(id);
					view.setTextColor(0x00FFFF00);
					selectAble();
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
	private void boldNum(int x) {
		Resources res = getResources();
		int guideId = res.getIdentifier("card" + x, "id", getPackageName());
		guideView = (TextView) findViewById(guideId);
		// TextViewの文字色を変更する（16進数で頭の2bitがアルファ値、00で透過率100%）
		guideView.setTextColor(0xFF00BFFF);
		// フォントのスタイル（太字、斜線など）を変更する
		guideView.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
	}// Card.BoldNum_**********

	// yellowNum関数…場札に置いたトランプの数字をガイド上で黄色にする処理
	private void yellowNum(int x) {
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
	private void deleteNum(int x) {
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

	private void redrawGuide() {
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

		final Animation in = AnimationUtils.loadAnimation(this, R.anim.down_in);
		final Animation out = AnimationUtils.loadAnimation(this, R.anim.down_out);

		//		// Portrait(縦長)
		//		if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
		//			switch (dpi) {
		//			case ldpi:
		//				txtSwitchFontSize = 24;
		//				break;
		//			case mdpi:
		//				txtSwitchFontSize = 32;
		//				break;
		//			case hdpi:
		//				txtSwitchFontSize = 36;
		//				break;
		//			case xhdpi:
		//				txtSwitchFontSize = 42;
		//				break;
		//			case xxhdpi:
		//				txtSwitchFontSize = 48;
		//				break;
		//			}
		//		}
		//		// Landscape(横長)
		//		else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
		//			switch (dpi) {
		//			case ldpi:
		//				txtSwitchFontSize = 20;
		//				break;
		//			case mdpi:
		//				txtSwitchFontSize = 20;
		//				break;
		//			case hdpi:
		//				txtSwitchFontSize = 24;
		//				break;
		//			case xhdpi:
		//				txtSwitchFontSize = 24;
		//				break;
		//			case xxhdpi:
		//				txtSwitchFontSize = 32;
		//				break;
		//			}
		//		}

		count = (TextSwitcher) findViewById(R.id.counterView);
		counterWidth = count.getWidth();
		counterHeight = count.getHeight();

		count.setInAnimation(in);
		count.setOutAnimation(out);
		count.setFactory(this);
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

	private void redrawCoin() {
		wagerView.setText(String.valueOf(coin.getWager()));
		winView.setText(String.valueOf(0));
		paidView.setText(String.valueOf(0));
		creditView.setText(String.valueOf(coin.getCredit()));
	}

	// cuCoin関数…cu = Count Upの略称、払い戻し時にコインの枚数が1枚ずつ
	// 増減する様子を表示する処理、引数は増減する枚数を渡す
	private void cuCoin(final int x) {
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

	// 払い戻し金を表示する処理
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

			for (int i = 1; i <= 5; i++) {
				id = res.getIdentifier("selectAble" + i, "id", getPackageName());
				view = (TextView) findViewById(id);

				if (trumpView[0].getSuit().equals(trumpView[i].getSuit())) {// スート(図柄)が一致した場合
					view.setTextColor(0xFFFFFF00);
				} else if (trumpView[0].getNumber() == trumpView[i].getNumber()) {// 数字が一致した場合
					view.setTextColor(0xFFFFFF00);
				} else {// 一致しなかった場合は透明にする
					view.setTextColor(0x00FFFF00);
				}

			}
		}

	}

	// ////////////////////////////////////////////////
	// Vertical and Horizontal
	// ////////////////////////////////////////////////

	private void vertical() {
		// 機種によって複数回onWindowFocusChanged()が呼ばれる場合があるので
		// FlagでonCreate()の実行後に一度だけ実行されるように記述する
		if (replaceFlag) {
			// ステータスバーの高さを取得
			//			Rect rect = new Rect();
			//			getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
			//			statusbarHeight = rect.top;
			//			Log.v(TAG, "ステータスバー height=" + statusbarHeight);

			// 縦画面の時のトランプのサイズを決定する
			trumpViewWidth = width / 6;
			trumpViewHeight = (int) (trumpViewWidth * 1.5);

			Log.v(TAG, "trump w=" + trumpViewWidth + " h=" + trumpViewHeight);

			//トランプの背景画像のサイズを変更する
			resizeTrump();

			// trumpViewの縦横を画像に合わせる
			for (int i = 0; i <= 5; i++) {
				//				trumpView[i].getLayoutParams().width = trumpBackView[0].getWidth();
				//				trumpView[i].getLayoutParams().height = trumpBackView[0].getHeight();
				trumpView[i].getLayoutParams().width = trumpViewWidth;
				trumpView[i].getLayoutParams().height = trumpViewHeight;
				trumpView[i].requestLayout();
			}

			// trumpViewのフォントサイズを変更する
			int fontSize = 10;

			fontSize = (int) ((trumpViewHeight / scale) / 4);
			fontSize -= fontSize / 10;
			//			Log.v(TAG, "TEST"+fontSize);

			//			switch (dpi) {
			//			case ldpi:
			//				fontSize = 20;
			//				break;
			//			case mdpi:
			//				fontSize = 20;
			//				break;
			//			case hdpi:
			//				fontSize = 20;
			//				break;
			//			case xhdpi:
			//				fontSize = 20;
			//				break;
			//			case xxhdpi:
			//				fontSize = 20;
			//				break;
			//			}

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
			//			int trumpWidth = trumpBackView[0].getWidth();
			//			Log.v(TAG, "trumpWidth=" + trumpWidth);
			//			int trumpHeight = trumpBackView[0].getHeight();
			//			Log.v(TAG, "trumpHeight=" + trumpHeight);
			//
			//			margin = (Math.max(trumpWidth * 5, width) - Math.min(trumpWidth * 5, width)) / 6;
			//			Log.v(TAG, "margin=" + margin);

			//			// trumpBackViewからマージンを取得
			//			trumpBackView[2].getLayoutParams();
			//			// マージン移動用のパラメータを取得する
			//			MarginLayoutParams marginParams2 = (MarginLayoutParams) trumpBackView[2].getLayoutParams();
			//			// 移動させたい距離に変更
			//			marginParams2.rightMargin += margin;
			//			// trumpBackViewへ反映
			//			trumpBackView[2].setLayoutParams(marginParams2);
			//
			//			trumpBackView[1].getLayoutParams();
			//			MarginLayoutParams marginParams1 = (MarginLayoutParams) trumpBackView[1].getLayoutParams();
			//			marginParams1.rightMargin += margin;
			//			trumpBackView[1].setLayoutParams(marginParams1);
			//
			//			trumpBackView[4].getLayoutParams();
			//			MarginLayoutParams marginParams4 = (MarginLayoutParams) trumpBackView[4].getLayoutParams();
			//			marginParams4.leftMargin += margin;
			//			trumpBackView[4].setLayoutParams(marginParams4);
			//
			//			trumpBackView[5].getLayoutParams();
			//			MarginLayoutParams marginParams5 = (MarginLayoutParams) trumpBackView[5].getLayoutParams();
			//			marginParams5.leftMargin += margin;
			//			trumpBackView[5].setLayoutParams(marginParams5);

			// Y軸回転用の変数を取得する
			centerX = trumpBackView[0].getWidth() / 2;
			centerY = trumpBackView[0].getLayoutParams().height;

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

			//カウンター処理
			txtSwitchOn();

			// 起動時のみ必要な処理が終了したのでフラグを変更する
			replaceFlag = false;

		}
	}

	private void horizontal() {
		// 機種によって複数回onWindowFocusChanged()が呼ばれる場合があるので
		// FlagでonCreate()の実行後に一度だけ実行されるように記述する
		if (replaceFlag) {
			// ステータスバーの高さを取得
			//			Rect rect = new Rect();
			//			getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
			//			statusbarHeight = rect.top;
			//			Log.v(TAG, "ステータスバー height=" + statusbarHeight);

			// 横画面の時のトランプのサイズを決定する
			TextView selectAble = (TextView) findViewById(R.id.selectAble1);

			if ((findViewById(R.id.ruler).getHeight()) > height / 2) {

				trumpViewHeight = height
						- (findViewById(R.id.ruler).getHeight())
						- 10 // マージン
						- selectAble.getLineHeight() //
						- 10; // マージン

			} else {
				trumpViewHeight = (findViewById(R.id.ruler).getHeight())
						- 10 // マージン
						- selectAble.getLineHeight() //
						- 10;
			}

			trumpViewWidth = (int) (trumpViewHeight / 1.5);
			Log.v(TAG, "trump w=" + trumpViewWidth + " h=" + trumpViewHeight);
			//トランプの背景画像のサイズを変更する
			resizeTrump();

			// trumpViewの縦横を画像に合わせる
			for (int i = 0; i <= 5; i++) {
				trumpView[i].getLayoutParams().width = trumpViewWidth;
				trumpView[i].getLayoutParams().height = trumpViewHeight;
				trumpView[i].requestLayout();
			}

			// trumpViewのフォントサイズを変更する
			int fontSize = 30;

			fontSize = (int) ((trumpViewHeight / scale) / 4);
			fontSize -= fontSize / 10;

			//			switch (dpi) {
			//			case ldpi:
			//				fontSize = 30;
			//				break;
			//			case mdpi:
			//				fontSize = 30;
			//				break;
			//			case hdpi:
			//				fontSize = 30;
			//				break;
			//			case xhdpi:
			//				fontSize = 30;
			//				break;
			//			case xxhdpi:
			//				fontSize = 30;
			//				break;
			//			}

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
			//			int trumpWidth = trumpBackView[0].getWidth();
			//			Log.v(TAG, "trumpWidth=" + trumpWidth);
			//			int trumpHeight = trumpBackView[0].getHeight();
			//			Log.v(TAG, "trumpHeight=" + trumpHeight);
			//
			//			margin = (Math.max(trumpWidth * 5, width) - Math.min(trumpWidth * 5, width)) / 6;
			//			Log.v(TAG, "margin=" + margin);
			//
			//			trumpBackView[2].getLayoutParams();
			//			// trumpBackViewからマージンを取得
			//			MarginLayoutParams marginParams2 = (MarginLayoutParams) trumpBackView[2].getLayoutParams();
			//			// 移動させたい距離に変更
			//			marginParams2.rightMargin += margin;
			//			// trumpBackViewへ反映
			//			trumpBackView[2].setLayoutParams(marginParams2);
			//
			//			trumpBackView[1].getLayoutParams();
			//			MarginLayoutParams marginParams1 = (MarginLayoutParams) trumpBackView[1].getLayoutParams();
			//			marginParams1.rightMargin += margin;
			//			trumpBackView[1].setLayoutParams(marginParams1);
			//
			//			trumpBackView[4].getLayoutParams();
			//			MarginLayoutParams marginParams4 = (MarginLayoutParams) trumpBackView[4].getLayoutParams();
			//			marginParams4.leftMargin += margin;
			//			trumpBackView[4].setLayoutParams(marginParams4);
			//
			//			trumpBackView[5].getLayoutParams();
			//			MarginLayoutParams marginParams5 = (MarginLayoutParams) trumpBackView[5].getLayoutParams();
			//			marginParams5.leftMargin += margin;
			//			trumpBackView[5].setLayoutParams(marginParams5);

			// Y軸回転用の変数を取得する
			centerX = trumpBackView[0].getWidth() / 2;
			centerY = trumpBackView[0].getLayoutParams().height;

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

			//カウンター処理
			txtSwitchOn();

			// makeView()内でフォントサイズを変更すると
			// TextSwitchewと同じlinearlayoutに配置されている子が
			// 文字の高さ分の空白が生まれてしまう(layout_Gravityを動的に変更しても改善せず)
			// 代価案としてマージンを詰める
			counterMsg.getLayoutParams();
			// counterMsgからマージンを取得
			MarginLayoutParams counterMsgParams = (MarginLayoutParams) counterMsg.getLayoutParams();
			// 移動させたい距離に変更
			counterMsgParams.topMargin -= txtSwitchFontSize;

			// counterMsgへ反映
			counterMsg.setLayoutParams(counterMsgParams);

			// 起動時のみ必要な処理が終了したのでフラグを変更する
			replaceFlag = false;

		}
	}

	// ////////////////////////////////////////////////
	// GameFlag
	// ////////////////////////////////////////////////

	// GameOver関数…ゲームフラグの管理
	private void GameOver() {
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

	private void GameClear() {

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
		txt = new TextView(this);

		txt.setHeight(counterHeight);

		if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			txtSwitchFontSize = (int) ((counterHeight / scale) * 0.7);
		}
		else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			txtSwitchFontSize = (int) ((counterHeight / scale) * 0.8);
		}

		txt.setGravity(Gravity.CENTER);
		txt.setTextColor(0xFFFFFFFF);
		txt.setTextSize(txtSwitchFontSize);
		return txt;
	}

	// ////////////////////////////////////////////////
	// 端末画面が回転した時の処理
	// ////////////////////////////////////////////////
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("COUNTER", counter);
		outState.putInt("COUNTER", counter);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		counter = savedInstanceState.getInt("COUNTER");

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

	// ディールボタンをクリックした時の処理
	OnClickListener dealBtnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (!coinFlag) {
				// 最小BET数を満たしていたらゲーム開始
				if (coin.getWager() >= coin.getMinbet() && counter == 0) {

					setTxtBounus(coin.getWager());

					// コイン操作画面を非表示にして、手札を表示する				
					findViewById(R.id.btnLayout).setVisibility(View.INVISIBLE);

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
					findViewById(R.id.btnLayout).setVisibility(View.INVISIBLE);
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
			findViewById(R.id.btnLayout).setVisibility(View.VISIBLE);

		}
	};

}
