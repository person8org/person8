(ns app.lib.bolt11
  (:require
   ["bolt11" :as bolt11]))

;; IMPORTANT: Decoded invoices may open for injection attacks...
;; See https://github.com/lightningnetwork/lightning-rfc/blob/master/11-payment-encoding.md#security-considerations-for-payment-descriptions

(def default-address "2MvznU28V9Zzi35p9m1aSB4qmvMZQGLVHt8") ; from http://lnd.fun/newinvoice

(def test-private-key "e126f68f7eafcc8b74f54d269fe206be715000f94dac067d1c04a8ca3b2db734")

(def test-payment-hash "56508716f097b609ad53c37a84f56f924a9625937929dba4fc06f6052c101068") ;; bogus/generated

(def invoice-template-encoded
  "lntb1u1pw0eymcpp5lv0peqg97a7a78qrawtlfjpyk5t0859890k76f9yaqx8qm7s2lwqdqcf35hv6twvusx27rsv4h8xetncqzpg664s6xjgkjhf5at0es52p3pfallkfglj3js5332yk53w0jmetma9h69esrlg03wx7ksvll6amt5kzgjx0gn238vazjzm4mer7lpwg7sqma2s7q")

(def invoice-template-decoded-js
  (memoize
   (fn []
     (bolt11/decode invoice-template-encoded))))

#_
(invoice-template-decoded-js)

(defn mock-encode-invoice [{:keys [address amount memo timestamp]}]
  ; https://www.npmjs.com/package/bolt11

  (->
   (js/Object.assign
    #js{}
    #js{:coinType "testnet"
        :address address
        :millisatoshis (* amount 1000)
        :description memo
        :timestamp timestamp}
     (invoice-template-decoded-js))
   (bolt11/encode)
   (.-paymentRequest)))

(defn encode-invoice [{:keys [private-key address amount memo payment-hash timestamp tags]
                       :or {payment-hash test-payment-hash
                            memo ""}}]
  ; https://www.npmjs.com/package/bolt11
  (bolt11/encode
   (js/Object.assign
    #js{}
    #js{:coinType "testnet"
        :address address
        :satoshis amount
        :timestamp timestamp
        :tags (clj->js
               (or tags
                   [#js{:tagName "payment_hash"
                        :data payment-hash}
                    #js{:tagName "description"
                        :data memo}]))})))

(defn sign [encoded private-key]
    (bolt11/sign encoded private-key))

(def test-payment-hash-tag
  #js{"tagName" "payment_hash",
      "data" "0001020304050607080900010203040506070809000102030405060708090102"})

#_
(->
 (encode-invoice
  {:address default-address
   :amount 200
   :memo "Need funds asap"
   :timestamp (js/Date.now)
   :tags [test-payment-hash-tag]})
 (sign test-private-key)
 (.-paymentRequest))
