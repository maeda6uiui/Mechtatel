# 概要

## クラス

ユーザはMechtatelクラスを継承して自身のクラスを作成する。
継承したクラスのinit(), dispose(), reshape(), update(), draw()内に処理をユーザの行いたい処理を記載する。

汎用的なメニュー機能やその他のGUI機能も提供したいが、これは優先度が低い。
まずは、ユーザが継承したクラス内で、図形(球、カプセル、3Dモデル、テキストなど)を簡単に描画できることを目指す。

core.vulkanパッケージにはVulkan関連のクラスを格納し、coreパッケージ以下のクラスはそれに対する抽象化を提供する。
たとえば、core.Textureクラスはcore.vulkan.Textureクラスを包含し、ユーザがcore.Textureクラスを扱うときには、Vulkan関連の内容は隠蔽されている。
このような設計にすることで、将来的にOpenGLにも対応したいとなったときに、core.opengl.Textureクラスを作成し、core.TextureクラスでOpenGLとVulkanの差異を吸収することができる(と思う)。

すべての図形(球や3Dモデルなど)はComponentを継承する。
2D図形はComponent2D、3D図形はComponent3Dを継承する。
Componentは位置やスケールなどの情報をもたず、その代わりに変換行列をもつ。
この変換行列をuniform変数としてシェーダに渡すことによって、図形の位置やスケールをコントロールする。

## シェーダ

標準シェーダとして以下のようなものを実装する。

### 3D

- 線分の描画
- 塗りつぶしされた三角形の描画
- テクスチャを用いる三角形の描画

これらのシェーダでは、フォグの機能も実装する。

球やカプセルなどの図形の描画、モデルの描画などは、上記のシェーダを使用することで実現可能だと思われる。

その他、スカイボックスの描画も標準機能として実装するべきだと考えている。

3Dの標準シェーダは*Mechtatel/Shader/Standard/3D*以下に格納する。

### 2D

- 線分の描画
- 塗りつぶしされた三角形の描画
- テクスチャを用いる三角形の描画
- (テキストの描画)
- (GUIの描画)

テキストの描画はシェーダというより、Javaプログラム側の問題かもしれない。

GUIの描画やその動作に関しては、[Dear ImGui](https://github.com/ocornut/imgui)を使用したい。
これはC++のライブラリだが、ちょっと検索すると、Javaでも使えるようにしてくれた偉大な先人がいるようなので、それを試してみたい。
たとえば、[imgui-java](https://github.com/SpaiR/imgui-java)とか[jimgui](https://github.com/ice1000/jimgui)とかが有望に思える。

2Dの標準シェーダは*Mechtatel/Shader/Standard/2D*以下に格納する。

## 描画の流れ

1. Component.draw()を実行
2. draw()が実行されたComponentをArrayListに追加する
3. MttVulkanInstance内のdraw()でArrayListに追加されているComponentの描画を実行し、ArrayListを空にする

描画予定のComponentを所持するのはMttInstanceで、低水準クラス(MttVulkanInstance)に対しては、ArrayListのgetのみを行えるインターフェイスを提供する。
すべての描画が終わった後、MttInstanceでArrayListを空にする。

