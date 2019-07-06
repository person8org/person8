(ns app.view.pane
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<! chan close! alts! timeout put!]]
   [reagent.core :as reagent
    :refer [atom]]
   [re-frame.core :as rf]
   [taoensso.timbre :as timbre]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons/FileCopy" :default CopyIcon]
   ["@material-ui/icons/ExpandMore" :default ExpandIcon]
   ["@material-ui/icons/Assignment" :default DocIcon]
   ["@material-ui/icons/AssignmentLate" :default NoDocIcon]
   ["@material-ui/icons/Edit" :default image-edit]
   ["material-ui-dropzone"
    :refer [DropzoneArea DropzoneDialog]]
   [cljs-drag-n-drop.core :as dnd]
   [app.lib.reagent-mui :as ui]
   [goog.string :as gstring]
   [app.drop
    :refer [decode-file]]
   [app.view.share :as share-view
    :refer [share-option]]))

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
                      (rf/dispatch [:user/drop item (decode-file file)])))})))
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

(defn navigator [{:keys [tab] :as session}]
  [ui/card
   (into [:ul.list-group {:style {:margin-top "1em"}}]
         (for [{:keys [id title] :as item}
               (if tab (:options @tab))]
           [:a.list-group-item
            {:class (if (and tab (= id (:current @tab)))
                      "active")
             :href (str "#tab/" id)}
            title]))])

(defn edit-button []
  [ui/raised-button
   {:label ""
    :label-position "before"
    :icon (image-edit)}])

(defn events-list [{events :events}]
    (into [ui/list
           [ui/subheader "Shared with:"]]
          (for [{:keys [label timestamp] :as event} events]
            [ui/list-item
             {:primary-text label
              :secondary-text timestamp}])))

(defn on-drop-fn [{:keys [item]}]
  (fn [file]
    ; this.state.fileObjects
    (let [current (reagent.core/current-component)]
      (this-as this
               (timbre/info "Dropzone file:" file (js-keys this) current #_(.. this -state -fileObjects))
               (rf/dispatch [:user/drop item (decode-file (js->clj file))])))))

(defn drop-zone-area [{:keys [item]}]
  [:> DropzoneArea
   {:accepted-files #js["image/*"]
    :files-limit 1
    :show-previews-in-dropzone false
    :drop-zone-class "dropzone-area"
    :on-change #(timbre/debug "Change in dropzone")
    :on-drop (on-drop-fn {:item item})}])

(defn card-image-slot [{:keys [item image show-dropzone]}]
  (timbre/debug "Card image slot:" show-dropzone)
  [:div {:style {:position "relative"}}
   [ui/card-media {:style {:height "auto"}
                   :image image
                   :component "img"}]
   [:div {:class-name (if show-dropzone "show-dropzone")
          :style {:position "absolute"
                  :top 0
                  :height "100%"}}
      [drop-zone-area {:item item}]]])

(defn profile-card [{{:keys [id label description edit share
                             image expandable text events] :as item} :item
                     default-expanded :default-expanded
                     :as props}]
  (let [content (if image {:media image}{:text text})
        feedback (atom nil)
        expanded (atom false)]
    (fn [{{:keys [id label description edit share
                  image expandable text events] :as item} :item
          default-expanded :default-expanded
          :as props}])
    [ui/card {:style {:width "100%"
                      :max-height "50vh"}}
     [:> mui/ExpansionPanel
      {:default-expanded default-expanded}
      [:> mui/ExpansionPanelSummary
       {:expand-icon (-> [:> ExpandIcon]
                         reagent/as-element)}
       [ui/card-header
        {:title (reagent/as-element
                 [:span label
                        (if events
                          [ui/badge {:badge-content (str (count events))
                                     :badge-style {:top "20px"}
                                     :secondary true}])])
         :subheader description
         :avatar (-> [:> mui/Avatar
                      (if image
                        [:> DocIcon]
                        [:> NoDocIcon])]
                     reagent/as-element)}]]
      [ui/expansion-panel-details
        [ui/card-content
         {:style {:width "100%"}}
         (if image
           [card-image-slot
            {:item item
             :show-dropzone (contains? #{:start :enter} @drag-status)
             :image image}])]
       (if image
        [ui/card-actions
         (if (and share true)
           [:div {:style {;:display "flex"
                          ;:justify-content "flex-end"
                          ;:width "100%"
                          :padding 0}}
            [share-option
             {:id id
              :content content
              :label label
              :feedback feedback}]
            #_[notice feedback #(reset! feedback nil)]])
         (if true ; edit
           [:span {:style {:width "1em"}}
            [edit-button]])
         (if (and events @expanded)
           [events-list {:events events}])])]]]))

(defn view [{:keys [item] :as props}]
  [drop-zone {:item item
              :style {:width "100%"}}
    [pick-zone {:item item
                :style {:width "100%"}}
     [profile-card props]]])
