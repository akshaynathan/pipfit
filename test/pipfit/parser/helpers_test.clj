(ns pipfit.parser.helpers_test
  (:require [clojure.test :refer :all]
            [pipfit.parser.helpers :refer :all]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            ))

(deftest test-date->timestamp
  (let [t (t/date-time 1986 10 14 4 3 27 456)
        jt (c/to-date t)
        ts (f/unparse (f/formatters :date-time-no-ms) t)
        ]
    (is (= ts (date->timestamp jt)))))

(deftest test-parse-money
  (is (= 1000 (parse-money "$ 1 0 . 0 0"))
      "parse-money must trim whitespace.")
  (is (= 1000 (parse-money "$10"))
      "parse-money must remove other characters.")
  (is (= 1095 (parse-money "$10 . 95"))
      "parse-money must convert to long." )
  (is (= 3 (parse-money "$ 00 . 03"))
      "parse-money must work on low values with 0 full dollars."))

(deftest test-validate-timestamp
  (is (validate-timestamp "2015-04-20T05:11:03Z"))
  (is (false? (validate-timestamp  "notatimestamp"))))

(deftest test-text-after
  (is (= (text-after "hello world" #"o ") "world"))
  )

(deftest test-text-before
  (is (= (text-before "hello world" #" w") "hello"))
  )

(deftest test-next-word
  (is (= (next-word " hello world") "hello"))
  )
