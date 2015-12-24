# cljsとツクールMVを使ってブラゲ作った

これは [Clojure Advent Calendar 2015](http://qiita.com/advent-calendar/2015/clojure) の23日目の記事です。

この記事では、[RPGツクールMV](https://tkool.jp/mv/)を使う手順を解説します。

ゲーム作成に興味のない人はこの記事を読む必要はありません。

ゲームだけ遊びたい人はこちらからどうぞ → h<a href="http://vnctst.tir.jp/op0015/" target="_blank">ttp://vnctst.tir.jp/op0015/</a>


## 目次

- [概要](#概要)
- [ツクールMVをmacにインストールする](#ツクールmvをmacにインストールする)
- [プロジェクトディレクトリ構造](#プロジェクトディレクトリ構造)
- [ツクールMVプロジェクトの作成](#ツクールmvプロジェクトの作成)
- [cljsプロジェクトの作成](#cljsプロジェクトの作成)
- [成果物](#成果物)
- [解説](#解説)
    - [ツクールMVからcljsを呼ぶ](#ツクールmvからcljsを呼ぶ)
    - [cljsからツクールMVの機能を使う](#cljsからツクールmvの機能を使う)
- [ツクールMVを使ってみた感想](#ツクールmvを使ってみた感想)
- [今後の課題](#今後の課題)
- [おまけ：cljs上でのevalについて](#おまけcljs上でのevalについて)


## 概要

- ツクールMVのプロジェクトは、直にhtml5実行可能な形式(index.htmlやjsの入ったディレクトリ)として保存されるので、lein(cljs)プロジェクトとの共存が容易に可能。
- 上記の特性を利用して、「単一のプロジェクトとして保存する」「ツクールMVからcljsを呼ぶ」「cljsからツクールMVが提供している機能を呼ぶ」を実現する事を考える。


## ツクールMVをmacにインストールする

0. osのバージョンが10.10より以前なら、アップグレードしておく。
    - アップグレードの際には http://apple.srad.jp/story/15/09/30/1529213/ に気をつけましょう(筆者はごっそりと退避されてしまい、しかもこの情報を知らなかったので単に削除されたと思って、再インストール祭りしてしまった)。
1. steamアカウントを取得し、steamクライアントをインストールする。
2. ツクールMVのsteamキーを入手する。筆者は[ドワンゴジェイピーストア](http://jpstore.dwango.jp/game/tkool_mv.php)で購入した。
3. http://jpstore.dwango.jp/game/steam-key/ を見ながら、steamキーを登録する。あとは勝手にダウンロードされてインストールされる。
4. ツクールMV開発環境を起動するのは、steamの「ライブラリ」のところから「RPG Maker MV」を選んで「起動」を押す。
    - steamを経由せずに起動を行おうとするとエラーになるようだ。面倒だが、毎回steam経由で起動するしかない。


## プロジェクトディレクトリ構造

前述の通り、「ツクールMVのプロジェクト」＝「index.html等の入った、html5実行可能なディレクトリ」であるので、今回のプロジェクト全体のディレクトリ構成は以下のような感じにする。

~~~
(project-dir)/
(project-dir)/.gitignore
(project-dir)/README.md
(project-dir)/project.clj
(project-dir)/src/
(project-dir)/src/cljs/
(project-dir)/src/cljs/...
(project-dir)/resources/
(project-dir)/resources/public/
(project-dir)/resources/public/...
(project-dir)/...
~~~

- 上記の中の `resources/public/` が、ツクールMVのプロジェクト一式相当になるようにする。


## ツクールMVプロジェクトの作成

- とりあえずツクールMVのIDEを起動し、「ファイル」→「プロジェクトの新規作成」を選択し、適当な保存場所を設定してから保存して終了する。
- 保存後、前述のディレクトリ構造になるように位置を移動させておく。
- 次回から、ツクールMVのIDEから「プロジェクトを開く」際には、上記の `resources/public/Game.rpgproject` をプロジェクトとして開くようにすればよい。


## cljsプロジェクトの作成

- 普通に`lein`を使ってcljsプロジェクトを開始する構成にしておく。
    - それぞれの人の流儀があると思うので、ここでは詳細は省略。
- `resources/public/` が、上で作成したツクールMVプロジェクトになるので、既にこの中にあるファイルに衝突しないように、ビルド先ディレクトリを選ぶ。
    - 同時に、`resources/public/index.html`を編集し、ビルドしたcljsのjsファイルも読み込むようにしておく(`script`タグのところ)。
        - この際の読み込み順序は結構考える必要があるが、「`js/plugins.js`の直前」もしくは「`js/main.js`の直前」が割と良い。これなら「cljsのロード時にMV組み込みの各種ライブラリが参照できる」「MVデータからcljs環境が参照できる」の両方を満たす事ができる(ただしプラグインの扱いがどうやっても微妙になる)。


## 成果物

- h<a href="http://vnctst.tir.jp/op0015/" target="_blank">ttp://vnctst.tir.jp/op0015/</a> から遊べます。

- ソースは https://github.com/ayamada/op0015 に置いてます。
    - ただし、ツクールMV由来のコード/画像/データ等はツクールMVの使用許諾契約書によりゲーム外では同梱できない為、抜いてあります。なので、このソースをそのまま持っていっても動きません。すいません。
        - ツクールMVを持ってる方なら、各ファイルを適当にそれらしく配置すれば動く筈です。具体的にどのファイル/ディレクトリを追加すればいいのかは [.gitignore](https://github.com/ayamada/op0015/blob/master/.gitignore) を見てください。
        - 筆者はfigwheelを利用しているので、開発時には `lein clean && rlwrap lein figwheel dev` を実行して、figwheelサーバを起動させておいてください。この状態でツクールMVから「テストプレイ」を実行すると、figwheelの自動リロード機能等が有効な状態でテストプレイができます。
          - デプロイ時には忘れずに `lein clean && lein with-profile prod cljsbuild once prod` を実行して、prod版ビルドにしてからデプロイしてください。


## 解説

### ツクールMVからcljsを呼ぶ

- ツクールMVの各種イベント設定で可能な「スクリプト」項目から、js経由でcljsを実行すればよい。
    - `cljs.core.rand_int(10)` みたいにjs形式で直に呼べばよい。ただしnamespaceおよびvarnameは手で`munge`しておく必要がある。詳細は`(doc munge)`を…と言いたいところだが何故かdocが書かれてないので解説しておくと、`(munge '*in*) => _STAR_in_STAR_` みたいにnamespaceやvarnameを実行環境VM(jvmとかjsとか)向けの名前に変換してくれる奴。引数がシンボルなら返り値もシンボル、引数が文字列なら返り値も文字列になる。逆の変換を行う`demunge`もある。
- MVの「変数の操作」のところでもスクリプト実行ができ、スクリプト実行の返り値をMV側の変数に保存する事ができる。一見、数値しか保存できないような感じに見えるが、文字列等を変数に保存しても別に問題ないようだ(勿論、数値演算はできなくなるが)。


### cljsからツクールMVの機能を使う

- ツクールMVはフレームワーク構成になっているものの、全体としては個別のライブラリの集まりなので、各ライブラリの機能を直接呼び出す事もできる。
    - ツクールMVのヘルプの一番下に組み込みのjsライブラリのリファレンスがある
        - pixiやlz-string等、ツクールMVが由来でないライブラリについては、ツクールMVのヘルプ内のリファレンスに詳細は書いてないので、個別に確認する必要がある
            - 時間があればpixi v2系とかの解説記事を書きたかったが、そんな時間はなかった
        - あとは、[RPGツクールMV Advent Calendar 2015](http://qiita.com/advent-calendar/2015/rmmv)にも参考になる記事が何個かある
    - これらのライブラリを利用する場合は、プロジェクトを `:optimizations :advanced` でコンパイルする予定があるならば、忘れずにexternsを書く事。
        - pixi v3系では[Google Closure Compiler向けのexterns](https://github.com/pixijs/pixi-closure-compiler)が公開されているのだが、ツクールMVで採用されているpixi v2系には対応するexternsは存在しない為、自分で用意する必要がある。

- 今回は時間がなかったのでこの方式は使ってない。が、特に問題なく利用できるだろう。


## ツクールMVを使ってみた感想

- デフォルト状態だと色々とファイルサイズが大きく、ブラゲ作成向きになってない
    - 別画面に移動する際に、いちいち「Now Loading」が出るのもよろしくない
    - ブラゲ向きにチューニングするのは結構面倒
    - スマホ上で動かす場合は、かなり不利ではなかろうか…

- IDEとしての出来は、良い面も悪い面もある
    - マップデータをサクサク作れるのは良い。ただし操作性にかなり癖があり、扱いに慣れるのに少し時間を必要とする
    - 敵や装備品等のデータを入力していくのはかなり面倒


## 今後の課題

- gitリポジトリへの保存手法の改善
    - ツクールMVが提供しているバイナリ系素材が容量を圧迫する、なんとかしたい
        - とりあえず手で、使ってない/使わないものを削除する事はできるが…
            - これについては一応、ツクールMV公式で、今後に「不要素材の削除ツール」を配布する予定との事らしい
    - 今回みたいに公開リポジトリに保存する場合は、ツクールMV由来のコード/素材を除外する必要がある。この除外自体は`.gitignore`だけで一応可能なのだが、以下が問題となる
        - 除外した素材類であっても加工したりする場合があるので、リビジョン管理したい
        - 新しくgit cloneした状態のところに、除外対象のファイルを一通り補完して動く状態にする作業がすごい面倒
    - よって、可能なら、バイナリ系アセットやツクールMV由来のファイルを、gitとは別に、なんかいい感じに管理してくれるツールがほしいところ

- 面白いゲームを作る
    - クソゲーでは駄目です！！！！！


## おまけ：cljs上でのevalについて

上記成果物内で利用している、`eval`機能についてのメモです。

- 実装時に参考にしたurl
    - http://swannodette.github.io/2015/07/29/clojurescript-17/
    - http://yogthos.net/posts/2015-11-12-ClojureScript-Eval.html
    - [figwheelのソース](https://github.com/bhauman/lein-figwheel)

```
(require 'cljs.js)
(require 'cljs.env)

(when-not (aget js/cljs "user")
  (aset js/cljs "user" (js-obj)))
(set! *print-fn* (fn [& _] nil))
(set! *print-err-fn* (fn [& _] nil))

(defn cljs-eval [s-expr]
  (cljs.js/eval (cljs.js/empty-state)
                s-expr
                {:eval cljs.js/js-eval :context :expr}
                identity))

(comment
  (cljs-eval '(+ 1 2)) => 3
  (cljs-eval '(def a 1))
  (cljs-eval '(inc a)) => 2
  )
```

- `:optimizations :advanced` だと動かない。常にエラーになる。つまり最適化は最善で `:optimizations :simple` になる。
- 上記の `:optimizations :advanced` が使えない事に加え、 `cljs.js` 名前空間等が組み込まれて肥大化する為、コンパイル後のjsファイルのサイズが6Mとかになる(通常なら、1Mはまず越えない)。
- 謎のテクノロジーによって、`def`, `defn`等のspecial formや、`when`, `and`等のmacro類も普通に動く。
    - もし `(+ 1 2)` とかは動くのに `(def a 1)` とかが動かない場合は、以下を確認する(具体的な対応コードは上記を参照)
        - `js/cljs.user` が `nil` ではなく、 `#js {}` になってなくてはならない
        - `*print-fn*` と `*print-err-fn*` をきちんと自前で設定する必要がある(デフォルトでは例外を投げるだけのfnが設定されている)
            - 最初の内、figwheel経由で起動すると`def`系が動くが、figwheel無しでビルドすると`def`系が動かないのに悩み、figwheelのソースを調べた結果、こういう事だと判明した(figwheelがこの辺りを勝手に適切に設定していた)

個人的な結論：

- 動く事は動くが、実用には色々と厳しい(少なくともブラゲ内で使う事を考えた場合は。node.js内で動かすとかならアリかもしれない)




