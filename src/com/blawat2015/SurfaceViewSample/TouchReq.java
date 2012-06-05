package com.blawat2015.SurfaceViewSample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * タッチ要求アイコンの描画
 */
public class TouchReq {
	static TouchReq instance = new TouchReq();

	private Paint paint = new Paint();
	private Bitmap[] iconBmp = new Bitmap[2];
	private int bmpId;
	private int cnt;
	private int x, y, w, h;

	private TouchReq() {
		ImgMgr img = ImgMgr.getInstance();
		iconBmp[0] = img.bmp[ImgMgr.ID_TOUCH0];
		iconBmp[1] = img.bmp[ImgMgr.ID_TOUCH1];
		bmpId = 0;
		w = iconBmp[0].getWidth();
		h = iconBmp[0].getHeight();
		x = (GWk.DEF_SCR_W - w) /2;
		y = GWk.DEF_SCR_H - h - 8;
	}

	public static TouchReq getInstance() {
		return instance;
	}

	public boolean onUpdate() {
		cnt++;
		if ( cnt > (GWk.FPS_VALUE / 2) ) {
			bmpId = (bmpId + 1) % 2;
			cnt = 0;
		}
		return true;
	}

	public void onDraw(Canvas c) {
		paint.setAntiAlias(false);
		paint.setAlpha(255);
		c.drawBitmap(iconBmp[bmpId], x, y, paint);
	}
}
