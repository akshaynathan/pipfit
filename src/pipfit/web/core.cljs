(ns ^:figwheel-always pipfit.core
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

(enable-console-print!)

(fw/start {})

(defn sign-in
  "Sign the user in using ajax."
  [data owner]
  (let [username (.-value (om/get-node owner "ufield"))
        password (.-value (om/get-node owner "pfield"))]
    (go (let [response (<! (http/post "/login" {:json-params {:username username
                                                              :password password}}))]
          (prn (:status response))
          (prn (:body response))))))

(defn handle-change
  "Modify state based on changes in username or password field."
  [e owner k]
  (om/set-state! owner k (.. e -target -value)))

(defn login-form [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:username "" :password ""})
    om/IRenderState
    (render-state [this state]
      (dom/div nil
       (dom/h2 nil "Login")
       (dom/div nil
        (dom/input #js {:type "text"
                        :ref "ufield"
                        :onChange #(handle-change % owner :username)
                        :value (:username state)})
        (dom/input #js {:type "password"
                        :ref "pfield"
                        :onChange #(handle-change % owner :password)
                        :value (:password state)}) 
        (dom/button #js {:onClick #(sign-in data owner)} "Submit"))))))

(om/root
  login-form
  {}
  {:target (. js/document (getElementById "app"))})
