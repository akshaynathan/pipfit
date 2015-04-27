(ns pipfit.parser.acctparsers.chasechecking
  (:require [pipfit.parser.helpers :refer :all]
            [pipfit.parser.transaction :refer :all]
            [pipfit.parser.accountparser :refer :all]
            [clojure.string :as string]
            [clj-time.format :as f]
            [clojure.tools.logging :as log]
            )
  )

; Bank - Chase
; Account Type - Checking / Debit
; Example messages - acctparsers/testfiles/chasechecking

; Current parser version, change on each revision.
(def version 1)

(def supported-transactions
  #{:DEBIT :BALANCE :WITHDRAW :TRANSFER}
  )

; Helper method to parse time from emails.
; Chase times in format MM/DD/YYYY H:MM:SS PM/AM TIMEZONE
(defn- parse-time [str-time]
  (let [formatter (f/formatter "MM/dd/yyyy h:mm:ss aa zzz")
        parsed-time (f/parse formatter (string/trim (string/replace
                                                      str-time
                                                      #"\s+" " ")))]
    (f/unparse (f/formatters :date-time-no-ms) parsed-time)))

; Helper method to parse out the actual transaction.
(defn- parse-transaction [text]
  (if (= (next-word text) "As")
    ; BALANCE
    (let [t (parse-time (text-before (text-after text #"of") #","))
          b (parse-money (re-find #"\$\d*\.\d*" text))]
      {:ttype :BALANCE, :amount b, :time t, :notes ""})
    (let [b (parse-money (second (string/split text #" ")))
          t (parse-time
              (text-before (text-after text #" on ") #" exceeded "))]
      (if (re-find #"(?i)withdraw" text)
        ; WITHDRAW
        {:ttype :WITHDRAW, :amount b, :time t, :notes ""}
        (let [r (string/trim (text-before (text-after text #" to ") #" on "))]
          (if (re-find #"(?i)transfer" text)
            ; TRANSFER
            {:ttype :TRANSFER, :amount b, :time t, :to r, :notes ""}
            ; DEBIT
            {:ttype :DEBIT, :amount b, :time t, :to r, :notes ""}))))))

(defrecord ChaseCheckingParser []
  AccountParser
  (get-info [this]
    (->AccountParserInfo "CHASE" :CHECKING version supported-transactions))
  ; TODO: Write tests for is-valid-for-email?
  (is-valid-for-email? [this email]
    (not (nil? (and 
                 (or (re-find #"alertsp\.chase\.com" (:sender email))
                     (re-find #"(?i)www\.Chase\.com" (:body email)))
                 (or (re-find #"minimum balance" (:body email))
                     (re-find #"Debit" (:subject email))
                     (re-find #"external transfer" (:body email))
                     (re-find #"ATM" (:body email)))))))
  (parse-message [this message]
    (let 
      [lines (string/split message #"\.[\r\n]+")
       ; Get integer account number from first line.
       acct-id (re-find #"\d+" (first lines))
       content (second lines)
       transaction (parse-transaction content)]
      (->ParsedMessage acct-id transaction)))
  (parse-ofx-transaction [this acctid transaction]
    (let
      [notes (if (nil? (:notes transaction))
               ""
               (:notes transaction))
       amount (:amount transaction)
       date (:date transaction)
       to (:name transaction)
       t (if (re-find #"(?i)withdraw" to)
           {:ttype :WITHDRAW :amount amount :notes notes :time date}
           (if (re-find #"(?i)web id" notes)
             {:ttype :TRANSFER :amount amount :notes notes :time date :to to}
             {:ttype :DEBIT :amount amount :notes notes :time date :to to}))]
      (->ParsedMessage acctid t))))
