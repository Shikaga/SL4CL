(ns SL4Cl.rttphandler
	(:use [SL4Cl.connection] :reload)
)

(def field-list '())
(def subject-list '{})
(def connection)
(def update-callbacks ())
(defstruct record-update :name :fields)

(defn handle-field-list [body]
	(let [split-list (map (fn [string] (split-by-equals string)) body)]
		(def field-list (zipmap (map first split-list) (map #(first (rest %)) split-list)))
	)
)

(defn add-callback [callback]
	(def update-callbacks (cons callback update-callbacks))
)
 		
(defn replace-with-field-name [update-value]
	(vector (field-list (first update-value)) (first (rest update-value)))
)
 
(defn create-update-message [head body]
	(let [name "DEMO_NAME"
		fields (map replace-with-field-name (map split-by-equals body))]
		(println (str "HEAD: " head))
		(struct-map record-update :name name :fields fields)
	)
)

(defn handle-record-update [head body] 
	(let [
		update-vector (create-update-message head body)]
		(doseq [x update-callbacks] (x update-vector))
	)
) 

(defn handle-record-image [head body]
	(println head body)
)

(defn handle-record-unknown [head body]
	(let [object-number (first (rest (clojure.string/split head #"38")))
		subject-name (first body)]
		(def subject-list (assoc subject-list object-number subject-name))
	)
)

(defn handle-container-not-found [head body]
	(let [sequence-number (apply str (take 2 (drop 2 head)))
		object-number (apply str (take 4 (drop 4 head)))]
		;(def subject-list (assoc subject-list object-number subject-name))
		(println "object number: " object-number " sequence number: " sequence-number)
	)
)

(defn rttp-handler [msg]
	(let [	split-message (split-by-space msg)
			rttp-code (first split-message)
			message-body (rest split-message)]
	  ;(println msg)
      (println rttp-code)
      (cond 
		(re-find #"0o" rttp-code)
		(handle-field-list message-body)
		(re-find #"38" rttp-code)
		(handle-record-unknown rttp-code message-body)
		(re-find #"6V" rttp-code)
		(handle-record-image rttp-code message-body)
		(re-find #"6c" rttp-code)
		(handle-record-update rttp-code message-body)
		(re-find #"8c" rttp-code)
		(handle-container-not-found rttp-code message-body)
	   )
	)
) 

(defn connect-to [server]
	(def connection (connect server rttp-handler))
)

(defn discard [subject-name]
	(write connection (str "000000 DISCARD " subject-name))
)

(defn discardX []
	(discard "/FX/USDGBP")
)

(defn request [subject-name]
	(write connection (str "000000 REQUEST " subject-name))
)


(defn requestCX []
	(request "/FX/MAJOR")
)

(defn login []
	(write connection (str "00000 LOGIN 000000 SL4J 0 admin admin"))
)
