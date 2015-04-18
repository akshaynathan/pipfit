(ns pipfit.parser.dispatcher
  :require [pipfit.parser.acctparsers.parserslist :refer :all]
  )

; The dispatch_parser method chooses and calls the correct
; parser on an email message and returns a parser.parser.ParsedMessage
; record.


