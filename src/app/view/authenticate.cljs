(ns app.view.authenticate
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   ["blockstack" :as blockstack
    :refer [UserSession]]
   ["../../gen/App" :default App :as app]
   ["react-router-dom"
    :refer [BrowserRouter]]
   [taoensso.timbre :as timbre]
   [app.events :as events]))

; (timbre/debug "Load authenticate:" app/default)

(def authenticate (reagent/adapt-react-class app/default))

(defn dispatch [name event]
  (timbre/debug "Dispatch:" name event)
  (case name
    "UserSession" (rf/dispatch [:user-session event])
    (timbre/warn "Unknown dispatch:" name)))

(defn auth [& content]
  "HOC with conditional content only shown after login"
  (timbre/debug "Render authenticate" authenticate)
  [:>  BrowserRouter
   (into [authenticate {:dispatch dispatch}]
     (map reagent/as-element content))])
