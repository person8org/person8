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
       :refer [DropzoneArea]]
   [cljs-drag-n-drop.core :as dnd]
   [app.lib.reagent-mui :as ui]
   [goog.string :as gstring]
   [app.lib.drop
    :refer [decode-file]]
   [app.view.share :as share-view
    :refer [share-option]]))

(def drag-status (rf/subscribe [:drag]))

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
    :labelposition "before"
    :icon (image-edit)}
   "Edit"])

(defn events-list [{events :events}]
    (into [ui/list
           [ui/subheader "Shared with:"]]
          (for [{:keys [label timestamp] :as event} events]
            [ui/list-item
             {:primary-text label
              :secondary-text timestamp}])))

(defn on-drop-fn [{:keys [item]}]
  (fn [file]
    (timbre/info "Drop file:" (:id item) (.-name file))
    (rf/dispatch [:user/drop item (decode-file file)])))

(defn drop-zone-area [{:keys [item]}]
  [:> DropzoneArea
   {:accepted-files #js["image/*"]
    :files-limit 1
    :show-previews-in-dropzone false
    :dropzone-class "dropzone-area"
    :on-change #(timbre/debug "Change in dropzone")
    :on-drop (on-drop-fn {:item item})}])

(defn card-image-slot [{:keys [item image show-dropzone]}]
  (timbre/debug "Card image slot:" show-dropzone)
  [:div {:style {:position "relative"
                 :overflow "hidden"}}
   [ui/card-media {:style {:height "auto"}
                   :image image
                   :component "img"}]
   [:div {:class-name (if show-dropzone "show-dropzone")
          :style {:position "absolute"
                  :top 0
                  :width "100%"
                  :height "100%"}}
      [drop-zone-area {:item item}]]])

(defn profile-card [{{:keys [id label description edit share
                             image expandable text events] :as item} :item
                     :as props}]
  (let [content (if image {:media image}{:text text})
        feedback (atom nil)
        expanded (atom false)]
    (fn [{{:keys [id label description edit share
                  image expandable text events] :as item} :item
          :as props}]
      (timbre/debug "For larger display")
      [ui/card {:style {:width "100%"
                        :height "100%"
                        :max-height "50vh"}}
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
                       reagent/as-element)}]
         [ui/card-content
          {:style {:width "100%" :height "auto"}}
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
            (if edit
              [:span {:style {:width "1em"}}
               [edit-button]])
            (if (and events @expanded)
              [events-list {:events events}])])])))


(defn profile-panel [{{:keys [id label description edit share
                              image expandable text events :as item]}
                      :item
                      :as props}]
  (timbre/debug "For mobile display or reduced width")
  (let [content (if image {:media image}{:text text})
        feedback (atom nil)
        expanded (atom false)]
    (fn [{{:keys [id label description edit share
                  image expandable text events] :as item} :item
          :as props}]
      [ui/card {:style {:width "100%"}}
       [ui/expansion-panel
        {:default-expanded false}
        [:> mui/ExpansionPanelSummary ; can't use ui/expansion-panel-summary
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
            (if edit
              [:span {:style {:width "1em"}}
               [edit-button]])
            (if (and events @expanded)
              [events-list {:events events}])])]]])))

#_
(defn view [{:keys [item] :as props}]
    [pick-zone {:item item
                :style {:width "100%"}}
     [profile-card props]])
