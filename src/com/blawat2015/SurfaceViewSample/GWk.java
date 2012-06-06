package com.blawat2015.SurfaceViewSample;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Vibrator;
import android.view.Window;

/**
 * グローバル変数相当
 */
final class GWk {
	// 目標FPS
	public final static long FPS_VALUE = 60;
	public final static long INTERVAL = (1000 * 1000 * 1000) / FPS_VALUE;

	// 標準として扱う画面サイズ
	public final static int DEF_SCR_W = 240;
	public final static int DEF_SCR_H = 320;

	// setFixedSize()を使うかどうか
	public static boolean fixedSizeEnable;

	// 極力拡大縮小しない描画処理をするか否か
	public static boolean disableScaleDraw;

	// 実機上の実画面サイズ
	public static int scrW, scrH;

	// 仮想実画面サイズ
	public static int virtualScrW, virtualScrH;

	// 拡大縮小して余った領域を塗り潰すための、幅、高さの記録用
	public static int screenBorderW, screenBorderH;

	// 画面の拡大縮小率
	public static float scaleX, scaleY;

	// BG0,BG1,雑魚敵の描画有効無効
	public static boolean[] layerDrawEnable = new boolean[3];

	public static Vibrator vib; // バイブレーション機能関係
	public static AudioManager amgr; // サイレントモード判別用
	public static Random rnd = new Random(); // 乱数

	// 実際に取得したタッチ座標
	public static float touchRealX,touchRealY;

	// 拡大縮小を加味したタッチ座標
	public static float touchX, touchY;
	public static boolean touchEnable;

	// タッチ座標を描画するためのワーク
	public static int drawTouchAlpha = 0;
	public static int drawTouchRadius = 0;
	public static Point touchPoint = new Point();

	// ミスした回数
	public static int miss = 0;
	public static boolean missEnable = false;

	// 時間記録用(単位：ms)
	public static long diffMilliTime = 0;
	public static long lastDiffMilliTime = 0;

	// キャラの数
	public static int charaCount = 0;

	// レベルが変わったかどうか
	public static boolean levelChangeEnable;
	public static int level;

	// フレームカウンタ
	public static long frameCounter = 0;

	// スローモーション表示
	// 0より大きければ、設定されたフレーム数分、スローモーションになる。
	public static int slowMotionCount = 0;

	// オプションメニューを開いているか否か
	public static boolean enableOpenMenu = false;

	public static Paint paint = new Paint();

	public static Window window;

	/**
	 * コンストラクタ
	 */
	public GWk() {
		fixedSizeEnable = false;
		disableScaleDraw = false;
		levelChangeEnable = false;
		level = 0;
	}

	/**
	 * タッチ情報をクリア
	 */
	public static void clearTouchInfo() {
		touchX = touchY = 0;
		touchEnable = false;
	}

	/**
	 * タッチ情報(描画用情報も含む)をクリア
	 */
	public static void clearTouchDrawInfo() {
		touchX = touchY = 0;
		drawTouchAlpha = 0;
	}

	/**
	 * 時間の文字列を返す
	 *
	 * @param mills
	 *            ミリ秒
	 * @return String (「xx:xx:xx」にした文字列)
	 */
	public static String getTimeStr(long mills) {
		long hh = mills / (60 * 60 * 1000);
		long mm = (mills / (60 * 1000)) % 60;
		long ss = (mills / (1000)) % 60;
		// long mi = (mills % 1000);

		return String.format("%02d:%02d:%02d", hh, mm, ss);
	}

	/**
	 * 縁取り文字を描画する
	 *
	 * @param c
	 *            Canvas
	 * @param s
	 *            文字列
	 * @param x
	 *            描画x座標
	 * @param y
	 *            描画y座標
	 * @param fgcolor
	 *            文字色(Color.BLACK等)
	 * @param bgcolor
	 *            縁取り色(Volor.WHITE等)
	 */
	public static void drawTextWidthBorder(Canvas c, String s, int x, int y,
			int fgcolor, int bgcolor) {
		paint.setAlpha(255);
		paint.setAntiAlias(true);

		// 何度か描画して縁取りをする
		paint.setColor(bgcolor);
		c.drawText(s, x - 1, y + 0, paint);
		c.drawText(s, x + 1, y + 0, paint);
		c.drawText(s, x + 0, y - 1, paint);
		c.drawText(s, x + 0, y + 1, paint);

		// 文字描画
		paint.setColor(fgcolor);
		c.drawText(s, x + 0, y + 0, paint);
	}

}
