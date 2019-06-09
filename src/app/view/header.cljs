(ns app.view.header
  (:require
   [taoensso.timbre :as timbre]
   [clojure.string :as string]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons" :as ic]
   ["@material-ui/icons/Menu" :default AppIcon]
   ["@material-ui/icons/FlashOn" :default LightningIcon]
   ["@material-ui/core/styles"
    :refer [makeStyles]]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [app.state :refer [app-state]]
   [app.events :refer [increment decrement]]
   [app.view.authenticate]))

(def debug (rf/subscribe [:debug]))

(def user-name (rf/subscribe [:user-name]))

(defn user-status-area [{:keys [signed-in-status]}]
  (let [state (reagent/atom {})
        open-menu #(swap! state assoc :anchor (.-currentTarget %))
        close-menu #(swap! state assoc :anchor nil)
        signout #(rf/dispatch [:sign-user-out])
        signin #(rf/dispatch [:sign-user-in])
        request-funds #(rf/dispatch [:request-funds])]
    (fn [{:keys [signed-in-status]}]
      (if (not signed-in-status)
        [:div
         [:> mui/Button  {:color "inherit"
                          :on-click signin}
          [:> ic/AccountCircle]
          "Sign In"]]
        [:div
         [:> mui/IconButton {:aria-owns "menu-appbar"
                             :aria-haspopup true
                             :color "inherit"
                             :on-click open-menu}
          [:> ic/AccountCircle] " "
          (string/replace @user-name ".id.blockstack" "")]
         [:> mui/Menu
          {:id "menu-appbar"
           :anchorEl (:anchor @state)
           :open (boolean (:anchor @state))
           :on-close close-menu}
          (for [{:keys [action label] :as item}
                [{:label "Sign Out" :action signout}
                 {:label "-"}
                 {:label "Request Funds" :action request-funds}
                 (if @debug {:label "Inbox" :action  #(rf/dispatch [:pane nil])})
                 (if @debug {:label "Profile" :action #(rf/dispatch [:pane :profile])})
                 (if @debug {:label "State" :action  #(rf/dispatch [:pane :state])})]
                :when (some? item)]
            ^{:key label}
            [:> mui/MenuItem
             {:on-click #(do
                           (if action (action))
                           (close-menu))}
             label])]]))))

(def requesting-funds (rf/subscribe [:requesting-funds]))

(defn lightning-button [{:keys [active]}]
  (let [action #(rf/dispatch [:request-funds active])]
    [:> mui/IconButton {:color "inherit"
                        :style {:color (if active "yellow" "inherit")}
                        :on-click action}
     [:> LightningIcon]]))


(def signed-in-status (rf/subscribe [:signed-in]))
(def product (rf/subscribe [:product]))

(defn header []
  [:div {:style {:flex-grow 1}}
   [:> mui/AppBar {:position "static"}
    [:> mui/Toolbar {}
     [:> mui/Icon
      {:style {:margin-right "1em"}}
      [:> AppIcon {:color "inherit"}]]
     [:> mui/Typography {:variant "h6"
                         :style {:flex 1}}
      (get @product :name "App")]
     [lightning-button {:active (not @requesting-funds)}]
     [user-status-area {:signed-in-status @signed-in-status}]]]])
