(ns app.events
  (:require
   [taoensso.timbre :as timbre]
   ["blockstack" :as blockstack  ;; temporary
    :refer [UserSession]]
   [app.state :as state
    :refer [app-state]]
   [re-frame.core :as rf]
   [mount.core :refer [defstate]]))

(rf/reg-event-db
 :initialize
 (fn [db [_ initial]]
   (timbre/debug "DB:" db)
   (if (empty? db) initial db)))

(rf/reg-sub
 :db
 (fn [db [_ & query]]
   db))

(rf/reg-sub
 :debug
 (fn [{:keys [debug] :as db} [_ & query]]
   (if (some? debug) debug false)))

(rf/reg-event-db
   :pane
   (fn [db [_ mode]]
     (timbre/info "Change pane:" mode)
     (assoc db :pane mode)))

(rf/reg-sub
 :pane
 (fn [db query]
   (get db :pane)))

(rf/reg-event-db
   :count
   (fn [db [_ f]]
     (assoc db :count
            (f (get db :count 0)))))

(rf/reg-sub
 :count
 (fn [db query]
   (get db :count 0)))

(defn increment
  [event]
  (.preventDefault event)
  (rf/dispatch [:count inc])
  (swap! app-state update-in [:count] inc))

(defn decrement
  [event]
  (.preventDefault event)
  (rf/dispatch [:count dec])
  (swap! app-state update-in [:count] dec))

(rf/reg-event-db
 :user-session
 (fn [db [_ value]]
   (assoc db :user-session value)))

(rf/reg-sub
 :user-session
 (fn [db query]
   (get db :user-session)))

(rf/reg-sub
 :user-data
 (fn [{:keys [user-session user-data] :as db} query]
   (or
    user-data
    ;; ### hmmm
    (if-let [data (and user-session
                      (.loadUserData user-session))]
      (clj->js data)))))

(rf/reg-event-db
 :user-data
 (fn [db [_ value]]
   (assoc db :user-data value)))

(rf/reg-sub
 :signed-in
 (fn [db query]
   ;; ## TODO: avoid repeated calls?
   (if-let [session (get db :user-session)]
     (.isUserSignedIn session))))

(rf/reg-event-db
 :sign-user-out
 (fn [{:keys [user-session] :as db} [_ query]]
   (timbre/debug "sign-user-out")
   ;; ## TODO: eliminate side effect
   (let [redirect-url nil]
     (if user-session
       (.signUserOut user-session redirect-url)
       (blockstack/signUserOut redirect-url)))
   db))

(rf/reg-event-db
 :sign-user-in
 (fn [{:keys [user-session] :as db} [_ query]]
   (if user-session
     (.redirectToSignIn user-session)
     (blockstack/redirectToSignIn))))

(rf/reg-event-db
 :file
 (fn [{:as db} [_ name content]]
   (assoc-in db [:file name] content)))

(rf/reg-sub
 :file
 (fn [db query]
   (-> (get db :file)
       (get query))))

(defn parse-json [s]
  (.parse js/JSON s))

(defn json-stringify [j]
  (.stringify js/JSON j))

(rf/reg-event-db
 :load-file
 (fn [{:keys [user-session] :as db}
      [_ name options {:keys [process]}]]
   (timbre/info "Load file:" name options)
   (-> (.getFile user-session name (clj->js options))
       (.then (fn [content]
                (-> [:file name ((or process identity) content)]
                    (rf/dispatch)))))
   db))

#_
(rf/dispatch [:load-file "me.json" {:decrypt false}
              {:process (comp js->clj parse-json)}])

(rf/reg-event-db
 :put-file
 (fn [{:keys [user-session file] :as db}
      [_ name options]]
   (.putFile user-session name
             (-> (get file name) clj->js json-stringify)
             (clj->js options))
   db))

#_
(rf/dispatch [:file "hello.json" {:hello "world"}])
#_
(rf/dispatch [:put-file "hello.json" {:encrypt false}])
#_
(rf/dispatch [:file "hello.json" {:hello "null!!!"}])
#_
(rf/dispatch [:load-file "hello.json" {:decrypt false}
              {:process (comp js->clj parse-json)}])

(rf/reg-sub
 :board
 (fn [db [_ query]]
   (get db :board)))

(rf/reg-event-db
 :select
 (fn [{:as db} [_ item]]
   (update db :board
           (fn [items]
             (map #(assoc % :selected (= % item))
                  items)))))

(rf/reg-event-db
 :replace-image
 (fn [db [_ {:keys [id] :as item}
            {:keys [url] :as file}]]
   (timbre/debug "Should replace image:" item file)
   (assoc db :board
         (map
           (fn [li]
             (if (= id (:id li))
               (assoc li :image url)
               li))
           (:board db)))))

(rf/reg-event-db
 :paste
 (fn [{:as db} [_ {:as item}]]
   (update db :board conj item)))

(rf/reg-event-db
 :drag
 (fn [{:as db} [_ [status & args]]]
   (timbre/debug "Drag:" status args)
   (assoc db :drag status)))

(rf/reg-sub
 :drag
 (fn [db [_ query]]
   (get db :drag)))

(rf/reg-sub
 :product
 (fn [db [_ query]]
   (get db :product)))

(def initial-db
  {:debug true
   :product {:name "Person8"}
   :board state/profile-fields})

(rf/dispatch [:initialize initial-db])
