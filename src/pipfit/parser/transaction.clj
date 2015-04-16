(ns pipfit.parser.transaction)

; We use a map to represent transactions.
; To add a new transaction type, add the type keyword to ttypes,
; and add any new fields to tfields along with a predicate function
; to validate them. In tfieldmapping, you must define which fields the
; transaction type contains.

; Set of transaction types.
(def ttypes #{:WITHDRAW :DEPOSIT :TRANSFER :PAYMENT})

; Map between fields and validation predicate
(def tfields
  (hash-map
    :from string?,
    :to string?,
    :amount #(and (integer? %) (pos? %)),
    :notes string?,
    :time string?, ; Timestamps in format from RFC 3339
    ))
     
; Mapping between transaction type and fields
(def tfieldmapping
  (hash-map
    :WITHDRAW #{:amount :notes :time},
    :DEPOSIT #{:from :amount :notes :time},
    :TRANSFER #{:from :to :amount :notes :time},
    :PAYMENT #{:to :amount :notes :time}
    ))

; TODO: Add validation for time field.
; Validate a map t as a transaction.
; Returns a list of just true if the transaction is valid or
; false and an error message.
(defn validate_transaction [t]
  "Validates a map as a transaction."
  (if (contains? ttypes (:ttype t))
    (loop [[f1 & more] (seq ((:ttype t) tfieldmapping))]
        (if (nil? f1) 
          (list true)
          (if-not ((f1 tfields) (f1 t))
            (list false (str "Invalid type or missing field " (name f1) "."))
            (recur more)
          )
        )
      )
    (list false "Transaction must contain valid type field.")
    ))
