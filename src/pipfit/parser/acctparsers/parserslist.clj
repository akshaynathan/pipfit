(ns pipfit.parser.acctparsers.parserslist
  (:require [pipfit.parser.acctparsers.chasechecking
             :refer [->ChaseCheckingParser]]
    )
  )

; This file holds a vector of all parsers. When implementing
; an additional parser, add the constructor to parsers below and
; require the implementation ns above.

(def parsers
  [
   ->ChaseCheckingParser
   ]
  )
