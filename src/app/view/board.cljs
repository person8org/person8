(ns app.view.board
  (:require
    [taoensso.timbre :as timbre]
    ["@material-ui/core" :as mui]
    ["@material-ui/icons/FileCopy" :default CopyIcon]
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

(defmethod pane :default [{:keys [mobile profile] :as session}]
  [:div {:style {:padding-top "60px"}}
   (if (= (get-in @mobile [:user :status]) "SUCCESS")
     (into
      [:div [identity-card session]]
      (for [item (:fields @profile)]
        [profile-card item]))
    [:div])])

(defn board-listing [items]
  (into
   [:> mui/List
    [invoice-view/funding-request-card]]
   (for [{:keys [selected] :as item} items]
     [:> mui/ListItem
      {:selected (boolean selected)}
      [pane/view {:item item}]])))

(defn board-pane [items]
  (timbre/debug "Board Pane:" items)
  [board-listing items])
