(ns ^:figwheel-always pipfit.web.cljs.login
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

(defn sign-in
  "Sign the user in via ajax."
  [data owner]
  (let [username (.-value (om/get-node owner "ufield"))
        password (.-value (om/get-node owner "pfield"))]
    (go (let [response 
              (<! (http/post "/signin" {:json-params {:username username
                                                      :password password}}))]
          (if (= 200 (:status response))
            (-> js/document
                .-location
                (set! "#/dashboard"))
            ; TODO: handle this more gracefully.
            (prn "INVALID LOGIN"))))))

(defn signed-in?
  "Checks if the current user is already signed in
  and writes result to a channel."
  [c]
  (go (let [response
            (<! (http/get "/signin"))]
        (>! c (= 200 (:status response))))))

; TODO: Validate input here.
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
      (dom/div #js {:className "container"}
        (dom/form #js {:className "loginform"}
          (dom/h2 #js {:className "formlabel"} "Login")
          (dom/label #js {:className "sr-only" :htmlFor "emailinput"}
                 "Email Address")
          (dom/input #js {:type "text"
                          :ref "ufield"
                          :id "emailinput"
                          :placeholder "email address" 
                          :required true
                          :className "form-control"
                          :onChange #(handle-change % owner :username)
                          :value (:username state)})
          (dom/label #js {:className "sr-only" :htmlFor "passinput"}
                 "Password")
          (dom/input #js {:type "password"
                          :ref "pfield"
                          :onChange #(handle-change % owner :password)
                          :id "passinput"
                          :required true
                          :className "form-control"
                          :placeholder "password"
                          :value (:password state)}) 
          (dom/button #js {:onClick #(sign-in data owner)
                           :className "btn btn-lg btn-primary btn-block"
                           } "Log In"))))))
