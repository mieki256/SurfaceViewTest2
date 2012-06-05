package com.blawat2015.SurfaceViewSample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.blawat2015.SurfaceViewSample.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

/**
 * SurfaceViewのテスト.
 *
 * ScheduledExecutorService でメインループ処理を行う版.
 */
public class SurfaceViewTest2Activity extends Activity {

	GWk gw = GWk.getInstance();
	MySurfaceView surface;

	/**
	 * SurfaceViewのサブクラス
	 */
	class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

		private long fpsFrameCounter = 0L;
		private long calcInterval = 0L;
		private long prevCalcTime = 0L;
		private double actualFps = 0;

		private SurfaceHolder holder = null;
		private ScheduledExecutorService mExec = null;
		private GameMgr gameMgr = null;

		private Rect rect = new Rect();

		/**
		 * コンストラクタ
		 *
		 * @param context
		 *            Context
		 */
		public MySurfaceView(Context context) {
			super(context);
			init();
		}

		public MySurfaceView(Context context, AttributeSet attrs) {
			super(context, attrs);
			init();
		}

		public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			init();
		}

		public void init() {
			LogUtil.d("SURFACE", "MySurfaceView()");
			gw.fixedSizeEnable = false;
			holder = getHolder();
			holder.addCallback(this);
			holder.setSizeFromLayout();
			setFocusable(true);
			requestFocus();
		}

		/**
		 * SurfaceView生成時に呼ばれる処理
		 */
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			LogUtil.d("SURFACE", "surfaceCreaded()");
			gameMgr = new GameMgr();

			// マナーモード等の判別用
			SndMgr snd = SndMgr.getInstance();
			snd.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			snd.checkSilentMode(); // 消音すべきモードかチェック

			startNow();
		}

		/**
		 * SurfaceViewに変更があった時に呼ばれる処理
		 */
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			LogUtil.d("SURFACE", "surfaceChanged()");

			// 画面の縦横幅を取得して関連変数を設定
			if (gw.fixedSizeEnable) {
				// setFixedSize() を使っている場合、
				// widthとheightに設定済みの値が入ってきてしまう模様。
				// 仕方ないので、別の取得方法で、w,hを取得する。
				Point p = getScreenSize();
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
		public Point getScreenSize() {
			Point p = new Point();
			Window wdw = getWindow();
			WindowManager mgr = wdw.getWindowManager();
			Display disp = mgr.getDefaultDisplay();
			p.x = disp.getWidth();
			p.y = disp.getHeight();
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
		public void setScreenWH(int width, int height) {
			gw.scrW = width;
			gw.scrH = height;
			gw.scaleX = ((float) gw.scrW) / ((float) GWk.DEF_SCR_W);
			gw.scaleY = ((float) gw.scrH) / ((float) GWk.DEF_SCR_H);

			// LogUtil.d("INFO", "Window w,h = " + scrW + "," + scrH);
			// LogUtil.d("INFO", "DefWdw w,h = " + DEF_SCR_W + "," + DEF_SCR_H);
			// LogUtil.d("INFO", "Scale x,y = " + scaleX + "," + scaleY);

			gw.screenBorderW = 0;
			gw.screenBorderH = 0;
			if (gw.scaleX < gw.scaleY) {
				gw.screenBorderH = (gw.scrH * GWk.DEF_SCR_W / gw.scrW)
						- GWk.DEF_SCR_H;
				gw.scaleY = gw.scaleX;
			} else if (gw.scaleX > gw.scaleY) {
				gw.screenBorderW = (gw.scrW * GWk.DEF_SCR_H / gw.scrH)
						- GWk.DEF_SCR_W;
				gw.scaleX = gw.scaleY;
			}
			gw.virtualScrW = GWk.DEF_SCR_W + gw.screenBorderW;
			gw.virtualScrH = GWk.DEF_SCR_H + gw.screenBorderH;

			if (gw.fixedSizeEnable) {
				holder.setFixedSize(gw.virtualScrW, gw.virtualScrH);
			}

			// if (fixedSizeEnable) {
			// LogUtil.d("INFO", "setFixedSize()");
			// } else {
			// LogUtil.d("INFO", "Canvas#scale()");
			// }
			// LogUtil.d("INFO", "Border w,h = " + screenBorderW + "," +
			// screenBorderH);
		}

		/**
		 * SurfaceViewが破棄された時に呼ばれる処理
		 */
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			LogUtil.d("SURFACE", "surfaceDestroyed()");
			exitJob();
		}

		/**
		 * 終了時に行う処理
		 */
		public void exitJob() {
			// スレッド終了
			mExec.shutdownNow();
			try {
				// スレッド終了まで待つ
				mExec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
			}
			mExec = null;

			// 画像を全て破棄する
			ImgMgr.getInstance().recycleImageAll();

			// BGMとSEを停止して破棄する
			SndMgr snd = SndMgr.getInstance();
			snd.releaseBgmAll();
			snd.releaseSeAll();
		}

		// 描画処理
		private void onDraw() {

			Canvas c;

			// Canvasを取得
			if (gw.fixedSizeEnable) {
				// setFixedSize使用時
				rect.set(0, 0, gw.virtualScrW, gw.virtualScrH);
				c = holder.lockCanvas(rect);
			} else {
				// Canvas#scale()使用時
				c = holder.lockCanvas();
			}

			if (c != null) {
				// 背景を指定色で塗りつぶし
				c.drawColor(Color.GRAY);

				if (!gw.fixedSizeEnable) {
					// 画面サイズに合わせて拡大縮小率を指定
					c.scale(gw.scaleX, gw.scaleY);
				}

				// 描画位置をずらす
				if (gw.screenBorderH > 0) {
					c.translate(0, gw.screenBorderH / 2);
				} else if (gw.screenBorderW > 0) {
					c.translate(gw.screenBorderW / 2, 0);
				}

				// クリッピング範囲を指定
				c.clipRect(0, 0, GWk.DEF_SCR_W, GWk.DEF_SCR_H);

				gameMgr.onDraw(c); // 各タスクの描画
				drawFps(c); // FPS測定値描画

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
		}

		/**
		 * メインループ部分相当
		 */
		public void startNow() {
			prevCalcTime = System.nanoTime();
			mExec = Executors.newSingleThreadScheduledExecutor();
			mExec.scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					// この中が一定時間毎に処理される
					// LogUtil.d("SURFACE_LOOP", "loop");

					// 更新処理
					gameMgr.onUpdate(getResources(), getApplicationContext());

					// 描画処理
					onDraw();

					gw.frameCounter++;
					calcFPS(); // FPSを計算
				}
			}, 0, GWk.INTERVAL, TimeUnit.NANOSECONDS);
			// INTERVALの間隔で処理が行われる
		}

		/**
		 * FPSを測定・計算する
		 */
		private void calcFPS() {
			fpsFrameCounter++;
			calcInterval += GWk.INTERVAL;

			// 1秒おきにFPSを再計算
			if (calcInterval >= (1000 * 1000 * 1000)) {
				long timeNow = System.nanoTime();

				// 実際の経過時間を測定(単位:ns)
				long realElapsedTime = timeNow - prevCalcTime;

				// FPSを計算
				actualFps = ((double) fpsFrameCounter / realElapsedTime)
						* (1000 * 1000 * 1000);

				fpsFrameCounter = 0;
				calcInterval = 0L;
				prevCalcTime = timeNow;
			}
		}

		/**
		 * FPS測定値を画面に描画
		 *
		 * @param c
		 */
		private void drawFps(Canvas c) {
			final int FPS_FONT_SIZE = 12;
			int x = 0;
			int y = FPS_FONT_SIZE - 2;
			String s = String.format("%.1f/%d FPS   %d frame", actualFps,
					GWk.FPS_VALUE, gw.frameCounter);
			gw.drawTextWidthBorder(c, s, x, y, Color.BLACK, Color.WHITE);
		}
	}

	public static class LogUtil {
		/**
		 * デバッグログ出力
		 */
		public static final void d(String tag, String msg) {
			// マニフェストでデバッグが有効なら出力する
			if (BuildConfig.DEBUG) Log.d(tag, msg);
		}
	}

	/**
	 * Activityが生成された時の処理. アプリ起動時、ここが最初に呼ばれる.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.main);

		// デバッグモードか調べる
		if (BuildConfig.DEBUG) {
			Log.e(getClass().getSimpleName(), "Debug Build");
		} else {
			Log.e(getClass().getSimpleName(), "Release Build");
		}

		LogUtil.d("Activity", "onCreate()");

		// バイブレーション用
		gw.vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		// フルスクリーンを指定
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// アプリタイトル表示を非表示に
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// SurfaceViewを生成
		surface = new MySurfaceView(this);
		setContentView(surface);
	}

	@Override
	protected void onDestroy() {
		LogUtil.d("Activity", "onDestroy()");
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		LogUtil.d("Activity", "onPause()");
		super.onPause();
	}

	@Override
	protected void onRestart() {
		LogUtil.d("Activity", "onRestart()");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		LogUtil.d("Activity", "onResume()");
		super.onResume();
	}

	@Override
	protected void onStart() {
		LogUtil.d("Activity", "onStart()");
		super.onStart();
	}

	@Override
	protected void onStop() {
		LogUtil.d("Activity", "onStop()");
		super.onStop();
	}

	// オプションメニューIDを定義
	private static final int MENU_ID_FIXSIZE = (Menu.FIRST + 1);
	private static final int MENU_ID_DRAWTYPE = (Menu.FIRST + 2);
	private static final int MENU_ID_BG0 = (Menu.FIRST + 3);
	private static final int MENU_ID_BG1 = (Menu.FIRST + 4);
	private static final int MENU_ID_ENEMY = (Menu.FIRST + 5);
	private static final int MENU_ID_SOUND = (Menu.FIRST + 6);
	private static final int MENU_ID_BGMOFF = (Menu.FIRST + 7);
	private static final int MENU_ID_BGMCHG = (Menu.FIRST + 8);

	/**
	 * メニューボタンが押された時のオプションメニュー表示
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// ID番号のリスト
		final int[] id = {
				MENU_ID_FIXSIZE, // 0
				MENU_ID_DRAWTYPE, // 1
				MENU_ID_BG0, // 2
				MENU_ID_BG1, // 3
				MENU_ID_ENEMY, // 4
				MENU_ID_SOUND, // 5
				MENU_ID_BGMOFF, // 6
				MENU_ID_BGMCHG, // 7
		};

		// 文字列番号のリスト
		// R.string.xxxx は、res/values/strins.xml で定義してある
		final int[] strn = {
				R.string.menu_fix_on, // 0
				R.string.menu_drawtype1, // 1
				R.string.menu_bg0_off, // 2
				R.string.menu_bg1_off, // 3
				R.string.menu_enemy_off, // 4
				R.string.menu_snd_off, // 5
				R.string.menu_bgm_all_off, // 6
				R.string.menu_bgm_change, // 7
		};

		// アイコン割り当て
		final int[] iconLst = {
				android.R.drawable.ic_menu_manage, // 0
				android.R.drawable.ic_menu_manage, // 1
				android.R.drawable.ic_menu_view, // 2
				android.R.drawable.ic_menu_view, // 3
				android.R.drawable.ic_menu_view, // 4
				android.R.drawable.ic_menu_manage, // 5
				android.R.drawable.ic_menu_manage, // 6
				android.R.drawable.ic_menu_manage, // 7
		};

		for (int i = 0; i < id.length; i++) {
			// メニュー項目を追加
			menu.add(Menu.NONE, id[i], Menu.NONE, strn[i]).setIcon(iconLst[i]);
		}
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * オプションメニューが開かれた際に呼ばれる処理
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		gw.enableOpenMenu = true;

		menu.findItem(MENU_ID_FIXSIZE).setVisible(!gw.fixedSizeEnable);

		menu.findItem(MENU_ID_DRAWTYPE).setTitle(
				(gw.disableScaleDraw) ? R.string.menu_drawtype0
						: R.string.menu_drawtype1);

		menu.findItem(MENU_ID_BG0).setTitle(
				(gw.layerDrawEnable[0]) ? R.string.menu_bg0_off
						: R.string.menu_bg0_on);
		menu.findItem(MENU_ID_BG1).setTitle(
				(gw.layerDrawEnable[1]) ? R.string.menu_bg1_off
						: R.string.menu_bg1_on);
		menu.findItem(MENU_ID_ENEMY).setTitle(
				(gw.layerDrawEnable[2]) ? R.string.menu_enemy_off
						: R.string.menu_enemy_on);

		SndMgr snd = SndMgr.getInstance();
		if (snd.silentEnbale) {
			menu.findItem(MENU_ID_SOUND).setTitle(R.string.menu_silent);
		} else if (snd.isSoundEnable()) {
			menu.findItem(MENU_ID_SOUND).setTitle(R.string.menu_snd_off);
		} else {
			menu.findItem(MENU_ID_SOUND).setTitle(R.string.menu_snd_on);
		}

		snd.pauseBgm();
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * オプションメニューが閉じられた際に呼ばれる処理
	 */
	@Override
	public void onOptionsMenuClosed(Menu menu) {
		gw.enableOpenMenu = false;
		SndMgr.getInstance().restartBgm();
		super.onOptionsMenuClosed(menu);
	}

	/**
	 * オプションメニューアイテムが選択された時の処理
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;

		switch (item.getItemId()) {
		case MENU_ID_FIXSIZE:
			// setFixedSize() を有効にする
			// (Canvas#scaleを無効にする)
			Point p = surface.getScreenSize();
			gw.fixedSizeEnable = true;
			surface.setScreenWH(p.x, p.y);
			break;
		case MENU_ID_DRAWTYPE:
			// 拡大縮小描画を積極的にするかどうかの有効無効を切り替える
			gw.disableScaleDraw = !gw.disableScaleDraw;
			break;
		case MENU_ID_BG0:
			// BG描画の有効無効を切り替える
			gw.layerDrawEnable[0] = !gw.layerDrawEnable[0];
			break;
		case MENU_ID_BG1:
			// BG描画の有効無効を切り替える
			gw.layerDrawEnable[1] = !gw.layerDrawEnable[1];
			break;
		case MENU_ID_ENEMY:
			// ENEMY描画を無効にする
			gw.layerDrawEnable[2] = !gw.layerDrawEnable[2];
			break;
		case MENU_ID_SOUND:
			// サウンド有効無効を切り替える
			SndMgr.getInstance().changeSoundMode();
			break;
		case MENU_ID_BGMOFF:
			// BGM全停止
			SndMgr.getInstance().stopBgmAll();
			break;
		case MENU_ID_BGMCHG:
			// BGM変更
			int n = SndMgr.getInstance().getNextBgmId();
			SndMgr.getInstance().changeBgm(n);
			break;
		default:
			ret = super.onOptionsItemSelected(item);
			break;
		}
		return ret;
	}

	/**
	 * 画面をタッチした時の処理
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 押し下げ
			gw.touchRealX = event.getX();
			gw.touchRealY = event.getY();
			// LogUtil.d("TOUCH", "ACTION_DOWN " + touchRealX + "," +
			// touchRealY);
			break;
		case MotionEvent.ACTION_MOVE:
			// 指をスライド
			// LogUtil.d("TOUCH", "ACTION_MOVE " + event.getX() + "," +
			// event.getY());
			break;
		case MotionEvent.ACTION_UP:
			// 指を持ち上げ
			// LogUtil.d("TOUCH", "ACTION_UP " + event.getX() + "," +
			// event.getY());
			break;
		default:
			break;
		}

		// return super.onTouchEvent(event);
		return true;
	}
}