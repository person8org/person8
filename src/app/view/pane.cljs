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
   ["@material-ui/icons/Person" :default social-person]
   [app.lib.reagent-mui :as ui]
   [goog.string :as gstring]
   [app.view.share :as share-view
    :refer [share-option]]))


(defmulti pane (fn [{:keys [stage] :as session}]
                  (if stage [@stage])))

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


(defn identity-card [{:keys [mobile] :as session}]
  (let [share false
        profile (get-in @mobile [:user :profile])
        content {:text (str (:firstName profile) " " (:lastName profile))}
        feedback (atom nil)]
    [ui/card {:style {:margin-top "0.3em"}}
     [ui/card-header
      {:avatar (social-person)
       :title "Your Name"
       :subtitle "What you prefer to be called"}]
     [ui/card-text (:firstName profile) " " (:lastName profile)]
     [ui/card-actions
      (if share
        [:div {:style {:display "flex"
                       :justify-content "flex-end"
                       :width "100%"
                       :padding 0}}
         [share-option
          {:id "name"
           :content content
           :label "Send name"
           :feedback feedback}]])]]))


(defn events-list [{events :events}]
    (into [ui/list
           [ui/subheader "Shared with:"]]
          (for [{:keys [label timestamp] :as event} events]
            [ui/list-item
             {:primary-text label
              :secondary-text timestamp}])))


(defn profile-card [{:keys [id label description edit share image expandable text events]
                     :as item}]
  (let [content (if image {:media image}{:text text})
        feedback (atom nil)
        expanded (atom false)]
   (fn [{:keys [label description edit share image expandable text events]
          :as item}]
    [ui/card {:style {:width "100%"}}
     [:> mui/ExpansionPanel
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
        [ui/card-action-area
         (if image
           [ui/card-media {:style {:height "auto"}
                           :image image
                           :component "img"}])]
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

(defmethod pane :default [{:keys [mobile profile] :as session}]
  [:div {:style {:padding-top "60px"}}
   (if (= (get-in @mobile [:user :status]) "SUCCESS")
     (into
      [:div [identity-card session]]
      (for [item (:fields @profile)]
        [profile-card item]))
    [:div])])
