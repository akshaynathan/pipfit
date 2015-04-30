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
    {:table {:headers ["Date" "Amount" "Recipient" "Type"]
             :transactions []}}))

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
  (om/root da/dashboard-table
           app-state
           {:target (. js/document (getElementById "app"))}))

; Redirect to the login page only if the user is not already signed
; in.
; TODO: Don't check this every time the page is refreshed.
(go (let [c (chan)
          _ (l/signed-in? c)
          sn (<! c)
          dest (if sn "#/dashboard" "#/login")]
      (-> js/document
          .-location
          (set! dest))))
