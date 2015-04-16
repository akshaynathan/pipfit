(ns pipfit.parser.mailparser)

; A parser is responsible for parsing transactions
; for a specific bank or credit card type.

; Struct to hold parser specific information.
(defrecord ParserInfo [
  ^String bank          ; The bank or institution.
  ^String account_type  ; Type of account at bank (checking / saving etc)

                        ; The following field changed whenever the
                        ; parser source is changed.
  ^Integer version      ; The version of the parser, starts at 1.

  supported_ttypes      ; Set of keywords from parser.transaction/ttypes
                        ; which this parser can export.
  ])

; Holds parsed output.
(defrecord ParsedMessage [
  ^String acct_id       ; Account identifier from the message. Typically
                        ; this is the last 4 digits of the acct/card no.
  parsed_t              ; Hashed Transaction. See parser.transaction.    
  ])

; Parsers can parse messages (plain text, xml, etc).
(defprotocol Parser
  "A parser which generates ParsedMessage from a message."
  (get_info [this] "Get info for the parser.")
  (parse_message [this message] "Parse the message m and returns a
                                ParsedMessage.")
  )

; TODO: Write validators.
