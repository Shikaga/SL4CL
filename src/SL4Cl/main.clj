(ns SL4Cl.main
	(:import (java.net Socket)
			 (java.io PrintWriter InputStreamReader BufferedReader))
	(:use [SL4Cl.core] :reload)
	(:use [SL4Cl.connection] :reload)
	(:use [SL4Cl.rttphandler] :reload)
	)
	(require 'clojure.string)
				
	(defn user-callback-example [updates]
		(println "USER CALLBACK")
		(println updates)
	)
	
	(add-callback user-callback-example)

(connect-to liberator)
(login)

(defn -main [& args]
	(print 1))