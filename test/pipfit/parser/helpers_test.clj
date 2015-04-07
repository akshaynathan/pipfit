(ns pipfit.parser.helpers-test
  (:require [clojure.test :refer :all]
            [parser.helpers :refer :all]))

(deftest test_parse_money
  (is (= 1000 (parse_money "$ 1 0 . 0 0"))
      "parse_money must trim whitespace."
      )
  (is (= 1000 (parse_money "$10"))
      "parse_money must remove other characters."
      )
  (is (= 1095 (parse_money "$10 . 95"))
      "parse_money must convert to long." 
      )
  (is (= 3 (parse_money "$ 00 . 03"))
      "parse_money must work on low values with 0 full dollars." 
      )
  )

; TODO: Add tests for other methods.
