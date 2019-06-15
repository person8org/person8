(ns app.view.dev
  (:require
   [taoensso.timbre :as timbre]
   ["@material-ui/core" :as mui]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [app.events]))


"Experimental features and development tools"

(defn basic-card [{:keys [title]} & body]
  [:> mui/Card
   [:> mui/CardHeader {:title title}]
   [:> mui/CardContent (into [:<>] body)]])


(defn data-table [data]
  (if (coll? data)
    [:> mui/Table {:padding "dense"}
     [:> mui/TableBody
      (for [[k v] data]
        ^{:key k}
        [:> mui/TableRow
         [:> mui/TableCell (str k)]
         [:> mui/TableCell (pr-str v)]])]]
    [:pre (pr-str data)]))


(defn user-profile-card [{:keys [user-data]}]
  [:> mui/Card
   [:> mui/CardHeader {:title "User Profile"}]
   [:> mui/CardContent
    [data-table (js->clj @user-data)]]])


(def db (rf/subscribe [:db]))

(defn state-inspector []
  [basic-card {:title "State"}
   [data-table @db]])
