(ns app.core
  (:require
   ["react" :as react] ;; required for mui...
   [reagent.core :as r]
   [mount.core :as mount]
   [app.view.core :as view]
   [app.lib.blockstack
    :refer [init-blockstack]]
   [app.datatransfer]
   [app.routing]
   [app.lib.drop]
   [app.events]
   [app.store]))

(defn mount-root []
  (r/render-component [view/app]
                      (.getElementById js/document "app")))

(defn ^:dev/before-load stop
  []
  (mount/stop))

(defn ^:dev/after-load start
  []
  (mount-root)
  (mount/start)
  (init-blockstack))

(defn ^:export main
  []
  (start))
