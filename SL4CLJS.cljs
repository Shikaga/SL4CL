(ns hello
  (:require
    [clojure.string :as string]
  )
)

(def session-id nil)

(defn ^:export greet [n]
  (apply str (interpose " " (range 1 n))))

(defn ^:export log [message]
  (.log js/console message)
)

(defn ^:export read [message]
  (log message)
  (log (string/split message #" "))
  (log (split-by-space message))
  (rttp-handler message)
)

(defn ^:export write [message]
  (.send js/Socket message)
)

(defn ^:export login []
  (write (str session-id " LOGIN 000000 SL4J 0 admin admin"))
)

(defn split-by-space [string]
  (string/split string #" ")
)

(defn handle-connection-message [rttp-code message-body]
  (log rttp-code)
  (log (first message-body))
  (def session-id (first message-body))
)

(defn rttp-handler [msg]
	(let [	split-message (split-by-space msg)
			rttp-code (first split-message)
			message-body (rest split-message)]
	 ; (log msg)
      (log rttp-code)
      (cond 
		(re-find #"01" rttp-code)
		(handle-connection-message rttp-code message-body)
;		(re-find #"0o" rttp-code)
;		(handle-field-list message-body)
;		(re-find #"38" rttp-code)
;		(handle-record-unknown rttp-code message-body)
;		(re-find #"3U" rttp-code)
;		(handle-record-response rttp-code message-body)
;		(re-find #"6V" rttp-code)
;		(handle-record-image rttp-code message-body)
;		(re-find #"6c" rttp-code)
;		(handle-record-update rttp-code message-body)
;		(re-find #"8c" rttp-code)
;		(handle-container-not-found rttp-code message-body)
;		(re-find #"7U" rttp-code)
;		(handle-container-image rttp-code message-body)
;		(re-find #"3a" rttp-code)
;		(handle-container-response rttp-code message-body)
	   )
	)
) 
