# Component3D

## 概要

3Dコンポーネントの基底クラス

3Dに関するコンポーネントはすべてこのクラスを親にもつ。

extends Component

## メソッド一覧

| 名前      | 概要                                 |
| --------- | ------------------------------------ |
| translate | コンポーネントを移動する             |
| rotX      | コンポーネントをX軸回りに回転する    |
| rotY      | コンポーネントをY軸回りに回転する    |
| rotZ      | コンポーネントをZ軸回りに回転する    |
| rot       | コンポーネントを任意軸回りに回転する |
| rescale   | コンポーネントの拡大・縮小を行う     |

## コメント

コンストラクタの引数として各種初期値を取り、モデル行列をセットアップする。
