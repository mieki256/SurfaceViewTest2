package com.blawat2015.SurfaceViewSample;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.blawat2015.SurfaceViewSample.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
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

	// setFixedSize()を使うかどうか
	private boolean fixedSizeEnable = false;

	// 極力拡大縮小しない描画処理をするか否か
	public boolean disableScaleDraw = false;

	// 目標FPS
	private final static long FPS_VALUE = 60;
	private final static long INTERVAL = (1000 * 1000 * 1000) / FPS_VALUE;

	// 標準として扱う画面サイズ
	private final static int defScrW = 240;
	private final static int defScrH = 320;

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

	// 実際に取得したタッチ座標
	public float touchRealX;
	public float touchRealY;

	// 拡大縮小を加味したタッチ座標
	public float touchX;
	public float touchY;
	public boolean touchEnable;

	// フレームカウンタ
	private long frameCounter;

	// Paint使用時の汎用ワーク
	private Paint paint = new Paint();

	// 画像関係
	public ImgMgr img;

	// サウンド関係
	public SndMgr snd;

	// バイブレーション機能関係
	public Vibrator vib;

	// 乱数
	public Random rnd = new Random();

	// キャラの数
	public int charaCount = 0;

	// レベルが変わったかどうか
	public boolean levelChangeEnable = false;
	public int level = 0;

	// BG0,1,雑魚敵の描画有効無効
	public boolean[] layerDrawEnable = new boolean[3];

	// タッチ座標を描画するためのワーク
	public int drawTouchAlpha = 0;
	public int drawTouchRadius = 0;
	public Point touchPoint = new Point();

	// オプションメニュー関係
	public boolean enableOpenMenu = false;

	// ミスした回数
	public int miss = 0;
	public boolean missEnable = false;

	// スローモーション表示
	// 0より大きければ、設定されたフレーム数分、スローモーションになる。
	public int slowMotionCount = 0;

	// 時間記録用(単位：ms)
	public long diffMilliTime = 0;
	public long lastDiffMilliTime = 0;

	/**
	 * 基本となるタスクのクラス
	 */
	public abstract class Task {

		/**
		 * 更新処理
		 *
		 * @return
		 */
		public boolean onUpdate() {
			return true;
		}

		/**
		 * 描画処理
		 *
		 * @param c
		 *            Canvas
		 */
		public void onDraw(Canvas c) {
		}

	}

	/**
	 * BG描画用クラス
	 */
	public class Bg extends Task {

		/**
		 * BGの描画領域を記録するためのクラス
		 */
		public class BgRect {
			public int w, h = 0;
			public boolean drawEnable = false;
			public Rect src = new Rect();
			public Rect dst = new Rect();

			public void setRect(int u, int v, int x, int y, int sw, int sh) {
				w = sw;
				h = sh;
				if (x >= 0 && x < defScrW && y >= 0 && y < defScrH && w > 0
						&& h > 0) {
					// 描画すべき矩形領域なので、描画元と描画先の範囲を指定
					if (w > defScrW - x) w = defScrW - x;
					if (h > defScrH - y) h = defScrH - y;
					src.set(u, v, u + w, v + h);
					dst.set(x, y, x + w, y + h);
					drawEnable = true;
				} else {
					drawEnable = false;
				}
			}

			/**
			 * 描画処理
			 *
			 * @param c
			 *            Canvas
			 * @param p
			 *            Paint
			 * @param bmp
			 *            Bitmap
			 */
			public void draw(Canvas c, Paint p, Bitmap bmp) {
				if (drawEnable) {
					c.drawBitmap(bmp, src, dst, p);
				}
			}
		}

		int kind, bgW, bgH, bgCounter = 0;
		float bgX, bgY;
		int bgU, bgV;
		private Bitmap bmp;
		private BgRect[] r = new BgRect[4];

		/**
		 * コンストラクタ
		 */
		public Bg(int sKind) {
			kind = sKind;
			bmp = img.getBgImg(sKind);

			// 画像の縦横幅を取得
			bgW = bmp.getWidth();
			bgH = bmp.getHeight();

			for (int i = 0; i < r.length; i++) {
				r[i] = new BgRect();
			}
			setPos(bgX, bgY);
		}

		/**
		 * BG座標を設定する.
		 *
		 * @param x
		 *            x座標
		 * @param y
		 *            y座標
		 */
		public void setPos(float x, float y) {
			bgX = x;
			bgY = y;
			setDrawArea();
		}

		/**
		 * BG描画領域(最大4分割)を計算して記録
		 */
		public void setDrawArea() {
			int u = (int) bgX;
			int v = (int) bgY;
			if (u < 0) {
				// マイナス値ならプラス値にする
				u = u + (bgW * ((-u / bgW) + 1));
			}
			if (v < 0) {
				v = v + (bgH * ((-v / bgH) + 1));
			}

			// 0～w,0～hの範囲に収める
			u %= bgW;
			v %= bgH;
			bgU = u;
			bgV = v;

			int uw = bgW - u;
			int vh = bgH - v;

			r[0].setRect(u, v, 0, 0, uw, vh);
			r[1].setRect(0, v, uw, 0, defScrW - uw, vh);
			r[2].setRect(u, 0, 0, vh, uw, defScrH - vh);
			r[3].setRect(0, 0, uw, vh, r[1].w, r[2].h);
		}

		/**
		 * 更新処理
		 */
		@Override
		public boolean onUpdate() {
			// 座標を変化させる
			float spd = (kind == 0) ? 1.0f : 2.0f;
			float dy = -2 * spd;
			bgX = (float) ((bgW / 3) * Math.sin(Math.toRadians(bgCounter)) * spd);
			bgY += dy;
			setDrawArea(); // 描画領域を計算
			bgCounter++;
			return true;
		}

		/**
		 * 描画処理
		 */
		@Override
		public void onDraw(Canvas c) {
			if (!layerDrawEnable[kind]) return;

			paint.setAntiAlias(false);

			// 透明度を指定(255で不透明、0で透明)
			paint.setAlpha((kind == 0) ? 255 : 224);
			// paint.setAlpha(255);

			if (disableScaleDraw) {
				// 画像から一部分を切り出したりせず、無頓着に全部描画する処理
				// drawBitmap()内で拡大縮小処理を必要としない
				int x = -bgU;
				int y = -bgV;
				int w = bmp.getWidth();
				int h = bmp.getHeight();

				c.drawBitmap(bmp, x, y, paint);
				c.drawBitmap(bmp, x + w, y, paint);
				c.drawBitmap(bmp, x, y + h, paint);
				c.drawBitmap(bmp, x + w, y + h, paint);

			} else {
				// 画像から必要な部分だけ切り出して描画する処理
				// drawBitmap()内で拡大縮小をしている可能性がある
				for (int i = 0; i < r.length; i++) {
					r[i].draw(c, paint, bmp);
				}
			}
		}
	}

	/**
	 * フレーム数その他描画クラス
	 *
	 */
	public class DrawFrameNumber extends Task {

		/**
		 * 描画処理
		 */
		@Override
		public void onDraw(Canvas c) {
			// 敵数、ミス回数、時間を描画
			long mills = (lastDiffMilliTime > 0) ? lastDiffMilliTime
					: diffMilliTime;
			String s = String.format("ENEMY: %2d    MISS: %d    TIME: %s",
					charaCount, miss, getTimeStr(mills));
			drawTextWidthBorder(c, s, 0, 24, Color.BLACK, Color.WHITE);
		}
	}

	/**
	 * 時間の文字列を返す
	 *
	 * @param mills
	 *            ミリ秒
	 * @return String (「xx:xx:xx」にした文字列)
	 */
	public String getTimeStr(long mills) {
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

	/**
	 * タッチ座標を記憶するクラス
	 */
	public class TouchPoint extends Task {
		int alpha = 0;
		int x = 0;
		int y = 0;

		/**
		 * 更新処理
		 */
		@Override
		public boolean onUpdate() {
			// タッチ座標を、拡大縮小を考慮した値に変換
			if (touchRealX > 0 || touchRealY > 0) {
				touchX = touchRealX / scaleX - (screenBorderW / 2);
				touchY = touchRealY / scaleY - (screenBorderH / 2);
				touchRealX = touchRealY = 0;
				// LogUtil.d("TOUCH", "scale " + touchX + "," + touchY);
				touchPoint.x = (int) touchX;
				touchPoint.y = (int) touchY;
				drawTouchAlpha = 255;
				drawTouchRadius = 8;
				touchEnable = true;
			} else {
				touchEnable = false;
			}
			return true;
		}

		/**
		 * タッチ関連情報を消去
		 */
		public void clear() {
			touchRealX = touchRealY = 0;
			touchX = touchY = 0;
			touchEnable = false;
			drawTouchAlpha = 0;
		}

	}

	/**
	 * タッチ座標を描画するクラス
	 */
	public class DrawTouchPoint extends Task {

		/**
		 * 描画処理
		 */
		@Override
		public void onDraw(Canvas c) {
			// alphaが0なら描画する必要はない
			if (drawTouchAlpha <= 0) return;

			// タッチした座標に十字線を描画
			int x0, y0, x1, y1;
			x0 = 0;
			x1 = defScrW;
			y0 = y1 = touchPoint.y;

			if (missEnable) {
				// 敵に当たってない
				paint.setColor(Color.argb(255, 255, 62, 0)); // 描画色を指定
			} else {
				// 敵に当たってる
				paint.setColor(Color.CYAN);
			}

			paint.setAntiAlias(false); // アンチエイリアスを指定
			paint.setAlpha(drawTouchAlpha); // 透明度を指定
			c.drawLine(x0, y0, x1, y1, paint); // 線を描画

			x0 = x1 = touchPoint.x;
			y0 = 0;
			y1 = defScrH;
			c.drawLine(x0, y0, x1, y1, paint);

			// 円を描画
			paint.setStyle(Style.FILL);
			c.drawCircle(touchPoint.x, touchPoint.y, drawTouchRadius, paint);

			drawTouchRadius += (48 - drawTouchRadius) / 3;
			drawTouchAlpha -= 32;
			if (drawTouchAlpha <= 0) drawTouchAlpha = 0;
		}
	}

	/**
	 * タイトル処理クラス
	 */
	public class Title extends Task {
		int x, y, ox, oy, bmpw, bmph, cnt;
		Rect src;
		RectF dst;
		Bitmap bmp;

		public Title() {
			bmp = img.bmp[ImgMgr.ID_LOGO_TITLE];
			bmpw = bmp.getWidth();
			bmph = bmp.getHeight();
			ox = defScrW / 2;
			oy = 130;
			cnt = 0;
			src = new Rect();
			dst = new RectF();
		}

		@Override
		public boolean onUpdate() {
			float ww = 32f;
			float wf = ww + (float) (ww * Math.sin(Math.toRadians(cnt)));
			float hf = wf * bmph / bmpw;
			float sw = (bmpw / 2 - wf);
			float sh = (bmph / 2 - hf);
			src.set(0, 0, bmpw, bmph);
			dst.set(ox - sw, oy - sh, ox + sw, oy + sh);
			cnt += 4;
			if (touchEnable) {
				// 画面をタッチされた
				touchX = touchY = 0;
				drawTouchAlpha = 0;
				return false;
			}
			return true;
		}

		@Override
		public void onDraw(Canvas c) {
			if (!layerDrawEnable[2]) return;
			paint.setAntiAlias(true);
			paint.setAlpha(255);
			c.drawBitmap(bmp, src, dst, paint);
		}
	};

	/**
	 * ステージクリア処理用クラス
	 */
	public class StageClear extends Task {
		int step = 0;
		int count = 0;
		int dispMiss = 0;
		int dispFrame = 0;

		public StageClear() {
			init(0, 0);
		}

		/**
		 * 初期化処理
		 */
		public void init(int missValue, int frameValue) {
			step = 0;
			count = (int) FPS_VALUE * 3 / 4;
			dispMiss = missValue;
			dispFrame = frameValue;
		}

		/**
		 * 更新処理
		 */
		@Override
		public boolean onUpdate() {
			boolean result = true;
			switch (step) {
			case 0:
				// 一定時間待つ
				if (--count <= 0) {
					touchX = touchY = 0;
					touchEnable = false;
					snd.playSe(SndMgr.SE_STGCLR);
					step++;
				}
				break;

			case 1:
				// 画面がタッチされるまで待つ
				if (touchEnable) step++;
				break;
			case 2:
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
			if (step == 1) {
				// ロゴ描画
				paint.setAlpha(255);
				paint.setColor(Color.BLACK);
				paint.setAntiAlias(false);
				Bitmap b = img.bmp[ImgMgr.ID_LOGO_CLEAR];
				int x = (defScrW - b.getWidth()) / 2;
				int y = 120;
				c.drawBitmap(img.bmp[ImgMgr.ID_LOGO_CLEAR], x, y, paint);

				// 文字を載せる背景矩形を描画
				paint.setAlpha(255);
				paint.setStyle(Style.FILL);
				paint.setColor(Color.argb(128, 0, 0, 0));
				int x0 = 0;
				int y0 = 180;
				int x1 = defScrW;
				int y1 = y0 + 48;
				c.drawRect(x0, y0, x1, y1, paint);

				// ミス回数とフレーム数を描画
				y = y0 + 16;
				drawTextWidthBorder(c, "MISS: " + dispMiss + "    TIME: "
						+ getTimeStr(lastDiffMilliTime), x, y, Color.WHITE,
						Color.BLACK);
			}
		}
	}

	/**
	 * SpeedUpロゴ表示用クラス
	 */
	public class SpdUpLogo extends Task {
		int step = 0;
		int cnt = 0;
		int x = 0;
		int y = 0;
		int dy = 0;
		int alpha = 0;
		Bitmap bmp = null;

		/**
		 * コンストラクタ
		 */
		public SpdUpLogo() {
			init();
		}

		/**
		 * 初期化処理
		 */
		public void init() {
			bmp = img.bmp[ImgMgr.ID_LOGO_SPEEDUP];
			step = 0;
			cnt = 0;
			x = (defScrW - bmp.getWidth()) / 2;
			y = defScrH / 2;
			dy = -1;
			alpha = 0;
		}

		/**
		 * 描画開始
		 */
		public void setDispEnable() {
			if (step <= 1) step = 2;
		}

		/**
		 * 更新処理
		 *
		 * @return
		 */
		@Override
		public boolean onUpdate() {
			switch (step) {
			case 0:
				init();
				step++;
				break;

			case 1:
				break;

			case 2:
				y = defScrH / 2;
				alpha = 0;
				step++;
				break;

			case 3:
				y += dy;
				alpha += 24;
				if (alpha >= 255) {
					alpha = 255;
					cnt = (int) FPS_VALUE;
					step++;
				}
				break;
			case 4:
				y += dy;
				if (--cnt <= 0) {
					step++;
				}
				break;
			case 5:
				y += dy;
				alpha -= 24;
				if (alpha <= 0) init();
				break;
			default:
				break;
			}
			return true;
		}

		/**
		 * 描画処理
		 *
		 * @param c
		 *            Canvas
		 */
		@Override
		public void onDraw(Canvas c) {
			if (step > 2) {
				paint.setAlpha(alpha);
				paint.setAntiAlias(true);
				c.drawBitmap(bmp, x, y, paint);
			}
		}
	}

	/**
	 * 画面の中をウロウロする雑魚敵のクラス
	 */
	public class ZakoChara extends Task {
		int sw = 32; // 画像横幅
		int sh = 32; // 画像縦幅

		public boolean befg = false; // 存在フラグ
		int imgKind = 0; // キャラ画像種類
		int patNum = 0; // キャラ画像パターン番号
		int alpha = 255; // 描画時の透明度
		float scale = 1.0f; // 描画時の拡大縮小率

		float x = 0f; // x座標
		float y = 0f; // y座標
		float dx = 0; // x速度
		float dy = 0; // y速度

		Rect src = new Rect(); // 描画元範囲
		Rect dst = new Rect(); // 描画先範囲
		Rect hitArea = new Rect();
		boolean drawHitAreaEnable = false;

		Bitmap img0;

		int step = 0;
		int cnt = 0;
		public boolean deadStart = false;

		/**
		 * コンストラクタ
		 */
		public ZakoChara() {
			init();
		}

		/**
		 * 初期化処理
		 */
		public void init() {
			befg = true;
			step = 0;

			alpha = 255;
			scale = 1.0f;
			drawHitAreaEnable = false;
			deadStart = false;

			// 初期座標設定
			x = defScrW / 2;
			y = defScrH / 2;

			// 速度設定
			int ang = rnd.nextInt(360);
			float spd = (float) (rnd.nextInt(30) + 10) / 10;
			double rad = Math.toRadians(ang);
			dx = (float) (spd * Math.cos(rad));
			dy = (float) (spd * Math.sin(rad));

			// 速度に応じて表示するパターンを設定
			if (spd > 3) {
				imgKind = 2;
			} else if (spd > 2) {
				imgKind = 1;
			} else {
				imgKind = 0;
			}
			patNum = 0;
			img0 = img.charaImg;

			setRect();
		}

		/**
		 * 描画に必要な座標範囲を設定する
		 */
		public void setRect() {
			int sx = sw * patNum;
			int sy = sh * imgKind;
			int w = (int) ((sw / 2) * scale);
			int h = (int) ((sh / 2) * scale);

			// 描画元範囲指定
			if (disableScaleDraw) {
				// 画像をそのまま描画する場合
				src.set(0, 0, sw, sh);
			} else {
				// 画像から一部分を切り出して描画する場合
				src.set(sx, sy, sx + sw, sy + sh);
			}

			// 描画先範囲指定
			dst.set((int) (x - w), (int) (y - h), (int) (x + w), (int) (y + h));
		}

		/**
		 * 更新処理
		 */
		@Override
		public boolean onUpdate() {
			if (!befg) return true;

			switch (step) {
			case 0:
				// 通常移動

				charaCount++;

				if (levelChangeEnable) {
					// レベルが変わったので速度を微妙に速くする
					if (level == 1) {
						dx *= 1.2f;
						dy *= 1.2f;
					} else {
						dx *= 1.6f;
						dy *= 1.6f;
					}
				}

				// 速度を加算
				x += dx;
				y += dy;

				// 画面端に来たら移動方向を反転
				int bw = (sw / 2);
				int bh = (sh / 2);
				if (x < bw || x > defScrW - bw) {
					dx *= -1;
				}
				if (y < bh || y > defScrH - bh) {
					dy *= -1;
				}

				if (frameCounter % 8 == 0) {
					// アニメパターン番号を1つ進める
					patNum++;
					patNum %= 2;
				}

				if (touchEnable) {
					// 画面をタップしているのでアタリ判定

					int w = (sw / 2);
					int h = (sh / 2);
					hitArea.set((int) (x - w), (int) (y - h), (int) (x + w),
							(int) (y + h));
					if (hitArea.left < touchX && touchX < hitArea.right
							&& hitArea.top < touchY && touchY < hitArea.bottom) {
						// タッチされた
						touchX = touchY = 0;
						touchEnable = false;

						drawHitAreaEnable = true;
						patNum = 2;
						deadStart = true;
						alpha = 200;
						cnt = (int) (FPS_VALUE / 3);
						step++;
					}
				}
				break;
			case 1:
				// 消滅処理
				scale += (6 - scale) / 8;
				alpha -= 3;
				if (alpha <= 0) alpha = 0;
				if (--cnt <= 0) {
					drawHitAreaEnable = false;
					befg = false;
				}
				break;
			default:
				break;

			}
			setRect();
			return true;
		}

		/**
		 * 描画処理
		 */
		@Override
		public void onDraw(Canvas c) {
			if (!befg || !layerDrawEnable[2] || step >= 2) return;

			// 描画
			paint.setAntiAlias(false);
			paint.setAlpha(alpha);

			if (disableScaleDraw) {
				int n = ImgMgr.ID_CHARA_SPLIT + (imgKind * 3) + patNum;
				Bitmap limg = img.bmp[n];
				if (scale == 1.0f) {
					// 等倍描画
					c.drawBitmap(limg, dst.left, dst.top, paint);
				} else {
					// 拡大縮小描画
					c.drawBitmap(limg, src, dst, paint);
				}
			} else {
				// 画像の一部分を切り出して描画する場合
				c.drawBitmap(img0, src, dst, paint);
			}

			if (drawHitAreaEnable) {
				paint.setAlpha(255);
				paint.setColor(Color.RED);
				paint.setStyle(Style.STROKE);
				c.drawRect(hitArea, paint);
			}
		}
	}

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
			charaCount = ENEMY_MAX;
			step = 0;
			miss = 0;
			missEnable = false;
			level = 0;
			frameCounter = 0;
			for (int i = 0; i < taskList.size(); i++) {
				taskList.get(i).init();
			}
			startTime = nowTime = System.currentTimeMillis();
			lastDiffMilliTime = diffMilliTime = 0;
		}

		/**
		 * 更新処理
		 */
		@Override
		public boolean onUpdate() {
			boolean result = true;

			int oldCharaCount = charaCount;
			charaCount = 0; // 敵数カウント用変数をクリア
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
							snd.playSeEnemyDamage();
						} else {
							// 最後の一匹なら別SE、かつ、スローモーション
							snd.playSe(SndMgr.SE_VOICE_UWAA_DELAY);
							slowMotionCount = (int) (FPS_VALUE * 1.5);
							lastDiffMilliTime = diffMilliTime;
						}

						// リストから取り出して一番最後に配置
						// 一番手前に描画されるようにする
						taskList.remove(i);
						i--;
						taskList.add(z);

						// BGM変更チェックをするように要求
						bgmChangeEnable = true;

						z.deadStart = false;
						missEnable = false;
					}
				}
				if (z.befg) countBeNum++;
			}

			nowTime = System.currentTimeMillis();
			diffTime = nowTime - startTime;
			diffMilliTime = diffTime;

			switch (step) {
			case 0:
				levelChangeEnable = false;

				// BGMを変更すべきかチェック
				if (bgmChangeEnable) {
					// 特定の敵数になったらBGMを変更
					switch (level) {
					case 0:
						if (charaCount <= 50 + 1) {
							snd.stopBgm();
							snd.startBgm(SndMgr.BGM_MILD);
							levelChangeEnable = true;
							level++;
						}
						break;

					case 1:
						if (charaCount <= 10 + 1) {
							snd.stopBgm();
							snd.startBgm(SndMgr.BGM_BOSS);
							levelChangeEnable = true;
							level++;
						}
						break;

					case 2:
						if (charaCount <= 1) {
							snd.stopBgm();
							level++;
						}
						break;
					default:
						break;
					}
				}

				spdLogo.onUpdate();
				if (levelChangeEnable) spdLogo.setDispEnable();

				if (countBeNum <= 0) {
					// 動いてる敵が一匹も居ない
					step++;
				} else {
					// 動いている敵が居る
					if (touchEnable) {
						// ここまでタッチ情報がクリアされていないということは、
						// 敵をタッチできなかった状態ということ
						touchX = touchY = 0;
						touchEnable = false;
						miss++; // ミス回数を+1する
						missEnable = true;
						snd.playSe(SndMgr.SE_MISS); // ミスSEを再生

						// バイブを振動(単位はms)
						vib.vibrate(200);
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

	/**
	 * タスク管理クラス
	 */
	public class GameMgr {
		Bg bg0;
		Bg bg1;
		TouchPoint tp;
		EnemyMgr enemyMgr;
		DrawTouchPoint drawTp;
		DrawFrameNumber drawFn;
		Title title;
		StageClear stgClr;
		int step = 0;
		int stepSub = 0;
		int count = 0;

		GameMgr() {
			step = 0;
			miss = 0;
			slowMotionCount = 0;
			img = new ImgMgr();
			snd = new SndMgr();
		}

		/**
		 * 更新処理
		 */
		public boolean onUpdate() {
			if (slowMotionCount % 4 == 0) {

				switch (step) {
				case 0:
					img.loadImageRes(); // 画像読み込み
					snd.loadSoundRes(); // サウンドデータ読み込み
					levelChangeEnable = false;
					slowMotionCount = 0;
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
					for (int i = 0; i < layerDrawEnable.length; i++) {
						layerDrawEnable[i] = true;
					}
					diffMilliTime = 0;
					lastDiffMilliTime = 0;
					step++;
					break;

				case 2:
					// タイトル画面表示
					if (!enableOpenMenu) {
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
					if (!enableOpenMenu) {
						boolean clearFg = false;

						bg0.onUpdate();
						bg1.onUpdate();
						tp.onUpdate();
						clearFg = !enemyMgr.onUpdate();
						drawTp.onUpdate();
						drawFn.onUpdate();
						if (clearFg) {
							stgClr.init(miss, (int) frameCounter);
							step++;
						}
					}
					break;

				case 4:
					// ステージクリア
					if (!enableOpenMenu) {
						bg0.onUpdate();
						bg1.onUpdate();
						tp.onUpdate();
						enemyMgr.onUpdate();
						if (!stgClr.onUpdate()) {
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

			if (slowMotionCount > 0) slowMotionCount--;

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

	/**
	 * 画像管理用クラス
	 */
	public class ImgMgr {
		// ビットマップ画像
		public Bitmap charaImg;

		public Bitmap[] bmp;

		public final static int ID_BG0 = 0;
		public final static int ID_BG1 = 1;
		public final static int ID_CHARA = 2;
		public final static int ID_CHARA_SPLIT = 3;
		public final static int ID_LOGO_TITLE = 12;
		public final static int ID_LOGO_CLEAR = 13;
		public final static int ID_LOGO_SPEEDUP = 14;

		// 画像リソースIDのリスト
		private final int[] imgId = {
				R.drawable.bg320x384, // 0
				R.drawable.bg320x384_2, // 1
				R.drawable.chara1_32, // 2
				R.drawable.chara2_32x32_00, // 3
				R.drawable.chara2_32x32_01, // 4
				R.drawable.chara2_32x32_02, // 5
				R.drawable.chara2_32x32_03, // 6
				R.drawable.chara2_32x32_04, // 7
				R.drawable.chara2_32x32_05, // 8
				R.drawable.chara2_32x32_06, // 9
				R.drawable.chara2_32x32_07, // 10
				R.drawable.chara2_32x32_08, // 11
				R.drawable.logo_title, // 12
				R.drawable.logo_clear, // 13
				R.drawable.logo_speedup, // 14
		};

		/**
		 * コンストラクタ
		 */
		public ImgMgr() {
			bmp = new Bitmap[imgId.length];

		}

		/**
		 * 画像読み込み
		 */
		public void loadImageRes() {
			for (int i = 0; i < imgId.length; i++) {
				bmp[i] = BitmapFactory.decodeResource(getResources(), imgId[i]);
			}

			charaImg = bmp[ID_CHARA];
		}

		/**
		 * BG用のbitmapを返す
		 *
		 * @param kind
		 *            0 or 1
		 * @return Bitmap
		 */
		public Bitmap getBgImg(int kind) {
			return bmp[((kind == 0) ? ID_BG0 : ID_BG1)];
		}

		/**
		 * 画像を全て破棄
		 */
		public void recycleImageAll() {
			for (int i = 0; i < bmp.length; i++) {
				if (bmp[i] != null) {
					bmp[i].recycle();
					bmp[i] = null;
				}
			}
		}

	}

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
		private final int[] seVoiceList = {
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
		private AudioManager audioManager;

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

			// マナーモード等の判別用
			audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
			checkSilentMode(); // 消音すべきモードかチェック
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
		 */
		public void loadSoundRes() {
			seLoadComplete = false;

			// BGMデータ読み込み
			for (int i = 0; i < bgmResIdList.length; i++) {
				bgm[i] = MediaPlayer.create(getApplicationContext(),
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
				seId[i] = sndPool.load(getApplicationContext(), seResIdList[i],
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

		/**
		 * 敵ダメージ時の音声SEをランダムに選んで再生
		 */
		public void playSeEnemyDamage() {
			int n = seVoiceList[rnd.nextInt(seVoiceList.length)];
			playSe(n);
		}
	}

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
			// LogUtil.d("SURFACE", "MySurfaceView()");
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
			if (fixedSizeEnable) {
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
			scrW = width;
			scrH = height;
			scaleX = ((float) scrW) / ((float) defScrW);
			scaleY = ((float) scrH) / ((float) defScrH);

			// LogUtil.d("INFO", "Window w,h = " + scrW + "," + scrH);
			// LogUtil.d("INFO", "DefWdw w,h = " + defScrW + "," + defScrH);
			// LogUtil.d("INFO", "Scale x,y = " + scaleX + "," + scaleY);

			screenBorderW = 0;
			screenBorderH = 0;
			if (scaleX < scaleY) {
				screenBorderH = (scrH * defScrW / scrW) - defScrH;
				scaleY = scaleX;
			} else if (scaleX > scaleY) {
				screenBorderW = (scrW * defScrH / scrH) - defScrW;
				scaleX = scaleY;
			}
			virtualScrW = defScrW + screenBorderW;
			virtualScrH = defScrH + screenBorderH;

			if (fixedSizeEnable) {
				holder.setFixedSize(virtualScrW, virtualScrH);
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
			img.recycleImageAll();

			// BGMとSEを停止して破棄する
			snd.releaseBgmAll();
			snd.releaseSeAll();
		}

		// 描画処理
		private void onDraw() {

			Canvas c;

			// Canvasを取得
			if (fixedSizeEnable) {
				// setFixedSize使用時
				rect.set(0, 0, virtualScrW, virtualScrH);
				c = holder.lockCanvas(rect);
			} else {
				// Canvas#scale()使用時
				c = holder.lockCanvas();
			}

			if (c != null) {
				// 背景を指定色で塗りつぶし
				c.drawColor(Color.GRAY);

				if (!fixedSizeEnable) {
					// 画面サイズに合わせて拡大縮小率を指定
					c.scale(scaleX, scaleY);
				}

				// 描画位置をずらす
				if (screenBorderH > 0) {
					c.translate(0, screenBorderH / 2);
				} else if (screenBorderW > 0) {
					c.translate(screenBorderW / 2, 0);
				}

				// クリッピング範囲を指定
				c.clipRect(0, 0, defScrW, defScrH);

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
					gameMgr.onUpdate(); // 更新処理
					onDraw(); // 描画処理
					frameCounter++;
					calcFPS(); // FPSを計算
				}
			}, 0, INTERVAL, TimeUnit.NANOSECONDS);
			// INTERVALの間隔で処理が行われる
		}

		/**
		 * FPSを測定・計算する
		 */
		private void calcFPS() {
			fpsFrameCounter++;
			calcInterval += INTERVAL;

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
					FPS_VALUE, frameCounter);
			drawTextWidthBorder(c, s, x, y, Color.BLACK, Color.WHITE);
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

	MySurfaceView surface = null;

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
		vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

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
		enableOpenMenu = true;

		menu.findItem(MENU_ID_FIXSIZE).setVisible(!fixedSizeEnable);

		menu.findItem(MENU_ID_DRAWTYPE).setTitle(
				(disableScaleDraw) ? R.string.menu_drawtype0
						: R.string.menu_drawtype1);

		menu.findItem(MENU_ID_BG0).setTitle(
				(layerDrawEnable[0]) ? R.string.menu_bg0_off
						: R.string.menu_bg0_on);
		menu.findItem(MENU_ID_BG1).setTitle(
				(layerDrawEnable[1]) ? R.string.menu_bg1_off
						: R.string.menu_bg1_on);
		menu.findItem(MENU_ID_ENEMY).setTitle(
				(layerDrawEnable[2]) ? R.string.menu_enemy_off
						: R.string.menu_enemy_on);

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
		enableOpenMenu = false;
		snd.restartBgm();
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
			fixedSizeEnable = true;
			surface.setScreenWH(p.x, p.y);
			break;
		case MENU_ID_DRAWTYPE:
			// 拡大縮小描画を積極的にするかどうかの有効無効を切り替える
			disableScaleDraw = !disableScaleDraw;
			break;
		case MENU_ID_BG0:
			// BG描画の有効無効を切り替える
			layerDrawEnable[0] = !layerDrawEnable[0];
			break;
		case MENU_ID_BG1:
			// BG描画の有効無効を切り替える
			layerDrawEnable[1] = !layerDrawEnable[1];
			break;
		case MENU_ID_ENEMY:
			// ENEMY描画を無効にする
			layerDrawEnable[2] = !layerDrawEnable[2];
			break;
		case MENU_ID_SOUND:
			// サウンド有効無効を切り替える
			snd.changeSoundMode();
			break;
		case MENU_ID_BGMOFF:
			// BGM全停止
			snd.stopBgmAll();
			break;
		case MENU_ID_BGMCHG:
			// BGM変更
			int n = snd.getNextBgmId();
			snd.stopBgm();
			snd.startBgm(n);
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
			touchRealX = event.getX();
			touchRealY = event.getY();
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