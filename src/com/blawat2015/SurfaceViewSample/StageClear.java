package com.blawat2015.SurfaceViewSample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * ステージクリア処理用クラス
 */
final class StageClear extends Task {
	private int step = 0;
	private int count = 0;
	private int dispMiss = 0;
	private Paint paint = new Paint();

	public StageClear() {
		init(0);
	}

	/**
	 * 初期化処理
	 */
	public void init(int missValue) {
		step = 0;
		count = (int) GWk.FPS_VALUE * 3 / 4;
		dispMiss = missValue;
		TouchReq.init();
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
				GWk.clearTouchInfo();
				Snd.playSe(Snd.SE_STGCLR);
				step++;
			}
			break;

		case 1:
			// 画面がタッチされるまで待つ
			TouchReq.onUpdate();
			if (GWk.touchEnable) step++;
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
			Bitmap b = Img.bmp[Img.ID_LOGO_CLEAR];
			int x = (GWk.DEF_SCR_W - b.getWidth()) / 2;
			int y = 120;
			c.drawBitmap(Img.bmp[Img.ID_LOGO_CLEAR], x, y, paint);

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
					+ GWk.getTimeStr(GWk.lastDiffMilliTime);
			GWk.drawTextWidthBorder(c, s, x, y, Color.WHITE, Color.BLACK);

			// タッチ要求アイコン描画
			TouchReq.onDraw(c);
		}
	}
}
