(ns pipfit.parser.acctparsers.parserslist
  (:require [pipfit.parser.acctparsers.chasechecking
             :refer [->ChaseCheckingParser]]
            [pipfit.parser.account :refer :all]
            [clojure.string :as s]))

; Map of all parsers. When adding a new parser, add the constructor
; here.

(def parsers
  {["chase", :CHECKING] ->ChaseCheckingParser})

(defn get-parser
  "Retrieves the parser for a bank and account type."
  [bank accttype]
  (get parsers [(s/lower-case bank), accttype]))
