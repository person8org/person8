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
  "Define a react hook with custom styles for mui on toplevel that are injected into the css of the page.
   Used to generate a style that is based on the current theme.
   Returns a function that when evaluated provides a map to mui classnames for the styles."
  (let [styles (styles/makeStyles (fn [theme] (clj->js (f theme))))]
    (fn [] (js->clj (styles) :keywordize-keys true))))

#_ ;; example
(def use-styles (mui-styles (fn [theme]
                              {:grid-bg {:padding (.spacing theme 1)}})))

(defn Block [props extra]
  "React function component using mui-styles"
  ;; Can't use Ratoms here!
  ;; Can take a function as children, in which case it is called with class
  (let [use-styles (.-muiStyles props) ; hook
        ensure-seq #(if (coll? %) % (vector %))
        class-list (map keyword
                        (-> (.-className props)
                            (js->clj)
                            (ensure-seq)))
        classes (use-styles)
        children (.-children props)]
    (reagent/as-element
       [:div {:class-name (->> (map #(get classes %) class-list)
                               (clojure.string/join " "))}
        (if (fn? children)
          (children classes)
          children)])))


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
