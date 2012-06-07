package com.blawat2015.SurfaceViewSample;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Process;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

/**
 * SurfaceViewのテスト.
 *
 * ScheduledExecutorService でメインループ処理を行う版.
 */
public final class SurfaceViewTest2Activity extends Activity {

	// Activityが生成された時の処理.
	// ここが最初に呼ばれる.
	// 通常、画面の向きを変えた時も呼ばれる.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.d("Activity", "onCreate Activity");

		// デバッグモードか調べる
		if (BuildConfig.DEBUG) {
			Log.e(getClass().getSimpleName(), "Debug Build");
		} else {
			Log.e(getClass().getSimpleName(), "Release Build");
		}

		// アプリタイトル表示を非表示に
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// フルスクリーンを指定
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// バイブレーション用
		GWk.vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		// サイレントモード判別用
		GWk.amgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		GWk.window = getWindow();

		// SurfaceViewを生成
		setContentView(new MySurfaceView(getApplicationContext()));
	}

	@Override
	protected void onStart() {
		super.onStart();
		LogUtil.d("Activity", "onStart Activity");
	}

	// リジューム時に呼ばれる処理
	// 初回起動時もここを通ることに注意
	@Override
	protected void onResume() {
		super.onResume();
		LogUtil.d("Activity", "onResume Activity");
		Snd.resumeBgm(); // BGM再生再開
	}

	// 一時停止された時に呼ばれる処理
	@Override
	protected void onPause() {
		super.onPause();
		LogUtil.d("Activity", "onPause Activity");
		Snd.pauseBgm(); // BGM一時停止
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		LogUtil.d("Activity", "onRestart Activity");
	}

	@Override
	protected void onStop() {
		super.onStop();
		LogUtil.d("Activity", "onStop Activity");
	}

	// Activityが破棄される時に呼ばれる処理.
	// 通常、何も設定しなければ、画面の向きが変わった時もここを通る.
	// (画面の向きが切り替わる度に、Activityの破棄と生成が行われる)
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.d("Activity", "onDestroy Activity");

		// 何の経緯で破棄されたのかを調べる
		int chg = getChangingConfigurations();
		LogUtil.d("Activity",
				"getChg :" + chg + " (" + String.format("0x%08x", chg) + ")");

		if (chg == 0) {
			// 戻るボタンや「EXIT」で終わった可能性が高い
			LogUtil.d("Activity", "push Back Button?");
			Snd.stopBgm(); // BGM停止
			Snd.releaseSoundResAll(); // サウンドデータの解放
			Img.releaseImageResAll(); // 画像データの解放
			GameMgr.init(); // 次回起動したときのためにワークを初期化
		} else if ((chg & ActivityInfo.CONFIG_ORIENTATION) != 0) {
			// 画面の向きが変わった
			LogUtil.d("Activity", "change Orientation");
		} else {
			// それ以外
			LogUtil.d("Activity", "Unknown");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		LogUtil.d("Activity", "onSaveInstanceState Activity");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		LogUtil.d("Activity", "onRestoreInstanceState Activity");
	}

	// 画面の向きが変わった時に呼ばれる処理
	//
	// 通常、ここは呼ばれないが、
	// AndroidManifest.xml の <Activity > 内に、
	// android:configChanges="orientation|keyboardHidden"
	// の記述を追加することで、呼ばれるようになる。
	//
	// 上記は Android 2.x 以前の記述内容で、
	// Andoroid 3.x 以降は記述内容が変わっている模様.
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		LogUtil.d("Activity", "onConfigurationChanged Activity");
	}

	// メニューボタンが押された時のオプションメニュー表示
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Option.createMenu(menu); // オプションメニュー生成
		return super.onCreateOptionsMenu(menu);
	}

	// オプションメニューが開かれた際に呼ばれる処理
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Option.prepareMenu(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	// オプションメニューが閉じられた際に呼ばれる処理
	@Override
	public void onOptionsMenuClosed(Menu menu) {
		Option.closeMenu();
		super.onOptionsMenuClosed(menu);
	}

	// オプションメニュー内のアイテムが選択された時の処理
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;

		switch (Option.selectedItem(item)) {
		case 0:
			break;
		case 1:
			// 終了処理(プロセスを殺す版)
			Snd.stopBgm();
			Snd.releaseSoundResAll();
			Img.releaseImageResAll();
			GameMgr.init();
			super.onDestroy();
			Process.killProcess(Process.myPid());
			break;
		case 2:
			// 終了処理(finish版)
			Snd.pauseBgm();
			finish();
			break;
		default:
			ret = super.onOptionsItemSelected(item);
			break;
		}
		return ret;
	}
}
