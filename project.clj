(defproject shadow-reagent-proto-repl "0.1.0-SNAPSHOT"
  :description "A leiningen project to use proto repl with shadow-cljs."
  :managed-dependencies [[reagent "0.9.0-SNAPSHOT"]
                         [re-frame "0.10.6"]]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 ; [org.clojure/clojurescript "1.10.520"]
                 [org.clojure/core.async "0.4.490"]
                 [thheller/shadow-cljs "2.8.52"]
                 ; [binaryage/devtools "0.9.10"]
                 [com.taoensso/timbre "4.10.0"]
                 [reagent]
                 [re-frame]
                 [metosin/reitit "0.3.9"]
                 [mount "0.1.16"]
                 ; [buddy/buddy-core "1.6.0"]
                 [cljs-http "0.1.46"]
                 [cljs-drag-n-drop "0.1.0"]]

  :min-lein-version "2.8.3"

  :source-paths ["src"]

  :repl-options {:nrepl-middleware
                 ;; 04 to ensure compatiblity with [nrepl 4+]
                 [;shadow.cljs.devtools.server.nrepl04/shadow-init ;; loads shadow-cljs.edn
                  shadow.cljs.devtools.server.nrepl04/cljs-load-file
                  shadow.cljs.devtools.server.nrepl04/cljs-eval
                  shadow.cljs.devtools.server.nrepl04/cljs-select
                  #_cemerick.piggieback/wrap-cljs-repl
                  #_cider.piggieback/wrap-cljs-repl]}

  :profiles
  {:repl
   {:dependencies []}
   :dev
   {:source-paths ["dev" "src"]
    :dependencies [[proto-repl "0.3.1"]
                   ;; nrepl has been upgraded to new coordinates, used by shadow-cljs:
                   [nrepl "0.6.0"]]}})
