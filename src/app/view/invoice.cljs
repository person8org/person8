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

(defn amount-field [{:keys [value]}]
  [:<>
   [:> mui/InputLabel
    {:htmlFor "input-amount"}
    "Amount"]
   [:> mui/Input
    {:id "input-amount"
     :value @value
     :fullWidth true
     :on-change (fn [e]
                  (reset! value (.. e -target -value)))
     :endAdornment
     (reagent/as-element
      [:> mui/InputAdornment {:position "end"} "SAT"])}]])

(defn memo-field [{:keys [value]}]
   [:> mui/TextField
    {:label "Memo"
     :value @value
     :fullWidth true
     :margin "normal"
     :on-change (fn [e]
                  (reset! value (.. e -target -value)))}])

(defn hash-field [{:keys [amount memo]}]
  [:div
     (str amount "+" memo)]) 


(defn funding-request-card []
  (let [amount (reagent/atom "")
        memo (reagent/atom "")]
    (fn []
      (if @requesting-funds
        [ui/card
         [ui/card-header
          {:title "Funding Request"
           :subheader "Receive funds through the Lightning Network"
           :avatar (-> [:> mui/Avatar [:> MoneyIcon]]
                       reagent/as-element)}]
         [ui/card-content
          [:> mui/FormControl
           [amount-field {:value amount}]
           [memo-field {:value memo}]
           [hash-field {:amount @amount :memo @memo}]]]]))))
