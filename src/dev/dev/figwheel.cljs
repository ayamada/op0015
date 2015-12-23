(ns dev.figwheel
  (:require-macros [project-clj.core :as project-clj])
  (:require [figwheel.client :as fw]))

(enable-console-print!)

(defn on-jsload []
  (let [on-jsloads (project-clj/get-in [:figwheel :on-jsloads])]
    (doseq [s on-jsloads]
      (let [[ns-str fn-str] (.split s "/")
            ns-strs (.split ns-str ".")
            chains (map munge-str
                        (concat ns-strs [fn-str]))
            f (reduce (fn [parent property]
                        (when parent
                          (aget parent property)))
                      js/window
                      chains)]
        (if f
          (f)
          (prn (str s " not found")))))))

(let [hostname (or
                 (project-clj/get-in [:figwheel :server-name])
                 js/window.location.hostname)
      port (project-clj/get-in [:figwheel :server-port] 3449)
      on-jsloads (project-clj/get-in [:figwheel :on-jsloads])]
  (fw/start {:websocket-url (str "ws://" hostname ":" port "/figwheel-ws")
             :on-jsload on-jsload
             :heads-up-display false}))

