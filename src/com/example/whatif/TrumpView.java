package com.example.whatif;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrumpView extends LinearLayout {

	private int number; // 1～13までのトランプの数字を格納する変数

	private String suit; // トランプの図柄の文字列を格納する変数
	private CheckDevice cd;
	private int size;

	private int trumpWidth;
	private int trumpHeight;

	private int color;

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
	public View addTrumpView(Deck deck, int x, Context context) {
		number = deck.trump.get(x).getNumber();
		suit = deck.trump.get(x).getSuit();
		View layout = LayoutInflater.from(context).inflate(R.layout.trump_view, this);

		TextView tv = (TextView) layout.findViewById(R.id.TrumpNum);
		tv.setText(String.valueOf(number));
		TextView tv1 = (TextView) layout.findViewById(R.id.TrumpSuit1);
		TextView tv2 = (TextView) layout.findViewById(R.id.TrumpSuit2);
		tv1.setText(suit);
		tv2.setText(suit);

		fixDisplay(context);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(trumpWidth, trumpHeight);

		tv.setTextSize(size / 6);
		tv1.setTextSize(size / 6);
		tv2.setTextSize(size / 4);

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

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(trumpWidth, trumpHeight);
		layout.setLayoutParams(params);

		return layout;
	}

	// 山札に使う空白のカスタムビューを返す
	public View addClearView(Context context) {

		View layout = LayoutInflater.from(context).inflate(R.layout.clear_view, this);

		fixDisplay(context);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(trumpWidth, trumpHeight);
		layout.setLayoutParams(params);

		return layout;
	}
	
	// トランプ裏面のカスタムビューを返す
	public View addBackView(Context context) {

		View layout = LayoutInflater.from(context).inflate(R.layout.back_view, this);

		fixDisplay(context);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(trumpWidth, trumpHeight);
		layout.setLayoutParams(params);

		return layout;
	}

	private void fixDisplay(Context context) {

		cd = new CheckDevice(context);

		size = (cd.getWidth() - 60) / 5;
		trumpWidth = size;
		trumpHeight = (int) (size * 1.5);

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

	public int getTrumpWidth() {
		return trumpWidth;
	}

	public int getTrumpHeight() {
		return trumpHeight;
	}

}
