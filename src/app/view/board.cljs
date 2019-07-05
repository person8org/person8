(ns app.view.board
  (:require
    [taoensso.timbre :as timbre]
    ["@material-ui/core" :as mui]
    ["@material-ui/icons/FileCopy" :default CopyIcon]
    ["@material-ui/core/Grid" :default Grid]
    ["@material-ui/core/Hidden" :default Hidden]
    [re-frame.core :as rf]
    [reagent.core :as reagent]
    [app.view.invoice :as invoice-view]
    [app.view.identity
     :refer [identity-card]]
    [app.view.pane :as pane
     :refer [profile-card]]))

(def debug (rf/subscribe [:debug]))

(defmulti pane (fn [{:keys [stage] :as session}]
                  (if stage [@stage])))

(defn board-listing [{:keys [items]}]
   (into
    [:> mui/List
     [invoice-view/funding-request-card]]
    (for [{:keys [selected] :as item} items]
       [:> mui/ListItem
        {:selected (boolean selected)}
        [pane/view {:item item}]])))

(defn board-grid [{:keys [items]}]
  (into
   [:> Grid {:container true :spacing 8}
     [:> Grid {:item true :xs 12}
       [invoice-view/funding-request-card]]
    (for [{:keys [selected] :as item} items]
      [:> Grid {:item true :xs 12 :sm 6 :md 4}
        #_{:selected (boolean selected)}
        [pane/view {:item item}]])]))

(defn board-pane [items]
  (timbre/debug "Board Pane:" items)
  [:<>
   [:> mui/Hidden {:sm-up true}
    [board-listing {:items items}]]
   [:> mui/Hidden {:xs-down true}
    [board-grid {:items items}]]])

(defmethod pane :default [{:keys [mobile profile] :as session}]
  [board-pane (:fields @profile)])
