package com.blawat2015.SurfaceViewSample;

/**
 * タッチ座標を記憶するクラス
 */
public class TouchPoint extends Task {
	GWk gw;

	public TouchPoint() {
		gw = GWk.getInstance();
	}

	/**
	 * 更新処理
	 */
	@Override
	public boolean onUpdate() {
		// タッチ座標を、拡大縮小を考慮した値に変換
		if (gw.touchRealX > 0 || gw.touchRealY > 0) {
			gw.touchX = gw.touchRealX / gw.scaleX - (gw.screenBorderW / 2);
			gw.touchY = gw.touchRealY / gw.scaleY - (gw.screenBorderH / 2);
			gw.touchRealX = gw.touchRealY = 0;
			gw.touchPoint.x = (int) gw.touchX;
			gw.touchPoint.y = (int) gw.touchY;
			gw.drawTouchAlpha = 255;
			gw.drawTouchRadius = 8;
			gw.touchEnable = true;
		} else {
			gw.touchEnable = false;
		}
		return true;
	}
}
