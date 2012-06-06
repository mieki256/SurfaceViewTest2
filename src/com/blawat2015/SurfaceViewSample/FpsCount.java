package com.blawat2015.SurfaceViewSample;

import android.graphics.Canvas;
import android.graphics.Color;

final class FpsCount {

	private static long fpsFrameCounter = 0L;
	private static long calcInterval = 0L;
	private static long prevCalcTime = 0L;
	private static double actualFps = 0;

	public static void init() {
		fpsFrameCounter = 0L;
		calcInterval = 0L;
		prevCalcTime = System.nanoTime();
		actualFps = 0;
	}

	/**
	 * FPSを測定・計算する
	 */
	public static void calcFPS() {
		fpsFrameCounter++;
		calcInterval += GWk.INTERVAL;

		// 1秒おきにFPSを再計算
		if (calcInterval >= (1000 * 1000 * 1000)) {
			long timeNow = System.nanoTime();

			// 実際の経過時間を測定(単位:ns)
			long realElapsedTime = timeNow - prevCalcTime;

			// FPSを計算
			actualFps = ((double) fpsFrameCounter / realElapsedTime)
					* (1000 * 1000 * 1000);

			fpsFrameCounter = 0;
			calcInterval = 0L;
			prevCalcTime = timeNow;
		}
	}

	/**
	 * FPS測定値を画面に描画
	 *
	 * @param c
	 */
	public static void drawFps(Canvas c) {
		final int FPS_FONT_SIZE = 12;
		int x = 0;
		int y = FPS_FONT_SIZE - 2;
		String s = String.format("%.1f/%d FPS   %d frame", actualFps,
				GWk.FPS_VALUE, GWk.frameCounter);
		GWk.drawTextWidthBorder(c, s, x, y, Color.BLACK, Color.WHITE);
	}
}
