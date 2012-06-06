package com.blawat2015.SurfaceViewSample;

import android.util.Log;

public class LogUtil {
	/**
	 * デバッグログ出力
	 */
	public static final void d(String tag, String msg) {
		// マニフェストでデバッグが有効なら出力する
		if (BuildConfig.DEBUG) Log.d(tag, msg);
	}
}

