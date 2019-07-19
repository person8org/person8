(ns app.view.core
  (:require
   [taoensso.timbre :as timbre]
   ["@material-ui/core" :as mui]
   ["@material-ui/core/styles/MuiThemeProvider" :default mui-ThemeProvider]
   ["@material-ui/core/styles"
    :refer [createMuiTheme]]
   ["@material-ui/core/colors" :as colors]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [mount.core :refer [defstate]]
   [app.lib.blockstack]
   [app.lib.dev :as dev]
   [app.view.appbar :as appbar
    :refer [header]]
   [app.view.board
    :refer [board-pane]]))

(defn header-theme []
  (createMuiTheme
   (clj->js
    {:palette {:type (if @(rf/subscribe [:theme]) "light" "dark")
               :primary colors/blueGrey
               :primary-text-color colors/white}
     :typography {:useNextVariants true}})))

(defn custom-theme []
  (createMuiTheme
   (clj->js
    {:palette {:type (if @(rf/subscribe [:theme]) "light" "dark")}
               ; :primary-text-color colors/white}
               ; :background (aget colors/blueGrey "700")
     :typography {:useNextVariants true}})))

(def user-data (rf/subscribe [:blockstack/user-data]))

(def pane (rf/subscribe [:pane]))

(def board-items (rf/subscribe [:board]))

(def signed-in-status (rf/subscribe [:signed-in-status]))

(def debug (rf/subscribe [:debug]))

(defn authenticated-hook [signed-in-status]
  "Affect what is shown after logging in and out"
  (timbre/debug "Authenticated Status Changed:" signed-in-status)
  ; class supposed to be added by script in head of html file
  ; when returning from blockstack:
  (case signed-in-status
    true
    (js/document.documentElement.classList.add "authenticated")
    false
    (js/document.documentElement.classList.remove "authenticated")
    nil)
  (when (some? signed-in-status)
    (js/document.documentElement.classList.remove "reloading")))

(defn on-authenticated-changes []
  (authenticated-hook @signed-in-status))

(defstate authenticated-track
  :start (reagent/track! on-authenticated-changes)
  :end (reagent/dispose! authenticated-track))

(defn page [{:keys [open]}]
  [:div.page
    {:style {:display (if-not open "none")
             :background-color (aget colors/blueGrey "700")}}
    (case (if @debug (or @pane :default) :default)
      :profile [dev/user-profile-card {:user-data user-data}]
      :state [dev/state-inspector]
      :default [board-pane (or @board-items)])])

(defn app []
  [:<>
   [:> mui-ThemeProvider
    {:theme (header-theme)}
    [header]]
   [:> mui-ThemeProvider
    {:theme (custom-theme)}
    [:div  ;:> mui/CssBaseline ;; Use with care, sets css on body which may affect landing page
      [page {:open (boolean @signed-in-status)}]]]])
