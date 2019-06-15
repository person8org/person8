(ns app.lib.blockstack
  (:require
   [taoensso.timbre :as timbre]
   ["blockstack" :as blockstack
    :refer [UserSession]]
   [re-frame.core :as rf]))


(rf/reg-event-fx
 :blockstack/init
 (fn [{{:keys [user-session] :as db} :db :as fx} [_]]
   (if (some? user-session)
     (timbre/info "Blockstack user session already established")
     (let [user-session (new UserSession)]
       (timbre/info "Blockstack user session created")
       ;; ## TODO: Factor as fx
       (when (and
              (not (.isUserSignedIn user-session))
              (.isSignInPending user-session))
         (timbre/info "Blockstack signin is pending")
         (-> (.handlePendingSignIn user-session)
             (.then (fn [user-data]
                      (rf/dispatch [:user-data user-data])))
             (.catch (fn [err]
                       (timbre/warn "Failed signing in:" err)))))
       {:dispatch [:user-session user-session]}))))


(defn init-blockstack []
  (rf/dispatch-sync [:blockstack/init]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SESSION

(rf/reg-event-db
 :user-session
 (fn [db [_ value]]
   (timbre/debug "Set user session:" value)
   (assoc db :user-session value)))

(rf/reg-sub
 :user-session
 (fn [{:keys [user-session] :as db} query]
   (timbre/debug "User Session:" user-session)
   user-session))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; AUTHENTICATION / SETUP

(rf/reg-sub
 :signed-in-status
 (fn [{:keys [signed-in-status user-session] :as db} query]
   (if (some? signed-in-status)
     signed-in-status
     (if user-session
       (.isUserSignedIn user-session)
       (timbre/warn "No user session established yet")))))

(rf/reg-fx
 :blockstack/sign-user-in
 (fn [{:keys [user-session redirect-uri]}]
   (if user-session
     (.redirectToSignIn user-session redirect-uri)
     (timbre/error "Missing user session"))))

(rf/reg-event-fx
 :sign-user-in
 (fn [{{:keys [user-session] :as db} :db :as fx}
      [_ {:keys [redirect-uri]
          :or {redirect-uri nil}}]]
   (timbre/info "Signing in..." user-session)
   {:db (assoc db :signed-in-status nil) ;; pending
    :blockstack/sign-user-in
    {:user-session user-session
     :redirect-uri redirect-uri}}))

(rf/reg-event-fx
 :signed-in
 (fn [{{:keys [user-session data-storage]:as db} :db :as fx}
      [_ query]]
   {:pre [(some? user-session)]}
   (timbre/info "Signed in..." user-session)
   {:db (assoc db :signed-in-status true)
    :blockstack/load-file
    (assoc data-storage
     :dispatch #(rf/dispatch [:loaded-content %]))}))

(rf/reg-fx
 :blockstack/sign-user-out
 (fn [{:keys [user-session redirect-url]}]
   (timbre/debug "Blockstack sign-user-out" redirect-url)
   (if user-session
     (.signUserOut user-session redirect-url)
     (do ;; keep legacy as fallback (useful during debug)
       (timbre/warn "Missing user session")
       (blockstack/signUserOut user-session redirect-url)))))


(rf/reg-event-fx
 :sign-user-out
 (fn [{{:keys [user-session] :as db} :db :as fx} [_ query]]
   (timbre/debug "Sign User Out" user-session)
   {:db (assoc db :signed-in-status false)
    :blockstack/sign-user-out
      {:user-session user-session}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; BLOCKSTACK USER DATA

(def blockstack-load-user-data
  ;; Assumes that user data doesn't change...
  (memoize
   (fn [user-session]
     {:pre [(some? user-session)]}
     (if-let [data (.loadUserData user-session)]
        (js->clj data)))))

(rf/reg-sub
 :user-data
 (fn [{:keys [user-session user-data] :as db} query]
   (timbre/debug "User Data:" user-data user-session)
   (or
    user-data
    ;; ## Fix: May reload data...
    (if user-session
      (blockstack-load-user-data user-session)))))

(rf/reg-event-db
 :user-data
 (fn [db [_ value]]
   (timbre/debug "Set user data:" value)
   (assoc db :user-data value)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; BLOCKSTACK FILE DATA

(rf/reg-fx
 :blockstack/load-file
 (fn [{:keys [user-session path options reader dispatch]
       :or {reader identity}}]
   (timbre/debug "Load blockstack file:" path)
   (-> (.getFile user-session path options)
       (.then (fn [content]
                (-> (or content "")
                    (reader)
                    (dispatch))))
       (.catch (fn [err]
                (timbre/warn "Failed loading blockstack file:" err))))))


(rf/reg-fx
 :blockstack/store-file
 (fn [{:keys [user-session path content options writer dispatch]
       :or {writer identity}}]
   (timbre/debug "Store blockstack file:" path)
   (-> (.putFile user-session path (writer content)(clj->js options))
       (.finally (fn [] (if dispatch (dispatch)))))))

(rf/reg-event-db
 :loaded-content
 (fn [{:keys [] :as db} [_ content]]
   (timbre/info "Loaded content:" content)
   (assoc db :content content)))

(rf/reg-event-fx
 :change-content
 (fn [{{:keys [content data-storage] :as db} :db :as fx} [_ new-content]]
   (timbre/info "Change content:" new-content)
   {:db
    (if (not= content new-content)
      (assoc db :content content)
      db)
    :blockstack/store-file
    (assoc data-storage
           :dispatch nil)}))
