# Sound3D

## 概要

3次元的なサウンドに関する機能を提供するクラス

このクラスの実装にはOpenALを用いる予定です。

extends Sound

## フィールド一覧

| 名前              | 概要       |
| ----------------- | ---------- |
| position          | 音源の位置 |
| direction         | 音源の方向 |
| distanceModel     | 減衰モデル |
| maxDistance       | 最大距離   |
| referenceDistance | 参照距離   |

## メソッド一覧

| 名前                 | 概要                 |
| -------------------- | -------------------- |
| setPosition          | 音源の位置を設定する |
| getPosition          | 音源の位置を取得する |
| setDirection         | 音源の向きを設定する |
| setDistanceModel     | 減衰モデルを設定する |
| setMaxDistance       | 最大距離を設定する   |
| setReferenceDistance | 参照距離を設定する   |

## コメント

このクラスで使用するファイルはモノラルのWAV形式を想定しています。

減衰モデルやそれに関連するパラメーターについてはあまり詳しくわからないので、実際にコードを書いて色々試すしかない...。

