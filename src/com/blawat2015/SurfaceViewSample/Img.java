package com.blawat2015.SurfaceViewSample;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.SurfaceView;

/**
 * 画像管理用クラス
 */
final class Img {

	// 画像番号
	public final static int ID_BG0 = 0;
	public final static int ID_BG1 = 1;
	public final static int ID_CHARA = 2;
	public final static int ID_CHARA_SPLIT = 3;
	public final static int ID_LOGO_TITLE = 12;
	public final static int ID_LOGO_CLEAR = 13;
	public final static int ID_LOGO_SPEEDUP = 14;
	public final static int ID_SOUNDOFF = 15;
	public final static int ID_SOUNDON = 16;
	public final static int ID_TOUCH0 = 17;
	public final static int ID_TOUCH1 = 18;

	// 画像リソースIDのリスト
	private final static int[] imgId = {
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
			R.drawable.soundoff_icon, // 15
			R.drawable.soundon_icon, // 16
			R.drawable.touch_icon0, // 17
			R.drawable.touch_icon1, // 18
	};

	public static Bitmap[] bmp = new Bitmap[imgId.length];
	private static Resources resources;

	/**
	 * コンストラクタ
	 */
	private Img() {
	}

	/**
	 * 初期化処理
	 * @param view
	 */
	public static void init(final SurfaceView view) {
		LogUtil.d("Img", "init Img");
		resources = view.getResources();

		LogUtil.d("Img", "load Images");
		for (int i = 0; i < imgId.length; i++) {
			if (bmp[i] == null || bmp[i].isRecycled() )
				bmp[i] = BitmapFactory.decodeResource(resources, imgId[i]);
		}
	}

	/**
	 * 画像を全て破棄
	 */
	public static void releaseImageResAll() {
		LogUtil.d("Img", "recycle Bmp All");
		for (int i = 0; i < bmp.length; i++) {
			if (bmp[i] != null) bmp[i].recycle();
		}
		System.gc();
	}
}
