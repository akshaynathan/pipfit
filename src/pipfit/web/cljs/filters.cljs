(ns ^:figwheel-always pipfit.web.cljs.filters
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [figwheel.client :as fw]
            [om-bootstrap.input :as i]
            [om-datepicker.dates :refer [today]]
            [om-bootstrap.button :as b]
            [om-datepicker.components :refer [datepicker]]
            [om-tools.dom :as d :include-macros :true]
            [cljs-http.client :as http]
            [om-tools.core :refer-macros [defcomponentk]]
            [cljs-pikaday.reagent :as pikaday]
            [cljs-time.coerce :as ctime]
            [cljs.core.async :refer [put! chan <!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn in-date-range?
  "Returns true of transaction is within date range from state."
  [transaction state]
  (let [dstart (.valueOf (:value (:start (:date state))))
        dend (.valueOf (:value (:end (:date state))))
        t (ctime/to-long (:time transaction))]
    (and (<= t dend) (>= t dstart))))

(defn matches-pattern?
  "Returns true of transaction name matches search pattern."
  [transaction state]
  (let [pattern (re-pattern (:search state))
        n (str (:name transaction))]
    (or (empty? (:search state)) 
        (boolean (re-find pattern n)))))

(defn in-amount-range?
  "Returns true of transaction amount is within amount range."
  [transaction state]
  (let [start (js/parseFloat (:start (:amount state)))
        end (js/parseFloat (:end (:amount state)))
        a (js/parseFloat (:amount transaction))]
    (and (<= a end) (> a start))))

(defn filter-transactions!
  "Filter the transactions."
  [state cursor]
  (let [matching-fn #(and (in-date-range? % state)
                          (matches-pattern? % state)
                          (in-amount-range? % state))]
   (om/update! cursor [:table :filtered_ts]
               (filter matching-fn (:transactions (:table cursor)))))) 

(defn handle-change
  "Updates field in app-state corresponding to the filters."
  [e cursor k]
  (om/update! cursor k (.. e -target -value)))

(defn filters-component [cursor owner]
  (reify
    om/IInitState
    (init-state [_] {:amount {:start 0
                              :end 0}
                     :date {:start {:value (today)}
                            :end {:value (today)}}
                     :search ""
                     })
    om/IRenderState
    (render-state [this state]
      (let [filter-cursor (:filters cursor)]
      (dom/div #js {:className "container filterContainer"}
        (dom/div #js {:className "dateFilters"}
          (dom/label nil "Date:")
          (om/build datepicker (:start (:date filter-cursor)))
          (dom/label nil "To")
          (om/build datepicker (:end (:date filter-cursor))))
        (dom/div #js {:className "amountFilters"}
          (dom/label nil "Amount:") 
          (dom/input #js {:type "text"
                          :ref "amountStart"
                          :value (:start (:amount filter-cursor))
                          :onChange #(handle-change % filter-cursor [:amount :start])})
          (dom/label nil "To") 
          (dom/input #js {:type "text"
                          :ref "amountEnd"
                          :value (:end (:amount filter-cursor))
                          :onChange #(handle-change % filter-cursor [:amount :end])}))
        (dom/div #js {:className "searchFilters"}
          (dom/label nil "Search")
          (dom/input #js {:type "text"
                          :ref "searchPattern"
                          :value (:search filter-cursor)
                          :onChange #(handle-change % filter-cursor :search)}))
        (dom/div #js {:className "categoryFilters"})
        (dom/button #js {:onClick #(filter-transactions! filter-cursor cursor)
                         :className "btn"}
                    "Filter"))))))
