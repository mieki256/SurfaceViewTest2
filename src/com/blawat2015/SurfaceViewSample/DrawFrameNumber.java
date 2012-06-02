package com.blawat2015.SurfaceViewSample;

import android.graphics.Canvas;
import android.graphics.Color;

/**
 * フレーム数その他描画クラス
 */
public class DrawFrameNumber extends Task {
	GWk gw;

	public DrawFrameNumber() {
		gw = GWk.getInstance();
	}

	/**
	 * 描画処理
	 */
	@Override
	public void onDraw(Canvas c) {
		// 敵数、ミス回数、時間を描画
		long mills = (gw.lastDiffMilliTime > 0) ? gw.lastDiffMilliTime
				: gw.diffMilliTime;
		String s = String.format("ENEMY: %2d    MISS: %d    TIME: %s",
				gw.charaCount, gw.miss, gw.getTimeStr(mills));
		gw.drawTextWidthBorder(c, s, 0, 24, Color.BLACK, Color.WHITE);
	}
}

