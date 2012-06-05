package com.blawat2015.SurfaceViewSample;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;

/**
 * タスク管理クラス
 */
public class GameMgr {
	GWk gw;
	Bg bg0;
	Bg bg1;
	TouchPoint tp;
	EnemyMgr enemyMgr;
	DrawTouchPoint drawTp;
	DrawFrameNumber drawFn;
	Title title;
	StageClear stgClr;
	SndMgr snd;
	int step = 0;
	int stepSub = 0;
	int count = 0;

	GameMgr() {
		gw = GWk.getInstance();
		snd = SndMgr.getInstance();
		step = 0;
		gw.miss = 0;
		gw.slowMotionCount = 0;
	}

	/**
	 * 更新処理
	 */
	/**
	 * 更新処理
	 * @param res Resource
	 * @param context Context
	 * @return falseなら処理終了
	 */
	public boolean onUpdate(Resources res, Context context) {
		if (gw.slowMotionCount % 4 == 0) {

			switch (step) {
			case 0:
				ImgMgr.getInstance().loadImageRes(res); // 画像読み込み
				snd.loadSoundRes(context); // サウンドデータ読み込み
				gw.levelChangeEnable = false;
				gw.slowMotionCount = 0;
				step++;
				break;

			case 1:
				bg0 = new Bg(0); // BG発生
				bg1 = new Bg(1);
				tp = new TouchPoint(); // タッチ座標記録クラス発生
				enemyMgr = new EnemyMgr(); // 敵発生
				drawTp = new DrawTouchPoint(); // タッチ座標描画クラス発生
				drawFn = new DrawFrameNumber(); // 数値表示クラス発生
				title = new Title(); // タイトル処理クラス発生
				stgClr = new StageClear(); // ステージクリア用タスク発生

				// レイヤー描画フラグを初期化
				for (int i = 0; i < gw.layerDrawEnable.length; i++) {
					gw.layerDrawEnable[i] = true;
				}
				gw.diffMilliTime = 0;
				gw.lastDiffMilliTime = 0;
				step++;
				break;

			case 2:
				// タイトル画面表示
				if (!gw.enableOpenMenu) {
					bg0.onUpdate();
					bg1.onUpdate();
					tp.onUpdate();
					drawFn.onUpdate();
					if (!title.onUpdate()) {
						snd.startBgm(SndMgr.BGM_FIRST); // BGM再生開始
						enemyMgr.init();
						step++;
					}
				}
				break;

			case 3:
				// ゲーム本編
				if (!gw.enableOpenMenu) {
					boolean clearFg = false;

					bg0.onUpdate();
					bg1.onUpdate();
					tp.onUpdate();
					clearFg = !enemyMgr.onUpdate();
					drawTp.onUpdate();
					drawFn.onUpdate();
					if (clearFg) {
						stgClr.init(gw.miss, (int) gw.frameCounter);
						step++;
					}
				}
				break;

			case 4:
				// ステージクリア
				if (!gw.enableOpenMenu) {
					bg0.onUpdate();
					bg1.onUpdate();
					tp.onUpdate();
					enemyMgr.onUpdate();
					if (!stgClr.onUpdate()) {
						snd.stopBgmAll();
						step = 2;
					}
				}
				break;

			default:
				break;
			}
		}

		// サウンド関連処理
		snd.update();

		gw.slowMotionCount--;
		if (gw.slowMotionCount < 0) gw.slowMotionCount = 0;

		return true;
	}

	/**
	 * 描画処理
	 *
	 * @param c
	 *            Canvas
	 */
	public void onDraw(Canvas c) {
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
			drawTp.onDraw(c);
			break;
		case 4:
			enemyMgr.onDraw(c);
			stgClr.onDraw(c);
			break;
		default:
			break;
		}

		if (step >= 2) {
			drawFn.onDraw(c);
		}
	}

}
