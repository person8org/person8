(ns app.view.invoice
  (:require
   [taoensso.timbre :as timbre]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons/FlashOn" :default MoneyIcon]
   ["@material-ui/icons/OfflineBolt" :default LightningIcon]
   ["@material-ui/icons/EnhancedEncryption" :default EncryptIcon]
   ["qrcode.react" :as QRCode]
   [goog.functions]
   [app.lib.bolt11 :as bolt11]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [app.lib.reagent-mui :as ui]
   [app.view.share :as share-view]))

(defn on-change-fn [value-var]
  (fn [e]
    (let [value (.. e -target -value)]
      (reset! value-var value))))

(def debounce goog.functions/debounce)

(defn amount-field [{:keys [value]}]
  [:<>
   [:> mui/InputLabel
    {:htmlFor "input-amount"}
    "Amount"]
   [:> mui/Input
    {:id "input-amount"
     :value @value
     :fullWidth true
     :on-change (on-change-fn value)
     :endAdornment
     (reagent/as-element
      [:> mui/InputAdornment {:position "end"} "SAT"])}]])

(defn memo-field [{:keys [value]}]
   [:> mui/TextField
    {:label "Memo"
     :value @value
     :fullWidth true
     :multiline true
     :margin "normal"
     :on-change (on-change-fn value)}])

(defn encode-invoice [{{:keys [address amount memo payment-hash timestamp]
                        :as invoice} :invoice
                       {:keys [private-key]
                        :as config} :config}]
  (try
    (-> {:address @address
         :amount @amount
         :memo @memo
         :payment-hash @payment-hash
         :timestamp @timestamp}
        (bolt11/encode-invoice)
        (bolt11/sign @private-key)
        (.. -paymentRequest))
    (catch :default e
      (timbre/warn e))))

(defn invoice-area
  [{{:keys [address amount memo payment-hash timestamp]
     :as invoice} :invoice
    {:keys [private-key]
     :as config} :config}]
  (case :bolt11
    :bolt11
    (let [hash (reagent/track! #(-> {:invoice invoice
                                     :config config}
                                    (encode-invoice)))]
        [:div {:style {:word-break "break-all"}}
          @hash])
    :table
    (->> invoice
         (map (fn [[k v]] [:div (name k) ":" (deref v)]))
         (into [:div]))
    :empty [:div]))

(defn qrcode-area [{:keys [invoice config]}]
  (let [hash (reagent/track! #(-> {:invoice invoice :config config}
                                  (encode-invoice)))]
    [:> QRCode {:value @hash
                :style {:width "auto" :height "100%"}}]))


(defn show-button [{:keys [value]}]
  [:> mui/FormControlLabel
   {:control
    (->
     [:> mui/Switch
      {:value @value
       :on-change (fn [e v](reset! value v))}]
     (reagent/as-element))
    :label "Review Lightning Settings"}])

(defn payment-hash-field [{:keys [value]}]
  [:> mui/TextField
   {:label "Payment Hash"
    :value @value
    :fullWidth true
    :margin "normal"
    :on-change (on-change-fn value)}])

(defn private-key-field [{:keys [value]}]
  [:> mui/TextField
   {:label "Private Key"
    :value @value
    :fullWidth true
    :margin "normal"
    :on-change (on-change-fn value)}])


(defn funding-request-card []
  (let [address (reagent/atom bolt11/default-address)
        amount (reagent/atom "")
        memo (reagent/atom "")
        payment-hash (reagent/atom bolt11/test-payment-hash)
        timestamp (reagent/atom (js/Date.now))
        invoice {:address address
                 :amount amount
                 :memo memo
                 :payment-hash payment-hash
                 :timestamp timestamp}
        shown (reagent/atom false)
        private-key (reagent/atom bolt11/test-private-key)
        config {:private-key private-key}
        feedback (reagent/atom "Sending funding request!")
        encoded-invoice (reagent/atom bolt11/invoice-template-encoded)]
    (fn []
      (let [content {:text (str @memo "\n" @encoded-invoice)}]
        [ui/card
           [ui/card-header
            {:title "Payment Request (experimental)"
             :subheader "Receive funds through the Lightning Network"
             :avatar (-> [:> mui/Avatar
                          [:> LightningIcon
                            {:style {:color "yellow"}}]]
                         reagent/as-element)}]
           [ui/card-content
            [ui/grid {:container true}
             [ui/grid {:item true :xs 12 :sm 6}
               [:> mui/FormControl
                 [amount-field {:value amount}]
                 [memo-field {:value memo}]]]
             [ui/grid {:item true :xs 12 :sm 6}
              [:div {:style {:margin-left "1em"}}
                [qrcode-area {:invoice invoice
                              :config config}]]]]]
           [ui/card-content
            [:> mui/Card
             #_
             [ui/card-header
              {:title "Lightning Hash"}]
             [ui/card-content
              [invoice-area {:invoice invoice
                             :config config}]]]]
           [ui/card-content
             [show-button {:value shown}]
             (if @shown
              [:> mui/FormControl {:full-width true}
               [payment-hash-field {:value payment-hash}]
               [private-key-field {:value private-key}]])]]))))
