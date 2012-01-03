(ns SL4Cl.main
	(:import (java.net Socket)
			 (java.io PrintWriter InputStreamReader BufferedReader))
	(:use [SL4Cl.core] :reload))
	
 (def liberator {:name "localvm.caplin.com" :port 50182})

 (declare conn-handler)

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
     (.flush)))

 (defn conn-handler [conn]
   (while 
    (nil? (:exit @conn))
    (let [	msg (.readLine (:in @conn))
			rttp-code (first (re-seq #"\w+" msg))]
      (println rttp-code)
      (cond 
       (re-find #"^ERROR :Closing Link:" msg) 
       (dosync (alter conn merge {:exit true}))
       (re-find #"^PING" msg)
       (write conn (str "PONG "  (re-find #":.*" msg)))
	   (re-find #"1b" rttp-code)
	   (println (str "FIELD LIST"))
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