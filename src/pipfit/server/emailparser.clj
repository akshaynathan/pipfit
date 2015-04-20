(ns pipfit.server.emailparser
  (:require [clojure.tools.logging :as log]
            [clojure.string :as s]
            [pipfit.parser.helpers :refer :all])
  (:import (javax.mail Message Session)
           (javax.mail.internet MimeMessage
                                MimeMultipart
                                InternetAddress)
           (java.util Properties)))

; Wrapper around javax.mail to provide email parsing functionality.

(defn- original-sender
  "Returns the original sender of a forwarded message or nil if the
  message was not forwarded."
  [body]
  (if (re-find #"(?i)forwarded" body)
    (let [[_ m] (last (re-seq 
                        #"(?i)(?:From:\s*)(.*>)(?:\n)"
                        body))]
      m)
    nil))

(defn- remove-forward-meta
  "Removes forward metadata from the text body."
  [body]
  (let [re #"(?i)forwarded|from\s*:|to\s*:|subject\s*:|date\s*:"]
    (s/trim (s/join "\n" (remove 
                           #(re-find re %)
                           (s/split-lines body)))))) 

(defmulti find-text-type
  "Recursively searches the parts for the text/plain body."
  class)

(defn- find-text-type
  "Recursively searches the parts for the text/plain body."
  [body]
  (loop [parts (- (.getCount body) 1)]
    (let [p (.getBodyPart body parts)
          t (.getContentType p)]
      (if (< parts 0)
        nil
        (if (re-find #"multipart" t)
          (find-text-type (.getContent p))
          (if (re-find #"plain" t)
            (.getContent p)
            (recur (- parts 1))))))))


(defn parse-email [email]
  "Parses an email message into a map of the following structure.
  {:to => list of receivers (strings)
   :from => direct sender (string)
   :subject => (string)
   :date => date message was sent (see parser/helpers.date->timestamp)
   :body => the text/plain body of the message or nil.
   :sender => the original sender of the message, if forwarded, or nil.
  }"
  (try
    (let [msg
          (MimeMessage. (Session/getDefaultInstance
                          (Properties.)) email)
          to (map str (.getRecipients
                        msg 
                        javax.mail.Message$RecipientType/TO))
          from (str (.getSender msg))
          subject (.getSubject msg)
          date (date->timestamp (.getSentDate msg))
          content-type (.getContentType msg)
          content (.getContent msg)
          ; We want to read only the text/plain content types.
          body (if (re-find #"multipart" content-type)
                 (find-text-type content) 
                 (if (re-find #"plain" content-type)
                   content
                   ""))
          sender (original-sender body)]
      {:to to
       :from from
       :subject subject
       :date date
       :body (remove-forward-meta body)
       :sender sender})
  (catch Exception e (log/error e "Unable to parse message."))))
