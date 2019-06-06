(ns app.view.board
  (:require
    [taoensso.timbre :as timbre]
    ["@material-ui/core" :as mui]
    ["@material-ui/icons" :as ic]
    ["@material-ui/icons/FileCopy" :default CopyIcon]
    [re-frame.core :as rf]
    [app.view.pane :as pane
     :refer []]))

(def test-url "https://images.pexels.com/photos/248797/pexels-photo-248797.jpeg")

(def debug (rf/subscribe [:debug]))

(defn card-file-area [{:keys [kind name id type data url] :as item}]
  [:<>
   [:> mui/CardHeader
     {:title name
      :subheader type
      :avatar nil}]])

(defn item-card [{:keys [kind id type data url] :as item}]
  [:> mui/Card
   {:on-click #(rf/dispatch [:select item])}
   (if @debug
     [:> mui/CardHeader
      {:title (str (or id url "???"))
       :subheader (str (or type "???"))}])
   (cond

     (= kind "file")
     [card-file-area item]

     (= type "text/plain")
     [:> mui/CardContent
       [:> mui/Typography {:component "p"} data]]

     url
     [:> mui/CardActionArea
      [:> mui/CardMedia
        {:title url
         :style {:height "140"}
         :component "img"
         :image url}]
      (if @debug
        [:> mui/CardContent
         [:> mui/Typography {:component "p"} "Image: " type]
         [:> mui/Typography {:component "p"} url]])]

     @debug
     [:> mui/CardContent
      (pr-str data)])])

(defn board-listing [items]
  (into
   [:> mui/List]
   (for [{:keys [selected] :as item} items]
     [:> mui/ListItem
      {:selected (boolean selected)}
      [pane/profile-card item]])))

(def drag-status (rf/subscribe [:drag]))

(defn drop-zone [& children]
  ;; consider instead using https://www.npmjs.com/package/material-ui-dropzone
  (into
   [:div
    {:style {:min-height "100vh"
             :width "100%"
             :border (if (= @drag-status :start)
                       "thick solid yellow"
                       "thin solid none")}}]
   children))

(defn board-pane [items]
  (timbre/debug "Board Pane:" items)
  [drop-zone
    [board-listing items]])
