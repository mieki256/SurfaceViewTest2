package com.blawat2015.SurfaceViewSample;

import android.app.Activity;
import android.content.Context;
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

	/**
	 * Activityが生成された時の処理.
	 * <p>
	 * アプリ起動時に最初に呼ばれる. 画面の縦横を切替えた時も呼ばれる模様.
	 * </p>
	 */
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
		setContentView(new MySurfaceView(this));
	}

	@Override
	protected void onStart() {
		LogUtil.d("Activity", "onStart Activity");
		super.onStart();
	}

	@Override
	protected void onResume() {
		LogUtil.d("Activity", "onResume Activity");
		Snd.resumeBgm();
		super.onResume();
	}

	@Override
	protected void onPause() {
		LogUtil.d("Activity", "onPause Activity");
		Snd.pauseBgm();
		super.onPause();
	}

	@Override
	protected void onRestart() {
		LogUtil.d("Activity", "onRestart Activity");
		super.onRestart();
	}

	@Override
	protected void onStop() {
		LogUtil.d("Activity", "onStop Activity");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		LogUtil.d("Activity", "onDestroy Activity");
		super.onDestroy();
	}

	/**
	 * メニューボタンが押された時のオプションメニュー表示
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Option.createOptionMenu(menu); // オプションメニュー生成
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * オプションメニューが開かれた際に呼ばれる処理
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Option.prepareMenu(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * オプションメニューが閉じられた際に呼ばれる処理
	 */
	@Override
	public void onOptionsMenuClosed(Menu menu) {
		Option.closeMenu();
		super.onOptionsMenuClosed(menu);
	}

	/**
	 * オプションメニューアイテムが選択された時の処理
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;

		switch( Option.selectedItem(item) ) {
		case 0:
			break;
		case 1:
			// 終了処理(プロセスを殺す版)
			Snd.pauseBgm();
			Img.releaseImageResAll();
			Snd.releaseSoundResAll();
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
