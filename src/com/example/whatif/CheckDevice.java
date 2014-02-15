package com.example.whatif;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

public class CheckDevice {

	private int width;
	private int height;

	@SuppressWarnings("deprecation")
	public CheckDevice(Context context) {

		// Activityを継承していないため、getWindowManager()メソッドは利用できない
		// Displayクラスのインスタンスを取得するため
		// 引数のContextを使用してWindowManagerを取得する
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		// Displayインスタンスを取得する
		Display display = wm.getDefaultDisplay();

		width = display.getWidth();
		height = display.getHeight();

		// Log.v("Test","w=" + width + " h=" + height);

	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
