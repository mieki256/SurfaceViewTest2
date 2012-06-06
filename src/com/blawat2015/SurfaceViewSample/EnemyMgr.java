package com.blawat2015.SurfaceViewSample;

import java.util.LinkedList;

import android.graphics.Canvas;

/**
 * 敵管理用クラス
 */
public class EnemyMgr extends Task {

	// タスクリスト
	private LinkedList<ZakoChara> taskList = new LinkedList<ZakoChara>();
	int step = 0;
	long startTime, nowTime, diffTime;
	final int ENEMY_MAX = 80;

	SpdUpLogo spdLogo = null;

	public EnemyMgr() {
		// 雑魚敵発生
		for (int i = 0; i < ENEMY_MAX; i++)
			taskList.add(new ZakoChara());

		spdLogo = new SpdUpLogo(); // SpeedUpロゴ用クラス発生
		init();
	}

	/**
	 * 初期化処理
	 */
	public void init() {
		GWk.charaCount = ENEMY_MAX;
		step = 0;
		GWk.miss = 0;
		GWk.missEnable = false;
		GWk.level = 0;
		GWk.frameCounter = 0;

		for (int i = 0; i < taskList.size(); i++)
			taskList.get(i).init();

		startTime = nowTime = System.currentTimeMillis();
		GWk.lastDiffMilliTime = GWk.diffMilliTime = 0;
	}

	/**
	 * 更新処理
	 */
	@Override
	public boolean onUpdate() {
		boolean result = true;

		int oldCharaCount = GWk.charaCount;
		GWk.charaCount = 0; // 敵数カウント用変数をクリア
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
						int r = GWk.rnd.nextInt(Snd.seVoiceList.length);
						Snd.playSe(Snd.seVoiceList[r]);
					} else {
						// 最後の一匹なら別SE、かつ、スローモーション
						Snd.playSe(Snd.SE_VOICE_UWAA_DELAY);
						GWk.slowMotionCount = (int) (GWk.FPS_VALUE * 1.5);
						GWk.lastDiffMilliTime = GWk.diffMilliTime;
					}

					// リストから取り出して一番最後に配置
					// 一番手前に描画されるようにする
					taskList.remove(i);
					i--;
					taskList.add(z);

					// BGM変更チェックをするように要求
					bgmChangeEnable = true;

					z.deadStart = false;
					GWk.missEnable = false;
				}
			}
			if (z.befg) countBeNum++;
		}

		nowTime = System.currentTimeMillis();
		diffTime = nowTime - startTime;
		GWk.diffMilliTime = diffTime;

		if (step == 0) {
			GWk.levelChangeEnable = false;

			// BGMを変更すべきかチェック
			if (bgmChangeEnable) {
				// 特定の敵数になったらBGMを変更
				final int[] lst = {
						50 + 1, Snd.BGM_MILD, //
						10 + 1, Snd.BGM_BOSS, //
						1, -1, //
						-1, -1, //
				};
				if (GWk.level <= 2 && GWk.charaCount <= lst[GWk.level * 2]) {
					int n = lst[GWk.level * 2 + 1];
					if (n >= 0) {
						Snd.changeBgm(n);
						GWk.levelChangeEnable = true;
					} else {
						Snd.stopBgm();
					}
					GWk.level++;
				}
			}

			spdLogo.onUpdate();
			if (GWk.levelChangeEnable) spdLogo.setDispEnable();

			if (countBeNum <= 0) {
				// 動いてる敵が一匹も居ない
				step++;
			} else {
				// 動いている敵が居る
				if (GWk.touchEnable) {
					// ここまでタッチ情報がクリアされていないということは、
					// 敵をタッチできなかった状態ということ
					GWk.clearTouchInfo();
					GWk.miss++; // ミス回数を+1する
					GWk.missEnable = true;
					Snd.playSe(Snd.SE_MISS); // ミスSEを再生

					// バイブを振動(単位はms)
					GWk.vib.vibrate(200);
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
