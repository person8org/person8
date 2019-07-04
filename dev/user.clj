(ns user
  (:require
   ;[nrepl.server] ; ad-hoc
   ;[shadow.cljs.devtools.server.fake-piggieback04]
   [shadow.cljs.devtools.server :as server]
   [shadow.cljs.devtools.api :as shadow]))

(defn http-handle [req]
  ; (timbre/info "->" req)
  (shadow.http.push-state/handle req))

(defn reset []
  (println "Reset Proto REPL")
  (server/start!)
  ; (shadow/compile :app)
  (if true
    (shadow/watch :app)
    (println "Activate hotloading with:\n (shadow/watch :app)"))
  (if false
    (shadow/nrepl-select :app)
    (println "Select a repl with:\n (shadow/nrepl-select :app)")))

(println "Custom Proto REPL Leiningen project started")
