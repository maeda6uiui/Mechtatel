# Mechtatel

## 概要

ゲームエンジンの中心となるクラス

このクラスを継承して各メソッドをoverrideすることによって動作をカスタマイズする。

## メソッド一覧

| 名前    | 概要                                           |
| ------- | ---------------------------------------------- |
| init    | ゲームエンジンの初期化時に呼ばれる             |
| dispose | ゲームエンジンの終了時に呼ばれる               |
| reshape | ウィンドウサイズの変更時に呼ばれる             |
| update  | ループ毎に呼ばれ、各コンポーネントの更新を行う |
| draw    | ループ毎に呼ばれ、各コンポーネントの描画を行う |
