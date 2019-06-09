(ns app.view.board
  (:require
    [taoensso.timbre :as timbre]
    ["@material-ui/core" :as mui]
    ["@material-ui/icons" :as ic]
    ["@material-ui/icons/FileCopy" :default CopyIcon]
    [cljs-drag-n-drop.core :as dnd]
    [re-frame.core :as rf]
    [reagent.core :as reagent]
    [app.drop
     :refer [decode-file]]
    [app.view.invoice :as invoice-view]
    [app.view.pane :as pane
     :refer []]))

(def debug (rf/subscribe [:debug]))

(def drag-status (rf/subscribe [:drag]))

(defn drop-zone [{:keys [item]} & children]
  ;; consider instead using https://www.npmjs.com/package/material-ui-dropzone
  (let [unique-key (keyword (str "drop-zone-" (random-uuid)))]
    (reagent/create-class
     {:component-did-mount
      (fn [comp]
        (timbre/debug "Mount dropzone:" comp)
        (let [node (reagent/dom-node comp)]
          (dnd/subscribe!
           node unique-key
           {:drop (fn [e files]
                    (timbre/debug "Drop:" files)
                    (let [[file] (js->clj (js/Array.from files))]
                      (rf/dispatch [:replace-image item (decode-file file)])))})))
      :component-will-unmount
      (fn [comp]
        (timbre/debug "Unmount dropzone:" comp)
        (let [node (reagent/dom-node comp)]
          (dnd/unsubscribe! node unique-key)))
      :reagent-render
      (fn [{:keys [item]} & children]
        ;; Bug: Not updating item if changed
        (into [:<>] children))})))

(defn pick-zone [{:keys [item] :as props} & children]
  (let [on-click #(rf/dispatch [:select item])]
    (into [:div (assoc props :on-click on-click)]
          children)))

(defn board-listing [items]
  (into
   [:> mui/List
    [invoice-view/funding-request-card]]
   (for [{:keys [selected] :as item} items]
     [:> mui/ListItem
      {:selected (boolean selected)}
      [drop-zone {:item item
                  :style {:width "100%"}}
       [pick-zone {:item item
                   :style {:width "100%"}}
        [pane/profile-card item]]]])))

(defn board-pane [items]
  (timbre/debug "Board Pane:" items)
  [board-listing items])
