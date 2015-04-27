(ns pipfit.server.helpers
  (:require [pipfit.db.mongoadapter :as d]
            [pipfit.parser.accountparser]
            [pipfit.parser.acctparsers.parserslist :as pl]
            [pipfit.server.emailparser :as ep]
            ))

(defn resolve-sender
  "Checks the recipients of a parsed email message against  user ids in the db to find the user."
  [email-map db]
  (let [ours (filter 
               #(not (nil? (re-find #"pipfit" %)))
               (:to email-map))
        ids (map #(second (re-find #"<([0-9a-zA-Z]*)@" %)) ours)
        user (some (partial d/find-user-by-id db) ids)]
    user))
