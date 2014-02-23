package com.example.whatif;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class FlipAnimationListener implements AnimationListener {

	private int index;

	public FlipAnimationListener(int index) {
		super();
		this.index = index;
	}

	@Override
	public void onAnimationEnd(Animation arg0) {

	}

	@Override
	public void onAnimationRepeat(Animation arg0) {

	}

	@Override
	public void onAnimationStart(Animation arg0) {

	}

	public int getIndex() {
		return index;
	}

}
