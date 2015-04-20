(ns pipfit.parser.acctparsers.chasechecking_test
  (:require [clojure.test :refer :all]
            [pipfit.parser.accountparser :refer :all]
            [pipfit.parser.acctparsers.chasechecking :refer :all]
            [clojure.string :as s]))

(def tfpath "test/pipfit/parser/acctparsers/testfiles/chasechecking/")

; Input OFX maps from ofxparser.
(def input-ofx
  {"debit" {:amount 729
            :date "2015-04-14T22:33:14Z"
            :id "201303041" 
            :notes ""
            :name "MCDONALDS"
            :type "DEBIT"
            }
   "withdraw" {:amount 30000
               :date "2015-03-17T03:34:44Z"
               :id "201303041" 
               :notes ""
               :name "NON-CHASE ATM WITHDRAW"
               :type "DEBIT"
               }
   "transfer" {:amount 33696
               :date "2015-02-21T07:20:13Z"
               :id "201303041" 
               :notes "WEB ID: 123455677"
               :name "DISCOVER"
               :type "DEBIT"
               }})

; Expected output for the test messages and OFX maps.
(def expected-parsed-messages
  {"debit" (->ParsedMessage "1234" {:ttype :DEBIT
                                    :to "MCDONALDS"
                                    :amount 729
                                    :notes ""
                                    :time "2015-04-14T22:33:14Z"
                                    })
   "balance" (->ParsedMessage "1234" {:ttype :BALANCE
                                      :amount 3101
                                      :notes ""
                                      :time "2015-03-17T03:19:33Z"
                                      })
   "withdraw" (->ParsedMessage "1234" {:ttype :WITHDRAW
                                       :amount 30000
                                       :notes ""
                                       :time "2015-03-17T03:34:44Z"
                                       })
   "transfer" (->ParsedMessage "1234" {:ttype :TRANSFER
                                       :to "DISCOVER"
                                       :amount 33696
                                       :notes ""
                                       :time "2015-02-21T07:20:13Z"
                                       })})

(deftest test-get-info
  (let [tp (->ChaseCheckingParser)
        info (get-info tp)]
    (is (validate-info info))
    (is (= (:bank info) "CHASE"))
    (is (= (:account-type info) :CHECKING))))

; Goes through supported types and checks that each parsed message
; matches the expected output.
(deftest test-parse-message
  (let [tp (->ChaseCheckingParser)
        parsed (reduce 
                 (fn [b a]
                   (let [f (s/lower-case (name a))
                         path (str tfpath f ".txt")
                         m (parse-message tp (slurp path))]
                    (assoc b f m)))
                 {} (:supported-ttypes (get-info tp)))
        ]
    (is (every? (fn [en]
                  (let [[k v] en]
                    (=
                     (get expected-parsed-messages k)
                     v)))
                parsed)
        (str "Expected:\n" expected-parsed-messages "\nReceived:\n" parsed)
        )))

(deftest test-parse-ofx
  (let [tp (->ChaseCheckingParser)]
    (doseq [[k v] input-ofx]
      (is (=
           (assoc (:transaction (parse-ofx-transaction tp "1234" v))
                  :notes "")
           (:transaction (get expected-parsed-messages k)))))))
