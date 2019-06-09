(ns app.view.invoice
  (:require
   [taoensso.timbre :as timbre]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons" :as ic]
   ["@material-ui/icons/Money" :default MoneyIcon]
   ["@material-ui/icons/EnhancedEncryption" :default EncryptIcon]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [app.view.reagent-mui :as ui]))


(def requesting-funds (rf/subscribe [:requesting-funds]))

(defn funding-request-card []
  (if @requesting-funds
    [ui/card
     [ui/card-header
      {:title "Funding Request"
       :subheader "Receive funds through the Lightning Network"
       :avatar (-> [:> mui/Avatar [:> MoneyIcon]]
                   reagent/as-element)}]
     [ui/card-content
      #_[:div "Requesting Funds"]]]))
