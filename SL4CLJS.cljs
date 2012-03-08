(ns sl4cljs
  (:require
    [clojure.string :as string]
  )
)

(def session-id nil)
(def field-list '())
(def subject-list '{})
;(defstruct record-update :subject-name :fields)

(defn handle-field-list [body]
	(let [split-list (map (fn [string] (split-by-equals string)) body)]
		(def field-list (zipmap (map first split-list) (map #(first (rest %)) split-list)))
	)
)

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

(defn split-by-equals [string]
  (string/split string #"=")
)
(defn split-by-space [string]
  (string/split string #" ")
)
(defn split-by-colon [string]
  (string/split string #":")
)

(defn handle-connection-message [rttp-code message-body]
  (log rttp-code)
  (log (first message-body))
  (def session-id (first message-body))
  (login)
)

(defn replace-with-field-name [update-value]
(vector (field-list (first update-value)) (first (rest update-value)))
)
 
(defn create-update-message [head body]
	(let [
		sequence-number (apply str (take 2 (drop 2 head)))
		object-number (apply str (take 4 (drop 4 head)))
		subject-name (get subject-list object-number)
		name subject-name
		fields (map replace-with-field-name (map split-by-equals body))
	]
		{:subject-name name :fields fields}
	)
)
(defn handle-record-unknown [head body]
	(let [object-number (first (rest (clojure.string/split head #"38")))
		subject-name (first body)]
		(def subject-list (assoc subject-list object-number subject-name))
	)
)

(defn handle-record-response [rttp-code message-body]
	(let [	object-number (apply str (take 4 (drop 2 rttp-code)))
		subject-name (first message-body)
		fields (rest message-body)]
		(def subject-list (assoc subject-list object-number subject-name))
	)
)

(defn handle-record-update [head body] 
	(let [
		update (create-update-message head body)]
		(user-callback-example update)
		;(doseq [callback update-callbacks]
		;	(if (= (get callback :subject-name) (get update :subject-name))
		;		((get callback :callback) update)
		;	)
		;)
	)
)

(defn handle-container-record [record-string]
	(let [	split-message (split-by-semicolon record-string)
		subject-name (first split-message)
		response-code (first (drop 1 split-message))
		object-number (first (drop 2 split-message))]
		(log (str subject-name response-code object-number))
		(if (not (= nil object-number)) 
			(def subject-list (assoc subject-list object-number subject-name))
		)
	)
)

(defn handle-container-response [head body]
	(log (str "CONTAINER RESPONSE " head body))
	(let [  object-number (apply str (take 4 (drop 2 head)))]
		;(def subject-list (assoc subject-list object-number subject-name))
		(log (str "object number: " object-number " sequence number: " "NONE"))
		(doseq [object body] (handle-container-record object))
	)
)

(defn rttp-handler [msg]
	(let [	split-message (split-by-space msg)
			rttp-code (first split-message)
			message-body (rest split-message)]
	 ; (log msg)
      (log rttp-code)
      (cond 
		(re-find #"^01" rttp-code)
		(handle-connection-message rttp-code message-body)
		(re-find #"^0o" rttp-code)
		(handle-field-list message-body)
		(re-find #"^38" rttp-code)
		(handle-record-unknown rttp-code message-body)
		(re-find #"^3U" rttp-code)
		(handle-record-response rttp-code message-body)
;		(re-find #"^6V" rttp-code)
;		(handle-record-image rttp-code message-body)
		(re-find #"^6c" rttp-code)
		(handle-record-update rttp-code message-body)
;		(re-find #"^8c" rttp-code)
;		(handle-container-not-found rttp-code message-body)
;		(re-find #"^7U" rttp-code)
;		(handle-container-image rttp-code message-body)
		(re-find #"^3a" rttp-code)
		(handle-container-response rttp-code message-body)
	   )
	)
)

(defn request [subject-name callback]
	(write (str session-id " REQUEST " subject-name))
	;(add-callback subject-name callback)
)

(defn discard [subject-name]
	(let [message (str session-id " DISCARD " subject-name)]
		(log message)
		(write message)
	)
)

(defn ^:export requestCX [] 
  (request "/CONTAINER/FX/Major" "NONE")
)

(defn ^:export discardCX [] 
  (discard "/CONTAINER/FX/Major")
)

(defn ^:export requestX [] 
  (request "/FX/USDGBP" "NONE")
)

(defn ^:export discardX [] 
  (discard "/FX/USDGBP")
)
				
(defn user-callback-example [updates]
	(log "USER CALLBACK")
	(loop [update-array (updates :fields)]
	  (if (empty? update-array)
	    '()
	    (do
	      (js/updateObjectField (updates :subject-name) (first (first update-array)) (first (rest (first update-array))))
	      ;(log (str (first (first update-array)) " " (first (rest (first update-array)))))
	      (recur (rest update-array))
	    )
	  )
)
