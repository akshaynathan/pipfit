(ns pipfit.parser.helpers)

(defn parse_money [mstr]
  "Parses string representing currency to long. Assumes value is in dollars."
  (.longValue (* 100 (BigDecimal. 
                        (clojure.string/trim 
                          (clojure.string/replace mstr #"[^0-9^\.]" ""))))))

(defn text_after [text re]
  "Returns text after the first instance of a regular expression re."
  (last (clojure.string/split text re 2))
  )

(defn text_before [text re]
  "Returns text after the first instance of a regular expression re."
  (first (clojure.string/split text re 2))
  )

(defn next_word [text]
  "Returns next word with whitespace removed."
  (first (clojure.string/split text #"\s" 2))
  )
