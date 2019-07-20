(ns app.lib.styling
  (:require
   [taoensso.timbre :as timbre]
   [reagent.core :as reagent]
   ["@material-ui/core/styles" :as styles
    :refer [makeStyles withTheme]]))

;; Reagent integration of react styling hooks (in-progress)
;; Alternative names: styles; theme;

(defn with-theme [f]
  (-> (withTheme (fn [theme]
                   (reagent/as-element
                     (f theme))))
      (reagent/adapt-react-class)))

(defn mui-styles [f]
  (styles/makeStyles (fn [theme] (clj->js (f theme)))))

(def use-styles (mui-styles (fn [theme]
                              {:grid-bg {:padding (.spacing theme 1)}})))

(defn Block [props extra]
  ;; React function component aka hook - Can't use Ratoms here!
  (let [; [count set-count] (react/useState 0)
        classes (js->clj (use-styles) :keywordize-keys true)]
    (timbre/debug "Canvas with class name:" classes)
    (reagent/as-element
     [:div {:class-name (:grid-bg classes)}
       (.-children props)])))

(def block (reagent/adapt-react-class Block))

#_
(def page
  (let [component (reagent/as-element page_)]
    (fn [props]
      (->
       (withTheme (fn [theme]
                    (timbre/debug "with theme" (js->clj theme))
                    component))
       #_(reagent/adapt-react-class)
       (vector props)))))
