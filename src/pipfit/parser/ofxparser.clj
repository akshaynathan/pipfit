(ns pipfit.parser.ofxparser
  (:require [pipfit.parser.transaction :refer :all]
            [pipfit.parser.helpers :refer :all]
            [clojure.tools.logging :as log]
            [clojure.string :as s]
            )
  (:import [java.io FileInputStream File]
           [java.util Date]
           [net.sf.ofx4j.io AggregateUnmarshaller]
           [net.sf.ofx4j.domain.data ResponseEnvelope]
           ; Basic
           [net.sf.ofx4j.domain.data.common TransactionList
                                            Transaction
                                            BalanceInfo]
           ; Checking/Saving
           [net.sf.ofx4j.domain.data.banking 
            BankAccountDetails
            BankStatementResponse
            BankStatementResponseTransaction
            BankingResponseMessageSet]
           ; Credit
           [net.sf.ofx4j.domain.data.creditcard
            CreditCardAccountDetails
            CreditCardStatementResponse
            CreditCardStatementResponseTransaction
            CreditCardResponseMessageSet]))

; Provides a parser for a subset of the OFX format by wrapping
; OFX4J. Functionality will be added as necessary.

(defmulti ofx->map
  "Parses OFX file into map. Unsupported tags are ignored."
  class)

(defmethod ofx->map ResponseEnvelope [e]
  (remove nil? (map ofx->map (.getMessageSets e))))

(defmethod ofx->map CreditCardResponseMessageSet [m]
  (map ofx->map (.getResponseMessages m)))

(defmethod ofx->map BankingResponseMessageSet [m]
  (map ofx->map (.getResponseMessages m)))

(defmethod ofx->map CreditCardStatementResponseTransaction [t]
  (ofx->map (.getMessage t)))

(defmethod ofx->map BankStatementResponseTransaction [t]
  (ofx->map (.getMessage t)))

(defmethod ofx->map CreditCardStatementResponse [r]
  {:account (ofx->map (.getAccount r))
   :available-balance (ofx->map (.getAvailableBalance r))
   :ledger-balance (ofx->map (.getLedgerBalance r))
   :currency (.getCurrencyCode r)
   :transactions (map ofx->map (.getTransactions
                                  (.getTransactionList r)))})

(defmethod ofx->map BankStatementResponse [r]
  {:account (ofx->map (.getAccount r))
   :available-balance (ofx->map (.getAvailableBalance r))
   :ledger-balance (ofx->map (.getLedgerBalance r))
   :currency (.getCurrencyCode r)
   :transactions (map ofx->map (.getTransactions
                                  (.getTransactionList r)))})

(defmethod ofx->map Transaction [t]
  {:amount (parse-money (.getAmount t))
   :date (date->timestamp (.getDatePosted t))
   :id (.getId t)
   :notes (.getMemo t)
   :name (.getName t)
   :type (str (.getTransactionType t))})

(defmethod ofx->map BankAccountDetails [acctdetails]
  {:account-number (.getAccountNumber acctdetails)
   :account-type (str (.getAccountType acctdetails))
   :routing-number (.getBankId acctdetails)})

(defmethod ofx->map CreditCardAccountDetails [acctdetails]
  {:account-number (.getAccountNumber acctdetails)})

(defmethod ofx->map BalanceInfo [balanceinfo]
  {:amount (parse-money (.getAmount balanceinfo))
   :as-of-date (date->timestamp (.getAsOfDate balanceinfo))})

; Ignore all unsupported classes.
(defmethod ofx->map :default [data]
  nil)

; Makes parsed ofx map into sequence of 
; acctparser.ParsedMessage's.
(defn- ofx->ParsedMessages [ofx-map]
  ofx-map
  )


(defn parse-file [path]
  (try 
    (ofx->ParsedMessages
      (ofx->map (.unmarshal (AggregateUnmarshaller. ResponseEnvelope)
                            (FileInputStream. (File. path)))))
    (catch Exception e
      (log/error (str "Unable to parse ofx file.\n" e)))))
