# Android SurfaceViewのテスト

## アプリ概要

- SurfaceViewの動作確認用に作成したアプリ。
- サウンド(BGM、SE)の再生テストも兼ねている。
- 一応ゲームの体裁にしてある。(もぐら叩きモドキ)

## 描画内容について

- 基準画面サイズは、240x320ドット。これを、縦横比を維持して拡大縮小、フルスクリーン表示している。
- スプライト相当(32x32ドット)を80枚、BGを2枚、表示している。
- これは、メガドライブの表示スペックとほぼ同等。
  - この程度を60FPSで描画できなければ、十数年前の家庭用ゲーム機にすら及ばないことになる。

## オプションメニューについて

オプションメニューから処理内容を変更できる。

- BGやスプライトの、表示有効無効を切替えられる。
- サウンドの有効無効を切替えられる。
- 自プロセスを殺す終了方法と、finish() による終了方法を試せる。
- 描画処理の切り替え。
  - DrawType0は、一つの画像から切り出して描画。
  - DrawType1は、極力個別のBitmapを描画。(画像からの切り出しを極力避けて描画。)
  - 起動直後は Canvas#scale を使って描画してるが、setFixedSize を使って描画するように切替可能。

## 実験結果

Lenovo IdeaPad A1では以下の結果になった。

- Canvas#scale で描画 ... 20FPS、描画は遅いが、画面はクッキリ。
- setFixedSize で描画 ... 60FPS、描画は早いが、画面はボケボケ。

## 添付データについて

添付の画像、ループBGM、SEは、自作したデータです。自由に使ってください。

## スクリーンショット

![タイトル画面](http://cloud.github.com/downloads/mieki256/SurfaceViewTest2/SurfaceViewTest2_ss_1.png)

![ゲーム画面1](http://cloud.github.com/downloads/mieki256/SurfaceViewTest2/SurfaceViewTest2_ss_2.png)

![ゲーム画面2](http://cloud.github.com/downloads/mieki256/SurfaceViewTest2/SurfaceViewTest2_ss_3.png)

![オプションメニュー](http://cloud.github.com/downloads/mieki256/SurfaceViewTest2/SurfaceViewTest2_ss_4.png)

![クリア画面](http://cloud.github.com/downloads/mieki256/SurfaceViewTest2/SurfaceViewTest2_ss_5.png)
