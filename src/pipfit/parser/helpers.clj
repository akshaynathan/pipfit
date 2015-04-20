(ns pipfit.parser.helpers
  (:require [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clojure.string :as s]
            [clojure.tools.logging :as log]
            ))

(defn date->timestamp
  "Parses a java.util.Date into the standard rfc3339 timestamp."
  [date]
  (f/unparse (f/formatters :date-time-no-ms)
             (c/from-date date)))

(defn parse-money
  "Parses string representing currency to long. Assumes value is in dollars."
  [mstr]
  (.longValue (* 100 (BigDecimal. 
                        (s/trim 
                          (s/replace mstr #"[^0-9^\.]" ""))))))

(defn validate-timestamp
  "Validates a timestamp."
  [ts]
  (try (do (f/parse (f/formatters :date-time-no-ms) ts) true)
       (catch Exception e (do (log/error e) false))))

(defn text-after [text re]
  "Returns text after the first instance of a regular expression re."
  (last (s/split text re 2))
  )

(defn text-before [text re]
  "Returns text after the first instance of a regular expression re."
  (first (s/split text re 2))
  )

(defn next-word [text]
  "Returns next word with whitespace removed."
  (first (s/split (s/trim text) #"\s" 2))
  )
