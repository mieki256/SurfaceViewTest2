Android SurfaceViewのテスト
===========================

オプションメニューから処理内容を変更できる。

起動直後は Canvas#scale を使って描画しているが、オプションメニューから setFixedSize を使って描画するよう切り替えることができるようにした。

Lenovo IdeaPad A1では以下の結果になった。

- Canvas#scale ... 20FPS
- setFixedSize ... 60FPS

その代り、後者は画面がボケボケになる。



