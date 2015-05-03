(ns ^:figwheel-always pipfit.web.cljs.dashboard
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [figwheel.client :as fw]
            [om-bootstrap.input :as i]
            [om-bootstrap.button :as b]
            [om-datepicker.components :refer [datepicker]]
            [om-tools.dom :as d :include-macros :true]
            [cljs-http.client :as http]
            [om-tools.core :refer-macros [defcomponentk]]
            [cljs-pikaday.reagent :as pikaday]
            [cljs.core.async :refer [put! chan <!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn get-transactions!
  [cursor]
  (go (let [response (<! (http/get "/transactions" {}) )]
        (if (= 200 (:status response))
          (om/update! cursor [:table :transactions] (:body response))
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
      (let [{:keys [transactions]} (:table cursor)]
        (dom/div #js {:className "container"}
          (dom/h2 nil "Transactions")
          (dom/div #js {:className "container filterContainer"}
            (dom/div #js {:className "dateFilters"}
              (dom/label nil "Date:")
              (om/build datepicker (:start (:date (:filters cursor))) {:opts {}})
              (dom/label nil "to")
              (om/build datepicker (:end (:date (:filters cursor))) {:opts {}})) 
            (dom/div #js {:className "amountFilters"}
              (dom/label nil "Amount:") 
              (dom/input #js {:type "text"
                              :ref "amountStart"
                              :value (:start (:amount (:filters cursor)))})
              (dom/label nil "To") 
              (dom/input #js {:type "text"
                              :ref "amountEnd"
                              :value (:end (:amount (:filters cursor)))}))
            (dom/div #js {:className "searchFilters"}
              (dom/label nil "Search")
              (dom/input #js {:type "text"
                              :ref "searchPattern"
                              :value (:str (:search (:filters cursor)))})) 
            (dom/div #js {:className "categoryFilters"})
            (dom/button #js {:onClick #(get-transactions! cursor)
                             :className "btn"}
                        "Filter"))
          (dom/table #js {:className "table"}
          (dom/thead nil
            (apply dom/tr nil
              (map #(dom/th nil %) (:headers (:table cursor)))))
            (apply dom/tbody nil
              (om/build-all table-row transactions))))))))
