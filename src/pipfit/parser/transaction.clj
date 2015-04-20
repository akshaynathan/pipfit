(ns pipfit.parser.transaction
  (:require [pipfit.parser.helpers :refer :all]
            [clojure.tools.logging :as log]))

; We use a map to represent transactions, updates to a bank account.
; To add a new transaction type, add the type keyword to ttypes,
; and add any new fields to tfields along with a predicate function
; to validate them. In tfieldmapping, you must define which fields the
; transaction type contains.

; Set of transaction types.
(def transaction-types #{:WITHDRAW ; ATM Withdrawal
                         :DEPOSIT  ; Deposit into acct.
                         :DEBIT    ; Debit from acct.
                         :TRANSFER ; Transfer to other acct.
                         :PAYMENT  ; Payment of CC bill.
                         :BALANCE  ; Acct balance update.
                         })

; Map between fields and validation predicate
(def transaction-fields
  {:from string?
   :to string?
   :amount #(and (integer? %) (pos? %))
   :notes string?
   :time validate-timestamp
  })
     
; Mapping between transaction type and fields
(def field-mapping
  {:WITHDRAW #{:amount :notes :time}
   :DEPOSIT #{:from :amount :notes :time}
   :DEBIT #{:to :amount :notes :time}
   :TRANSFER #{:from :to :amount :notes :time}
   :PAYMENT #{:amount :notes :time}
   :BALANCE #{:amount :notes :time}})

; Validate a map t as a transaction.
(defn validate-transaction [t]
  "Validates a map as a transaction."
  (if (contains? transaction-types (:ttype t))
    (loop [[f1 & more] (seq ((:ttype t) field-mapping))]
      (if (nil? f1)
        true
        (if-not ((f1 transaction-fields) (f1 t))
          (do (log/error "Invalid or missing field" f1) false)
          (recur more))))
    (do (log/error "Invalid transaction type " (:ttype t)) false)))
