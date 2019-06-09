(ns app.view.core
  (:require
   [taoensso.timbre :as timbre]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons" :as ic]
   ["@material-ui/core/styles/MuiThemeProvider" :default mui-ThemeProvider]
   ["@material-ui/core/styles" :refer [createMuiTheme]]
   ["@material-ui/core/colors" :as colors]
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

;:primary1-color (color :blue-grey900
;:primary2-color (color :indigo700)
;:primary3-color (color :indigo200)
;:alternate-text-color (color :white))) ;; used for appbar text
;:primary-text-color (color :light-black)

(timbre/debug "colors:" (js-keys colors/blueGrey))
(timbre/debug "color:" (aget colors/blueGrey "900"))

(defn header-theme []
  (createMuiTheme
   (clj->js
    {:palette {:type "dark"
               :primary colors/blueGrey
               :primary-text-color colors/white
               :typography {:useNextVariants true}}})))

(defn custom-theme []
  (createMuiTheme
   (clj->js
    {:palette {:type "dark"
               ; :background (aget colors/blueGrey "700")
               :typography #js {:useNextVariants true}}})))

(def pane (rf/subscribe [:pane]))

(def board-items (rf/subscribe [:board]))

(defn app []
  [:<>
   [:> mui/CssBaseline]
   [:> mui-ThemeProvider
    {:theme (header-theme)}
    [header]]
   [:> mui-ThemeProvider
    {:theme (custom-theme)}
    [auth
     [:div {:style {:background-color (aget colors/blueGrey "700")}}
       (case (or @pane :default)
         :profile [dev/user-profile-card {:user-data user-data}]
         :state [dev/state-inspector]
         :default [board-pane (or @board-items)])]]]])
