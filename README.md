# SurfaceViewTest2

## SurfaceViewTest2について

### アプリ概要

* Androidアプリの作成テスト。
* SurfaceView の動作確認・ベンチマーク代わりに作成。
* サウンド(BGM、SE)の再生テストも兼ねている。
* 一応、ゲームの体裁にしてある。(もぐら叩きモドキ)

### スクリーンショット

<div>
<link media="screen" rel="stylesheet" href="https://dl.dropbox.com/u/84075965/js/colorbox/colorbox.css" />
<link media="screen" rel="stylesheet" href="https://dl.dropbox.com/u/84075965/js/colorbox/floatlist.css" />
<script type="text/javascript" src="http://www.google.com/jsapi"></script>
<script type="text/javascript">google.load('jquery', '1')</script>
<script src="https://dl.dropbox.com/u/84075965/js/colorbox/jquery.colorbox-min.js"></script>
<script type="text/javascript">
<!--
$(document).ready(function(){
$(".slshow").colorbox({rel:'slshow', transition:"fade", speed:100});
$(".single").colorbox();
});
-->
</script>
<ul class="floatlist">
<li><a class="slshow" href="https://dl.dropbox.com/u/84075965/screenshot/SurfaceViewTest2/SurfaceViewTest2_ss_1.png" title="タイトル画面"><img src="thumb1.png" /></a></li>
<li><a class="slshow" href="https://dl.dropbox.com/u/84075965/screenshot/SurfaceViewTest2/SurfaceViewTest2_ss_2.png" title="ゲーム画面1"><img src="thumb2.png" /></a></li>
<li><a class="slshow" href="https://dl.dropbox.com/u/84075965/screenshot/SurfaceViewTest2/SurfaceViewTest2_ss_3.png" title="ゲーム画面2"><img src="thumb3.png" /></a></li>
<li><a class="slshow" href="https://dl.dropbox.com/u/84075965/screenshot/SurfaceViewTest2/SurfaceViewTest2_ss_5.png" title="クリア画面"><img src="thumb5.png" /></a></li>
<li><a class="slshow" href="https://dl.dropbox.com/u/84075965/screenshot/SurfaceViewTest2/SurfaceViewTest2_ss_4.png" title="オプションメニュー"><img src="thumb4.png" /></a></li>
</ul>
<p class="clearLeft"></p>
</div>

### オプションメニューについて

オプションメニューから処理内容を変更できる。

* BGやスプライトの、表示有効無効を切替えられる。
* サウンドの有効無効を切替えられる。
* 自プロセスを殺す終了方法と、finish() による終了方法を試せる。

* 描画処理の切り替え。
  * 起動直後は Canvas#scale を使って描画してるが、
    setFixedSize を使って描画するように切替可能。
  * DrawType0は、一つの画像から切り出して描画。
  * DrawType1は、極力個別のBitmapを描画。
    (画像からの切り出しを極力避けて描画。)

### 開発環境

* Windows7 x64
* eclipse 3.7.2 + ADT
* Android 2.3.3 (API10)

### 添付データについて

添付の画像、ループBGM、SEは、自作したデータです。自由に使ってください。

### 描画内容

* 基準画面サイズは、240x320ドット。
  これを、縦横比を維持して拡大縮小、フルスクリーン表示している。
* スプライト相当(32x32ドット)を80枚、BGを2枚、表示している。

これは、メガドライブの表示スペックとほぼ同等。
この程度を60FPSで描画できなければ、
十数年前の家庭用ゲーム機にすら及ばないことに… (´･ω･｀)

### 実験結果

Lenovo IdeaPad A1では以下の結果になった。

* Canvas#scale で描画 ... 20FPS、描画は遅いが、画面はクッキリ。
* setFixedSize で描画 ... 60FPS、描画は早いが、画面はボケボケ。

