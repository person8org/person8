(ns app.view.header
  (:require
   [taoensso.timbre :as timbre]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons" :as ic]
   ["@material-ui/icons/AssignmentReturned" :default AppIcon]
   ["@material-ui/core/styles"
    :refer [makeStyles]]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [app.state :refer [app-state]]
   [app.events :refer [increment decrement]]
   [app.view.authenticate]))

(def debug (rf/subscribe [:debug]))

(defn user-status-area [{:keys [signed-in-status]}]
  (let [state (reagent/atom {})
        open-menu #(swap! state assoc :anchor (.-currentTarget %))
        close-menu #(swap! state assoc :anchor nil)
        signout #(rf/dispatch [:sign-user-out])
        signin #(rf/dispatch [:sign-user-in])]
    (fn [{:keys [signed-in-status]}]
      (if (not @signed-in-status)
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
          [:> ic/AccountCircle]]
         [:> mui/Menu
          {:id "menu-appbar"
           :anchorEl (:anchor @state)
           :open (boolean (:anchor @state))
           :on-close close-menu}
          (for [{:keys [action label] :as item}
                [{:label "Sign Out" :action signout}
                 (if @debug {:label "-"})
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


(def signed-in-status (rf/subscribe [:signed-in]))


(defn header []
  [:div {:style {:flex-grow 1}}
   [:> mui/AppBar {:position "static"}
    [:> mui/Toolbar {}
     [:> mui/Avatar
      {:style {:margin-right "1em"}}
      [:> AppIcon {:color :secondary}]]
     [:> mui/Typography {:variant "h6"
                         :style {:flex 1}}
      "ClipBox"]
     [user-status-area {:signed-in-status signed-in-status}]]]])
