package com.blawat2015.SurfaceViewSample;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Vibrator;

/**
 * グローバル変数相当
 */
public class GWk {
	private static GWk instance = new GWk();

	// 目標FPS
	public final static long FPS_VALUE = 60;
	public final static long INTERVAL = (1000 * 1000 * 1000) / FPS_VALUE;

	// 標準として扱う画面サイズ
	public final static int DEF_SCR_W = 240;
	public final static int DEF_SCR_H = 320;

	// setFixedSize()を使うかどうか
	public boolean fixedSizeEnable;

	// 極力拡大縮小しない描画処理をするか否か
	public boolean disableScaleDraw;

	// 実機上の実画面サイズ
	public int scrW;
	public int scrH;

	// 仮想実画面サイズ
	public int virtualScrW;
	public int virtualScrH;

	// 拡大縮小して余った領域を塗り潰すための、幅、高さの記録用
	public int screenBorderW;
	public int screenBorderH;

	// 画面の拡大縮小率
	public float scaleX;
	public float scaleY;

	// BG0,BG1,雑魚敵の描画有効無効
	public boolean[] layerDrawEnable = new boolean[3];

	public Vibrator vib; // バイブレーション機能関係
	public Random rnd = new Random(); // 乱数

	// 実際に取得したタッチ座標
	public float touchRealX;
	public float touchRealY;

	// 拡大縮小を加味したタッチ座標
	public float touchX;
	public float touchY;
	public boolean touchEnable;

	// タッチ座標を描画するためのワーク
	public int drawTouchAlpha = 0;
	public int drawTouchRadius = 0;
	public Point touchPoint = new Point();

	// ミスした回数
	public int miss = 0;
	public boolean missEnable = false;

	// 時間記録用(単位：ms)
	public long diffMilliTime = 0;
	public long lastDiffMilliTime = 0;

	// キャラの数
	public int charaCount = 0;

	// レベルが変わったかどうか
	public boolean levelChangeEnable;
	public int level;

	// フレームカウンタ
	public long frameCounter;

	// スローモーション表示
	// 0より大きければ、設定されたフレーム数分、スローモーションになる。
	public int slowMotionCount = 0;

	// オプションメニューを開いているか否か
	public boolean enableOpenMenu;

	Paint paint = new Paint();

	/**
	 * コンストラクタ
	 */
	private GWk() {
		fixedSizeEnable = false;
		disableScaleDraw = false;
		levelChangeEnable = false;
		enableOpenMenu = false;
		level = 0;
	}

	public static GWk getInstance() {
		return instance;
	}

	/**
	 * タッチ情報をクリア
	 */
	public void clearTouchInfo() {
		touchX = touchY = 0;
		touchEnable = false;
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
	public void drawTextWidthBorder(Canvas c, String s, int x, int y,
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
