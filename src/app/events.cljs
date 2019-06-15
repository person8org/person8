(ns app.events
  (:require
   [taoensso.timbre :as timbre]
   [re-frame.core :as rf]
   [app.state :as state]
   [mount.core :refer [defstate]]
   [app.lib.blockstack]))

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

(rf/reg-sub
 :user-name
 (fn [{:keys [user-data] :as db} query]
   (if user-data
     (.-username user-data))))

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

(defn update-board [{:keys [board] :as db} id f & args]
  "DB with the mathing board item updated by the function f"
  (assoc db :board
         (map
          (fn [li]
            (if (= id (:id li))
              (do
                (timbre/debug "Update selected:" li)
                (apply f li args))
              li))
          board)))

(rf/reg-event-db
 :replace-image
 (fn [db [_ {:keys [id] :as item}
            {:keys [url] :as file}]]
   (timbre/debug "Should replace image:" item file)
   (update-board db id assoc :image url)))

(rf/reg-event-db
 :paste
 (fn [{:keys [board] :as db}
      [_ {:keys [url] :as item}]]
   (timbre/info "Paste:" item)
   (let [[selected] (filter :selected board)]
     (timbre/debug "Paste into:" selected)
     (if selected
       (update-board db (:id selected) assoc :image url)
       (update db :board conj item)))))

(rf/reg-event-db
 :drag
 (fn [{:as db} [_ [status & args]]]
   (timbre/debug "Drag:" status args)
   (assoc db :drag status)))

(rf/reg-sub
 :drag
 (fn [db [_ query]]
   (get db :drag)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; APP ROUTING EVENTS

(rf/reg-event-fx
 :app/enter
 (fn [{:as db} [_ {:as item}]]
   (timbre/info "Enter App")
   {}))

(rf/reg-event-fx
 :app/exit
 (fn [{:as db} [_ {:as item}]]
   (timbre/info "Exit App")
   {:dispatch [:sign-user-out]}))

(rf/reg-event-fx
 :app/signin
 (fn [{:as db} [_ {:as item}]]
   (timbre/info "Signin")
   {:dispatch [:sign-user-in]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(rf/reg-sub
 :product
 (fn [db [_ query]]
   (get db :product)))

(rf/reg-sub
 :requesting-funds
 (fn [db [_]]
   (get db :requesting-funds)))

(rf/reg-event-db
 :request-funds
 (fn [{:as db} [_ status]]
   (timbre/debug "Request Funds")
   (assoc db :requesting-funds (if (some? status) status true))))


(def initial-db
  {:debug false
   :product {:name "Person8"}
   :board state/profile-fields})

(rf/dispatch [:initialize initial-db])
