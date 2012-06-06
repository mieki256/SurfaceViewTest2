package com.blawat2015.SurfaceViewSample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * タッチ要求アイコンの描画
 */
final class TouchReq {
	private static Paint paint = new Paint();
	private static Bitmap[] iconBmp = new Bitmap[2];
	private static int bmpId;
	private static int cnt;
	private static int x, y, w, h;

	private TouchReq() {
		init();
	}

	public static void init() {
		iconBmp[0] = Img.bmp[Img.ID_TOUCH0];
		iconBmp[1] = Img.bmp[Img.ID_TOUCH1];
		bmpId = 0;
		w = iconBmp[0].getWidth();
		h = iconBmp[0].getHeight();
		x = (GWk.DEF_SCR_W - w) /2;
		y = GWk.DEF_SCR_H - h - 8;
	}

	public static boolean onUpdate() {
		cnt++;
		if ( cnt > (GWk.FPS_VALUE / 2) ) {
			bmpId = (bmpId + 1) % 2;
			cnt = 0;
		}
		return true;
	}

	public static void onDraw(Canvas c) {
		paint.setAntiAlias(false);
		paint.setAlpha(255);
		c.drawBitmap(iconBmp[bmpId], x, y, paint);
	}
}
