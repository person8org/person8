(ns app.lib.drop
  (:require
    [taoensso.timbre :as timbre]
    [re-frame.core :as rf]
    [mount.core :refer [defstate]]
    [cljs-drag-n-drop.core :as dnd]
    [app.datatransfer
     :refer [dispatch-paste]]
    [app.lib.url
      :refer [as-url]]))

"Handle drag and drop events from the os, converting between formats"

(defn decode-file [file]
  (let [url (as-url file)]
    {:kind "file"
     :name (.-name file)
     :type (.-type file)
     :size (.-size file)
     :modified (.-lastModified file)
     :data file
     :url  url}))

(defn drop-handler [e files]
  (timbre/debug "Drop Files:" e files)
  (doseq [file files]
    (let [url (as-url file)
          item-type "image/tiff"]
      (dispatch-paste (decode-file file)))))


(defn enable-drop []
  (dnd/subscribe! js/document.documentElement :unique-key
                  {:start (fn [e]
                            (println "d1 start")
                            (rf/dispatch [:drag [:start e]]))
                   :enter (fn [e]
                            (println "d1 enter")
                            (rf/dispatch [:drag [:enter e]]))
                   :drop  (fn [e files]
                            (println "d1 drop")
                            (let [files (js->clj (js/Array.from files))]
                              (drop-handler e files)
                              (rf/dispatch [:drag [:drop e files]])))
                   :leave (fn [e]
                            (println "d1 leave")
                            (rf/dispatch [:drag [:leave e]]))
                   :end   (fn [e]
                            (println "d1 end")
                            (rf/dispatch [:drag [:end e]]))}))


(defn disable-drop []
  (dnd/unsubscribe! js/document.documentElement
                    :unique-key))

#_ ; replaced by drop zones
(defstate drop-target
  :start (enable-drop)
  :stop (disable-drop))

#_ ; replaced by DropzoneArea, kept for reference
(defn drop-zone [{:keys [item]} & children]
  ;; consider instead using https://www.npmjs.com/package/material-ui-dropzone
  (let [unique-key (keyword (str "drop-zone-" (random-uuid)))]
    (reagent/create-class
     {:component-did-mount
      (fn [comp]
        (timbre/debug "Mount dropzone:" comp)
        (let [node (reagent/dom-node comp)]
          (dnd/subscribe!
           node unique-key
           {:drop (fn [e files]
                    (timbre/debug "Drop:" files)
                    (let [[file] (js->clj (js/Array.from files))]
                      (rf/dispatch [:user/drop item (decode-file file)])))})))
      :component-will-unmount
      (fn [comp]
        (timbre/debug "Unmount dropzone:" comp)
        (let [node (reagent/dom-node comp)]
          (dnd/unsubscribe! node unique-key)))
      :reagent-render
      (fn [{:keys [item]} & children]
        ;; Bug: Not updating item if changed
        (into [:<>] children))})))
