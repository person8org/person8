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

(timbre/info "HEADER:" (js-keys colors/blue))

(defn header-theme [theme]
  (timbre/debug "Header theme:" theme)
  (createMuiTheme
   (clj->js
    {:overrides
     (if (= theme "dark")
       {:MuiAppBar
         {:colorPrimary
           {:backgroundColor (aget colors/blueGrey 800)}}})

     :palette {:type (or theme "dark")}
     :typography {:useNextVariants true}}))) ;(or theme "dark")


(defn custom-theme [theme]
  (createMuiTheme
   (clj->js
    {:palette {:type (or theme "dark")}
     :typography {:useNextVariants true}})))

(def user-data (rf/subscribe [:blockstack/user-data]))

(def pane (rf/subscribe [:pane]))

(def board-items (rf/subscribe [:board]))

(def signed-in-status (rf/subscribe [:signed-in-status]))

(def debug (rf/subscribe [:debug]))

(def theme (rf/subscribe [:theme]))

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

(defn page []
  [:div.page
   {:style {:background-color nil #_(aget colors/blueGrey "700")}}
   (case (if @debug (or @pane :default) :default)
     :profile [dev/user-profile-card {:user-data user-data}]
     :state [dev/state-inspector]
     :default [board-pane (or @board-items)])])

(defn app []
  [:<>
   [:> mui-ThemeProvider
     {:theme (header-theme @theme)}
     [:> mui/CssBaseline
       [header]]]
   [:div {:hidden (not @signed-in-status)}
     [:> mui-ThemeProvider
      {:theme (custom-theme @theme)}
      [:> mui/CssBaseline
       [page {:open (boolean @signed-in-status)}]]]]])
