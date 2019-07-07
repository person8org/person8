(ns app.view.board
  (:require
    [taoensso.timbre :as timbre]
    ["@material-ui/core" :as mui]
    ["@material-ui/core/styles" :as styles
     :refer [makeStyles]]
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
  (timbre/debug "Show as list")
  (into
    [:> mui/List]
    (for [{:keys [id selected] :as item} items]
       ^{:key id}
       [:> mui/ListItem
        {:selected (boolean selected)}
        [pane/profile-panel {:item item}]])))

#_
(timbre/debug "->" styles/makeStyles makeStyles)

#_
(defn mui-styles [f]
  (styles/makeStyles (fn [theme] (clj->js (f theme)))))

#_
(def use-styles (mui-styles (fn [theme]
                              {:card {:padding (.spacing theme 2)}})))
#_
(def classes (styles))

(defn board-grid [{:keys [items]}]
  (timbre/debug "Show as grid")
  (into
   [:> Grid {:container true :spacing 8}]
   (for [{:keys [id selected] :as item} items]
      ^{:key id}
      [:> Grid {:item true :xs 12 :sm 6 :md 4}
        #_{:selected (boolean selected)}
       [pane/pick-zone {:item item
                        :style {:width "100%"}}
        [pane/profile-card {:item item}]]])))

(def requesting-funds (rf/subscribe [:requesting-funds]))

(defn board-pane [items]
  (let [on-dialog-close #(rf/dispatch [:request-funds false])]
    (fn [items]
      (timbre/debug "Board Pane:" items)
      [:<>
       [:> mui/Dialog {:open @requesting-funds
                       :on-close on-dialog-close}
        [invoice-view/funding-request-card]]
       [:> mui/Hidden {:sm-up true}
        [board-listing {:items items}]]
       [:> mui/Hidden {:xs-down true}
        [board-grid {:items items}]]])))

(defmethod pane :default [{:keys [mobile profile] :as session}]
  [board-pane (:fields @profile)])
