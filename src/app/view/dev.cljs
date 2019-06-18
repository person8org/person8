(ns app.view.dev
  (:require
   [taoensso.timbre :as timbre]
   ["@material-ui/core" :as mui]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [app.events]))


"Experimental features and development tools"

(defn ppr-str [data]
  "Like pr-str but pretty print with indents"
  (with-out-str
    (cljs.pprint/pprint data)))

(defn basic-card [{:keys [title]} & body]
  [:> mui/Card
   [:> mui/CardHeader {:title title}]
   [:> mui/CardContent (into [:<>] body)]])


(defn data-table [data]
  (if (coll? data)
    [:> mui/Table {:padding "dense"}
     [:> mui/TableBody
      (for [[k v] data]
        ^{:key k}
        [:> mui/TableRow {:style {:max-width "100%"}}
         [:> mui/TableCell {:style {:vertical-align "top"
                                    :font-weight "bolder"}}
          (str k)]
         [:> mui/TableCell
          [:pre [:code (ppr-str v)]]]])]]
    [:pre
     [:code
      (ppr-str data)]]))


(defn user-profile-card [{:keys [user-data]}]
  [:> mui/Card
   [:> mui/CardHeader {:title "User Profile"}]
   [:> mui/CardContent
    [data-table (js->clj @user-data)]]])


(def db (rf/subscribe [:db]))

(defn state-inspector []
  [basic-card {:title "State"}
   [data-table @db]])

(rf/reg-sub
 :debug
 (fn [{:keys [debug] :as db} [_ & query]]
   (if (some? debug) debug false)))

(def debug-sub (rf/subscribe [:debug]))

(rf/reg-event-db
   :debug
   (fn [{:keys [status] :as db} [_ & [change]]]
     (assoc db :debug (if (some? change) change (not status)))))

(defn debug [& [status]]
  (timbre/info status (some? status))
  (let [status (if (some? status) status (not @debug-sub))]
    (rf/dispatch [:debug status])
    status))

(when-not (exists? js/debug)
  (set! js/debug debug))
