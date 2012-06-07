package com.blawat2015.SurfaceViewSample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

final class Main {

	private static ScheduledExecutorService mExec;
	private static SurfaceView view;
	private static Rect rect = new Rect();

	/**
	 * メインループ部分相当
	 */
	public static void startNow(final SurfaceView view) {
		Main.view = view;
		Snd.checkSilentMode(); // 消音すべきモードかチェック
		FpsCount.init();

		mExec = Executors.newSingleThreadScheduledExecutor();
		mExec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				// この中が、一定時間毎(INTERVAL ナノ秒間隔)で処理される
				// LogUtil.d("Main_run", "main loop");
				Main.runLoop();
			}
		}, 0, GWk.INTERVAL, TimeUnit.NANOSECONDS);

		LogUtil.d("Main", "start thread");
	}

	public static void runLoop() {
		// 更新処理
		GameMgr.onUpdate(Main.view);

		// 描画処理
		final SurfaceHolder holder = view.getHolder();
		final Canvas c;

		// Canvas取得
		if (GWk.fixedSizeEnable) {
			// setFixedSize使用時
			rect.set(0, 0, GWk.virtualScrW, GWk.virtualScrH);
			c = holder.lockCanvas(rect);
		} else {
			// Canvas#scale()使用時
			c = holder.lockCanvas();
		}

		if (c != null) {
			// 背景を指定色で塗りつぶし
			c.drawColor(Color.GRAY);

			if (!GWk.fixedSizeEnable) {
				// 画面サイズに合わせて拡大縮小率を指定
				c.scale(GWk.scaleX, GWk.scaleY);
			}

			// 描画位置をずらす
			if (GWk.screenBorderH > 0) {
				c.translate(0, GWk.screenBorderH / 2);
			} else if (GWk.screenBorderW > 0) {
				c.translate(GWk.screenBorderW / 2, 0);
			}

			// クリッピング範囲を指定
			c.clipRect(0, 0, GWk.DEF_SCR_W, GWk.DEF_SCR_H);

			GameMgr.onDraw(c); // 各タスクの描画
			FpsCount.drawFps(c); // FPS測定値を描画

			// 拡大縮小後の余った領域を塗り潰す…はずだけど上手く行かなかった
			// if (screenBorderW > 0 || screenBorderH > 0) {
			// Paint p = paint;
			// p.setAntiAlias(false);
			// p.setStyle(Style.FILL);
			// p.setStrokeWidth(0);
			// p.setColor(Color.GRAY);
			// Rect r = new Rect();
			// if (screenBorderW > 0) {
			// r.left = screenWidth - screenBorderW;
			// r.top = 0;
			// } else {
			// r.left = 0;
			// r.top = screenHeight - screenBorderH;
			// }
			// r.right = screenWidth - 1;
			// r.bottom = screenHeight - 1;
			// c.scale(1.0f, 1.0f);
			// c.drawRect(r, p);
			// }

			holder.unlockCanvasAndPost(c); // 画面に描画
		}

		GWk.frameCounter++;
		FpsCount.calcFPS(); // FPSを計算
	}

	/**
	 * スレッド終了処理
	 */
	public static void endNow() {
		LogUtil.d("Main", "stop thread (start)");
		mExec.shutdownNow();
		try {
			// スレッド終了まで待つ
			mExec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
		}
		mExec = null;
		LogUtil.d("Main", "stop thread (end)");
	}

	/**
	 * 現在の画面サイズを記録する
	 *
	 * @param width
	 *            画面横幅
	 * @param height
	 *            画面縦幅
	 */
	public static void saveScreenWH(int width, int height) {
		if (GWk.fixedSizeEnable) {
			// setFixedSize() を使っている場合、
			// widthとheightに設定済みの値が入ってきてしまう模様。
			// 仕方ないので、別の取得方法で、w,hを取得する。
			Point p = Main.getWindowSize();
			setScreenWH(p.x, p.y);
		} else {
			setScreenWH(width, height);
		}
	}

	/**
	 * 画面サイズを取得する
	 *
	 * @return Pointクラスで横幅と縦幅を返す
	 */
	public static Point getWindowSize() {
		Point p = new Point();
		Display disp = GWk.window.getWindowManager().getDefaultDisplay();
		p.x = disp.getWidth();
		p.y = disp.getHeight();
		LogUtil.d("Main", "get Window size " + p.x + "," + p.y);
		return p;
	}

	/**
	 * 画面サイズに関連するワークを設定
	 *
	 * @param width
	 *            画面横幅(単位はピクセル)
	 * @param height
	 *            画面縦幅(単位はピクセル)
	 */
	public static void setScreenWH(int width, int height) {
		GWk.scrW = width;
		GWk.scrH = height;
		GWk.scaleX = ((float) GWk.scrW) / ((float) GWk.DEF_SCR_W);
		GWk.scaleY = ((float) GWk.scrH) / ((float) GWk.DEF_SCR_H);

		// LogUtil.d("INFO", "Window w,h = " + scrW + "," + scrH);
		// LogUtil.d("INFO", "DefWdw w,h = " + DEF_SCR_W + "," + DEF_SCR_H);
		// LogUtil.d("INFO", "Scale x,y = " + scaleX + "," + scaleY);

		GWk.screenBorderW = 0;
		GWk.screenBorderH = 0;
		if (GWk.scaleX < GWk.scaleY) {
			GWk.screenBorderH = (GWk.scrH * GWk.DEF_SCR_W / GWk.scrW)
					- GWk.DEF_SCR_H;
			GWk.scaleY = GWk.scaleX;
		} else if (GWk.scaleX > GWk.scaleY) {
			GWk.screenBorderW = (GWk.scrW * GWk.DEF_SCR_H / GWk.scrH)
					- GWk.DEF_SCR_W;
			GWk.scaleX = GWk.scaleY;
		}
		GWk.virtualScrW = GWk.DEF_SCR_W + GWk.screenBorderW;
		GWk.virtualScrH = GWk.DEF_SCR_H + GWk.screenBorderH;

		if (GWk.fixedSizeEnable) {
			SurfaceHolder holder = view.getHolder();
			holder.setFixedSize(GWk.virtualScrW, GWk.virtualScrH);
		}

		// if (fixedSizeEnable) {
		// LogUtil.d("INFO", "setFixedSize()");
		// } else {
		// LogUtil.d("INFO", "Canvas#scale()");
		// }
		// LogUtil.d("INFO", "Border w,h = " + screenBorderW + "," +
		// screenBorderH);
	}
}
