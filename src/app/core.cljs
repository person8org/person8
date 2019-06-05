(ns app.core
  (:require
   [reagent.core :as r]
   [mount.core :as mount]
   ["../gen/App" :as App]
   [app.view.core :as view]
   [app.datatransfer]
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
  (mount/start))

(defn ^:export main
  []
  (start))
