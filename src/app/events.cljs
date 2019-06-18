(ns app.events
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<! chan close! alts! timeout put!]]
   [clojure.spec.alpha :as s]
   [taoensso.timbre :as timbre]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [app.state :as state]
   [mount.core :refer [defstate]]
   [app.store :as store]
   [app.lib.blockstack]))

(defn log-event [& [label]]
  (rf/->interceptor
    :id      ::log-args
    :before  (fn [context]
               (let [event (get-in context [:coeffects :event])]
                 (timbre/debug (or label "Event:") event)
                 context))))


(rf/reg-event-db
 :initialize
 (fn [db [_ initial]]
   (timbre/debug "DB:" db)
   (if (empty? db) initial db)))

(rf/reg-event-db
   :pane
   [(log-event "Change pane:")]
   (fn [db [_ mode]]
     (assoc db :pane mode)))

(rf/reg-sub
 :pane
 (fn [db query]
   (get db :pane)))

(rf/reg-sub
 :user-name
 (fn [{:keys [user-data] :as db} query]
   (if user-data
     (or (:username user-data)
         (.-username user-data)))))

(rf/reg-event-db
 :file
 (fn [{:as db} [_ name content]]
   (assoc-in db [:file name] content)))

(rf/reg-sub
 :file
 (fn [db query]
   (-> (get db :file)
       (get query))))

(defn ^{:foo abcd} parse-json [s]
  (.parse js/JSON s))

(defn json-stringify [j]
  (.stringify js/JSON j))

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
  "DB with the matching board item updated by the function f"
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
 [(log-event "Should replace image:")]
 (fn [db [_ {:keys [id] :as item}
            {:keys [url] :as file}]]
   (update-board db id assoc :image url)))

(rf/reg-fx
 :store/store-image
 (fn [{:keys [user-session path data]}]
   (store/store-image {:user-session user-session :path path} data)
   nil))

(rf/reg-event-fx
 :paste
 [(log-event "Paste:")]
 (fn [{{:keys [user-session board] :as db} :db :as fx}
      [_ {:keys [url data path] :as item}]]
   (let [[selected] (filter :selected board)]
     (timbre/debug "Paste into:" selected)
     (let [store? (not path)
           path (or path (str (random-uuid)))
           item (assoc item :path path)]
       (assoc fx
         :store/store-image
         (when store?
           {:user-session user-session :path path :data data})
         :db
         (if selected
           (update-board db (:id selected) assoc :image url :path path)
           (update db :board conj item)))))))

(rf/reg-event-fx
 :user/drop
 [(log-event)]
 (fn [{{:keys [user-session] :as db} :db :as  fx}
      [_ {:keys [id] :as item}{:keys [url data] :as file}]]
   (let [path (str (random-uuid))]
     (assoc fx
       :store/store-image {:user-session user-session :path path :data data}
       :db (update-board db id assoc :image url :path path)))))

(rf/reg-event-db
 :drag
 [(log-event "Drag:")]
 (fn [{:as db} [_ [status & args]]]
   (assoc db :drag status)))

(rf/reg-sub
 :drag
 (fn [db [_ query]]
   (get db :drag)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; APP ROUTING EVENTS

(rf/reg-event-fx
 :app/enter
 [(log-event)]
 (fn [{:as db} [_ {:as item}]]
   {}))

(rf/reg-event-fx
 :app/exit
 [(log-event)]
 (fn [{:as fx} [_ {:as item}]]
   {:dispatch [:sign-user-out]}))

(rf/reg-event-fx
 :app/signin
 [(log-event)]
 (fn [{:as fx} [_ {:as item}]]
   {:dispatch [:sign-user-in]}))

(defn change-board-for-demo [items]
  (map
   (fn [item]
     (if (= (:id item) "social-security-card")
       (assoc item :icon nil)
       item))
   items))

(rf/reg-event-fx
 :app/demo
 [(log-event)]
 (fn [{:keys [db] :as fx} [_ {:as item}]]
   {:db
    (update-in db [:board] change-board-for-demo)}))


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
 [(log-event)]
 (fn [{:as db} [_ status]]
   (assoc db :requesting-funds (if (some? status) status true))))

(rf/reg-event-db
   :state/board
   [(log-event)]
   (fn [{:as db} [_ board]]
     (assoc db :board board)))

(defn call-resolved-images [user-session index dispatch]
   (dispatch (store/merge-defaults index))
   (doseq [{:keys [id path] :as entry} index]
     (go-loop [image (<! (store/load-image {:user-session user-session
                                            :path path}))]
       (if-let [url (and image (.-url image))]
         (rf/dispatch [:replace-image {:id id} {:url url}])
         (timbre/warn "No image loaded for:" path)))))

(rf/reg-event-fx
   :state/loaded
   [(log-event)]
   (fn [{{:keys [user-session] :as db} :db :as fx}
        [_ index]]
     (call-resolved-images user-session index
                                 #(rf/dispatch [:state/board %]))
     {}))

(rf/reg-event-fx
 :state/load ;; load all content from files at startup
 [(log-event)]
 (fn [{{:keys [user-session] :as db} :db :as fx} [_]]
   {:pre [user-session]}
   {:blockstack/list-files {:user-session user-session}
    :blockstack/load-file
    (assoc store/data-storage
            :user-session user-session
            :dispatch #(rf/dispatch [:state/loaded %]))}))

(rf/reg-event-fx
   :stored
   [(log-event)]
   (fn [{:as fx} [_ content]]
     {}))

(rf/reg-event-fx
 :state/store
 [(log-event)]
 (fn [{{:keys [user-session] :as db} :db :as fx} [_ index]]
   {:pre [user-session]}
   {:blockstack/store-file
    (assoc store/data-storage
           :content index
           :user-session user-session
           :dispatch #(rf/dispatch [:stored %]))}))


;;;;;


(defonce board-sub (rf/subscribe [:board]))

(defn index-subscribe []
   (let [board @board-sub]
     (timbre/debug "Potential board change:" board)
     (if-not (= board state/profile-fields)
       (store/encode-index board))))

(defonce index-sub (reagent.ratom/reaction (index-subscribe)))

#_
(defstate index-sub
  :start (reagent.ratom/reaction (index-subscribe))
  :end (reagent/dispose! index-sub))
#_
(defstate index-sub
  :start (reagent/track! index-subscribe)
  :end (reagent/dispose! index-sub))

(defn on-content-change []
  (let [index @index-sub]
    (timbre/debug "Potential index change:" index)
    (when (some? index)
      (rf/dispatch [:state/store index]))))

(defstate user-content-track
  :start (reagent/track! on-content-change)
  :end (reagent/dispose! user-content-track))

;;;;;;

(def user-data-sub (rf/subscribe [:blockstack/user-data]))

(defn on-user-data-change []
  (let [user-data @user-data-sub]
    (timbre/debug "Tracked User Data:" user-data)
    (if (some? user-data)
      (rf/dispatch [:state/load])
      (timbre/warn "No user data"))))

(defstate user-data-track
  :start (reagent/track! on-user-data-change)
  :end (reagent/dispose! user-data-track))

#_ ; ;; ## not triggered but why?
(defstate load-user-state
  :start (reagent/wrap @user-data-sub
                       #(if (some? %)
                          (rf/dispatch [:load-user])
                          (timbre/warn "No user data"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def initial-db
  {:product {:name "Person8"}})

(rf/dispatch [:initialize initial-db])

#_
(rf/dispatch [:debug true])
