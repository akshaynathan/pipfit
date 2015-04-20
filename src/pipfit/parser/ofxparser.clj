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
           ; Supported types
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
            CreditCardResponseMessageSet]
           )
  )

; Provides a parser for a subset of the OFX format by wrapping
; OFX4J. Functionality will be added as necessary.

(defrecord AccountUpdate [])

(defmulti parse-ofx
  "Parse OFX into map."
  class)

(defmethod parse-ofx ResponseEnvelope [e]
  (remove nil? (map parse-ofx (.getMessageSets e))))

(defmethod parse-ofx CreditCardResponseMessageSet [m]
  (map parse-ofx (.getResponseMessages m)))

(defmethod parse-ofx BankingResponseMessageSet [m]
  (map parse-ofx (.getResponseMessages m)))

(defmethod parse-ofx CreditCardStatementResponseTransaction [t]
  (parse-ofx (.getMessage t)))

(defmethod parse-ofx BankStatementResponseTransaction [t]
  (parse-ofx (.getMessage t)))

(defmethod parse-ofx CreditCardStatementResponse [r]
  {:account (parse-ofx (.getAccount r))
   :available-balance (parse-ofx (.getAvailableBalance r))
   :ledger-balance (parse-ofx (.getLedgerBalance r))
   :currency (.getCurrencyCode r)
   :transactions (map parse-ofx (.getTransactions
                                  (.getTransactionList r)))
   })

(defmethod parse-ofx BankStatementResponse [r]
  {:account (parse-ofx (.getAccount r))
   :available-balance (parse-ofx (.getAvailableBalance r))
   :ledger-balance (parse-ofx (.getLedgerBalance r))
   :currency (.getCurrencyCode r)
   :transactions (map parse-ofx (.getTransactions
                                  (.getTransactionList r)))
   })

(defmethod parse-ofx Transaction [t]
  {:amount (parse-money (.getAmount t))
   :date (date->timestamp (.getDatePosted t))
   :id (.getId t)
   :notes (.getMemo t)
   :name (.getName t)
   :type (str (.getTransactionType t))
   }
  )

(defmethod parse-ofx BankAccountDetails [acctdetails]
  {:account-key (.getAccountKey acctdetails)
   :account-number (.getAccountNumber acctdetails)
   :account-type (str (.getAccountType acctdetails))
   :routing-number (.getBankId acctdetails)
   })

(defmethod parse-ofx CreditCardAccountDetails [acctdetails]
  {:account-key (.getAccountKey acctdetails)
   :account-number (.getAccountNumber acctdetails)
   })

(defmethod parse-ofx BalanceInfo [balanceinfo]
  {:amount (parse-money (.getAmount balanceinfo))
   :as-of-date (date->timestamp (.getAsOfDate balanceinfo))
   })


; Ignore all unsupported classes.
(defmethod parse-ofx :default [data]
  nil)

; Retrieves the transaction type by parsing
; transaction information.
(defn- get-transaction-type [transaction]
  
  )

; Makes parsed ofx map into sequence of 
; acctparser.ParsedMessage's.
(defn- ofx->ParsedMessages [ofx-map]
  ofx-map
  )

(defn parse-file [path]
  (try 
    (ofx->ParsedMessages
      (parse-ofx (.unmarshal (AggregateUnmarshaller. ResponseEnvelope)
                              (FileInputStream. (File. path)))))
    (catch Exception e
      (log/error (str "Unable to parse ofx file.\n" e)))))
