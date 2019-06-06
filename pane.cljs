(ns app.mobile.pane
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
   [cljsjs.material-ui]
   [cljs-react-material-ui.core :as material
    :refer [get-mui-theme color]]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.icons :as ic]
   [goog.string :as gstring]
   [util.lib :as lib
    :refer [pp-element pp-str]]))

(def develop false)

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

(defn share-content [{:keys [from target content]}]
  (let [msg {:to (:number target)
             :from from
             :body content}]
    (rf/dispatch [:request [:twilio/send-sms msg]])))

(defn notice [message & [on-request-close]]
  [ui/snackbar {:open (if @message true false)
                :message (or @message "Sent SMS with your name")
                :auto-hide-duration 4000
                :on-request-close on-request-close}])

(defn share-button [action]
  (case :raised
    :floating
    [ui/floating-action-button
     {:secondary true}
     (ic/notification-sms)]
    :raised
    [ui/raised-button
     {:label "Share"
      :label-position "before"
      :icon (ic/notification-sms)
      :on-click action}]))

(defn share-dialog [{:keys [open content label send-action cancel-action]}]
  (let [targets [{:id "1" :label "SF Social Services" :number "+415-111-1111"}
                 {:id "2" :label "SF Food Bank" :number "+415-222-2222"}]
        selected (atom "1")
        find-target (fn [] (first (filter #(= @selected (:id %)) targets)))
        on-change-selected (fn [event index value]
                             (reset! selected value))]
    (fn [{:keys [open content label send-action cancel-action]}]
      [ui/dialog {:open open
                  :modal true
                  :actions [(reagent/as-element
                             [ui/flat-button
                              {:label "Cancel"
                               :primary false
                               :on-click cancel-action}])
                            (reagent/as-element
                             [ui/flat-button
                              {:label "Send SMS"
                               :primary true
                               :on-click #(send-action
                                           {:target (find-target)})}])]
                  :title (str "Share" label)}
       (into
        #_
        [ui/drop-down-menu {:auto-width true
                            :on-change on-change-selected
                            :value @selected}]
        [ui/select-field {:floating-label-text "With:"
                          :on-change on-change-selected
                          :value @selected}]
        (for [{:keys [id label]} targets]
          [ui/menu-item
           {:value id
            :primary-text label}]))])))

(defn now-timestamp []
  "Quick and dirty timestamp"
  (let [date (new js/Date)]
    (str
     (.toLocaleDateString date "en-US") " "
     (.toLocaleTimeString date "en-US"))))

#_
(now-timestamp)

(defn share-option [{:keys [id content feedback label]}]
  (let [opened (atom false)
        feedback (atom nil)]
    (fn [{:keys [id content feedback label]}]
      (let [send-action (fn [{:keys [target]}]
                          (do (share-content
                               {:from "Anonymous"
                                :target target
                                :content content})
                            (go-loop [ignore (<! (async/timeout 2000))]
                             (rf/dispatch [:new-field-event id
                                           {:label (str (:label target) " (SMS)")
                                            :type "sms"
                                            :number (:number target)
                                            :timestamp (now-timestamp)}]))
                           (reset! opened false)
                           (reset! feedback (str "Sent an SMS with your " label))))
            open-action #(reset! opened true)
            cancel-action #(reset! opened false)]
        [:div
         [share-button open-action]
         [share-dialog {:label label
                        :open @opened
                        :cancel-action cancel-action
                        :send-action send-action
                        :content content}]
         [notice feedback #(reset! feedback nil)]]))))

(defn edit-button []
  [ui/raised-button
   {:label ""
    :label-position "before"
    :icon (ic/image-edit)}])


(defn identity-card [{:keys [mobile] :as session}]
  (let [share false
        profile (get-in @mobile [:user :profile])
        content {:text (str (:firstName profile) " " (:lastName profile))}
        feedback (atom nil)]
    [ui/card {:style {:margin-top "0.3em"}}
     [ui/card-header
      {:avatar (ic/social-person)
       :title "Your Name"
       :subtitle "What you prefer to be called"}]
     [ui/card-text (:firstName profile) " " (:lastName profile)]
     [ui/card-actions #_{:style {:position "relative" :right 0}}
      (if share
        [:div {:style {:display "flex"
                       :justify-content "flex-end"
                       :width "100%"
                       :padding 0}}
         [share-option {:id "name" :content content :label "Send name" :feedback feedback}]])]]))

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
    [ui/card
     {:expanded @expanded
      :on-expand-change #(do (timbre/debug "Card expanded:" %)
                           (reset! expanded %))}
     [ui/card-header
      {:title (reagent/as-element [:span
                                   label
                                   (if events
                                     [:sup.badge.badge-primary
                                      {:style {:margin-left "0.5em"}}
                                      (count events)]
                                     #_
                                     [:span {:style {:background-color "red"
                                                     :color "white"
                                                     :width "1em"
                                                     :padding "0.1em"
                                                     :padding-left "0.4em"
                                                     :padding-right "0.4em"
                                                     :margin-bottom "0.2em"
                                                     :margin-left "0.5em"
                                                     :border-radius "50%"}}
                                       (str " " (count events)) "."]
                                     #_
                                     [ui/badge
                                       {:badge-content (str (count events))
                                        :badge-style {:top "20px"}
                                        :secondary true}])])
       :subtitle description
       :act-as-expander expandable
       :show-expandable-button expandable
       :avatar (ic/action-assignment)}]
     (if image
       [ui/card-media {:expandable expandable}
        [:img {:src image}]])
     [ui/card-actions #_{:style {:position "relative" :right 0}}
      (if (and share @expanded)
        [:div {:style {:display "flex"
                       :justify-content "flex-end"
                       :width "100%"
                       :padding 0}}
         [share-option {:id id :content content :label label :feedback feedback}]
         [notice feedback #(reset! feedback nil)]])
      (if edit
        [:span {:style {:width "1em"}} " "
         [edit-button]])
      (if (and events @expanded)
        [events-list {:events events}])]])))

(defmethod pane :default [{:keys [mobile profile] :as session}]
  [:div {:style {:padding-top "60px"}}
   (if develop [lib/pp-table session][:div])
   (if (= (get-in @mobile [:user :status]) "SUCCESS")
     (into
      [:div [identity-card session]]
      (for [item (:fields @profile)]
        [profile-card item]))
    [:div])])
