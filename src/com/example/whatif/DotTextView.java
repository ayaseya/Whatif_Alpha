package com.example.whatif;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

public class DotTextView extends TextView {

	private int fontSize;

	public DotTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Activityを継承していないため、getWindowManager()メソッドは利用できない
		// Displayクラスのインスタンスを取得するため
		// 引数のContextを使用してWindowManagerを取得する
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		// Displayインスタンスを取得する
		Display display = wm.getDefaultDisplay();

		int width = display.getWidth();
		int height = display.getHeight();

		// Log.v("Test","w=" + width + " h=" + height);

	

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
			fontSize = 18;
			//			Log.v(TAG, "xxhdpi（480dpi）960×1440px" + width);
		}

		setTextSize(fontSize);
		
		Typeface typeface = Typeface.createFromAsset(
				context.getAssets(),
				"PixelMplus12-Regular.ttf");
		setTypeface(typeface);

	}

}
