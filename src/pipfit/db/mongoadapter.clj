(ns pipfit.db.mongoadapter
  (:require [monger.core :as m]
            [monger.collection :as mc]
            [monger.joda-time]
            [monger.json]
            [clj-time.core :as t]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]])
  (:import [com.mongodb DB MongoOptions ServerAddress]
           [org.bson.types ObjectId]))

(defn- authenticate
  "Authenticate with a db."
  [db username password]
  (if (m/authenticate db username (.toCharArray password))
    db
    (log/error "Unable to authenticate with db:" (str db))))

(defn connect
  "Connect to a remote database at host:port."
  [host port db-name username password]
  (let [conn (m/connect {:host host :port port})
        db (m/get-db conn db-name)]
    (authenticate db username password)))

(defn connect-to-uri
  "Connect to database at uri."
  [uri username password]
  (let [[conn db] (m/connect-via-uri uri)] 
    (authenticate db username password)))

(defn connect-using-env
  "Connects to a database using values stored in env
  variables DBHOST, DBNAME, DBPORT, DBUSER, DBPASS."
  []
  (connect (env :dbhost)
           (Integer. (env :dbport))
           (env :dbname)
           (env :dbuser)
           (env :dbpass)))

(defn create-user
  "Creates a new user."
  [db email passhash salt]
  (mc/insert-and-return db "Users" {:_id (ObjectId.)
                                    :email email
                                    :password passhash
                                    :salt salt}))

(defn create-bank-account
  "Creates a new account." 
  [db uid bank accttype acctstr]
  (mc/insert-and-return db "Accounts" {:_id (ObjectId.)
                                       :uid (ObjectId. uid)
                                       :acctstr acctstr
                                       :bank bank
                                       :type accttype
                                       :balance {:ledger 0
                                                 :available 0
                                                 :date (t/epoch)}}))

(defn create-transactions
  "Adds transactions to an account."
  [db uid aid transaction]
  (if (= (:ttype transaction) :BALANCE)
    (mc/update-by-id db "Accounts" aid {:balance {(keyword 
                                                    (:notes transaction))
                                                  (:amount transaction)
                                                  :date (:time transaction)}})
    (mc/insert-and-return db "Transactions" {:_id (ObjectId.)
                                           :uid (ObjectId. uid)
                                           :aid (ObjectId. aid)
                                           :type (:ttype transaction)
                                           :amount (:amount transaction)
                                           :to (:to transaction)
                                           :from (:from transaction)
                                           :notes (:notes transaction)
                                           :date (:time transaction)})))

(defn find-user-by-id
  "Finds a user by id."
  [db uid]
  (first (mc/find-maps db "Users" {:_id (ObjectId. uid)})))

(defn find-acct-by-idstr
  "Finds an acount by the account string."
  [db uid acctstr]
  (first (mc/find-maps db "Accounts" {:uid (ObjectId. uid) :acctstr acctstr})))
