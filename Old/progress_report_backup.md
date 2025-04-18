# 進捗報告のバックアップ

## 2024-05-03

Skeletal Animationの機能を実装しました。
Blenderなどで作成したモデルのアニメーションをMechtatelで再生することができます。

![Skeletal Animation 1](./Image/skeletal_animation.png)

![Skeletal Animation 2](./Image/skeletal_animation_2.png)

![Skeletal Animation 3](./Image/skeletal_animation_3.png)

## 2024-03-24

~~ImGuiのウィンドウがモデルの後ろ側に行ってしまう問題は解決できなかったので、~~ キー入力でメインのレンダリング結果の表示・非表示を変更できるようにしました。
[テストコード](./mechtatel-core/src/test/java/com/github/maeda6uiui/mechtatel/ModelViewerTest.java)
→ImGuiのスタイルをLightに変更したら半透明な感じで表示されるようになりました。
Darkだと黒(0)ベースなので、加算して最終的な描画結果を作成するときにメインの描画に負けてしまう感じですね。(たぶん)

![ImGuiのウィンドウと3Dモデル](./Image/imgui_5.png)

モデル描画のために別のスクリーンを用意して、その描画結果を`ImGui.image`で表示させようと思いましたが、この方法だとうまくいきませんでした。
ImGuiのImageにはVkDescriptorSetを渡すらしいのですが、ImGui Javaの現在の実装だとint型しか受け付けない仕様になっていて、long型を使うVkDescriptorSetを渡すことができませんでした。
これについてはImGui Javaのレポジトリに[Issue](https://github.com/SpaiR/imgui-java/issues/185)が上がっていました。

## 2024-03-23

モデルビューアーのサンプルを作成しています。
ImGuiのウィンドウがモデルの後ろ側に行ってしまうので、これの回避策を調査しています。

![ImGuiのウィンドウと3Dモデル](./Image/imgui_4.png)

## 2024-03-10

デフォルトのシェーダーをユーザーが指定したものに変更する機能を実装しました。
Javaコードからの変更と設定ファイルを用いた変更が可能です。
([テストコード](./mechtatel-core/src/test/java/com/github/maeda6uiui/mechtatel/OverwriteDefaultShadersTest.java)を参照のこと)

Present Nabor (最終的な描画結果をウィンドウに表示するためのNabor)で使用するシェーダーを変更して、表示結果の色を反転させてみました。

![デフォルトシェーダーの変更](./Image/overwrite_default_shaders.png)

一見地味な機能ですが、カスタマイズ性という点ではかなり有用な機能ではないかと思います。
Mechtatelのデフォルトのシェーダーだとちょっと物足りないというときに、気軽にシェーダーを変更できるようになります。
シェーダーの入力と出力に縛りがあるので、何でもかんでも自由にできるというわけではありませんが...。

## 2024-02-12

Maven CentralにSNAPSHOTビルドをデプロイしました。

## 2024-02-11

全画面表示(Full Screen)とウィンドウ化された全画面表示(Windowed Full Screen)をサポートしました。
設定ファイルから設定を変更できます。

```json
"window": {
    "title": "Mechtatel",
    "width": 1280,
    "height": 720,
    "resizable": false,
    "fullScreen": false,
    "windowedFullScreen": false,
    "monitorIndex": 0
}
```

全画面表示またはウィンドウ化された全画面表示を利用する場合は、どのモニターを利用するか(`monitorIndex`)を指定する必要があります。
0を指定すると最初に見つかったモニター、-1を指定すると最後に見つかったモニター、それ以外の値を指定するとその他の任意のモニターを指定することができます。

## 2024-02-08

無事にImGuiを導入することができました。

![ImGuiのサンプルウィンドウ](./Image/imgui_3.png)

## 2024-02-06

それっぽいものが表示されました。

![ImGuiのウィンドウ](./Image/imgui_2.png)

マウス操作がバグってるみたいなので修正していきます。

## 2024-02-05

Vertex BufferのレイアウトがImGuiとMechtatelで違うから、ImGuiの出してくるByteBufferをそのままMechtatelに投げてもちゃんと描画されないんですね。
ImGuiの出してくるByteBufferを一度数値に戻してからMechtatelのVertex形式に変換してみます。

？？？

![ImGuiのウィンドウ？](./Image/imgui.png)

## 2024-02-04

ImGuiを組み込む作業を進めています。
プログラムを実行できるようにはなりましたが、まだ画面に何も表示されない状態なので、もう少し時間がかかりそうです。

## 2024-01-27

自分の使っているUbuntu環境でMechtatelのウィンドウをリサイズするとJVMがクラッシュするという問題があり、色々調べて試してみたものの結局解決方法はわかりませんでした。
ウィンドウをリサイズしなければこの問題は起きないので、設定を変更してデフォルトでウィンドウのリサイズを無効化しました。
この件についてはGitHubの[Issue](https://github.com/maeda6uiui/Mechtatel/issues/1)を作成してあります。
これ以上調査しても無限に時間を溶かすだけのような気がしたので、この件については一旦ここで区切りとさせてもらいます。

## 2023-12-16

複数のウィンドウを表示できるようになりました。

![複数ウィンドウの表示](./Image/multiple_windows.png)

## 2023-11-24

- `mechtatel-logging`モジュールを追加しました
- LinuxとmacOS用のバイナリを追加しました

## 2023-11-23

これまで開発してきたコア機能を`mechtatel-core`モジュールにまとめました。
かなり大規模な変更を加えたので、もしかしたら動かなくなっているところがあるかもしれません。

Mechtatelの標準アセットはresourcesディレクトリに配置するように変更しました。
これにより、標準アセットはビルド後のJARファイル内に含まれるようになります。
標準アセットの読込みは`getResource`を用いて以下のように行います。

```java
skyboxModel = this.createModel(
        "skybox",
        Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Skybox/skybox.obj"))
);
```

resourcesディレクトリ以外にあるファイル(ユーザーが用意したファイルなど)は、別途URLを作成してメソッドの引数に渡すことで読み込むことができます。

```java
URL fragShaderResource;
try {
    fragShaderResource = Paths.get("./Mechtatel/Addon/maeda6uiui/Shader/sepia.frag").toUri().toURL();
} catch (MalformedURLException e) {
    e.printStackTrace();
    this.closeWindow();

    return;
}

var naborInfo = new FlexibleNaborInfo(
        Objects.requireNonNull(this.getClass().getResource(
                "/Standard/Shader/PostProcessing/post_processing.vert")),
        fragShaderResource
);
naborInfo.setLightingType("parallel_light");
screenCreator.addFlexibleNaborInfo("sepia", naborInfo);
```

これまで./Mechtatel/Binに配置していたバイナリについても、resourcesディレクトリに配置し、Gitで管理するようにしました。
今の段階ではWindows版のバイナリのみですが、macOSとLinux用のバイナリについても追加する予定です。
これにより、開発者が各自でバイナリを用意するという面倒な前準備が必要なくなるはずです。

## 2023-09-17

久しぶりにIntelliJを起動してMechtatelのコードを更新しました。

GUIコンポーネントを作成するメソッドの引数が多かったので、必要な変数をクラスにまとめて、Builder形式のSetterを用意してみました。
(Builderの意味をちゃんと理解してないので、もしかしたらこの実装をBuilderとは呼ばないかも...)

```java
@Override
public void init() {
    var keyInterpreter = new JISKeyInterpreter();
    textarea = this.createTextarea(
            new MttTextarea.MttTextareaCreateInfo()
                    .setX(-0.9f)
                    .setY(-0.9f)
                    .setWidth(0.9f)
                    .setHeight(0.9f)
                    .setCaretLength(0.1f)
                    .setCaretMarginX(0.001f)
                    .setCaretMarginY(0.01f)
                    .setFontName(Font.SANS_SERIF)
                    .setFontStyle(Font.PLAIN)
                    .setFontSize(32)
                    .setFontColor(Color.GREEN)
                    .setFrameColor(Color.WHITE)
                    .setCaretColor(Color.LIGHT_GRAY)
                    .setCaretBlinkInterval(0.5f)
                    .setSecondsPerFrame(this.getSecondsPerFrame())
                    .setRepeatDelay(0.5f)
                    .setKeyInterpreter(keyInterpreter)
                    .setSupportedCharacters(MttTextarea.DEFAULT_SUPPORTED_CHARACTERS)
    );
}
```

## 2023-08-06

MechtatelをPythonから扱う機能を実装していましたが、実装が面倒なことに加えて技術的な壁にぶち当たったので、この機能の開発は断念することにします。
その代わりに、YAMLファイルで処理を記述できるようにすることを検討しています。
(GitHub Actionsのワークフローみたいなものを想定しています)

## 2023-06-04

テキストエリアを実装しました。
複数行のテキストを入力できます。

![テキストエリア](./Image/textarea.png)

スクロールバーを付けるか迷いましたが、ゲーム画面でそんなに長い文を入力することなんてたぶんないし、実装も面倒そうなのでやめました。

## 2023-06-03

テキストボックスを実装しました。
ちゃんと点滅するキャレットもついています。

![テキストボックス](./Image/textbox.png)

とりあえずは日本語キーボードを想定しているので、英語キーボードなどを使う場合には別途KeyInterpreterを実装して、キーと出力文字の対応づけを行う必要があります。

また、ひらがなや漢字の入力には対応していません。
Mechtatelの仕様上、使いたい文字をあらかじめすべて指定しておく必要があるため、膨大な文字数が必要な日本語の入力に対応するのは難しいと思います。
プログラムからIMEを扱う方法についても正直よくわかりません。

複数行のテキストを入力できるテキストエリアについてもついでに実装しようと思います。

## 2023-06-02

ユーザー定義のFragmentシェーダーをPost Processingで使用できる機能を実装しました。
この機能を使って、Mechtatelの標準シェーダーでは用意されていない、セピア化処理を実装してみました。

![ユーザー定義のFragmentシェーダーを用いたセピア化](./Image/user_defined_shader_sepia.png)

この機能によって、描画結果のカスタマイズ性が大幅に向上します！

制限としては、単一のFragmentシェーダー(とVertexシェーダー)で完結する処理を想定しているため、Shadow Mappingのように複数組のシェーダーが必要な処理はこの機能では実現できません。
また、UBOについてもなんでも使えるわけではなく、あらかじめMechtatel側で用意してあるものの中から必要なものを選んで使っていただく形になります。

## 2023-05-31

スカイボックスの描画を修正しました。

![スカイボックス2](./Image/skybox_2.jpg)

スカイボックスのエッジに黒い線が入ってしまう件については、モデルの変更およびサンプラーのAddress Modeの設定で改善しました。

自分の環境では最終的に画面に出力する画像のフォーマットがUNORMなのに、Naborで扱う画像のフォーマットがSRGBになっていたため、出力結果が想定よりも暗くなっていました。(これまでのスクリーンショットを参照)
Naborで扱う画像のフォーマットの他、サンプラーの設定もユーザーが行えるようにしました。

別々のスクリーンの描画結果を合成して最終的な描画結果を生成するという処理はそれなりに高負荷な処理だと思いますが、自分の環境(AMD Ryzen 7 5700G)だとGPU使用率が40 %弱なので、まだどうにかなりそうです。

## 2023-05-30

二つの描画結果を合成する機能を実装しました。

![加算](./Image/texture_operation_add.jpg)

![乗算](./Image/texture_operation_mul.jpg)

性能が低めのノートパソコンみたいな環境だと、もしかするとそろそろ動作が重くなってくる頃かもしれません。
ちゃんとしたGPUを積んでいない普通のノートパソコンでも動作するようなゲームエンジンにしたいので、そのあたりは難しいところです...。

既存の機能でスカイボックスの描画は実現できましたが、エッジに黒い線が入っているのが気になります。

![スカイボックス](./Image/skybox.jpg)

以前OpenGLでスカイボックスを描画したときにも同じような問題があって、そのときはサンプラーの設定を変えることで解決したのですが、今回はサンプラーの設定を変えても解消しませんでした...。
よくわからないので、ぼかしをかけるという力技で解決できないか試してみます。

## 2023-05-22

レンダリング結果をテクスチャとして別のレンダリングで使用できる機能を実装しました。

![Textured screen](./Image/textured_screen.png)

バグったり実装の方針が決まらず右往左往したりでとても苦労しました...。
この機能があると、画面の分割表示をしたり、監視カメラやテレビを表現したり、あるいは鏡面反射を表現したりすることが可能になるはずです！

次は、二つのテクスチャの加算や乗算、深度による合成などの機能を実装していきます。
まだ実装のイメージがついていないので、コミットを乱打することになると思います...。

## 2023-05-07

スクリーンショットの保存機能を実装しました。

![Take screenshot](./Image/take_screenshot.jpg)

## 2023-05-06

MP3とWAVの再生機能を実装しました。

シャドウマッピングの改善をしようと思いましたが、どこから手をつければいいのかわかりません...。
すべてのシチュエーションにおいて最適なシャドウマッピングなんてものはたぶんなくて、それぞれのシチュエーションに対して適宜パラメーターを調整していい感じに見えるようにする、という流れな気がします...。
(言い訳)

これ以上こだわると沼にはまりそうなので、シャドウマッピングの改善は完了とします。

![Shadow Mapping](./Image/shadow_mapping_5.jpg)

それと、今までは機能をテストするたびに`MyMechtatel`クラスを上書きしてしまっていたのですが、これからはテストごとに新しいクラスを作成するようにします。
最近は機能が増えてきて、開発者自身も「あれ、あの機能どうやって使うんだっけ...」みたいなことがあるので、テストに使ったコードを残すようにします。
Gitで管理しているので過去のバージョンを確認すればいいんですが、いちいち探すのも面倒なので...。

## 2023-05-05

BD1ファイルの読込機能を実装しました。

![Snow Base](./Image/snow_base.jpg)

## 2023-03-21

リストボックスを実装しました。

![リストボックス](./Image/listbox.png)

## 2023-03-12

塗りつぶした四角形の描画機能を実装しました。
今までありそうで地味に実装していなかった機能です。

![塗りつぶしされた四角形](./Image/filled_quadrangles.png)

塗りつぶした四角形を用いて縦向きのスクロールバーを実装しました。

![縦向きのスクロールバー](./Image/vertical_scrollbars.png)

どのくらいスクロールされたのか取得するメソッドはこれから用意します。

## 2023-02-23

テキストの描画をちょっと改善して、ボタンの枠も描画するようにしました。
マウスをクリックしたときの動作はコールバックで指定します。

![ボタン2](./Image/button_2.png)

チェックボックスも実装しました。
左側のボックスをクリックすると選択状態と非選択状態を切り替えることができます。

![チェックボックス](./Image/checkbox.png)

## 2023-02-05

ボタンのつもりです。
カーソルを合わせると色が変化します。

![ボタン](./Image/button.png)

クリックしたときの動作はまだ実装できていません。
最初はボタンの外枠も描画するようにしていましたが、自分の力量不足でただ鬱陶しいだけになってしまったので、文字だけ描画するようにしました。

## 2023-01-29

![モデルとボックスの衝突判定](./Image/collision_3.png)

この画像の撮影のためにスポットライトを複数設置しようとしたら、なぜか最初に設定したライトしか反映されず、バリデーションエラーも特に出ていなかったので、原因を突き止めるのに時間がかかってしまいました。
原因としては、VkDescriptorSetLayoutBinding.descriptorCountにセットする値が間違っていたのと、余計なVkWriteDescriptorSetを作成してvkUpdateDescriptorSetsを実行していたことでした。

その他地味につまづいたところとしては、Bulletではモデル対モデルの当たり判定はできないというところです。
まあこれは考えてみれば(たぶん)当たり前の話で、任意の形をしたモデル同士の当たり判定を現実的な時間内で行うアルゴリズムなんてないですよね。

当たり判定についてはとりあえずこのくらいにしておいて、次はボタンやスクロールバーといったGUIコンポーネントを実装していきたいと思います。

## 2023-01-28

久しぶりの更新です。

カプセルとボックスの当たり判定も実装しました。
ゲームを作る上で必要な当たり判定はこれで一通り実装できたと思います。

![球、カプセル、ボックス、平面の衝突判定](./Image/collision_2.png)

## 2022-10-09

球と平面の衝突判定を実装しました。
物理演算にはBulletを使用しています。
画像だけだとイマイチわかりにくいですね...。

![球と平面の衝突判定](./Image/collision.png)

## 2022-10-08

音声ファイルの再生機能を実装しました。
今のところ、サポートしているフォーマットはOGGのみです。
モノラルサウンドを使用する場合は3Dサウンドとなります。

## 2022-10-02

任意のUnicode文字を表示できるようになりました。
(表示する文字に対応しているフォントを使用する必要があります)

![テキストの描画3](./Image/text_rendering_3.png)

## 2022-10-01

まだ完全ではないですが、それっぽい描画結果を得ることができました。

![テキストの描画](./Image/text_rendering.png)

まだ修正すべき点も多いですが、任意のASCII文字を表示できるようになりました。

![テキストの描画2](./Image/text_rendering_2.png)

## 2022-09-11

2D直線の描画機能を実装しました。

![2D直線の描画](./Image/line_2d.png)

2Dの描画機能を一通り実装できたら、次はテキストの描画に移る予定です。
ちょっと調べてみたところ、テキストの描画はそんなに簡単ではなさそうですね...。

## 2022-08-21

カプセルの描画機能を実装しました。

![カプセルの描画](./Image/capsule.png)

キーボードでカメラを操作できる機能(`FreeCamera`)を実装しました。

## 2022-05-08

球の描画機能を実装しました。

![球の描画](./Image/sphere.png)

## 2022-05-06

直線の描画機能を実装しました。

![直線の描画](./Image/lines.png)

![グリッドの描画](./Image/grid.png)

関係ないですが、開発環境を再びWindowsに戻しました。

## 2022-05-01

キーボード入力とマウス入力を実装しました。

## 2021-12-12

開発環境をUbuntuに移行しました。
Ubuntuでも問題なく動作しているように見えます。

![Ubuntuで実行](./Image/run_on_ubuntu.jpg)

ただ、Windowsで開発していたときには出なかったValidation Errorが出ます。

```
Validation Error: [ VUID-VkPipelineLayoutCreateInfo-pSetLayouts-00288 ] Object 0: handle = 0x7f82a0d39be0, type = VK_OBJECT_TYPE_DEVICE; | MessageID = 0xef93e10c | vkCreatePipelineLayout(): max per-stage uniform buffer bindings count (66) exceeds device maxPerStageDescriptorUniformBuffers limit (64). The Vulkan spec states: The total number of descriptors of the type VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER and VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC accessible to any shader stage across all elements of pSetLayouts must be less than or equal to VkPhysicalDeviceLimits::maxPerStageDescriptorUniformBuffers (https://vulkan.lunarg.com/doc/view/1.2.198.0/linux/1.2-extensions/vkspec.html#VUID-VkPipelineLayoutCreateInfo-pSetLayouts-00288)
```

スポットライトでUBOを64個割り当てているので、それが上限値を超えていますよ、というエラーだと思います。
カメラで1個、ライティング情報で1個、スポットライトで64個なので、エラーメッセージに出力されている数(66)とも一致します。
試しにスポットライトに割り当てるUBOを32個にしたら、このValidation Errorは出なくなりました。

Windowsで開発しているときにはこのValidation Errorは出なかったはずなんですが、原因はよくわかりません。

このエラーを無視しても動作に問題はなさそうなので、とりあえずはこのまま進めていきたいと思います。

## 2021-11-17

基礎的なシャドウマッピングは実装できました。

![Shadow](./Image/shadow_mapping_2.jpg)

![Shadow](./Image/shadow_mapping_3.jpg)

色付きの影を出すこともできます。

![Shadow](./Image/shadow_mapping_4.jpg)

## 2021-11-14

ライトから見たDepthは正しいようだ。

![Shadow](./Image/shadow_mapping_depth.jpg)

こんなコードを書いてデバッグを試みた。

```glsl
vec4 shadowCoords=biasMat*shadowInfos[0].lightProj*shadowInfos[0].lightView*modelMat*vec4(position,1.0);
float shadowDepth=texture(sampler2D(shadowDepthTextures[0],textureSampler),shadowCoords.xy).r;

outColor=vec4(shadowCoords.z-shadowDepth);
```

![Shadow](./Image/shadow_mapping_debug.jpg)

なんか影らしきものは出ている。

shadowCoords.zが現在処理している座標のライトから見たDepthで、shadowDepthは先に取得しておいたライトから見たDepthになっている。
shadowCoords.z > shadowDepthなら、その座標には影がかかっているということになる。
逆に、shadowCoords.z = shadowDepthとなっている場合、その座標には影はかからない。

つまり上の画像で、黒くなっている部分(shadowCoords.z - shadowDepth = 0)には影がかからず、灰色になっている部分には影がかかるはず...。

ちゃんと灰色になっているから、影がかかるはずなんだが...。
平面の一部が影がかかる場所ではないのに灰色になっているのも少し気になる。

---

何とかできました。影が荒いのは許してください。

![Shadow](./Image/shadow_mapping.jpg)

Fragment Shaderで使用しているbiasMatが原因でした。

修正後のbiasMatは以下のとおり。

```glsl
mat4 biasMat;
biasMat[0]=vec4(0.5,0.0,0.0,0.0);
biasMat[1]=vec4(0.0,0.5,0.0,0.0);
biasMat[2]=vec4(0.0,0.0,1.0,0.0);
biasMat[3]=vec4(0.5,0.5,0.0,1.0);
```

~~詳しい原因究明はまた今度...~~

## 2021-11-13

近いところまで来ている気がするけど、どこが間違っているのかわからない。

![Shadow](./Image/shadow_mapping_invalid.jpg)

遅延レンダリング(Deferred Rendering)を採用している関係で、今のやり方がそもそも間違っているのかもしれない。
以前にOpenGLで前方レンダリング(Forward Rendering)を用いてシャドウマッピングを実装したときには、ここまで苦労しなかったような気がする...。

これを何とかしてクリアしないと、(自分の心情的に)先に進めない。

## 2021-11-03

シャドウマッピングがなかなかうまくいかない...。
惜しいところまでは来ている気がするけど。

## 2021-10-06

点光源(ポイントライト)を実装しました。

![Point](./Image/point_light.jpg)

複数のポイントライトを設置するとこんな感じになります。

![Point](./Image/point_lights.jpg)

![Point](./Image/point_lights_2.jpg)

スポットライトのコードを流用することで比較的簡単に実装できました。

また、同じモデルを何度も読み込むのは無駄なので、モデルの複製(同じモデルの読み込みは一度のみ、テクスチャは共有)を行う機能も実装しました。

## 2021-10-03

スポットライトを実装しました。

![Spotlights](./Image/spotlights.jpg)

Post Processing用のNaborを接続するのは比較的簡単にできるようになりました。
ユーザが書くコードとしては、以下のような感じになります。

```java
var ppNaborNames = new ArrayList<String>();
ppNaborNames.add("spotlight");
this.createPostProcessingNabors(ppNaborNames);
```

上の画像ではSpotlight用のNaborしか使用していないのでこのようなコードになりますが、たとえば、Parallel LightをかけてからFogをかけるなら、

```java
var ppNaborNames = new ArrayList<String>();
ppNaborNames.add("parallel_light");
ppNaborNames.add("fog");
this.createPostProcessingNabors(ppNaborNames);
```

というようにすればいいだけです。

次は点光源とシャドウマッピングを実装していきたいと思います。

## 2021-09-26

フォグを実装しました。

![Fog](./Image/fog.jpg)

## 2021-09-25

シェーディングを実装しました。

![Shading](./Image/shading.jpg)

## 2021-09-20

Nabor (露: Набор 英: Kit)という概念を導入してみました。
NaborはRender PassやGraphics Pipelineといったレンダリングに必要なデータ一式をもちます。

現在はGBufferNaborとPresentNaborの二つのNaborがあります。
GBufferNaborを用いて色(Albedo)や深度(Depth)を出力し、その画像をPresentNaborのシェーダに渡して最終的な描画結果を画面に出力します。
これが効率的なやり方なのかどうかはわかりませんが、この方法を用いれば、Naborを複数個繋げて色々なエフェクトをかけることができます。(たぶん)

GBufferNaborから出力される内容としては、以下のようになります。

### Albedo

![Cube](./Image/cube_albedo.jpg)

### Depth

![Cube](./Image/cube_depth.jpg)

### Position

![Cube](./Image/cube_position.jpg)

### Normal

![Cube](./Image/cube_normal.jpg)

あまり詳しく理解していないのですが、これらの出力を利用して、遅延シェーディング(Deferred Shading)というのを行いたいと思っています。

## 2021-09-05

ノードを連結して描画結果を自由にカスタマイズできる、みたいなことをやりたい。
イメージとしては、Blenderのノードビュー(?)みたいな感じです。

![Blenderのノードビュー](./Image/blender_node_view.jpg)

VulkanのSubpassあたりを使えば実装できるのかなと考えていますが、Render PassとかSubpassとか、まだあんまりはっきりと理解できていないので、ここからしばらくは彷徨うことになりそうです。

## 2021-08-28

そこはかとなくいい感じのものができました。

![Cube](./Image/cube_2.jpg)

描画結果はこれまでと同じですが、それを実現するコードの方に進歩があったと思います。
ユーザはMechtatelクラスを継承して自身のクラスを作成し、そのinit()、dispose()、reshape()、update()という四つのメソッド内に処理を記述します。

```java
package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;

import java.io.IOException;

public class MyMechtatel extends Mechtatel {
    public MyMechtatel(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings settings;
        //Load settings from a JSON file
        try {
            settings = new MttSettings("./Mechtatel/Setting/settings.json");
        }
        //If the program fails to load the JSON file, then use the default settings
        catch (IOException e) {
            settings = new MttSettings();
        }

        new MyMechtatel(settings);
    }

    private Model3D model;

    @Override
    public void init() {
        model = this.createModel3D("./Mechtatel/Model/Cube/cube.obj");
    }

    @Override
    public void dispose() {
        //Components are automatically cleaned up, so you don't have to explicitly clean up the component.
        //model.cleanup();
    }

    @Override
    public void reshape(int width, int height) {

    }

    @Override
    public void update() {

    }
}
```

現在使用しているのはVulkanですが、同じようにすれば、OpenGLに対する抽象化も提供することができるはずです。(たぶん)

## 2021-08-25

Specフォルダ内のREADME.mdを更新しただけですが、もう一度プログラムの作成に進みます。(仕様書の作成とは...)

当面の予定ですが、まずは図形を描画するためのComponent周りを実装していきます。
その後、基本的な描画機能を提供する標準シェーダを追加していきます。

うまくいかなかったらそのときまた考えます。

## 2021-08-21

複数のモデルを表示することができました。

![Two](./Image/two_cubes.jpg)

このあたりで一度立ち止まって、仕様書の作成に戻りたいと思います。

## 2021-08-18

3Dモデル(Utah Teapot)を表示できました。
これで[チュートリアル](https://github.com/Naitsirc98/Vulkan-Tutorial-Java)の内容を一通り実装したことになります。
このチュートリアルなしではここまでたどり着けなかったので、感謝しかないです。

![Utah](./Image/teapot.jpg)

ところで、複数のテクスチャを表示する方法がわかりません。
自分が変な操作をしているのが原因なのは明確ですが、Validation Errorが出ます。
複数のテクスチャを使用するモデルを表示したいので、この点に関して調べる必要がありますね。

### 追記

複数のテクスチャを使用するモデルを表示できました。

![Cube](./Image/cube.jpg)

テクスチャごとにDescriptor Setを作成するという手法を取りましたが、これが正しいのかどうかはよくわかりません。

## 2021-08-14

テクスチャを表示できました。

![Lenna](./Image/lenna.jpg)

現状、Vulkan関連のコードはほとんど[ここ](https://github.com/Naitsirc98/Vulkan-Tutorial-Java)からコピーしている感じです。
一つのクラスに全部のコードを入れたくないので、可能な限り細かく複数のクラスに分けています。

## 2021-08-09

ようやく三角形を描画するところまで来ました。

![三角形](./Image/triangle.jpg)

## 2021-07-18

雑な仕様書を作成しています。

Vulkanを明確に理解しているわけではないので、Mechtatelをどんな感じの仕様にすればいいのか、正直よくわかりません。
特に、Vulkanと深く関わる低水準な部分については、今の自分の経験と知識では、これ以上仕様を詰めることは難しいと思います。

そこで、勉強も兼ねて、少しずつプログラムを作成していきたいと思います。
まずは(仕様書で言うところの)Coreの部分を実装していき、それがうまくいくようであれば、基本的なComponentの実装に進みたいと思います。

もちろん、(今までのように)うまくいかなくて匙を投げるようなことがあるかもしれませんが、結局のところ、あれはだめだ、これもだめだ、というふうに試行錯誤を繰り返すことによって、徐々に形の定まった、洗練された~~ゴミ~~作品が出来上がっていくはずなので、まあ、気長に構えて進めていこうと思います。

## 2021-07-04

過去数年の失敗を鑑みて、先に仕様書を作成することにします。

これまでは、思いついたものをそのまま作っていったので、ある程度作業が進んでから、「あれ、これじゃうまくいかないんじゃないか」みたいな感じになって、そのコード(あるいはプロジェクトそのもの)を放棄するということが多発していました。

この反省を活かし、細部までとは言わずとも、大雑把に全体像がつかめるくらいの仕様書は作成するべきだと考えました。

仕様書はMarkdown形式で作成し、Specフォルダ配下に置いていきます。
