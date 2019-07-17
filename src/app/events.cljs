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

(defn check-and-throw [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

(s/def ::db (s/keys :req-un [::product ::board]
                    :opt-un []))

(def validate-db
  (rf/after (partial check-and-throw ::db)))

(rf/reg-event-db
 :initialize
 [validate-db]
 (fn [db [_ initial]]
   (timbre/debug "DB:" db)
   (if (empty? db) initial db)))

(rf/reg-event-db
   :pane
   [(log-event "Change pane:")
    validate-db]
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

(rf/reg-sub
 :selected
 (fn [{:as db} [_]]
   (filter :selected (:board db))))

(defn update-board [{:keys [board] :as db} id f & args]
  "DB with the matching board item updated by the function f"
  (assoc db :board
         (map
          (fn [li]
            (if (= id (:id li))
              (do
                (timbre/debug "Update:" li " => " (apply f li args))
                (apply f li args))
              li))
          board)))

(rf/reg-fx
 :store/update-image
 (fn [{:keys [user-session id type data] :as arg}]
   (let [image {:type type
                :data data}]
     (timbre/debug "Update image:" data
                   (js-keys data))
     (store/update-image user-session id image))
   nil))

(rf/reg-event-db
 :replace-image
 [(log-event)
  validate-db]
 (fn [db [_ {:keys [id] :as item}
            {:keys [url path] :as file}]]
   ; replace without storing...
   (update-board db id assoc :image url :path path)))

(rf/reg-event-fx
 ::update-image
 [(log-event)
  validate-db]
 (fn [{{:keys [user-session user-data board] :as db} :db :as fx}
      [_ id {:keys [url data type] :as item}]]
   {:pre [(string? url)]}
   (let []
     {:store/update-image
      {:user-session user-session
       :id id
       :type type
       :data data}
      :db (if id
            (update-board db id assoc :id id :image url)
            (update db :board conj {:id (str (random-uuid))
                                    :image url}))})))

(rf/reg-event-fx
 :user/paste
 [(log-event)
  validate-db]
 (fn [{{:keys [user-session board] :as db} :db :as fx}
      [_ payload]]
   (let [[selected] (filter :selected board)]
     {:dispatch [::update-image (:id selected) payload]})))

(rf/reg-event-fx
 :user/drop
 [(log-event)
  validate-db]
 (fn [{{:keys [user-session] :as db} :db :as  fx}
      [_ {:keys [id] :as item} payload]]
   {:dispatch [::update-image id payload]}))

(rf/reg-event-fx
 :user/upload
 [(log-event)]
 (fn [{{:keys [user-session] :as db} :db :as  fx}
      [_ {:keys [id] :as item} payload]]
   {:dispatch [::update-image id payload]}))

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

(defn redirect-entry [& [path]]
  ;; ## FIX: Use reitit.frontend.easy/href or other library
  (str
   (.. js/window -location -origin)
   "/" path
   (.. js/window -location -search)))

(rf/reg-event-fx
 :app/signin
 [(log-event)]
 (fn [{:as fx} [_ {:as item}]]
   {:dispatch [:sign-user-in {:redirect-uri (redirect-entry "enter")}]}))

(rf/reg-event-fx
 :app/reset!
 [(log-event)
  validate-db]
 (fn [{:keys [db] :as fx} [_ {:as item}]]
   {:db (assoc db :board [])
    :dispatch [:state/store []]}))

(rf/reg-event-fx
 :app/clear-files!
 [(log-event)]
 (fn [{:keys [user-session] :as db} [_]]
   (store/delete-all-files! user-session)
   nil))

(defn change-board-for-demo [items]
  (map
   (fn [item]
     (if (= (:id item) "social-security-card")
       (assoc item :icon nil)
       item))
   items))

(rf/reg-event-fx
 :app/demo
 [(log-event)
  validate-db]
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
 [(log-event)
  validate-db]
 (fn [{:as db} [_ status]]
   (assoc db :requesting-funds (if (some? status) status true))))

(rf/reg-event-db
   :state/board
   [(log-event)
    validate-db]
   (fn [{:as db} [_ board]]
     (assoc db :board board)))

(defn resolve-image [user-session {:keys [id image] :as entry}]
   (timbre/debug "Resolve Image:" entry)
   (go-loop [image (<! (store/load-image {:user-session user-session
                                          :path image}))]
       (if-let [url (and image (.-url image))]
         (rf/dispatch [:replace-image {:id id} {:url url}])
         (timbre/warn "No image loaded for:" id))))

(rf/reg-fx
 :state/load
 (fn [{:keys [user-session] :as arg}]
   (timbre/debug "FX state/load")
   (let [in (store/load-index user-session)]
     (go-loop [entry (<! in)]
       (timbre/info "Loaded:" entry)
       (when (some? entry)
         (resolve-image user-session entry)
         (recur (<! in)))))
   nil))

(rf/reg-event-fx
 :state/load ;; load all content from files at startup
 [(log-event)]
 (fn [{{:keys [user-session] :as db} :db :as fx} [_]]
   {:pre [user-session]}
   {:blockstack/list-files {:user-session user-session}
    :state/load {:user-session user-session}}))

(rf/reg-event-fx
   :stored
   [(log-event)]
   (fn [{:as fx} [_ content]]
     {}))

(rf/reg-event-fx
 :state/store ;; ## moot?
 [(log-event)]
 (fn [{{:keys [user-session] :as db} :db :as fx} [_ index]]
   {:pre [user-session]}
   {:blockstack/store-file
    (assoc store/data-storage
           :content index
           :user-session user-session
           :dispatch #(rf/dispatch [:stored %]))}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def user-data-sub (rf/subscribe [:blockstack/user-data]))

(defn on-user-data-change []
  (let [user-data @user-data-sub]
    #_(timbre/debug "Tracked User Data:" user-data)
    (if (some? user-data)
      (rf/dispatch [:state/load])
      (timbre/warn "Waiting for user data"))))

(defstate user-data-track
  :start (reagent/track! on-user-data-change)
  :end (reagent/dispose! user-data-track))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def initial-db
  {:product {:name "Person8"}
   :board state/profile-fields})

(timbre/info "Initialize state")
(rf/dispatch-sync [:initialize initial-db])
