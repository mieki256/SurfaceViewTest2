package com.blawat2015.SurfaceViewSample;

import java.util.LinkedList;

import android.graphics.Canvas;

/**
 * 敵管理用クラス
 */
public class EnemyMgr extends Task {
	GWk gw;
	SndMgr snd;

	// タスクリスト
	private LinkedList<ZakoChara> taskList = new LinkedList<ZakoChara>();
	int step = 0;
	long startTime, nowTime, diffTime;
	final int ENEMY_MAX = 80;

	SpdUpLogo spdLogo = null;

	public EnemyMgr() {
		gw = GWk.getInstance();
		snd = SndMgr.getInstance();

		// 雑魚敵発生
		for (int i = 0; i < ENEMY_MAX; i++)
			taskList.add(new ZakoChara());

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

		for (int i = 0; i < taskList.size(); i++)
			taskList.get(i).init();

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
						snd.playSe(SndMgr.seVoiceList[r]);
					} else {
						// 最後の一匹なら別SE、かつ、スローモーション
						snd.playSe(SndMgr.SE_VOICE_UWAA_DELAY);
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

		if (step == 0) {
			gw.levelChangeEnable = false;

			// BGMを変更すべきかチェック
			if (bgmChangeEnable) {
				// 特定の敵数になったらBGMを変更
				final int[] lst = {
						50 + 1, SndMgr.BGM_MILD, //
						10 + 1, SndMgr.BGM_BOSS, //
						1, -1, //
						-1, -1, //
				};
				if (gw.level <= 2 && gw.charaCount <= lst[gw.level * 2]) {
					int n = lst[gw.level * 2 + 1];
					if (n >= 0) {
						snd.changeBgm(n);
						gw.levelChangeEnable = true;
					} else {
						snd.stopBgm();
					}
					gw.level++;
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
					snd.playSe(SndMgr.SE_MISS); // ミスSEを再生

					// バイブを振動(単位はms)
					gw.vib.vibrate(200);
				}
			}
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * 描画処理
	 */
	@Override
	public void onDraw(Canvas c) {
		for (int i = 0; i < taskList.size(); i++)
			taskList.get(i).onDraw(c);// 描画

		spdLogo.onDraw(c);
	}

}
