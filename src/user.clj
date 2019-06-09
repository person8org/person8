(ns user
  (:require
   [taoensso.timbre :as timbre]
   [shadow.cljs.devtools.server :as server]
   [shadow.cljs.devtools.api :as shadow]))

(defn http-handle [req]
  (timbre/info "->" req)
  (shadow.http.push-state/handle req))

(defn reset []
  (println "Reset Proto REPL")
  (server/start!)
  (if true
    (shadow/watch :app)
    (println "Activate hotloading with:\n (shadow/watch :app)"))
  (if true
    (shadow/nrepl-select :app)
    (println "Select a repl with:\n (shadow/nrepl-select :app)")))

(println "Custom Proto REPL Leiningen project started")
