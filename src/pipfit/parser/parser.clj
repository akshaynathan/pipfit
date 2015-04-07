(ns pipfit.parser.parser)

; A parser is responsible for parsing transactions
; for a specific bank or credit card type.

; Struct to hold parser specific information.
(defrecord ParserInfo [
  ^String bank          ; The bank or institution.
  ^String account_type  ; Type of account at bank (checking / saving etc)

                        ; The following two fields are changed whenever
                        ; the parser source is changed.
  ^Integer version      ; The version of the parser, starts at 1.
  ^String last_updated  ; The last time the parser file was updated. Time
                        ; format from RFC 3339. 

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
  (get_info [this message] "Get info for the parser. If the message is not
                           yet available, m can be the empty string. Note
                           that without the message, ParserInfo may be
                           incomplete.")
  (parse_message [this message] "Parse the message m and returns a
                                ParsedMessage.")
  )

; TODO: Write validators.
