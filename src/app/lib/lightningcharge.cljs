(ns app.lib.lightningcharge
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<! chan close! alts! timeout put!]]
   [cljs-http.client :as http]
   [taoensso.timbre :as timbre]
   ["lightning-charge-client" :as lightning-charge-client]))

;; https://www.npmjs.com/package/lightning-charge-client

;; http://api-token:terjenorderhaug@localhost:9112

(def default-endpoint #_"http://localhost:9112" "http://127.0.0.1")
(def api-token "terjenorderhaug")



(defn fetch-info1 []
  (go (let [response (<! (http/get "http://api-token:terjenorderhaug@127.0.0.1:9112/info"
                                   {:basic-auth {:username "api-token" :password api-token}
                                    :with-credentials? false}))]
        (prn (:status response))
        (prn (:body response)))))

#_
(fetch-info1)


(defn fetch-info-ok [] ;; OK200 from server but fails cors
  (go (let [response (<! (http/get "http://127.0.0.1:9112/info"
                                   {:with-credentials? true}))]
        (prn response)
        (timbre/debug "=>" response))))


#_
(fetch-info-ok)


(defn fetch-info []
  ; triggers OPTIONS request from browser, which lightning-charge seems unprepared for
  (go (let [response (<! (http/get "http://localhost:9112/info"
                                   {:basic-auth {:username "api-token"
                                                 :password api-token}
                                    :with-credentials? false}))]
        (prn response)
        (timbre/debug "=>" response))))

#_
(fetch-info)


(defn promise-echo [promise]
  (-> promise
      (.then (fn [result](timbre/info result)))
      (.catch (fn [err](timbre/warn err)))))

(def charge-client
  (do ;memoize
   (fn
     ([] (charge-client default-endpoint api-token))
     ; ([url](lightning-charge-client url))
     ([endpoint api-token]
      (lightning-charge-client endpoint api-token)))))

#_
(-> (charge-client "http://localhost:9112" "terjenorderhaug")
    (.-req))

#_
(-> (charge-client "http://localhost:9112" "terjenorderhaug")
    (.info)
    (promise-echo))


#_
(-> (charge-client "http://api-token:terjenorderhaug@localhost:9112" nil)
    (.info)
    (promise-echo))


#_
(charge-client)

(defn invoice [client {:keys [currency amount] :as arg}]
  (.invoice client (clj->js arg)))

#_
(-> (invoice (charge-client) {:currency "USD" :amount 0.15})
    (promise-echo))
