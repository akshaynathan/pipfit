(ns pipfit.parser.transaction_test
  (:require [clojure.test :refer :all]
            [pipfit.parser.transaction :refer :all]))

(deftest test-validate-transaction
  (is (false? (validate-transaction {:ttype :INVALID
                                     :from ""
                                     :to ""
                                     :amount 10
                                     }))
      "Transaction must have valid type.")
  (is (false? (validate-transaction {:ttype :WITHDRAW
                                     :amount 10
                                     }))
      "Transaction must have all required fields.")
  (is (false? (validate-transaction {:ttype :WITHDRAW
                                     :amount ""
                                     :notes ""
                                     :time ""
                                     }))
      "Fields must be of correct type.")
  (is (false? (validate-transaction {:ttype :WITHDRAW
                                     :amount 10
                                     :notes ""
                                     :time "notatimestamp"
                                     }))
      "Time field must be correct format.")
  (is (validate-transaction {:ttype :WITHDRAW
                             :amount 10
                             :notes ""
                             :time "2015-04-20T05:11:03Z"
                             })))
