(ns pipfit.parser.transaction_test
  (:require [clojure.test :refer :all]
            [pipfit.parser.transaction :refer :all]))

(deftest test_validate_transaction
  ; Invalid type
  (is (false? (first 
                (validate_transaction
                  (hash-map
                    :ttype :INVALID,
                    :from "",
                    :to "",
                    :amount 10,
                    ))))
      "Transactions must have valid type."
      )
  ; Missing field
  (is (false? (first 
                (validate_transaction
                  (hash-map
                    :ttype :WITHDRAW,
                    :amount 10,
                    ))))
      "Transaction must have all required fields for type."
      )
  ; Incorrect field type
  (is (false? (first 
                (validate_transaction
                  (hash-map
                    :ttype :WITHDRAW,
                    :notes "",
                    :amount "",
                    ))))
      "Transaction fields must be of correct type."
      )
  (is (first 
                (validate_transaction
                  (hash-map
                    :ttype :WITHDRAW,
                    :notes "",
                    :amount 5,
                    :time "",
                    )))
      "validate_transaction must return true on correct transaction."
      )
  )

