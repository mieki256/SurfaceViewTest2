package com.blawat2015.SurfaceViewSample;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * タッチ座標を描画するクラス
 */
public class DrawTouchPoint extends Task {

	GWk gw = GWk.getInstance();
	Paint paint = new Paint();

	/**
	 * 描画処理
	 */
	@Override
	public void onDraw(Canvas c) {
		// alphaが0なら描画する必要はない
		if (gw.drawTouchAlpha <= 0) return;

		// タッチした座標に十字線を描画
		int x0, y0, x1, y1;
		x0 = 0;
		x1 = GWk.defScrW;
		y0 = y1 = gw.touchPoint.y;

		if (gw.missEnable) {
			// 敵に当たってない
			paint.setColor(Color.argb(255, 255, 62, 0)); // 描画色を指定
		} else {
			// 敵に当たってる
			paint.setColor(Color.CYAN);
		}

		paint.setAntiAlias(false); // アンチエイリアスを指定
		paint.setAlpha(gw.drawTouchAlpha); // 透明度を指定
		c.drawLine(x0, y0, x1, y1, paint); // 線を描画

		x0 = x1 = gw.touchPoint.x;
		y0 = 0;
		y1 = GWk.defScrH;
		c.drawLine(x0, y0, x1, y1, paint);

		// 円を描画
		paint.setStyle(Style.FILL);
		c.drawCircle(gw.touchPoint.x, gw.touchPoint.y, gw.drawTouchRadius, paint);

		gw.drawTouchRadius += (48 - gw.drawTouchRadius) / 3;
		gw.drawTouchAlpha -= 32;
		if (gw.drawTouchAlpha <= 0) gw.drawTouchAlpha = 0;
	}
}

