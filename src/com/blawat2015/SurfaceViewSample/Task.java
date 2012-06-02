package com.blawat2015.SurfaceViewSample;

import android.graphics.Canvas;

/**
 * 基本となるタスクのクラス
 */
public abstract class Task {

	/**
	 * 更新処理
	 *
	 * @return
	 */
	public boolean onUpdate() {
		return true;
	}

	/**
	 * 描画処理
	 *
	 * @param c
	 *            Canvas
	 */
	public void onDraw(Canvas c) {
	}

}

