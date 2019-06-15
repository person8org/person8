(ns app.view.invoice
  (:require
   [taoensso.timbre :as timbre]
   ["@material-ui/core" :as mui]
   ["@material-ui/icons/FlashOn" :default MoneyIcon]
   ["@material-ui/icons/EnhancedEncryption" :default EncryptIcon]
   ["bolt11" :as bolt11]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [app.view.reagent-mui :as ui]
   [app.view.share :as share-view]))

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

(def default-address "2MvznU28V9Zzi35p9m1aSB4qmvMZQGLVHt8") ; from http://lnd.fun/newinvoice

(def invoice-template-encoded
  "lntb1u1pw0eymcpp5lv0peqg97a7a78qrawtlfjpyk5t0859890k76f9yaqx8qm7s2lwqdqcf35hv6twvusx27rsv4h8xetncqzpg664s6xjgkjhf5at0es52p3pfallkfglj3js5332yk53w0jmetma9h69esrlg03wx7ksvll6amt5kzgjx0gn238vazjzm4mer7lpwg7sqma2s7q")

(def invoice-template-decoded-js
  (memoize
   (fn []
     (bolt11/decode invoice-template-encoded))))

(defn encode-invoice [{:keys [address amount memo timestamp]}]
  ; https://www.npmjs.com/package/bolt11
  (bolt11/encode
   (js/Object.assign
    #js{}
    #js{:coinType "testnet"
        :address @address
        :satoshis @amount
        :description @memo
        :timestamp @timestamp}
    (invoice-template-decoded-js))))

#_
(encode-invoice { :address (reagent/atom default-address)
                  :amount (reagent/atom 200)
                  :memo (reagent/atom "Need funds asap")
                  :timestamp (reagent/atom (js/Date.now))})

(defn hash-field [{:as invoice}]
  (case :empty
    :bolt11
    (let [hash (reagent.ratom/make-reaction
                #(try (encode-invoice invoice)))]
      [:div @hash])
    :table
    (->> invoice
         (map (fn [[k v]] [:div (name k) ":" (deref v)]))
         (into [:div]))
    :empty [:div]))

(defn funding-request-card []
  (let [address (reagent/atom default-address)
        amount (reagent/atom "")
        memo (reagent/atom "")
        timestamp (reagent/atom (js/Date.now))
        feedback (reagent/atom "Sending funding request!")
        encoded-invoice (reagent/atom invoice-template-encoded)]
    (fn []
      (let [content {:text (str @memo "\n" @encoded-invoice)}]
        (if @requesting-funds
         [ui/list-item
          [ui/card
           [ui/card-header
            {:title "Payment Request"
             :subheader "Receive funds through the Lightning Network"
             :avatar (-> [:> mui/Avatar
                          [:> MoneyIcon
                            {:style {:color "yellow"}}]]
                         reagent/as-element)}]
           [ui/card-content
            [:> mui/FormControl
             [amount-field {:value amount}]
             [memo-field {:value memo}]
             [hash-field {:address  address
                          :amount amount
                          :memo memo
                          :timestamp timestamp}]]
            [ui/card-actions
             [share-view/share-option
              {:id "payment-request"
               :content content
               :label "request for funds"
               :feedback feedback}]]]]])))))
