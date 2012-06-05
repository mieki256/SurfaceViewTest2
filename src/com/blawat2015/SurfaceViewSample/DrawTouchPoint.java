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
		int alpha = gw.drawTouchAlpha;
		if (alpha <= 0) return;

		// タッチした座標に十字線を描画する

		// 敵に当たってるか否かで描画色を変える
		paint.setColor((gw.missEnable)? Color.argb(255, 255, 62, 0) : Color.CYAN);

		paint.setAntiAlias(false); // アンチエイリアスを無効に
		paint.setAlpha(alpha); // 透明度を指定

		int x,y, x0, y0, x1, y1;
		x = gw.touchPoint.x;
		y = gw.touchPoint.y;
		x0 = 0;
		x1 = GWk.DEF_SCR_W;
		y0 = y;
		y1 = y;
		c.drawLine(x0, y0, x1, y1, paint); // 水平線を描画

		x0 = x;
		x1 = x;
		y0 = 0;
		y1 = GWk.DEF_SCR_H;
		c.drawLine(x0, y0, x1, y1, paint); // 垂直線を描画

		// 円を描画
		int r = gw.drawTouchRadius;
		paint.setStyle(Style.FILL);
		c.drawCircle(x, y, r, paint);

		r += (48 - r) / 5;
		alpha -= 24;
		if (alpha <= 0) alpha = 0;

		gw.drawTouchRadius = r;
		gw.drawTouchAlpha = alpha;
	}
}

