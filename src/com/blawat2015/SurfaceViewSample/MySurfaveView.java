package com.blawat2015.SurfaceViewSample;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * SurfaceViewのサブクラス
 */
class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	/**
	 * コンストラクタ
	 *
	 * @param context
	 *            Context
	 */
	public MySurfaceView(Context context) {
		super(context);
		LogUtil.d("SURFACE", "init");
		GWk.fixedSizeEnable = false;
		setFocusable(true);
		requestFocus();
		getHolder().addCallback(this);
		getHolder().setSizeFromLayout();
	}

	/**
	 * SurfaceView生成時に呼ばれる処理
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		LogUtil.d("SURFACE", "surfaceCreaded");
		Main.startNow(this); // スレッドを開始
	}

	/**
	 * SurfaceViewに変更があった時に呼ばれる処理
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		LogUtil.d("SURFACE", "surfaceChanged");
		Main.saveScreenWH(width, height); // 画面の縦横幅を取得かつ記録
	}

	/**
	 * SurfaceViewが破棄された時に呼ばれる処理
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		LogUtil.d("SURFACE", "surfaceDestroyed");
		Main.endNow(); // スレッド終了
	}

	/**
	 * 画面をタッチした時の処理
	 */
	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 押し下げ
			GWk.touchRealX = event.getX();
			GWk.touchRealY = event.getY();
			// LogUtil.d("TOUCH", "DOWN " + touchRealX + "," +
			// touchRealY);
			break;
		case MotionEvent.ACTION_MOVE:
			// 指をスライド
			// LogUtil.d("TOUCH", "MOVE " + event.getX() + "," +
			// event.getY());
			break;
		case MotionEvent.ACTION_UP:
			// 指を持ち上げ
			// LogUtil.d("TOUCH", "UP " + event.getX() + "," +
			// event.getY());
			break;
		default:
			break;
		}

		// return super.onTouchEvent(event);
		return true;
	}
}
