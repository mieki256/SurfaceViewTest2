package com.blawat2015.SurfaceViewSample;

import java.util.LinkedList;

import android.graphics.Canvas;

/**
 * 敵管理用クラス
 */
public class EnemyMgr extends Task {
	GWk gw = GWk.getInstance();

	// タスクリスト
	private LinkedList<ZakoChara> taskList = new LinkedList<ZakoChara>();
	int step = 0;
	long startTime, nowTime, diffTime;
	final int ENEMY_MAX = 80;

	SpdUpLogo spdLogo = null;

	public EnemyMgr() {
		// 雑魚敵発生
		for (int i = 0; i < ENEMY_MAX; i++) {
			taskList.add(new ZakoChara());
		}

		// SpeedUpロゴ用クラス発生
		spdLogo = new SpdUpLogo();

		init();
	}

	/**
	 * 初期化処理
	 */
	public void init() {
		gw.charaCount = ENEMY_MAX;
		step = 0;
		gw.miss = 0;
		gw.missEnable = false;
		gw.level = 0;
		gw.frameCounter = 0;
		for (int i = 0; i < taskList.size(); i++) {
			taskList.get(i).init();
		}
		startTime = nowTime = System.currentTimeMillis();
		gw.lastDiffMilliTime = gw.diffMilliTime = 0;
	}

	/**
	 * 更新処理
	 */
	@Override
	public boolean onUpdate() {
		boolean result = true;

		int oldCharaCount = gw.charaCount;
		gw.charaCount = 0; // 敵数カウント用変数をクリア
		boolean bgmChangeEnable = false;
		int countBeNum = 0;

		for (int i = 0; i < taskList.size(); i++) {
			ZakoChara z = taskList.get(i);
			if (z.onUpdate() == false) { // 更新失敗なら
				taskList.remove(i); // そのタスクを消す
				i--;
			} else {
				if (z.deadStart) {
					// 雑魚敵消滅処理が開始された
					if (oldCharaCount > 1) {
						// ダメージSEを再生
						int r = gw.rnd.nextInt(SndMgr.seVoiceList.length);
						gw.snd.playSe(SndMgr.seVoiceList[r]);
					} else {
						// 最後の一匹なら別SE、かつ、スローモーション
						gw.snd.playSe(SndMgr.SE_VOICE_UWAA_DELAY);
						gw.slowMotionCount = (int) (GWk.FPS_VALUE * 1.5);
						gw.lastDiffMilliTime = gw.diffMilliTime;
					}

					// リストから取り出して一番最後に配置
					// 一番手前に描画されるようにする
					taskList.remove(i);
					i--;
					taskList.add(z);

					// BGM変更チェックをするように要求
					bgmChangeEnable = true;

					z.deadStart = false;
					gw.missEnable = false;
				}
			}
			if (z.befg) countBeNum++;
		}

		nowTime = System.currentTimeMillis();
		diffTime = nowTime - startTime;
		gw.diffMilliTime = diffTime;

		switch (step) {
		case 0:
			gw.levelChangeEnable = false;

			// BGMを変更すべきかチェック
			if (bgmChangeEnable) {
				// 特定の敵数になったらBGMを変更
				switch (gw.level) {
				case 0:
					if (gw.charaCount <= 50 + 1) {
						gw.snd.stopBgm();
						gw.snd.startBgm(SndMgr.BGM_MILD);
						gw.levelChangeEnable = true;
						gw.level++;
					}
					break;

				case 1:
					if (gw.charaCount <= 10 + 1) {
						gw.snd.stopBgm();
						gw.snd.startBgm(SndMgr.BGM_BOSS);
						gw.levelChangeEnable = true;
						gw.level++;
					}
					break;

				case 2:
					if (gw.charaCount <= 1) {
						gw.snd.stopBgm();
						gw.level++;
					}
					break;
				default:
					break;
				}
			}

			spdLogo.onUpdate();
			if (gw.levelChangeEnable) spdLogo.setDispEnable();

			if (countBeNum <= 0) {
				// 動いてる敵が一匹も居ない
				step++;
			} else {
				// 動いている敵が居る
				if (gw.touchEnable) {
					// ここまでタッチ情報がクリアされていないということは、
					// 敵をタッチできなかった状態ということ
					gw.clearTouchInfo();
					gw.miss++; // ミス回数を+1する
					gw.missEnable = true;
					gw.snd.playSe(SndMgr.SE_MISS); // ミスSEを再生

					// バイブを振動(単位はms)
					gw.vib.vibrate(200);
				}
			}
			break;

		case 1:
			result = false;
			break;

		default:
			break;
		}

		return result;
	}

	/**
	 * 描画処理
	 */
	@Override
	public void onDraw(Canvas c) {
		for (int i = 0; i < taskList.size(); i++) {
			taskList.get(i).onDraw(c);// 描画
		}
		spdLogo.onDraw(c);
	}

}

