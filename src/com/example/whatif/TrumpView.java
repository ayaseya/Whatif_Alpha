package com.example.whatif;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrumpView extends LinearLayout {

	private int number;
	private int suit;

	public TrumpView(Context context, AttributeSet attrs) {
		super(context, attrs);

		View layout = LayoutInflater.from(context).inflate(R.layout.trump_view, this);

	}

	public void setNumber(int number) {
		this.number = number;
		TextView tv = (TextView)findViewById(R.id.TrumpNum);
		tv.setText(String.valueOf(number));
	}

	public void setSuit(int suit) {
		suit = suit;
		TextView tv1 = (TextView)findViewById(R.id.TrumpSuit1);
		TextView tv2 = (TextView)findViewById(R.id.TrumpSuit2);
		tv1.setText(suit);
		tv2.setText(suit);
			
	}

}
