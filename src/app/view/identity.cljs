(ns app.view.identity
  (:require
    ["@material-ui/core" :as mui]
    ["@material-ui/icons/Person" :default social-person]
    [app.view.share :as share-view
     :refer [share-option]]
    [app.lib.reagent-mui :as ui]))

(defn identity-card [{:keys [mobile] :as session}]
  (let [share false
        profile (get-in @mobile [:user :profile])
        content {:text (str (:firstName profile) " " (:lastName profile))}
        feedback (atom nil)]
    [ui/card {:style {:margin-top "0.3em"}}
     [ui/card-header
      {:avatar (social-person)
       :title "Your Name"
       :subtitle "What you prefer to be called"}]
     [ui/card-text (:firstName profile) " " (:lastName profile)]
     [ui/card-actions
      (if share
        [:div {:style {:display "flex"
                       :justify-content "flex-end"
                       :width "100%"
                       :padding 0}}
         [share-option
          {:id "name"
           :content content
           :label "Send name"
           :feedback feedback}]])]]))
