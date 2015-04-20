(ns pipfit.parser.accountparser
  (:require [pipfit.parser.transaction :refer :all]
            [pipfit.parser.account :refer :all]))

; An account parser is responsible for parsing transactions
; for a specific bank or credit card account type.

(defrecord AccountParserInfo [
  ^String bank      ; The bank or institution.
  account-type      ; Type of account at bank (in parser.account/account-types).

  ^Integer version  ; The version of the parser, starts at 1.
                    ; In implementations, the version is changed everytime
                    ; the source is modified.

  supported-ttypes  ; Set of parser.transaction/transaction-types which this
                    ; parser supports.
  ])

(defrecord ParsedMessage [
  ^String acct-id ; Account identifier from the message. Typically
                  ; this is the entire or last 4 digits of the 
                  ; acct/card no.
  transaction     ; See parser.transaction.
  ])

(defprotocol AccountParser
  (get-info 
    [this] "Get AccountParserInfo for this parser.")
  (is-valid-for-email?
    [this message] "Checks if the parser is valid for an email message.")
  (parse-message
    [this message] "Parse email message and return ParsedMessage.")
  (parse-ofx-transaction 
    [this tmap]
    "Parse ofx transaction map (see parser.ofxparser) and return
    ParsedMessage")) 

(defn validate-info
  "Validates an AccountParserInfo record."
  [i]
  (and (string? (:bank i))
       (contains? account-types (:account-type i))
       (integer? (:version i))
       (pos? (:version i))
       (every? #(contains? transaction-types %) (:supported-ttypes i))))

(defn validate-parsed-message
  "Validates a ParsedMessage"
  [m]
  (and (string? (:acct-id m))
       (validate-transaction (:transaction m))))
