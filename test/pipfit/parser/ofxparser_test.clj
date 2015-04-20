(ns pipfit.parser.ofxparser_test
  (:require [clojure.test :refer :all]
            [pipfit.parser.ofxparser :refer :all])
   (:import [java.io FileInputStream File]
            [java.util Date]
            [net.sf.ofx4j.io AggregateUnmarshaller]
            [net.sf.ofx4j.domain.data ResponseEnvelope]))

(def testpath "test/pipfit/parser/ofxtest.txt")

(def expected {:account {:account-number "4388576084151573"}
               :available-balance
               {:amount 800000
                :as-of-date "2015-04-20T12:00:00Z"}
               :ledger-balance
               {:amount 20000,
                :as-of-date "2015-04-20T12:00:00Z"}
               :currency "USD"
               :transactions (list
                              {:amount 100
                               :date "2015-03-27T12:00:00Z"
                               :id "2015032724246515085286099801817"
                               :notes nil
                               :name "POPEYES"
                               :type "DEBIT"}
                              {:amount 200
                               :date "2015-04-14T12:00:00Z"
                               :id "2015041424046035103000232490936"
                               :notes nil
                               :name "CHEVRON"
                               :type "DEBIT"}
                              {:amount 1600
                               :date "2015-04-01T12:00:00Z"
                               :id "2015040124431055091207000000046"
                               :notes nil
                               :name "BESTBUY"
                               :type "DEBIT"}
                              {:amount 70000
                               :date "2015-04-14T12:00:00Z"
                               :id "2015041421041041600021910619742"
                               :notes nil
                               :name "PAYMENT"
                               :type "CREDIT"})}) 

(deftest test-ofx->map
  (let [m (ofx->map (.unmarshal (AggregateUnmarshaller. ResponseEnvelope)
                            (FileInputStream. (File. testpath))))] 
    (is (= m (list (list expected))))))
