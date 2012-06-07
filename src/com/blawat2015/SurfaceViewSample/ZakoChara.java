package com.blawat2015.SurfaceViewSample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;

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

	int step = 0;
	int cnt = 0;
	public boolean deadStart = false;

	Paint paint;

	/**
	 * コンストラクタ
	 */
	public ZakoChara() {
		paint = new Paint();
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
		x = GWk.DEF_SCR_W / 2;
		y = GWk.DEF_SCR_H / 2;

		// 速度設定
		int ang = GWk.rnd.nextInt(360);
		float spd = (float) (GWk.rnd.nextInt(30) + 10) / 10;
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
		if (GWk.disableScaleDraw) {
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

			GWk.charaCount++;

			if (GWk.levelChangeEnable) {
				// レベルが変わったので速度を微妙に速くする
				if (GWk.level == 1) {
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
			if (x < bw || x > GWk.DEF_SCR_W - bw) {
				dx *= -1;
			}
			if (y < bh || y > GWk.DEF_SCR_H - bh) {
				dy *= -1;
			}

			if (GWk.frameCounter % 16 == 0) {
				// アニメパターン番号を1つ進める
				patNum++;
				patNum %= 2;
			}

			if (GWk.touchEnable) {
				// 画面をタップしているのでアタリ判定

				int w = (sw / 2);
				int h = (sh / 2);
				hitArea.set((int) (x - w), (int) (y - h), (int) (x + w),
						(int) (y + h));
				if (hitArea.left < GWk.touchX && GWk.touchX < hitArea.right
						&& hitArea.top < GWk.touchY
						&& GWk.touchY < hitArea.bottom) {
					// タッチされた
					GWk.clearTouchInfo();
					drawHitAreaEnable = true;
					patNum = 2;
					deadStart = true;
					alpha = 200;
					cnt = (int) (GWk.FPS_VALUE / 3);
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
		if (!befg || !GWk.layerDrawEnable[2] || step >= 2) return;

		// 描画
		paint.setAntiAlias(false);
		paint.setAlpha(alpha);

		if (GWk.disableScaleDraw) {
			// 画像の一部分を極力切り出さないで描画する場合
			int n = Img.ID_CHARA_SPLIT + (imgKind * 3) + patNum;
			Bitmap limg = Img.bmp[n];
			if (scale == 1.0f) {
				// 等倍描画
				c.drawBitmap(limg, dst.left, dst.top, paint);
			} else {
				// 拡大縮小描画
				c.drawBitmap(limg, src, dst, paint);
			}
		} else {
			// 画像の一部分を切り出して描画する場合
			Bitmap limg = Img.bmp[Img.ID_CHARA];
			c.drawBitmap(limg, src, dst, paint);
		}

		if (drawHitAreaEnable) {
			paint.setAlpha(255);
			paint.setColor(Color.RED);
			paint.setStyle(Style.STROKE);
			c.drawRect(hitArea, paint);
		}
	}
}
