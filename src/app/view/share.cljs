(ns app.view.share
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<! chan close! alts! timeout put!]]
   [taoensso.timbre :as timbre]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons" :as ic]
   ["@material-ui/icons/Sms" :default SmsIcon]
   [app.view.reagent-mui :as ui]
   [re-frame.core :as rf]
   [reagent.core :as reagent]))


(defn now-timestamp []
  "Quick and dirty timestamp"
  (let [date (new js/Date)]
    (str
     (.toLocaleDateString date "en-US") " "
     (.toLocaleTimeString date "en-US"))))


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
     {:secondary true
      :icon [SmsIcon]}]
    :raised
    [ui/raised-button
     {:on-click action}
     [:span {:style {:margin-right "1em"}}
      "Share"]
     [:> SmsIcon]]))

(defn share-dialog [{:keys [open content label send-action cancel-action]}]
  (let [targets [{:id "1" :label "SF Social Services" :number "+415-111-1111"}
                 {:id "2" :label "SF Food Bank" :number "+415-222-2222"}]
        selected (atom "1")
        find-target (fn [] (first (filter #(= @selected (:id %)) targets)))
        on-change-selected (fn [event index value]
                             (reset! selected value))]
    (fn [{:keys [open content label send-action cancel-action]}]
      [ui/dialog {:open open}
       [:> mui/DialogTitle (str "Share " label)]
       [:> mui/FormControl
        [:> mui/InputLabel "With:"]
        (into
         [ui/select-field {:on-change on-change-selected
                           :value @selected}]
         (for [{:keys [id label]} targets]
           [ui/menu-item {} label]))]
       [:> mui/DialogActions
                [ui/flat-button    {:variant "outlined"
                                    :color "secondary"
                                    :on-click cancel-action}
                  "Cancel"]
                [ui/flat-button    {:color "primary"
                                    :on-click #(send-action
                                                {:target (find-target)})}
                  "Send SMS"]]])))

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
