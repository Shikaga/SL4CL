(ns SL4Cl.connection
	(:import (java.net Socket)
	(java.io PrintWriter InputStreamReader BufferedReader))
)
	(require 'clojure.string)

(def liberator {:name "localvm.caplin.com" :port 50182})
 
(declare conn-handler)
  	
(defn split-by-space [string]
	(clojure.string/split string #" ")
)
	
(defn split-by-equals [string]
	(clojure.string/split string #"=")
)

(defn connect [server output]
   (let [socket (Socket. (:name server) (:port server))
         in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
         out (PrintWriter. (.getOutputStream socket))
         conn (ref {:in in :out out})]
     (doto (Thread. #(conn-handler conn output)) (.start))
     conn))

(defn write [conn msg]
	(doto (:out @conn)
	(.println (str msg ""))
	(.flush))
)
	
(defn conn-handler [conn callback]
	(while 
		(nil? (:exit @conn))
		(callback (.readLine (:in @conn)))  
	)
)