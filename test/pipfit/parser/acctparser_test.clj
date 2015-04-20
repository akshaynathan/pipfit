(ns pipfit.parser.acctparser_test
  (:require [clojure.test :refer :all]
            [pipfit.parser.accountparser :refer :all]))

(deftest test-validate-info
  (is (not (validate-info (->AccountParserInfo "" :INVALID 1 #{:WITHDRAW}))))
  (is (not (validate-info (->AccountParserInfo "" :CHECKING 1 #{:INVALID}))))
  (is (validate-info (->AccountParserInfo "" :CHECKING 1 #{:WITHDRAW}))))

(deftest test-validate-parsed-message
  (is (not (validate-parsed-message (->ParsedMessage
                                      1
                                      #{:ttype :BALANCE
                                        :amount 1
                                        :notes ""
                                        :time "2013-03-05T12:00:00Z"
                                        }))))
  (is (not (validate-parsed-message (->ParsedMessage
                                      "1"
                                      #{:ttype :INVALID
                                        :amount 1
                                        :notes ""
                                        })))))
