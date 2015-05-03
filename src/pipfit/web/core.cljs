(ns ^:figwheel-always pipfit.web.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [figwheel.client :as fw]
            [om-bootstrap.input :as i]
            [pipfit.web.cljs.login :as l]
            [pipfit.web.cljs.dashboard :as da]
            [om-bootstrap.button :as b]
            [om-tools.dom :as d :include-macros :true]
            [cljs-http.client :as http]
            [om-datepicker.dates :refer [today]]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :refer-macros [defroute]]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]]
            [goog.history.EventType :as EventType])
  (:import goog.History) 
  (:require-macros [cljs.core.async.macros :refer [go]]) )

; TODO: Move this to dev profile.
(enable-console-print!)
(fw/start {})

(defonce app-state
  (atom
    {:date {:value (today)}
     :filters {:date {:start {:value (today)}
                      :end {:value (today)}}
               :amount {:start 0
                        :end 1000000}
               :search ""}
     :table {:headers ["Date" "Amount" "Recipient" "Type"]
             :filtered? false
             :transactions []
             :filtered_ts []}}))

; Client side routing set-up.
(sec/set-config! :prefix "#")
(let [history (History.)
      navigation EventType/NAVIGATE]
  (goog.events/listen history
                     navigation
                     #(-> % .-token sec/dispatch!))
  (doto history (.setEnabled true)))

(defroute "/login" []
  (om/root l/login-form
           app-state
           {:target (. js/document (getElementById "app"))}))

(defroute "/dashboard" []
  (do 
    (l/signed-in?)
    (om/root da/dashboard-table
             app-state
             {:target (. js/document (getElementById "app"))}))) 

(-> js/document
    .-location
    (set! "#/dashboard"))
