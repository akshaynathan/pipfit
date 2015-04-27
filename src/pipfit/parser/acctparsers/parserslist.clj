(ns pipfit.parser.acctparsers.parserslist
  (:require [pipfit.parser.acctparsers.chasechecking
             :refer [->ChaseCheckingParser]]
            [pipfit.parser.account :refer :all]
            [pipfit.parser.accountparser :refer :all]
            [clojure.string :as s]
            [clojure.tools.logging :as log]))

; Map of all parsers. When adding a new parser, add the constructor
; here.

(def parsers
  {["chase", :CHECKING] ->ChaseCheckingParser})

(defn get-parser
  "Retrieves the parser for a bank and account type."
  [bank accttype]
  (get parsers [(s/lower-case bank), accttype]))

(defn parse-email-map
  "Chooses the correct parser and parses a map describing an email message (see server.emailparser)."
  [email]
  (let [ps (filter #(is-valid-for-email? % email) (vals parsers))
        [v r] ps]
    (if r
      (log/error "Multiple parsers match email.\n" ps)
      (if v
        (parse-message (v) (:body email))
        (log/error "No parsers match email.")))))
