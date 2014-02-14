package com.example.whatif;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrumpView extends LinearLayout {

	private int number; // 1～13までのトランプの数字を格納する変数
	private int suit; // トランプの図柄のリソースID(R.string.xxx)を格納する変数
	private String str; // トランプの図柄の文字列を格納する変数

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
		str = typedArray.getString(R.styleable.TrumpView_suit); // 属性値が設定されていない時はnullが格納される

		// 属性値が初期値でない場合、TextViewに変数の値を表示する
		if (number > 0) {
			TextView tv = (TextView) findViewById(R.id.TrumpNum);
			tv.setText(String.valueOf(number));
		}
		if (!"null".equals(str)) {// !str.equals(null)だと、strがnullの場合に例外が発生する
			TextView tv1 = (TextView) findViewById(R.id.TrumpSuit1);
			TextView tv2 = (TextView) findViewById(R.id.TrumpSuit2);
			tv1.setText(str);
			tv2.setText(str);
		}
	}

	// 1～13までのトランプの数字を設定するメソッド
	public void setNumber(int number) {
		this.number = number;
		TextView tv = (TextView) findViewById(R.id.TrumpNum);
		tv.setText(String.valueOf(number));
	}

	// R.string.xxx(int型)のリソースIDを設定するメソッド
	public void setSuit(int suit) {
		this.suit = suit;
		TextView tv1 = (TextView) findViewById(R.id.TrumpSuit1);
		TextView tv2 = (TextView) findViewById(R.id.TrumpSuit2);
		tv1.setText(suit);
		tv2.setText(suit);

	}

	// TrumpViewとして定義したカスタムビューを返す
	public View addTrumpView(Deck deck, int x, Context context) {
		number = deck.trump.get(x).getNumber();
		str = deck.trump.get(x).getSuit();
		View layout = LayoutInflater.from(context).inflate(R.layout.trump_view, this);

		TextView tv = (TextView) layout.findViewById(R.id.TrumpNum);
		tv.setText(String.valueOf(number));
		TextView tv1 = (TextView) layout.findViewById(R.id.TrumpSuit1);
		TextView tv2 = (TextView) layout.findViewById(R.id.TrumpSuit2);
		tv1.setText(str);
		tv2.setText(str);
		
		
		// 画面解像度によってトランプの大きさを変更する
		// あわせてfontのサイズも変更する
		
		
		return layout;
	}

}
