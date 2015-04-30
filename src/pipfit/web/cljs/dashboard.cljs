(ns ^:figwheel-always pipfit.web.cljs.dashboard
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [figwheel.client :as fw]
            [om-bootstrap.input :as i]
            [om-bootstrap.button :as b]
            [om-tools.dom :as d :include-macros :true]
            [cljs-http.client :as http]
            [om-tools.core :refer-macros [defcomponentk]]
            [cljs.core.async :refer [put! chan <!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn get-transactions!
  [cursor]
  (go (let [response (<! (http/get "/transactions" {}) )]
        (if (= 200 (:status response))
          (om/update! cursor [:transactions] (:body response))
          (prn "INVALID RESPONSE.")))))

(defn table-row [transaction owner]
  (reify
    om/IRender
    (render
      [_]
      (apply dom/tr nil
        (map #(dom/td nil %) (vals transaction))))))

(defn dashboard-table [cursor owner]
  (reify
    om/IInitState
    (init-state [_] {})
    om/IRenderState
    (render-state [this state]
      (let [{:keys [transactions]} cursor]
        (dom/div #js {:className "container"}
          (dom/button #js {:onClick #(get-transactions! cursor)} "Filter")
          (dom/h2 nil "Transactions")
          (dom/table #js {:className "table"}
          (dom/thead nil
            (apply dom/tr nil
              (map #(dom/th nil %) (:headers cursor))))
          (apply dom/tbody nil
            (om/build-all table-row transactions))))))))
