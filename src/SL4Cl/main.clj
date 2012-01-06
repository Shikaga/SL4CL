(ns SL4Cl.main
	(:import (java.net Socket)
			 (java.io PrintWriter InputStreamReader BufferedReader))
	(:use [SL4Cl.core] :reload)
	)
	(require 'clojure.string)
	
 (def liberator {:name "localvm.caplin.com" :port 50182})
 (def field-list '())

 (declare conn-handler)
	
	(defn split-by-space [string]
		(clojure.string/split string #" ")
	)
	
	(defn split-by-equals [string]
		(clojure.string/split string #"=")
	)
	
 (defn connect [server]
   (let [socket (Socket. (:name server) (:port server))
         in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
         out (PrintWriter. (.getOutputStream socket))
         conn (ref {:in in :out out})]
     (doto (Thread. #(conn-handler conn)) (.start))
     conn))

	(defn write [conn msg]
		(doto (:out @conn)
		(.println (str msg ""))
		(.flush))
	)

	(defn handle-field-list [body]
		(let [split-list (map (fn [string] (split-by-equals string)) body)]
			(def field-list (zipmap (map first split-list) (map #(first (rest %)) split-list)))
		)
	)
	
	(defstruct update-part :type :flag :value)
	
	(defn get-field-from-code "Returns the field associated with given code in the field list" [code]
		(get field-list code)
	)
 		
	(defn replace-with-field-name [update-value]
		(vector (field-list (first update-value)) (first (rest update-value)))
	)
	
	(defn user-callback-example [updates]
		(println "USER CALLBACK")
		(println updates)
	)
	
	(def update-callbacks (cons user-callback-example '()))
	
	(defn handle-record-update [body] 
		(let [update-vector (map replace-with-field-name (map split-by-equals body))]
			(doseq [x update-callbacks] (x update-vector))
		)
	)
	
	(defn discard [conn]
		(write conn (str "000000 DISCARD /FX/USDGBP"))
	)
	
	(defn request [conn]
		(write conn (str "000000 REQUEST /FX/USDGBP"))
	)
	
 (defn conn-handler [conn]
   (while 
    (nil? (:exit @conn))
    (let [	msg (.readLine (:in @conn))
			split-message (split-by-space msg)
			rttp-code (first split-message)
			message-body (rest split-message)]
	  ;(println msg)
      (println rttp-code)
      (cond 
       (re-find #"^ERROR :Closing Link:" msg) 
       (dosync (alter conn merge {:exit true}))
       (re-find #"^PING" msg)
       (write conn (str "PONG "  (re-find #":.*" msg)))
		(re-find #"0o" rttp-code)
		(handle-field-list message-body)
		(re-find #"6c" rttp-code)
		(handle-record-update message-body)
	   ))))

 (defn login [conn]
	(write conn (str "00000 LOGIN 000000 SL4J 0 admin admin"))
   )

 (def irc (connect liberator))
 (login irc)
 ;;(write irc "JOIN #clojure")
 ;;(write irc "QUIT")

(defn -main [& args]
	(print 1))