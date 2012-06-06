package com.blawat2015.SurfaceViewSample;

import android.graphics.Point;
import android.view.Menu;
import android.view.MenuItem;

/**
 * オプションメニュー関係の処理
 */
final class Option {
	// オプションメニューIDを定義
	private static final int MENU_ID_FIXSIZE = (Menu.FIRST + 1);
	private static final int MENU_ID_DRAWTYPE = (Menu.FIRST + 2);
	private static final int MENU_ID_BG0 = (Menu.FIRST + 3);
	private static final int MENU_ID_BG1 = (Menu.FIRST + 4);
	private static final int MENU_ID_ENEMY = (Menu.FIRST + 5);
	private static final int MENU_ID_SOUND = (Menu.FIRST + 6);
	private static final int MENU_ID_BGMOFF = (Menu.FIRST + 7);
	private static final int MENU_ID_BGMCHG = (Menu.FIRST + 8);
	private static final int MENU_ID_KILL = (Menu.FIRST + 9);
	private static final int MENU_ID_EXIT = (Menu.FIRST + 10);

	/**
	 * オプションメニューを生成
	 * @param menu Menu
	 */
	public static void createOptionMenu(Menu menu) {
		// ID番号のリスト
		// R.string.xxxx は、res/values/strins.xml で定義してある
		final int[][] idList = {
				{
						MENU_ID_FIXSIZE, R.string.menu_fix_on,
						android.R.drawable.ic_menu_manage
				},
				{
						MENU_ID_DRAWTYPE, R.string.menu_drawtype1,
						android.R.drawable.ic_menu_manage
				},
				{
						MENU_ID_BG0, R.string.menu_bg0_off,
						android.R.drawable.ic_menu_view
				},
				{
						MENU_ID_BG1, R.string.menu_bg1_off,
						android.R.drawable.ic_menu_view
				},
				{
						MENU_ID_ENEMY, R.string.menu_enemy_off,
						android.R.drawable.ic_menu_view
				},
				{
						MENU_ID_SOUND, R.string.menu_snd_off,
						android.R.drawable.ic_menu_manage
				},
				{
						MENU_ID_BGMOFF, R.string.menu_bgm_all_off,
						android.R.drawable.ic_menu_manage
				},
				{
						MENU_ID_BGMCHG, R.string.menu_bgm_change,
						android.R.drawable.ic_menu_manage
				},
				{
						MENU_ID_KILL, R.string.menu_kill,
						android.R.drawable.ic_menu_close_clear_cancel
				},
				{
						MENU_ID_EXIT, R.string.menu_exit,
						android.R.drawable.ic_menu_close_clear_cancel
				},
		};

		for (int i = 0; i < idList.length; i++) {
			// メニュー項目を追加
			menu.add(Menu.NONE, idList[i][0], Menu.NONE, idList[i][1]).setIcon(
					idList[i][2]);
		}
	}

	/**
	 * メニューが開かれた時の処理
	 * @param menu Menu
	 */
	public static void prepareMenu(Menu menu) {
		GWk.enableOpenMenu = true;

		menu.findItem(MENU_ID_FIXSIZE).setVisible(!GWk.fixedSizeEnable);

		menu.findItem(MENU_ID_DRAWTYPE).setTitle(
				(GWk.disableScaleDraw) ? R.string.menu_drawtype0
						: R.string.menu_drawtype1);

		menu.findItem(MENU_ID_BG0).setTitle(
				(GWk.layerDrawEnable[0]) ? R.string.menu_bg0_off
						: R.string.menu_bg0_on);
		menu.findItem(MENU_ID_BG1).setTitle(
				(GWk.layerDrawEnable[1]) ? R.string.menu_bg1_off
						: R.string.menu_bg1_on);
		menu.findItem(MENU_ID_ENEMY).setTitle(
				(GWk.layerDrawEnable[2]) ? R.string.menu_enemy_off
						: R.string.menu_enemy_on);

		if (Snd.silentEnbale) {
			menu.findItem(MENU_ID_SOUND).setTitle(R.string.menu_silent);
		} else if (Snd.isSoundEnable()) {
			menu.findItem(MENU_ID_SOUND).setTitle(R.string.menu_snd_off);
		} else {
			menu.findItem(MENU_ID_SOUND).setTitle(R.string.menu_snd_on);
		}

		Snd.pauseBgm();
	}

	/**
	 * メニューが閉じられた時の処理
	 */
	public static void closeMenu() {
		GWk.enableOpenMenu = false;
		Snd.resumeBgm();
	}

	/**
	 * アイテムが選択された時の処理
	 * @param item MenuItem
	 * @return 0=通常処理,1=Killを選択,2=exitを選択,3=それ以外
	 */
	public static int selectedItem(MenuItem item) {
		int ret = 0;

		switch (item.getItemId()) {
		case MENU_ID_FIXSIZE:
			// setFixedSize() を有効にする
			// (Canvas#scaleを無効にする)
			Point p = Main.getWindowSize();
			GWk.fixedSizeEnable = true;
			Main.setScreenWH(p.x, p.y);
			break;
		case MENU_ID_DRAWTYPE:
			// 拡大縮小描画を積極的にするかどうかの有効無効を切り替える
			GWk.disableScaleDraw = !GWk.disableScaleDraw;
			break;
		case MENU_ID_BG0:
			// BG描画の有効無効を切り替える
			GWk.layerDrawEnable[0] = !GWk.layerDrawEnable[0];
			break;
		case MENU_ID_BG1:
			// BG描画の有効無効を切り替える
			GWk.layerDrawEnable[1] = !GWk.layerDrawEnable[1];
			break;
		case MENU_ID_ENEMY:
			// ENEMY描画を無効にする
			GWk.layerDrawEnable[2] = !GWk.layerDrawEnable[2];
			break;
		case MENU_ID_SOUND:
			// サウンド有効無効を切り替える
			Snd.changeSoundMode();
			break;
		case MENU_ID_BGMOFF:
			// BGM全停止
			Snd.stopBgmAll();
			break;
		case MENU_ID_BGMCHG:
			// BGM変更
			int n = Snd.getNextBgmId();
			Snd.changeBgm(n);
			break;
		case MENU_ID_KILL:
			// 終了処理(プロセスを殺す版)
			ret = 1;
			break;
		case MENU_ID_EXIT:
			// 終了処理(finish版)
			ret = 2;
			break;
		default:
			ret = 3;
			break;
		}
		return ret;
	}

}
