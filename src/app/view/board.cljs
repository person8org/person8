(ns app.view.board
  (:require
    [taoensso.timbre :as timbre]
    ["@material-ui/core" :as mui]
    ["@material-ui/icons/FileCopy" :default CopyIcon]
    ["@material-ui/core/Grid" :default Grid]
    ["@material-ui/core/Hidden" :default Hidden]
    ["react" :as react]
    [re-frame.core :as rf]
    [reagent.core :as reagent]
    [app.lib.styling :as styling]
    [app.view.info :as info]
    [app.view.invoice :as invoice-view]
    [app.view.identity
     :refer [identity-card]]
    [app.view.entry :as entry
     :refer [profile-card]]))

(def debug (rf/subscribe [:debug]))

(defmulti pane (fn [{:keys [stage] :as session}]
                  (if stage [@stage])))

(defn error-boundary [{:as props} & children]
  "Catch errors to limit the effect on rendering"
  (let [error-status (reagent/atom nil)]
    (reagent/create-class
     {:get-derived-state-from-error
      (fn [error]
        #js{:hasError true})
      :component-did-catch
      (fn [error info]
        (timbre/warn error info)
        (reset! error-status error))
      :reagent-render
      (fn [& children]
        (if-not @error-status
          (into [:<>] children)
          [:div.error-boundary]))})))

(defn board-listing [{:keys [items]}]
  (timbre/debug "Show as list")
  (into
    [:> mui/List]
    (for [{:keys [id selected] :as item} items]
       ^{:key id}
       [:> mui/ListItem
        {:selected (boolean selected)}
        [entry/profile-panel {:item item}]])))

(defn board-grid [{:keys [items]}]
  (timbre/debug "Show as grid" #_(js-keys classes))
  [styling/block {}
         (into
          [:> Grid {:container true :spacing 1}]
          (for [{:keys [id selected] :as item} items]
            ^{:key id}
           [:> Grid {:item true :xs 12 :sm 6 :md 4
                     :classes {:root "board-grid-item"}}
             #_{:selected (boolean selected)}
             [error-boundary {}
              [entry/pick-zone {:item item
                                :style {:width "100%"
                                        :height "100%"}}
               [entry/profile-card {:item item}]]]]))])

(def requesting-funds (rf/subscribe [:requesting-funds]))

(def open-dialog (rf/subscribe [:app/open-dialog]))

(defn modal-dialog []
  "Placeholder for modal dialogs"
  (let [on-dialog-close #(rf/dispatch [:app/open-dialog nil])
        [name & args] @open-dialog]
    [:> mui/Dialog {:class-name "mui-modal-dialog"
                    :open (some? name)
                    :on-close on-dialog-close}
     (case name ;; # use hide/show for faster response?
       :invoice-view/funding-request-card
       [invoice-view/funding-request-card]
       :entry/expanded-view
       (apply vector entry/expanded-view args)
       :info/view
       (apply vector info/view args)
       nil)]))

(defn board-pane [items]
  [error-boundary {}
       [modal-dialog]
       [:> mui/Hidden {:sm-up true}
        [board-listing {:items items}]]
       [:> mui/Hidden {:xs-down true}
        [board-grid {:items items}]]])

(defmethod pane :default [{:keys [mobile profile] :as session}]
  [board-pane (:fields @profile)])
