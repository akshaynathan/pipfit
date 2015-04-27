(ns pipfit.server.mailgunserver
  (:gen-class)
  (:require [pipfit.db.mongoadapter :refer :all]
            [pipfit.server.helpers :refer :all]
            [pipfit.server.emailparser :refer [original-sender
                                               remove-forward-meta]]
            [pipfit.parser.helpers :refer [epoch->timestamp]]
            [pipfit.parser.acctparsers.parserslist :refer :all]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]))

; Implements an http server to accept mailgun post requests
; on receipt of an email. The incoming "messages" are then parsed
; and added to the database.

; Assume db config stored in env.
(def db (connect-using-env))

(defn mailgun-handler
  "Handler for mail posted by mailgun. We will parse and decode 
  the mail and add it to the database."
  [request]  
  (try (let
       [_ (log/info "Parsing email from: "
                    (get (:params request) "sender"))
        email-map {:to [(get (:params request) "recipient")]
                   :from (get (:params request) "sender")
                   :subject (get (:params request) "subject")
                   :body (remove-forward-meta
                           (get (:params request) "body-plain"))
                   :sender (original-sender (get (:params request) "body-plain"))
                   :date (epoch->timestamp
                           (* 1000 
                              (Long. (get (:params request) "timestamp"))))}
        _ (log/info "Email map: " email-map)
        p (parse-email-map email-map) 
        _ (log/info "Parsed message: " p)
        uid (:_id (resolve-sender email-map db))
        _ (add-parsed-message-to-db db p uid)]
    {:status 200})
       (catch Exception e
         (log/error "Unable to parse message: " (.getMessage e))
         (.printStackTrace e)
         {:status 406}))) 

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port)))]
   (run-jetty 
     (wrap-params mailgun-handler)
     {:port port :join? false})))
