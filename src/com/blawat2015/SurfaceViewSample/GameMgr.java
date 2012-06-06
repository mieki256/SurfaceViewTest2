package com.blawat2015.SurfaceViewSample;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.SurfaceView;

/**
 * タスク管理クラス
 */
final class GameMgr {
	private static Bg bg0;
	private static Bg bg1;
	private static EnemyMgr enemyMgr;
	private static Title title;
	private static StageClear stgClr;
	private static int step = 0;
	private static Paint paint = new Paint();

	private GameMgr() {
		step = 0;
		GWk.miss = 0;
		GWk.slowMotionCount = 0;
	}

	/**
	 * 更新処理
	 *
	 * @param res
	 *            Resource
	 * @param context
	 *            Context
	 * @return falseなら処理終了
	 */
	public static boolean onUpdate(final SurfaceView view) {
		switch (step) {
		case 0:
			// 画像とサウンドデータを読み込み、
			// LogUtil.d("GameMgr", "load resource");
			Img.init(view);
			Snd.init(view);

			GWk.levelChangeEnable = false;
			GWk.slowMotionCount = 0;
			step++;
			break;

		case 1:
			// 各種タスク発生
			LogUtil.d("GameMgr", "Task born");

			bg0 = new Bg(0); // BG発生
			bg1 = new Bg(1);
			enemyMgr = new EnemyMgr(); // 敵発生
			title = new Title(); // タイトル処理クラス発生
			stgClr = new StageClear(); // ステージクリア用タスク発生

			// レイヤー描画フラグを初期化
			for (int i = 0; i < GWk.layerDrawEnable.length; i++)
				GWk.layerDrawEnable[i] = true;

			GWk.diffMilliTime = 0;
			GWk.lastDiffMilliTime = 0;
			step++;
			break;

		case 2:
			// タイトル画面表示
			if (!GWk.enableOpenMenu) {
				saveTouchPoint();
				bg0.onUpdate();
				bg1.onUpdate();
				if (!title.onUpdate()) {
					Snd.startBgm(Snd.BGM_FIRST); // BGM再生開始
					enemyMgr.init();
					GWk.slowMotionCount = 0;
					step++;
				}
			}
			break;

		case 3:
			// ゲーム本編
			if (!GWk.enableOpenMenu) {
				if (GWk.slowMotionCount % 4 == 0) {
					boolean clearFg = false;
					saveTouchPoint();
					bg0.onUpdate();
					bg1.onUpdate();
					clearFg = !enemyMgr.onUpdate();
					if (clearFg) {
						stgClr.init(GWk.miss);
						step++;
					}
				}
			}
			break;

		case 4:
			// ステージクリア
			if (!GWk.enableOpenMenu) {
				if (GWk.slowMotionCount % 4 == 0) {
					saveTouchPoint();
					bg0.onUpdate();
					bg1.onUpdate();
					enemyMgr.onUpdate();
					if (!stgClr.onUpdate()) {
						Snd.stopBgmAll();
						step = 2;
					}
				}
			}
			break;

		default:
			break;
		}

		// サウンド関連処理
		Snd.update();

		GWk.slowMotionCount--;
		if (GWk.slowMotionCount < 0) GWk.slowMotionCount = 0;

		return true;
	}

	/**
	 * 描画処理
	 *
	 * @param c
	 *            Canvas
	 */
	public static void onDraw(Canvas c) {
		if (step >= 2) {
			bg0.onDraw(c);
			bg1.onDraw(c);
		}

		switch (step) {
		case 2:
			title.onDraw(c);
			break;
		case 3:
			enemyMgr.onDraw(c);
			drawTouchPoint(c);
			break;
		case 4:
			enemyMgr.onDraw(c);
			stgClr.onDraw(c);
			break;
		default:
			break;
		}

		// 敵数、ミス回数、時間を描画
		if (step >= 2) drawTime(c);
	}

	/**
	 * 敵数、ミス回数、時間を描画
	 *
	 * @param c
	 */
	public static void drawTime(Canvas c) {
		long mills = (GWk.lastDiffMilliTime > 0) ? GWk.lastDiffMilliTime
				: GWk.diffMilliTime;
		String s = String.format("ENEMY: %2d    MISS: %d    TIME: %s",
				GWk.charaCount, GWk.miss, GWk.getTimeStr(mills));
		GWk.drawTextWidthBorder(c, s, 0, 24, Color.BLACK, Color.WHITE);
	}

	/**
	 * タッチした座標にエフェクトを描画
	 *
	 * @param c
	 *            Canvas
	 */
	public static void drawTouchPoint(Canvas c) {
		// alphaが0なら描画する必要はない
		int alpha = GWk.drawTouchAlpha;
		if (alpha <= 0) return;

		// タッチした座標に十字線を描画する

		// 敵に当たってるか否かで描画色を変える
		paint.setColor((GWk.missEnable) ? Color.argb(255, 255, 62, 0)
				: Color.CYAN);

		paint.setAntiAlias(false); // アンチエイリアスを無効に
		paint.setAlpha(alpha); // 透明度を指定

		int x, y, x0, y0, x1, y1;
		x = GWk.touchPoint.x;
		y = GWk.touchPoint.y;
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
		int r = GWk.drawTouchRadius;
		paint.setStyle(Style.FILL);
		c.drawCircle(x, y, r, paint);

		r += (48 - r) / 5;
		alpha -= 24;
		if (alpha <= 0) alpha = 0;

		GWk.drawTouchRadius = r;
		GWk.drawTouchAlpha = alpha;
	}

	/**
	 * タッチした座標を、拡大縮小を考慮した値に変換して保存
	 */
	public static void saveTouchPoint() {
		// タッチ座標を、拡大縮小を考慮した値に変換
		if (GWk.touchRealX > 0 || GWk.touchRealY > 0) {
			GWk.touchX = GWk.touchRealX / GWk.scaleX - (GWk.screenBorderW / 2);
			GWk.touchY = GWk.touchRealY / GWk.scaleY - (GWk.screenBorderH / 2);
			GWk.touchRealX = GWk.touchRealY = 0;
			GWk.touchPoint.x = (int) GWk.touchX;
			GWk.touchPoint.y = (int) GWk.touchY;
			GWk.drawTouchAlpha = 255;
			GWk.drawTouchRadius = 8;
			GWk.touchEnable = true;
		} else {
			GWk.touchEnable = false;
		}
	}

}
