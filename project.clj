
(def the-title "オッサンの冒険")
(def short-title "オッサン")
(def game-url "http://vnctst.tir.jp/op0015/")
(def intro-url "https://github.com/ayamada/op0015")
(def version "1.0.2")
(def port-dev 8015)
(def port-fw 9015)
(def docroot-in-resources "public")

;;; figwheelの :on-jsload で実行したい関数名をnamespace付きの文字列で複数登録
;;; TODO: これは無駄に複雑になる原因になっているので、どうにかしたいが…
;;;       根本のfigwheelが複雑なので、どうしようもないのでは？
;(def on-jsloads ["vnctst.scene/refresh-by-figwheel!"])
(def on-jsloads [])







(def foreign-libs
  [
   ])
(def externs
  [
   "src/externs/rmmv.js"
   ;"src/externs/pixi_v2.js"
   ;"src/externs/lz-string.js"
   ])

;;; :optimizations :advanced にすると、evalが動かなくなってしまった。
;;; どうしようもないっぽいので、今回は:advancedにはしない事にした。
(def use-advanced-optimizations? false)

(def compiler-option-prod
  {:foreign-libs foreign-libs
   :main 'op0015.core
   :output-to "resources/public/cljs/cl.js"
   :externs externs
   ;:preamble ["src/js/license.js"]
   :language-in :ecmascript5
   :language-out :ecmascript5
   :pretty-print false
   :optimizations (if use-advanced-optimizations? :advanced :simple)
   })

(def compiler-option-dev
  (merge compiler-option-prod
         {:output-dir "resources/public/cljs/out"
          :asset-path "cljs/out"
          ;:preamble []
          :optimizations :none
          :source-map true
          :source-map-timestamp true
          :cache-analysis true
          :pretty-print true
          }))










(defproject jp.tir.vnctst/op0015 version
  :min-lein-version "2.5.0"
  :url ~intro-url
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.189"]
                 [org.clojure/core.async "0.2.374"]
                 [jp.ne.tir/project-clj "0.1.2"]
                 ;[ring "1.4.0"]
                 ;[hiccup "1.0.5"]
                 ;[cljs-ajax "0.3.11"]
                 ]
  :source-paths ["src/clj" "src/cljs"]
  :plugins [[lein-cljsbuild "1.1.2"]
            ;[lein-ring "0.9.7"]
            ]
  :clean-targets ^{:protect false} [:target-path
                                    :compile-path
                                    "resources/public/cljs"
                                    "figwheel_server.log"]
  ;; NB: 実際のコマンドは以下のようになる
  ;;     - prod: lein clean && lein with-profile prod cljsbuild once prod
  ;;     - dev: rlwrap lein figwheel dev
  :profiles {:prod {:source-paths ["src/clj" "src/cljs" "src/prod"]}
             :dev {:source-paths ["src/clj" "src/cljs" "src/dev"]
                   :plugins [[lein-figwheel "0.5.0-2"]]
                   :dependencies [[figwheel "0.5.0-2"]]}}
  :cljsbuild {:builds {:prod {:source-paths ["src/cljs" "src/prod"]
                              :compiler ~compiler-option-prod
                              :jar true}
                       :dev {:source-paths ["src/cljs" "src/dev"]
                             :compiler ~compiler-option-dev
                             :figwheel {:on-jsload "dev.figwheel/on-jsload"
                                        :heads-up-display false}
                             :jar true}}}
  ;:main vnctst-server-dev.core
  ;:ring {:handler vnctst-server-dev.core/app
  ;       :init vnctst-server-dev.core/init
  ;       :port ~port-dev
  ;       }
  :figwheel {:http-server-root ~docroot-in-resources
             :server-name "localhost"
             :server-port ~port-fw
             :server-logfile "figwheel_server.log"
             :on-jsloads ~on-jsloads
             }
  :game-url ~game-url
  )
