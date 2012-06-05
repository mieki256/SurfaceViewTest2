package com.blawat2015.SurfaceViewSample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * ステージクリア処理用クラス
 */
public class StageClear extends Task {
	int step = 0;
	int count = 0;
	int dispMiss = 0;
	int dispFrame = 0;
	Paint paint = new Paint();
	GWk gw;
	ImgMgr img;

	public StageClear() {
		init(0, 0);
	}

	/**
	 * 初期化処理
	 */
	public void init(int missValue, int frameValue) {
		gw = GWk.getInstance();
		img = ImgMgr.getInstance();
		step = 0;
		count = (int) GWk.FPS_VALUE * 3 / 4;
		dispMiss = missValue;
		dispFrame = frameValue;
	}

	/**
	 * 更新処理
	 */
	@Override
	public boolean onUpdate() {
		boolean result = true;
		switch (step) {
		case 0:
			// 一定時間待つ
			if (--count <= 0) {
				gw.clearTouchInfo();
				SndMgr.getInstance().playSe(SndMgr.SE_STGCLR);
				step++;
			}
			break;

		case 1:
			// 画面がタッチされるまで待つ
			TouchReq.getInstance().onUpdate();
			if (gw.touchEnable) step++;
			break;
		case 2:
			result = false;
			break;

		default:
			break;
		}
		return result;
	}

	/**
	 * 描画処理
	 */
	@Override
	public void onDraw(Canvas c) {
		if (step == 1) {
			// ロゴ描画
			paint.setAlpha(255);
			paint.setColor(Color.BLACK);
			paint.setAntiAlias(false);
			Bitmap b = img.bmp[ImgMgr.ID_LOGO_CLEAR];
			int x = (GWk.DEF_SCR_W - b.getWidth()) / 2;
			int y = 120;
			c.drawBitmap(img.bmp[ImgMgr.ID_LOGO_CLEAR], x, y, paint);

			// 文字を載せる背景矩形を描画
			paint.setAlpha(255);
			paint.setStyle(Style.FILL);
			paint.setColor(Color.argb(128, 0, 0, 0));
			int x0 = 0;
			int y0 = 180;
			int x1 = GWk.DEF_SCR_W;
			int y1 = y0 + 48;
			c.drawRect(x0, y0, x1, y1, paint);

			// ミス回数とフレーム数を描画
			y = y0 + 16;
			String s = "MISS: " + dispMiss + "    TIME: "
					+ GWk.getTimeStr(gw.lastDiffMilliTime);
			gw.drawTextWidthBorder(c, s, x, y, Color.WHITE, Color.BLACK);

			// タッチ要求アイコン描画
			TouchReq.getInstance().onDraw(c);
		}
	}
}
