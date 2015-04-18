(ns pipfit.parser.acctparsers.chasechecking
  (:require [pipfit.parser.helpers :refer :all]
            [pipfit.parser.transaction :refer :all]
            [pipfit.parser.acctparser :refer :all]
            [clojure.string :as string]
            [clj-time.format :as f]
            )
  )

; Bank - Chase
; Account Type - Checking / Debit

; Current parser version, change on each revision.
(def version 1)

(def supported_transactions
  #{:PAYMENT}
  )

; Helper method to parse time from emails.
; Chase times in format MM/DD/YYYY H:MM:SS PM/AM TIMEZONE
(defn- parse_time [str_time]
  (let [formatter (f/formatter "MM/dd/yyyy h:mm:ss aa zzz")
        parsed_time (f/parse formatter str_time)]
    (f/unparse (f/formatters :date-time-no-ms) parsed_time)))


(defrecord ChaseCheckingParser []
  Parser
  (get_info [this]
    (->ParserInfo "Chase" "Checking" version supported_transactions)
    )
  ; Example messages are below
  ; TODO: Handle other types of transactions.
  (parse_message [this message]
    (let 
      [
        lines (string/split message #"[\r\n]+")
        ; Get integer account number from first line.
        acct_id (re-find #"\d+" (first lines))
        content (second lines)
        ; Read amount in
        amount (parse_money (second (string/split content #" ")))
        ; Get recipient (between lowercase "to" and "on")
        recipient (string/trim (text_before (text_after content #" to ") #" on ")) 
        ; Get and parse time
        parsed_time (parse_time
                     (text_before (text_after content #" on ") #" exceeded "))
       ]
      (->ParsedMessage acct_id (hash-map
                                 :ttype :PAYMENT
                                 :to recipient
                                 :amount amount
                                 :notes ""
                                 :time parsed_time
                                 )))))

; TODO: Add more example messages.
; TODO: Move all example messages to the test cases.

; EXAMPLE MESSAGES
;
; :PAYMENT
; 
; This is an Alert to help manage your account ending in 8307. \n
; A $5.00 debit card transaction to VENMO on 03/30/2015 9:21:07 PM EDT exceeded
; your $1.00 set Alert limit. \n
; If you have any questions about this transaction, please call 1-877-CHASEPC. \n
;
; :WITHDRAWAL
;
; :TRANSFER
;
; :DEPOSIT
;
