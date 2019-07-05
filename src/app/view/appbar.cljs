(ns app.view.appbar
  (:require
   [taoensso.timbre :as timbre]
   [clojure.string :as string]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons/Menu" :default AppIcon]
   ["@material-ui/icons/FlashOn" :default LightningIcon]
   ["@material-ui/icons/AccountCircle" :default AccountCircle]
   ["@material-ui/icons/CloudUpload" :default UploadIcon]
   ["@material-ui/core/styles"
    :refer [makeStyles]]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [app.drop
    :refer [decode-file]]))

(def debug (rf/subscribe [:debug]))

(def user-name (rf/subscribe [:user-name]))

(def selected (rf/subscribe [:selected]))

(defn short-username-field [{:keys [user-name]}]
  (if (string? user-name)
    (string/replace user-name ".id.blockstack" "")))

(defn user-status-area [{:keys [signed-in-status]}]
  (let [state (reagent/atom {})
        open-menu #(swap! state assoc :anchor (.-currentTarget %))
        close-menu #(swap! state assoc :anchor nil)
        signout #(rf/dispatch [:app/exit])
        signin #(rf/dispatch [:app/signin])
        request-funds #(rf/dispatch [:request-funds])]
    (fn [{:keys [signed-in-status]}]
      (if (not signed-in-status)
        [:div
         [:> mui/Button  {:color "inherit"
                          :on-click signin}
          [:> AccountCircle]
          (if (some? signed-in-status)
            "Sign In")]]
        [:div
         [:> mui/Button {:aria-owns "menu-appbar"
                         :aria-haspopup true
                         :color "inherit"
                         :on-click open-menu}
          [:> AccountCircle] " "
          [short-username-field {:user-name @user-name}]]
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
    [:> mui/Tooltip {:title (if active "Request funds" "Hide funds request")}
     [:> mui/Button {:color "inherit"
                     :style {:color (if active "yellow" "inherit")}
                     :on-click action}
      [:> LightningIcon]]]))

(defn upload-file []
  (timbre/debug "Upload file"))

(defn uploaded [evt]
  (timbre/debug "Uploaded:" evt)
  (let [files (.. evt -target -files)
        [file] (js->clj (js/Array.from files))]
    ;; # FIX: @selected should be in argument in case it changed...
    (rf/dispatch [:user/upload (first @selected)(decode-file file)])))

(defn upload-button [{:keys [active]}]
  ; see upload button example: https://material-ui.com/components/buttons/#contained-buttons
  (timbre/debug "Upload button:" active @selected)
  (let [selected-label (:label (first @selected))
        tooltip (if selected-label
                  (str "Upload " selected-label)
                  "Upload Image")]
    [:div {:style (if-not active {:display "none"})}
     [:input
      {:accept "image/*"
       :hidden true
       ; :style {:display "none"}
       :type "file"
       :on-change uploaded
       :id "file-upload"}]
     [:label {:html-for "file-upload"}
      [:> mui/Tooltip {:title tooltip}
       [:> mui/Button
        {; :variant "contained"
         :component "span"}
        [:> UploadIcon]]]]]))

(def signed-in-status (rf/subscribe [:signed-in-status]))
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
     [upload-button {:active (not (empty? @selected))}]
     [lightning-button {:active (not @requesting-funds)}]
     [user-status-area {:signed-in-status @signed-in-status}]]]])
