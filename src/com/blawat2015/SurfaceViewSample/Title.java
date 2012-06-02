package com.blawat2015.SurfaceViewSample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * タイトル処理クラス
 */
public class Title extends Task {
	GWk gw;
	int x, y, ox, oy, bmpw, bmph, cnt;
	Rect src;
	RectF dst;
	Bitmap bmp;
	Bitmap sndbmp;
	Paint paint = new Paint();

	public Title() {
		gw = GWk.getInstance();
		bmp = gw.img.bmp[ImgMgr.ID_LOGO_TITLE];
		bmpw = bmp.getWidth();
		bmph = bmp.getHeight();
		ox = GWk.defScrW / 2;
		oy = 130;
		cnt = 0;
		src = new Rect();
		dst = new RectF();
	}

	@Override
	public boolean onUpdate() {
		float ww = 32f;
		float wf = ww + (float) (ww * Math.sin(Math.toRadians(cnt)));
		float hf = wf * bmph / bmpw;
		float sw = (bmpw / 2 - wf);
		float sh = (bmph / 2 - hf);
		src.set(0, 0, bmpw, bmph);
		dst.set(ox - sw, oy - sh, ox + sw, oy + sh);
		cnt += 4;
		if (gw.touchEnable) {
			// 画面をタッチされた
			gw.touchX = gw.touchY = 0;
			gw.drawTouchAlpha = 0;
			return false;
		}
		return true;
	}

	@Override
	public void onDraw(Canvas c) {
		if (!gw.layerDrawEnable[2]) return;
		paint.setAntiAlias(true);
		paint.setAlpha(255);
		c.drawBitmap(bmp, src, dst, paint);
	}

};

