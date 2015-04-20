(ns pipfit.server.emailparser_test
  (:require [clojure.test :refer :all]
            [pipfit.server.emailparser :refer :all])
  (:import [java.io File FileInputStream]))

(def testpath "test/pipfit/server/testemail.txt")

(def expected  {:to '("akshay.nathan@yale.edu")
                :from ""
                :date "2015-04-20T11:03:34Z"
                :body "THIS IS A TEST MESSAGE"
                :subject "Fwd: TEST"
                :sender "Akshay Nathan <akshay.nathan@yale.edu>"
                })

(deftest test-parse-email
  (let [f (FileInputStream. (File. testpath))
        p (parse-email f)]
    (is (= expected p))))
