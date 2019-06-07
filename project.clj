(defproject shadow-reagent-proto-repl "0.1.0-SNAPSHOT"
  :description "A leiningen project to use proto repl with shadow-cljs."
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "0.4.490"]
                 [thheller/shadow-cljs "2.8.39"]
                 ; [binaryage/devtools "0.9.10"]
                 [proto-repl-charts "0.3.1"]
                 [proto-repl "0.3.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.6"]
                 [mount "0.1.16"]
                 [cljs-http "0.1.46"]
                 [cljs-drag-n-drop "0.1.0"]]

  :source-paths ["src"]

  :repl-options {:nrepl-middleware
                 [shadow.cljs.devtools.server.nrepl/cljs-load-file
                  shadow.cljs.devtools.server.nrepl/cljs-eval
                  shadow.cljs.devtools.server.nrepl/cljs-select
                  #_cemerick.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev {:source-paths ["dev" "src"]}})
