(ns pipfit.web.clj.login
  (:require 
    [pipfit.db.mongoadapter :as db]
    [cemerick.friend :as friend]
    (cemerick.friend  [workflows :as workflows] [credentials :as creds])
    [ring.middleware.json :as json]
    [ring.middleware.session :as ring-session]
    [ring.middleware.keyword-params :as params]
    [compojure.core :as compojure :refer  (GET POST ANY defroutes)]
    (compojure  [handler :as handler] [route :as route])
    [ring.util.response :as resp]
    [clojure.tools.logging :as log]
    [cemerick.friend.credentials :refer  (hash-bcrypt)]))

; This file defines the login api. The front-end can exchange
; username,password pairs for session info here.

(def db (db/connect-using-env))

; TODO: Use salts
(defn get-user-for-username
  "Returns a user map of the following format:
  {:identity username :username username :password hashedpass :uid userid}
  from the database for the given username."
  [username]
  (let [um (db/get-user-by-name db username)]
    (if um
      {:identity username
       :username username
       :password (:password um)
       :uid (str (:_id um))}
      nil)))

(defn get-transactions
  "Returns a vector of transactions for the current user in the format
  {time: time amount: amount name: name :type type}"
  []
  (let [_ (log/info (friend/current-authentication))
        uid (:uid (friend/current-authentication))
        ts (db/get-transactions-for-user db uid)]
    (map #(hash-map :time (:date %)
            :amount (/ (:amount %) 100)
            :name (:to %)
            :type (:type %)) ts)))

(defn authenticate-user  [{username "username" password "password"}]
    (if-let  [user-record  (get-user-for-username username)]
          (if  (creds/bcrypt-verify password  (:password user-record))
                  (dissoc user-record :password))))

(defn authentication-workflow []
  (compojure/routes
    (GET "/logout" req
         (friend/logout* {:status 200}))
    (POST "/login" {body :body}
          (if-let [user-record (authenticate-user body)]
            (workflows/make-auth user-record
                                 {:cemerick.friend/workflow
                                  :authentication-workflow
                                  ::friend/redirect-on-auth? false})
            {:status 401}))))

(def app-routes
  (compojure/routes
   (GET "/transactions" req
        
           (friend/authenticated (let [_ (log/info "We HERE")
                 _ (log/info (friend/current-authentication))
                 ts (get-transactions)]
           {:status 200
          :headers {"Content-Type" "application/json"}
          :body ts
          }))
         )
    (POST "/login" req
           {:status 200
            :headers {"Content-Type" "application/json"}
            :body (friend/current-authentication) 
            }    
          )))

(def secure-app
  (-> app-routes
      (friend/authenticate {:workflows [(authentication-workflow)]})
      (ring-session/wrap-session)
      (params/wrap-keyword-params)
      (json/wrap-json-body)
      (json/wrap-json-response  {:pretty true})))

(def app
  (compojure/routes
    (GET "/" req (resp/resource-response "index.html" {:root "public"}))
    secure-app))
