package com.blawat2015.SurfaceViewSample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * BG描画用クラス
 */
public class Bg extends Task {

	/**
	 * BGの描画領域を記録するためのクラス
	 */
	public class BgRect {
		public int w, h;
		public boolean drawEnable;
		public Rect src, dst;

		public BgRect() {
			w = h = 0;
			drawEnable = false;
			src = new Rect();
			dst = new Rect();
		}

		public void setRect(int u, int v, int x, int y, int sw, int sh,
				int scrw, int scrh) {
			w = sw;
			h = sh;
			if (x >= 0 && x < scrw && y >= 0 && y < scrh && w > 0 && h > 0) {
				// 描画すべき矩形領域なので、描画元と描画先の範囲を指定
				if (w > scrw - x) w = scrw - x;
				if (h > scrh - y) h = scrh - y;
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
			if (drawEnable) c.drawBitmap(bmp, src, dst, p);
		}
	}

	GWk gw;
	private Paint paint = new Paint();
	int kind, bgW, bgH, bgCounter = 0;
	float bgX, bgY;
	int bgU, bgV;
	private Bitmap bmp;
	private BgRect[] r = new BgRect[4];

	/**
	 * コンストラクタ
	 */
	public Bg(int sKind) {
		gw = GWk.getInstance();
		kind = sKind;
		bmp = ImgMgr.getInstance().getBgImg(sKind);

		// 画像の縦横幅を取得
		bgW = bmp.getWidth();
		bgH = bmp.getHeight();

		for (int i = 0; i < r.length; i++)
			r[i] = new BgRect();

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
		int dw = GWk.DEF_SCR_W;
		int dh = GWk.DEF_SCR_H;

		r[0].setRect(u, v, 0, 0, uw, vh, dw, dh);
		r[1].setRect(0, v, uw, 0, dw - uw, vh, dw, dh);
		r[2].setRect(u, 0, 0, vh, uw, dh - vh, dw, dh);
		r[3].setRect(0, 0, uw, vh, r[1].w, r[2].h, dw, dh);
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
		if (!gw.layerDrawEnable[kind]) return;

		paint.setAntiAlias(false);

		// 透明度を指定(255で不透明、0で透明)
		paint.setAlpha((kind == 0) ? 255 : 224);
		// paint.setAlpha(255);

		if (gw.disableScaleDraw) {
			// 画像から一部分を切り出したりせず、無頓着に全部描画する処理
			// drawBitmap()内で拡大縮小処理を必要としない、はず
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
