(ns op0015.core
  (:refer-clojure :exclude [eval])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]
                   [project-clj.core :as project-clj])
  (:require [cljs.core.async :as async :refer [>! <!]]
            [cljs.js]
            [cljs.env]
            [cljs.tools.reader :as edn]
            [clojure.string :as string]
            [goog.dom]
            [goog.style]
            ))

(defonce flags (atom #{}))
(defn ^:export clear-flags [] (reset! flags #{}))
(defn ^:export set-flag [s]
  (swap! flags conj s))



;;; TODO: 時間があったらoverlay方式のモーダルダイアログにしたい。
;;;       ただしその場合はasyncな処理になる為、
;;;       MV側と連携するには、ちょっと工夫する必要がある
(defn- prompt [text]
  (let [r (js/prompt text "")]
    ;; NB: js/prompt発動時に、マウスのボタン押し状態が「押しっぱなし」判定に
    ;;     なる場合があるようなので、それを明示的に解除するようにする
    ;;     (js/promptを使わないようにすれば、この処理は不要になる)
    (js/TouchInput.clear)
    ;; 文字列が空ならnilを返すようにする(キャンセル扱い)
    (when-not (empty? r)
      r)))


(defn- eval [s-expr]
  (when-not (aget js/cljs "user")
    ;; TODO: devビルド時は普通にjs/console.logに出力するようにする
    (set! *print-fn* (fn [& _] nil))
    (set! *print-err-fn* (fn [& _] nil))
    (aset js/cljs "user" (js-obj)))
  (cljs.js/eval (cljs.js/empty-state)
                s-expr
                {:eval cljs.js/js-eval :context :expr}
                identity))

(defn- rmmv-message-escape [message]
  (string/replace message "\\" "\\\\"))

;;; NB: ツクールMVのメッセージウィンドウには「改ページ」という概念はない。
;;;     その代わり、メッセージウィンドウは四段固定なので、四行表示する事で
;;;     改ページを実現している
(defn ^:export read-eval [text]
  (try
    (when-let [input (prompt text)]
      (js/$gameMessage.add (str "入力されたS式：\n"
                                (rmmv-message-escape input)
                                "\n\n"))
      ;; TODO: 上記のメッセージを表示してから、後続処理を行いたい。
      ;;       具体的には、例えば `(js/alert 1)` を入力した場合、
      ;;       コード上はこの順番で実行されるが、ユーザが実際に目にする順番は
      ;;       「ダイアログ→入力されたS式のメッセージ→残り」の順になる。
      ;;       これはよくないので、可能であれば
      ;;       「入力されたS式のメッセージ→ダイアログ→残り」の順になるように
      ;;       実装したい。
      ;;       ただ、これをきちんと実装するには、core.async的な連携が
      ;;       必要になると思われるので、作業量的に今回は諦める事にする。
      (let [s-expr (edn/read-string input)]
        (js/$gameMessage.add (str "READされたS式：\n"
                                  (rmmv-message-escape (pr-str s-expr))
                                  "\n\n"))
        (let [result (eval s-expr)]
          (js/$gameMessage.add (str "評価結果：\n"
                                    (rmmv-message-escape (pr-str result)))))))
    (catch :default e
      ;(js/console.log (.-stack e))
      (js/$gameMessage.add (str "エラーが発生しました：\n"
                                (.-stack e))))))



(defn- url-encode [text]
  (js/encodeURIComponent (str text)))

(defn gen-tweet-url [text & opt-args]
  ;; NB: 以下のオプションについては指定せずに、本文に埋め込んだ方がよい
  ;;     (ほとんどのオプションが、単に末尾に追記されるだけなので)
  (let [options (if (map? (first opt-args))
                  (first opt-args)
                  (apply hash-map opt-args))
        {url :url ; 末尾にurl追加
         hashtags :hashtags ; コンマ区切りでタグ指定(指定時は#抜きで)
         via :via ; 「@hogeさんから」を追加する(指定時は@なしで)
         related :related ; suggest機能用のヒント文字列指定らしい、詳細不明
         in-reply-to :in-reply-to ; 特定ツイートへの返信にする時用
         } options]
    (str "https://twitter.com/intent/tweet"
         "?"
         "text=" (url-encode text)
         (and url (str "&url=" (url-encode url)))
         (and hashtags (str "&hashtags=" (url-encode hashtags)))
         (and via (str "&via=" (url-encode via)))
         (and related (str "&related=" (url-encode related)))
         (and in-reply-to (str "&in-reply-to="
                               (url-encode in-reply-to)))
         )))

(defn tweet! [text & opt-args]
  (js/window.open (apply gen-tweet-url text opt-args) "_blank"))

(defn- gen-clear-tweet-url []
  (let [achievement-max 3
        achievement-now (count @flags)
        achievement-rate (int (* 100 (/ achievement-now achievement-max)))
        url (project-clj/get :game-url)
        ver (project-clj/get :version)
        text (str ""
                  "【" "#オッサンの冒険" ":" ver "】"
                  "オッサン、家に帰る。"
                  "　"
                  "おたから回収率："
                  achievement-now "/" achievement-max
                  "(" achievement-rate "%)"
                  "【" url "】"
                  )]
    (gen-tweet-url text)))




(defn- pause-mv! []
  (set! js/SceneManager._stopped true))

(defn- resume-mv! []
  (set! js/SceneManager._stopped false)
  (js/SceneManager.requestUpdate))



(def overlay-dom-id "overlay-dom")

(defn- get-overlay-dom []
  (if-let [dom (goog.dom/getElement overlay-dom-id)]
    dom
    (let [dom (js/document.createElement "div")
          parent js/document.body
          h (fn [e]
              (.preventDefault e)
              (.stopPropagation e)
              (goog.style/setStyle dom "display" "none")
              (resume-mv!)
              false)]
      (.setAttribute dom "id" overlay-dom-id)
      (goog.style/setStyle dom "background-color" "rgba(0, 0, 0, 0.75)")
      (goog.style/setStyle dom "position" "fixed")
      (goog.style/setStyle dom "left" "0")
      (goog.style/setStyle dom "right" "0")
      (goog.style/setStyle dom "top" "0")
      (goog.style/setStyle dom "bottom" "0")
      (goog.style/setStyle dom "z-index" "99999")
      (goog.style/setStyle dom "display" "none")
      (goog.style/setStyle dom "vertical-align" "middle")
      (.addEventListener dom "click" h false)
      ;(.addEventListener dom "touchend" h false) ; 必要かどうか不明
      (goog.dom/appendChild parent dom)
      dom)))

(defn- gen-button-dom [button-label url]
  (let [div (js/document.createElement "div")
        button (js/document.createElement "button")
        h (fn [e]
            (.preventDefault e)
            (.stopPropagation e)
            (js/window.open url "_blank")
            (goog.style/setStyle (get-overlay-dom) "display" "none")
            (resume-mv!)
            false)]
    (goog.style/setStyle div "position" "relative")
    (goog.style/setStyle div "top" "0")
    (goog.style/setStyle div "bottom" "0")
    (goog.style/setStyle div "left" "0")
    (goog.style/setStyle div "right" "0")
    ;(goog.style/setStyle div "background-color" "rgba(255, 0, 0, 0.75)")
    (goog.style/setStyle div "height" "100%")
    (goog.style/setStyle div "text-align" "center")
    (goog.style/setStyle button "position" "absolute")
    (goog.style/setStyle button "margin" "-25%")
    (goog.style/setStyle button "width" "50%")
    (goog.style/setStyle button "top" "50%")
    (goog.style/setStyle button "bottom" "50%")
    (goog.style/setStyle button "left" "50%")
    (goog.style/setStyle button "right" "50%")
    (goog.style/setStyle button "padding" "1em")
    (goog.style/setStyle button "font-size" "2em")
    (set! (.-innerHTML button) button-label)
    (.addEventListener button "click" h false)
    ;(.addEventListener button "touchend" h false) ; 必要かどうか不明
    (goog.dom/appendChild div button)
    div))

(defn- overlay-button! [button-label url]
  (pause-mv!)
  (let [overlay-dom (get-overlay-dom)
        ;; TODO: button-domを使い捨てずに使い回すようにする
        button-dom (gen-button-dom button-label url)]
    ;; overlay-domをcleanupする
    (goog.dom/removeChildren overlay-dom)
    ;; overlay-domの中央にbutton-domを設置する
    (goog.dom/appendChild overlay-dom button-dom)
    ;; overlay-domの非表示属性を除去する
    (goog.style/setStyle overlay-dom "display" "inline-block")
    true))

(def ac-url "https://github.com/ayamada/op0015/blob/master/clojure-advent-calendar-2015.md")

(defn ^:export open-ac []
  (overlay-button! "アドベントカレンダー記事本文を開く" ac-url))

(defn ^:export open-tweet []
  (overlay-button! "TWEETページを開く" (gen-clear-tweet-url)))

