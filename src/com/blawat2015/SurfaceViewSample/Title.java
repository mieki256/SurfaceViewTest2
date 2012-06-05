package com.blawat2015.SurfaceViewSample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * タイトル処理クラス
 */
public class Title extends Task {
	GWk gw;
	SndMgr snd;
	int x, y, ox, oy, bmpw, bmph, cnt;
	Rect src;
	RectF dst;
	Bitmap bmp;
	Bitmap[] sndbmp = new Bitmap[2];
	Paint paint = new Paint();
	int sndBmpId;

	// サウンド有効無効マーク表示用の情報
	private final static int SNDMARK_WH = 24; // 縦横幅
	private final static int SNDMARK_X = GWk.DEF_SCR_W -  SNDMARK_WH - 4;
	private final static int SNDMARK_Y = 32;

	/**
	 * コンストラクタ
	 */
	public Title() {
		gw = GWk.getInstance();
		snd = SndMgr.getInstance();
		bmp = ImgMgr.getInstance().bmp[ImgMgr.ID_LOGO_TITLE];
		sndbmp[0] = ImgMgr.getInstance().bmp[ImgMgr.ID_SOUNDOFF];
		sndbmp[1] = ImgMgr.getInstance().bmp[ImgMgr.ID_SOUNDON];
		bmpw = bmp.getWidth();
		bmph = bmp.getHeight();
		ox = GWk.DEF_SCR_W / 2;
		oy = 130;
		cnt = 0;
		src = new Rect();
		dst = new RectF();
		sndBmpId = 0;
	}

	/**
	 * 更新処理
	 */
	@Override
	public boolean onUpdate() {

		boolean result = true;

		// タイトルロゴの拡大縮小値を設定
		float ww = 32f;
		float wf = ww + (float) (ww * Math.sin(Math.toRadians(cnt)));
		float hf = wf * bmph / bmpw;
		float sw = (bmpw / 2 - wf);
		float sh = (bmph / 2 - hf);
		src.set(0, 0, bmpw, bmph);
		dst.set(ox - sw, oy - sh, ox + sw, oy + sh);
		cnt += 4;

		if (gw.touchEnable) {
			// 画面をタッチされた

			if (SNDMARK_X < gw.touchX && gw.touchX < SNDMARK_X + SNDMARK_WH
					&& SNDMARK_Y < gw.touchY
					&& gw.touchY < SNDMARK_Y + SNDMARK_WH) {
				// サウンドマーク内でタッチされた。サウンドの有効無効を切替
				snd.changeSoundMode();
				if ( snd.isSoundEnable() ) {
					snd.playSe(SndMgr.SE_VOICE_IYOU);
				}
			} else {
				// サウンドマーク外でタッチされた。ゲーム開始
				result = false;
			}
			gw.clearTouchDrawInfo(); // タッチ座標描画をクリア
		}

		// サウンド有効無効を表示に反映
		sndBmpId = (snd.isSoundEnable()) ? 1 : 0;

		return result;
	}

	/**
	 * 描画処理
	 */
	@Override
	public void onDraw(Canvas c) {
		if (!gw.layerDrawEnable[2]) return;

		// タイトルロゴ描画
		paint.setAntiAlias(true);
		paint.setAlpha(255);
		c.drawBitmap(bmp, src, dst, paint);

		// サウンドマーク描画
		c.drawBitmap(sndbmp[sndBmpId], SNDMARK_X, SNDMARK_Y, paint);
	}

};
