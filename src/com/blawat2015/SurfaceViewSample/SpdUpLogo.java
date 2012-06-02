package com.blawat2015.SurfaceViewSample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * SpeedUpロゴ表示用クラス
 */
public class SpdUpLogo extends Task {
	int step = 0;
	int cnt = 0;
	int x = 0;
	int y = 0;
	int dy = 0;
	int alpha = 0;
	Bitmap bmp = null;
	GWk gw;
	Paint paint = new Paint();

	/**
	 * コンストラクタ
	 */
	public SpdUpLogo() {
		gw = GWk.getInstance();
		init();
	}

	/**
	 * 初期化処理
	 */
	public void init() {
		bmp = gw.img.bmp[ImgMgr.ID_LOGO_SPEEDUP];
		step = 0;
		cnt = 0;
		x = (GWk.defScrW - bmp.getWidth()) / 2;
		y = GWk.defScrH / 2;
		dy = -1;
		alpha = 0;
	}

	/**
	 * 描画開始
	 */
	public void setDispEnable() {
		if (step <= 1) step = 2;
	}

	/**
	 * 更新処理
	 *
	 * @return
	 */
	@Override
	public boolean onUpdate() {
		switch (step) {
		case 0:
			init();
			step++;
			break;

		case 1:
			break;

		case 2:
			y = GWk.defScrH / 2;
			alpha = 0;
			step++;
			break;

		case 3:
			y += dy;
			alpha += 24;
			if (alpha >= 255) {
				alpha = 255;
				cnt = (int) GWk.FPS_VALUE;
				step++;
			}
			break;
		case 4:
			y += dy;
			if (--cnt <= 0) {
				step++;
			}
			break;
		case 5:
			y += dy;
			alpha -= 24;
			if (alpha <= 0) init();
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * 描画処理
	 *
	 * @param c
	 *            Canvas
	 */
	@Override
	public void onDraw(Canvas c) {
		if (step > 2) {
			paint.setAlpha(alpha);
			paint.setAntiAlias(true);
			c.drawBitmap(bmp, x, y, paint);
		}
	}
}

