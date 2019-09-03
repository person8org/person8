(ns app.routing
  (:require
   [taoensso.timbre :as timbre]
   [reitit.core :as router]
   [reitit.frontend :as frontend]
   [reitit.frontend.easy :as easy]
   [reitit.coercion :as coercion]
   [reitit.coercion.spec :as rspec]
   [re-frame.core :as rf]
   [mount.core :refer [defstate]]))

(def routes
    [["/" {:name :home}]
     ["/signin" {:name :signin
                 :dispatch :app/signin}]
     ["/exit" {:name :exit
               :dispatch :app/exit}]
     ["/enter" {:name :enter
                :dispatch :app/enter
                :replace-state [:home]}]
     ["/reset" {:name :reset
                :dispatch :app/reset!}]
     ["/demo" {:name :demo
               :dispatch :app/demo}]])

(rf/reg-fx
 ::navigate!
 (fn [k params query]
   (easy/push-state k params query)))

(rf/reg-fx
 ::replace-state
 (fn [[k & [params query]]]
   (when k
     (easy/replace-state k params query))))

(rf/reg-event-fx
 ::navigate
 (fn [{:keys [db] :as fx} [_ match]]
   (timbre/debug "Navigate:" match)
   (when match
     (let [name (get-in match [:data :name])
           dispatch (get-in match [:data :dispatch])
           replace-state (get-in match [:data :replace-state])
           params (get-in match [:path-params])
           query (get-in match [:query-params])]
       {::replace-state replace-state
        :dispatch [dispatch {:params params :query query}]}))))

(defn enable-routing []
  (easy/start!
    (frontend/router routes {:data {:coercion rspec/coercion}})
    (fn [new-match]
      (timbre/debug "Route match:" new-match)
      (rf/dispatch [::navigate new-match]))
    ;; set to false to enable HistoryAPI
    ;; if true query args aren't passed on...
    ;; should perhaps be reported as issue to reitit?
    {:use-fragment false}))

#_ ;; has to be after everything else
(defstate routing-state
  :start (enable-routing))
