(ns pipfit.web.clj.login
  (:require 
    [pipfit.db.mongoadapter :as db]
    [cemerick.friend :as friend]
    (cemerick.friend  [workflows :as workflows] [credentials :as creds])
    [ring.middleware.json :as json]
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
                                  :authorisation-workflow})
            {:status 401}))))

(def app-routes
  (compojure/routes
    (GET "/whoami" req
         (do
           {:status 200
            :headers {"Content-Type" "application/json"}
            :body (friend/current-authentication) 
            }))))

(def app
    (->  (handler/site
                   (friend/authenticate app-routes 
                                        {:workflows 
                                         [(authentication-workflow)]}))
              (params/wrap-keyword-params)
              (json/wrap-json-body)
              (json/wrap-json-response  {:pretty true})))


