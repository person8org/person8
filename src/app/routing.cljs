(ns app.routing
  (:require
   [taoensso.timbre :as timbre]
   [accountant.core :as accountant]
   [reitit.core :as router]
   [re-frame.core :as rf]
   [mount.core :refer [defstate]]))


(def routes
  (router/router
    [["/signin" :app/signin]
     ["/exit" :app/exit]
     ["/enter" :app/enter]
     ["/demo" :app/demo]]))


(defn enable-routing []
  (timbre/debug "Enable routing")
  (accountant/configure-navigation!
   {:reload-same-path? true
    :nav-handler
    (fn [path]
      (timbre/info "Navgiate:" path)
      (if-let [match (router/match-by-path routes path)]
        (let [{:keys [data path-params]} match
              name (:name data)]
          (rf/dispatch [name path-params]))))
    :path-exists?
    (fn [path]
      (timbre/debug "Path Exists?" path)
      (if (router/match-by-path routes path)
        true)
      true)}))

(defn disable-routing []
  #_
  (accountant/unconfigure-navigation!))


(defstate routing-state
  :start (enable-routing)
  :end (disable-routing))
