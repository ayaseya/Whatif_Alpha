package com.example.whatif;

import java.util.ArrayList;

import android.content.Context;

public class Deck {
	ArrayList<Trump> trump;

	public Deck(Context context) {
		trump = new ArrayList<Trump>();

		for (int i = 1; i <= 13; i++) {
			trump.add(new Trump(i,context.getResources().getString(R.string.spade) ));
		}
		for (int i = 1; i <= 13; i++) {
			trump.add(new Trump(i,context.getResources().getString(R.string.heart) ));
		}
		for (int i = 1; i <= 13; i++) {
			trump.add(new Trump(i,context.getResources().getString(R.string.club) ));
		}
		for (int i = 1; i <= 13; i++) {
			trump.add(new Trump(i,context.getResources().getString(R.string.diamond) ));
		}
		
		
	}

}