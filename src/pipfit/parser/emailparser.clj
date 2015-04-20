(ns pipfit.parser.emailparser
  (:require [clojure.tools.logging :as log]
            [clojure.string :as s]
            )
  (:import (javax.mail Message Session)
           (javax.mail.internet MimeMessage
                                MimeMultipart
                                InternetAddress)
           (java.util Properties)
           )
  )

; This class provides the utility function parse_email
; which parses an email message into a map of the following structure
; {:to => list of strings of receievers
;  :from
;  :subject
;  :date => The date the message was sent.
;  :body => The text/plain body of the message if multipart.
;  :osender => The original sender if this is a forwarded message.
; }
; Uses Javax.mail library.

(defn- original_sender [content]
  "Finds the original sender of a forwarded message.
   Returns nil if there is no original sender."
  (if (re-find #"(?i)forwarded" content)
    (let [[_ m] (re-find #"(?i)(?:From:\s*)(.*>)(?:\n)" content)]
      m)
    nil))

(defn- remove_forward_meta [content]
  (let [meta_re #"(?i)forwarded|from\s*:|to\s*:|subject\s*:|date\s*:"]
    (s/trim (s/join "\n" (remove #(re-find meta_re %) (s/split-lines content))))
    )
  )

(defn- find_text_part [content]
  "Recursively searches message body for text/plain
  part and returns it."
  (loop [parts (- (.getCount content) 1)]
    (let [part (.getBodyPart content parts)
          part_type (.getContentType part)]
      (if (< parts 0)
        ""
        (if (re-find #"multipart" part_type)
          (find_text_part (.getContent part))
          (if (re-find #"plain" part_type)
            ; We found it!
            (.getContent part)
            (recur (- parts 1))))))))

(defn parse_email [email]
  "Parses an email into a map. If an error occurs, prints
   the error message and returns nil."
  (try
    (let [msg
          (MimeMessage. (Session/getDefaultInstance
                          (Properties.)) email)
          to (map str (.getRecipients msg 
                                      javax.mail.Message$RecipientType/TO))
          from (str (.getSender msg))
          subject (.getSubject msg)
          date (.getSentDate msg)
          content_type (.getContentType msg)
          content (.getContent msg)
          ; We want to read only the text/plain content types.
          body (if (re-find #"multipart" content_type)
                 (find_text_part content) 
                 (if (re-find #"plain" content_type)
                   content
                   ""
                   ))
          osender (original_sender body)
          ]
      {:to to
       :from from
       :subject subject
       :date date
       :body  (remove_forward_meta body)
       :osender osender
       }
      )
  (catch Exception e (log/error e "Unable to parse message."))))
