package com.example.whatif;

import java.util.ArrayList;

import android.content.Context;

public class Deck {
	ArrayList<Trump> trump;

	public Deck(Context context) {
		trump = new ArrayList<Trump>();

		for (int i = 1; i <= 13; i++) {
			trump.add(new Trump(i, context.getResources().getString(R.string.spade), i - 1));
		}
		for (int i = 1; i <= 13; i++) {
			trump.add(new Trump(i, context.getResources().getString(R.string.heart), i + 12));
		}
		for (int i = 1; i <= 13; i++) {
			trump.add(new Trump(i, context.getResources().getString(R.string.club), i + 25));
		}
		for (int i = 1; i <= 13; i++) {
			trump.add(new Trump(i, context.getResources().getString(R.string.diamond), i + 38));
		}

	}

}