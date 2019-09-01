(ns app.view.info
  (:require
   ["@material-ui/core" :as mui]
   [taoensso.timbre :as timbre]
   [re-frame.core :as rf]
   [reagent.core :as reagent]))

(defn view []
  (timbre/info "Open info dialog")
  [:div
   [:> mui/DialogContent
    [:> mui/DialogTitle "About"]
    [:> mui/DialogContentText
     "Person8 is an online vault to safekeep your personal identity information."
     " Simply load or drop images into the app. The files will be strongly encrypted for your protection."
     " In case you need access to the documents, double-click top view and optionally download the decrypted files."
     ""]
    [:> mui/DialogTitle "Privacy"]
    [:> mui/DialogContentText
     "We respect your privacy."
     "Your data is for your eyes only, even we cannot view your content."]]])
