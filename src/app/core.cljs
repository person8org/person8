(ns app.core
  (:require
   [reagent.core :as r]
   [mount.core :as mount]
   [app.lib.blockstack]
   [app.view.core :as view]
   [app.lib.blockstack
    :refer [init-blockstack]]
   [app.datatransfer]
   [app.drop]
   [app.events]))

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
