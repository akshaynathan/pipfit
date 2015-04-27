(ns pipfit.server.helpers
  (:require [pipfit.db.mongoadapter :as d]
            [pipfit.parser.accountparser]
            [pipfit.parser.acctparsers.parserslist :as pl]
            [pipfit.server.emailparser :as ep]
            [clojure.tools.logging :as log]))

(defn resolve-sender
  "Checks the recipients of a parsed email message against  user ids in the db to find the user."
  [email-map db]
  (let [ours (filter 
               #(not (nil? (re-find #"pipfit" %)))
               (:to email-map))
        ids (map #(second (re-find #"\s*([0-9a-zA-Z]*)@" %)) ours)
        user (some (partial d/find-user-by-id db) ids)]
    user))

(defn add-parsed-message-to-db
  "Adds ParsedMessage to the db and returns the added transaction."
  [db pm uid]
  (let [; Find the account.
        aid (:_id (d/find-acct-by-idstr db (str uid) (str (:acct-id pm))))
        _ (log/info uid aid)
        ; Create the transaction
        t (d/create-transactions db (str uid) (str aid) (:transaction pm))]
    t))
