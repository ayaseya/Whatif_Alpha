package com.example.whatif;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrumpView extends LinearLayout {

	private int number; // 1～13までのトランプの数字を格納する変数

	private String suit; // トランプの図柄の文字列を格納する変数

	private int fontSize;

	private int trumpWidth;
	private int trumpHeight;

	private int color;

	// LogCat用のタグを定数で定義する
	public static final String TAG = "Test";

	public TrumpView(Context context) {
		super(context);
	}

	public TrumpView(Context context, AttributeSet attrs) {
		super(context, attrs);

		View layout = LayoutInflater.from(context).inflate(R.layout.trump_view, this);
		// styleable から TypedArray の取得
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TrumpView);

		// レイアウト時に設定された属性値を取得する
		number = typedArray.getInt(R.styleable.TrumpView_number, 1);// 属性値が設定されていない時は第2引数が格納される
		suit = typedArray.getString(R.styleable.TrumpView_suit); // 属性値が設定されていない時はnullが格納される

		// 属性値が初期値でない場合、TextViewに変数の値を表示する
		if (number > 0) {
			TextView tv = (TextView) findViewById(R.id.TrumpNum);
			tv.setText(String.valueOf(number));
		}
		if (!"null".equals(suit)) {// !suit.equals(null)だと、suitがnullの場合に例外が発生する
			TextView tv1 = (TextView) findViewById(R.id.TrumpSuit1);
			TextView tv2 = (TextView) findViewById(R.id.TrumpSuit2);
			tv1.setText(suit);
			tv2.setText(suit);
		}
	}

	// TrumpViewとして定義したカスタムビューを返す
	public View addTrumpView(Deck deck, int index, Context context) {
		number = deck.trump.get(index).getNumber();
		suit = deck.trump.get(index).getSuit();
		View layout = LayoutInflater.from(context).inflate(R.layout.trump_view, this);

		TextView tv = (TextView) layout.findViewById(R.id.TrumpNum);
		tv.setText(String.valueOf(number));
		TextView tv1 = (TextView) layout.findViewById(R.id.TrumpSuit1);
		TextView tv2 = (TextView) layout.findViewById(R.id.TrumpSuit2);
		tv1.setText(suit);
		tv2.setText(suit);

		fixDisplay(context);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(trumpWidth, trumpHeight, Gravity.NO_GRAVITY);

		tv.setTextSize(fontSize);
		tv1.setTextSize(fontSize);
		tv2.setTextSize(fontSize * 2);

		color = Color.BLACK;
		tv.setTextColor(color);

		if (suit == context.getResources().getString(R.string.heart) ||
				suit == context.getResources().getString(R.string.diamond)) {
			color = Color.RED;

		}

		tv1.setTextColor(color);
		tv2.setTextColor(color);

		layout.setLayoutParams(params);

		return layout;
	}

	// 山札に使う空白のカスタムビューを返す
	public View addLayoutView(Context context) {

		View layout = LayoutInflater.from(context).inflate(R.layout.layout_view, this);

		fixDisplay(context);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(trumpWidth, trumpHeight, Gravity.CENTER);
		layout.setLayoutParams(params);

		return layout;
	}

	// 手札の配置に使う半透明のカスタムビューを返す
	public View addClearView(Context context) {

		View layout = LayoutInflater.from(context).inflate(R.layout.clear_view, this);

		fixDisplay(context);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(trumpWidth, trumpHeight, Gravity.CENTER);
		layout.setLayoutParams(params);

		return layout;
	}

	// トランプ裏面のカスタムビューを返す
	public View addBackView(Context context) {

		View layout = LayoutInflater.from(context).inflate(R.layout.back_view, this);

		fixDisplay(context);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(trumpWidth, trumpHeight, Gravity.CENTER);
		layout.setLayoutParams(params);

		return layout;
	}

	private void fixDisplay(Context context) {

		// Activityを継承していないため、getWindowManager()メソッドは利用できない
		// Displayクラスのインスタンスを取得するため
		// 引数のContextを使用してWindowManagerを取得する
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		// Displayインスタンスを取得する
		Display display = wm.getDefaultDisplay();

		int width = display.getWidth();
		int height = display.getHeight();

		// Log.v("Test","w=" + width + " h=" + height);

		trumpWidth = (width - 60) / 5;
		trumpHeight = (int) (trumpWidth * 1.5);

		if (width <= 240) {//ldpi（120dpi）240×320px
			fontSize = 16;
			//			Log.v(TAG, "ldpi（120dpi）240×320px" + width);
		} else if (240 < +width && +width <= 320) {//mdpi（160dpi）320×480px
			fontSize = 18;
			//			Log.v(TAG, "mdpi（160dpi）320×480px" + width);
		} else if (320 < +width && +width <= 480) {//hdpi（240dpi）480×800px
			fontSize = 20;
			//			Log.v(TAG, "hdpi（240dpi）480×800px" + width);
		} else if (480 < +width && +width <= 640) {//xhdpi（320dpi）640×960px
			fontSize = 22;
			//			Log.v(TAG, "xhdpi（320dpi）640×960px" + width);
		} else if (640 < +width) {//xxhdpi（480dpi）960×1440px
			fontSize = 22;
			//			Log.v(TAG, "xxhdpi（480dpi）960×1440px" + width);
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		super.setOnClickListener(l);
	}

	public int getNumber() {
		return number;
	}

	public String getSuit() {
		return suit;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setSuit(String suit) {
		this.suit = suit;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getTrumpWidth() {
		return trumpWidth;
	}

	public int getTrumpHeight() {
		return trumpHeight;
	}

}
