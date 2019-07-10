(ns app.lib.reagent-mui
  (:refer-clojure :exclude [list])
  (:require
   [reagent.core :as reagent]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons/FileCopy" :default CopyIcon]))

"Shim for reagent mui"

(assert mui/Card)

(defn card [& props]
  (into [:> mui/Card] props))

(defn card-header [& props]
  (into [:> mui/CardHeader] props))

(defn card-media [& props]
  (into [:> mui/CardMedia] props))

(defn card-action-area [& props]
  (into [:> mui/CardActionArea] props))

(defn card-actions [& props]
  (into [:> mui/CardActions] props))

(defn card-text [& props]
  (into [:> mui/CardText] props))

(defn card-content [& props]
  (into [:> mui/CardContent] props))

(defn expansion-panel [& props]
  (into [:> mui/ExpansionPanel] props))

(defn expansion-panel-summary [& props]
  (into [:> mui/ExpansionPanelSummary] props))

(defn expansion-panel-details [& props]
  (into [:> mui/ExpansionPanelDetails] props))

(def grid (reagent/adapt-react-class mui/Grid))

(defn list [& props]
  (into [:> mui/List] props))

(defn list-item [& props]
  (into [:> mui/ListItem] props))

(defn snackbar [ & props]
  (into [:> mui/Snackbar] props))

(defn floating-action-button [& props]
  (into [:> mui/Button] props))

(defn raised-button [& props]
  (into [:> mui/Button] props))

(defn flat-button [& props]
  (into [:> mui/Button] props))

(defn dialog [& props]
  (into [:> mui/Dialog]
    props))

(defn subheader [& props]
  (into [:> mui/Subheader] props))

(defn select-field [& props]
  (into [:> mui/Select] props))

(defn menu-item [& props]
  (into [:> mui/MenuItem] props))

(defn badge [& props]
  (into [:> mui/Badge] props))
