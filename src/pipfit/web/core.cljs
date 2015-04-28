(ns ^:figwheel-always pipfit.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [figwheel.client :as fw]
            [om-bootstrap.input :as i]
            [pipfit.web.cljs.login :as l]
            [om-bootstrap.button :as b]
            [om-tools.dom :as d :include-macros :true]
            [cljs-http.client :as http]
            [om-tools.core :refer-macros [defcomponentk]]
            [cljs.core.async :refer [put! chan <!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(fw/start {})

(om/root
  l/login-form
  {}
  {:target (. js/document (getElementById "app"))})
