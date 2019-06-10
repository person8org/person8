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
                :on-exiting on-request-close}])

(defn share-button [action]
  (case :raised
    :floating
    [ui/floating-action-button
     {:secondary true
      :icon [SmsIcon]}]
    :raised
    [ui/raised-button
     {:on-click action
      :variant "outlined"}
     [:span {:style {:margin-right "0.5em"}}
      "Send SMS"]
     [:> SmsIcon]]))

(defn recipient-selector [{:keys [targets selected on-change]}]
  (timbre/debug "Render: recipient selector")
  [:> mui/FormControl
   [:> mui/InputLabel {:htmlFor "recipient"}
    "Recipient:"
    (into
     [:> mui/Select
      {:onChange on-change
       :on-change on-change
       :style {:min-width "10em"}
       :SelectDisplayProps {:on-change on-change}
       :input-props {; :name "recipient"
                     :id "recipient"}
       :value @selected}]
     (for [{:keys [id label]} targets]
        [ui/menu-item {:value id}
         label]))]])


(defn share-dialog [{:keys [open content label send-action cancel-action]}]
  (let [targets [{:id "1" :label "SF Social Services" :number "+415-111-1111"}
                 {:id "2" :label "SF Food Bank" :number "+415-222-2222"}
                 {:id "3" :label "My Emergency contact" :number "+415-975-1717"}]
        selected (reagent/atom (:id (first targets)))
        find-target (fn [] (first (filter #(= @selected (:id %)) targets)))
        on-change-selected (fn [event & [index value]]
                             (timbre/debug "Selected" event)
                             (let [val (.. event -target -value)]
                               (timbre/debug "Selected:" val index value)
                               (reset! selected val)))]
    (fn [{:keys [open content label send-action cancel-action] :as config}]
      (timbre/debug "Dialog:" config)
      [ui/dialog {:open open}
       [:> mui/DialogTitle (str "Send " label)]
       [:> mui/DialogContent
        [recipient-selector {:targets targets
                             :on-change on-change-selected
                             :selected selected}]]
       [:> mui/DialogActions
                [ui/flat-button    {:color "secondary"
                                    :on-click cancel-action}
                  "Cancel"]
                [ui/flat-button    {:variant "outlined"
                                    :color "primary"
                                    :on-click #(send-action
                                                {:target (find-target)})}
                  "Send SMS"]]])))

(defn share-option [{:keys [id content feedback label]}]
  (let [opened (reagent/atom false)
        feedback (reagent/atom nil)]
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
            open-action #(do (timbre/debug "Open share dialog")
                           (reset! opened true))
            cancel-action #(do (timbre/debug "Close share dialog")
                             (reset! opened false))]
        [:div
         [share-button open-action]
         [share-dialog {:label label
                        :open @opened
                        :cancel-action cancel-action
                        :send-action send-action
                        :content content}]
         [notice feedback #(reset! feedback nil)]]))))
