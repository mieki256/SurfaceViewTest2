package com.blawat2015.SurfaceViewSample;

import java.io.IOException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

/**
 * サウンド関連クラス
 */
public class SndMgr implements OnLoadCompleteListener {

	// BGM番号を定義
	// この番号を指定して、BGMを再生する
	public final static int BGM_FIRST = 0;
	public final static int BGM_BOSS = 1;
	public final static int BGM_MILD = 2;

	// BGMリソースIDリスト
	private final int[] bgmResIdList = {
			R.raw.bgm01, // 0
			R.raw.bgm02, // 1
			R.raw.bgm03, // 2
	};

	// SE番号を定義
	// この番号を指定して、SEを再生する
	public final static int SE_MISS = 0;
	public final static int SE_VOICE_GYA = 1;
	public final static int SE_VOICE_HUNYA = 2;
	public final static int SE_VOICE_IYOU = 3;
	public final static int SE_VOICE_KYAA = 4;
	public final static int SE_VOICE_OU = 5;
	public final static int SE_VOICE_UO = 6;
	public final static int SE_VOICE_UOU = 7;
	public final static int SE_VOICE_UOUU = 8;
	public final static int SE_VOICE_WAA = 9;
	public final static int SE_VOICE_WHEU = 10;
	public final static int SE_VOICE_WII = 11;
	public final static int SE_VOICE_UWAA_DELAY = 12;
	public final static int SE_STGCLR = 13;

	// SEリソースIDリスト
	private final int[] seResIdList = {
			R.raw.se_miss, // 0
			R.raw.se_voice_gya, // 1
			R.raw.se_voice_hunya, // 2
			R.raw.se_voice_iyou, // 3
			R.raw.se_voice_kyaa, // 4
			R.raw.se_voice_ou, // 5
			R.raw.se_voice_uo, // 6
			R.raw.se_voice_uou, // 7
			R.raw.se_voice_uouu, // 8
			R.raw.se_voice_waa, // 9
			R.raw.se_voice_wheu, // 10
			R.raw.se_voice_wii, // 11
			R.raw.se_voice_uwaa_delay1, // 12
			R.raw.se_stgclr, // 13
	};

	// 敵ダメージ時の音声SE番号リスト
	// この中からランダムに再生する
	public final static int[] seVoiceList = {
			SE_VOICE_GYA, SE_VOICE_HUNYA, SE_VOICE_IYOU, SE_VOICE_KYAA,
			SE_VOICE_OU, SE_VOICE_UO, SE_VOICE_UOU, SE_VOICE_UOUU,
			SE_VOICE_WAA, SE_VOICE_WHEU, SE_VOICE_WII,
	};

	private MediaPlayer[] bgm;
	private SoundPool sndPool;

	// SE(SoundPoll)ID記録用
	private int[] seId;

	// SEデータ読み込み終了フラグ
	public boolean seLoadComplete = false;

	// 現在再生中のBGM番号を記録
	int bgmNumber;

	// テスト用：bgmを順に鳴らすためのワーク
	private int testBgmIndex;

	// マナーモード判別その他を行うためにAudioManagerを用意する
	public AudioManager audioManager;

	// 消音すべきモードか否か(マナーモード等の情報)
	public boolean silentEnbale = false;

	// サウンドが無効か否か (trueなら無効)
	public boolean soundDisable = false;

	/**
	 * コンストラクタ
	 */
	public SndMgr() {
		bgm = new MediaPlayer[bgmResIdList.length];
		seId = new int[seResIdList.length];
		bgmNumber = -1;
		seLoadComplete = false;
		testBgmIndex = 0;
		silentEnbale = false;
		soundDisable = false;
	}

	/**
	 * 更新処理
	 */
	public void update() {
		boolean oldFg = silentEnbale;

		// 消音すべきモードかチェック
		silentEnbale = checkSilentMode();

		if (oldFg != silentEnbale) {
			// モードの切り替えがあった
			checkBgmStatus();
		}
	}

	/**
	 * サウンドの有効無効を切り替える
	 */
	public void changeSoundMode() {
		soundDisable = !soundDisable;
		checkBgmStatus();
	}

	/**
	 * サウンドの有効無効の切り替えに伴い、BGMの再生と停止を指定する
	 */
	public void checkBgmStatus() {
		if (bgmNumber >= 0) {
			// BGM再生中として扱うべき状態
			if (isSoundEnable()) {
				// サウンド有効に変化した。BGM再生開始
				bgm[bgmNumber].start();
			} else {
				// サウンド無効に変化した。BGMを停止
				stopBgmSub(bgm[bgmNumber]);
			}
		}
	}

	/**
	 * 消音すべきモードかどうかを返す。
	 */
	public boolean checkSilentMode() {
		boolean fg = false;
		switch (audioManager.getRingerMode()) {

		case AudioManager.RINGER_MODE_SILENT:
			// サイレントモード
			// LogUtil.d("INFO", "SILENT_MODE");
			fg = true;
			break;

		case AudioManager.RINGER_MODE_VIBRATE:
			// バイブレートモード(マナーモード)
			// LogUtil.d("INFO", "VIBRATE_MODE");
			fg = true;
			break;

		case AudioManager.RINGER_MODE_NORMAL:
			// 通常モード
			// LogUtil.d("INFO", "NORMAL_MODE");
			fg = false;
			break;

		default:
			// LogUtil.d("INFO", "UNKNOWN_MODE");
			fg = false;
			break;

		}
		return fg;
	}

	/**
	 * サウンド有効か否かを返す
	 *
	 * @return trueなら有効、falseなら無効
	 */
	public boolean isSoundEnable() {
		return ((!silentEnbale) && (!soundDisable));
	}

	/**
	 * SEデータのロードが終了した際に呼ばれる処理
	 */
	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
		if (status == 0) {
			seLoadComplete = true;
		}
	}

	/**
	 * サウンドデータ読み込み
	 * @param context Context
	 */
	public void loadSoundRes(Context context) {
		seLoadComplete = false;

		// BGMデータ読み込み
		for (int i = 0; i < bgmResIdList.length; i++) {
			bgm[i] = MediaPlayer.create(context,
					bgmResIdList[i]);

			// ループ再生することを指定
			bgm[i].setLooping(true);

			// ストリームタイプを指定
			bgm[i].setAudioStreamType(AudioManager.STREAM_MUSIC);

			// 再生のための前準備として prepare()が必要らしいのだが、
			// SDKのバージョンによっては、create()の中で既に呼んでいるようで、
			// もしかすると呼ぶ必要はないらしい？

			// try {
			// bgm[i].prepare();
			// } catch (IllegalStateException e) {
			// e.printStackTrace();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
		}

		// SEデータ読み込み
		sndPool = new SoundPool(seResIdList.length,
				AudioManager.STREAM_MUSIC, 0);
		sndPool.setOnLoadCompleteListener(this);
		for (int i = 0; i < seResIdList.length; i++) {
			seId[i] = sndPool.load(context, seResIdList[i],
					1);
		}
	}

	/**
	 * BGM再生開始
	 *
	 * @param n
	 *            BGM番号
	 */
	public void startBgm(int n) {
		// このタイミングでseekTo()を使うと音が二重に聞こえる…
		// bgm[n].seekTo(0);

		if (isSoundEnable()) bgm[n].start();
		bgmNumber = n;
	}

	/**
	 * BGM停止の実処理
	 *
	 * @param mp
	 *            MediaPlayer
	 */
	private void stopBgmSub(MediaPlayer mp) {
		if (mp.isPlaying()) {
			mp.stop();

			// reset() を呼ぶと、次回再生した際に鳴らなくなった…
			// mp.reset();

			try {
				mp.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// このタイミングでseekTo()を使えば、
		// 次回鳴らした際、頭から再生できている、ように聞こえた
		mp.seekTo(0);
	}

	/**
	 * BGM停止
	 */
	public void stopBgm() {
		if (bgmNumber < 0) return;
		stopBgmSub(bgm[bgmNumber]);
		bgmNumber = -1;
	}

	/**
	 * 全てのBGMを停止
	 */
	public void stopBgmAll() {
		for (int i = 0; i < bgm.length; i++) {
			stopBgmSub(bgm[i]);
		}
		bgmNumber = -1;
	}

	/**
	 * BGMを一時停止
	 */
	public void pauseBgm() {
		if (bgmNumber < 0) return;
		if (bgm[bgmNumber].isPlaying()) {
			bgm[bgmNumber].pause();
		}
	}

	/**
	 * BGMの再開
	 */
	public void restartBgm() {
		if (bgmNumber < 0) return;
		if (isSoundEnable()) bgm[bgmNumber].start();
	}

	/**
	 * 全BGMデータを解放
	 */
	public void releaseBgmAll() {
		for (int i = 0; i < bgm.length; i++) {
			bgm[i].setLooping(false);

			// 以下の3つをセットで呼ばないとハマるらしい…
			bgm[i].stop();
			bgm[i].reset();
			bgm[i].release();
		}
	}

	/**
	 * 現在のBGM番号の、次のBGM番号を返す
	 *
	 * @return 次のBGM番号
	 */
	public int getNextBgmId() {
		final int[] list = {
				BGM_FIRST, BGM_MILD, BGM_BOSS
		};

		int n = list[testBgmIndex];
		testBgmIndex++;
		testBgmIndex %= list.length;
		return n;
	}

	/**
	 * SEを再生
	 *
	 * @param id
	 *            SE番号
	 */
	public void playSe(int id) {
		if (!seLoadComplete) return;
		if (isSoundEnable())
			sndPool.play(seId[id], 1.0f, 1.0f, 0, 0, 1.0f);
	}

	/**
	 * 全SEデータを解放
	 */
	public void releaseSeAll() {
		for (int i = 0; i < seResIdList.length; i++) {
			int id = seId[i];
			sndPool.stop(id);
			sndPool.unload(id);
		}
		sndPool.release();
	}

}

