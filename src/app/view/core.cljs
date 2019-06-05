(ns app.view.core
  (:require
   [taoensso.timbre :as timbre]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons" :as ic]
   ["@material-ui/core/styles/MuiThemeProvider" :default mui-ThemeProvider]
   ["@material-ui/core/styles" :refer [createMuiTheme]]
   ["@material-ui/core/colors" :as mui-colors]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [app.state :refer [app-state]]
   [app.events :refer [increment decrement]]
   [app.view.authenticate
    :refer [auth]]
   [app.view.dev :as dev]
   [app.view.header :as header
    :refer [header]]
   [app.view.board
    :refer [board-pane]]))

;; https://v3-8-0.material-ui.com/

(def user-data (rf/subscribe [:user-data]))

(defn custom-theme []
  (createMuiTheme
   (clj->js {:palette {:type "light"
                       :primary (.-blue mui-colors)
                       :secondary (.-orange mui-colors)
                       :typography #js {:useNextVariants true}}})))

(def pane (rf/subscribe [:pane]))

(def board-items (rf/subscribe [:board]))

(defn app []
  [:<>
   [:> mui-ThemeProvider
    {:theme (custom-theme)}
    [:<>
     [:> mui/CssBaseline]
     [header]
     [auth
       (case (or @pane :default)
         :profile [dev/user-profile-card {:user-data user-data}]
         :state [dev/state-inspector]
         :default [board-pane (or @board-items)])]]]])
