(ns app.datatransfer
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<! chan close! alts! timeout put!]]
   [goog.fx.DragDrop]
   [goog.fx.DragDropGroup]
   [goog.fs.FileReader :default FileReader]
   [goog.fs :as fs]
   [cljs-http.client :as http]
   [taoensso.timbre :as timbre]
   [re-frame.core :as rf]
   [mount.core :refer [defstate]]
   [app.lib.url
    :refer [as-url]]
   [app.lib.clipboard :as clipboard
    :refer [modern-clipboard]]))


"Data transfer through clipboard (copy/cut/paste) and filesystem drag/drop"

; https://google.github.io/closure-library/api/goog.events.FileDropHandler.html
; https://github.com/google/closure-library/blob/master/closure/goog/demos/dragdrop.html
; https://github.com/google/closure-library/blob/master/closure/goog/demos/drag.html
; https://google.github.io/closure-library/api/goog.fx.DragDrop.html
; https://github.com/tonsky/cljs-drag-n-drop

(defn clipboard-put [type data]
  ;; ## TODO: Use goog?
  "Writes the data to the clipboard. Returns a Promise resolved when the operation has succeeded."
  (let [out (async/promise-chan)]
    (case type
      "text/plain"
      (-> (.. js/navigator -clipboard writeText text)
          (.then (fn [response] (put! out response))))
      (timbre/error "Can't put in clipboard:" type))))

(defn dispatch-paste [{:as payload}]
  (timbre/debug "Dispatch Paste:" payload)
  (rf/dispatch [:paste (assoc payload
                              :id (random-uuid))]))


(defn get-as-string [item]
  {:pre [(some? item)]}
  ; https://developer.mozilla.org/en-US/docs/Web/API/DataTransferItem/getAsString
  ; Mozilla specify callback but others say it returns a string...
  ; so try to cover both, or find a goog for itt
  (let [out (async/promise-chan)
        result (.getAsString item (fn [value] (put! out value)))]
    (when result
      (put! out result))
    out))

(defn file-as-url [file]
  {:pre [(some? file)]}
  (timbre/debug "File as URL:" file)
  ; https://google.github.io/closure-library/api/goog.fs.FileReader.html
  (let [out (async/promise-chan)]
      (-> (.readAsDataUrl FileReader file)
          (.then (fn [value] (put! out value))))
      #_
      (let [reader (new js/FileReader)]
        (set! (.-onload reader)
              (fn [data]
                (put! out data)))
        (.readAsDataUrl reader file))
    out))

(defn file-as-blob-async [file cb]
  (let [reader (new js/FileReader)]
    (set! (.-onload reader)
          #(cb (.-result reader)))
    (.readAsArrayBuffer reader file)))

(defn load-datatransfer [transfer]
  ; https://developer.mozilla.org/en-US/docs/Web/API/DataTransferItem
  (let [types (map js->clj (.-types transfer))
        items (.-items transfer)]
    (doseq [i (range (.-length items))]
      (let [item (aget items i)
            item-kind (.-kind item)
            item-type (.-type item)]
        (timbre/info "Clip=" (.stringify js/JSON item)
                     (js->clj item) item-type)
        (cond
          (and (= item-kind "file")
               ;; weird but sometimes false
               (some? (.getAsFile item)))
          (let [file (.getAsFile item) ;; don't do in async as it may no longer exist
                url (as-url file)]
              (dispatch-paste {:kind item-kind
                               :type item-type
                               :data file
                               :url  url}))

          (or (= item-type "text/plain")
              (= item-type "string"))
          (let [in (get-as-string item)]
            (go-loop [data (<! in)]
              (dispatch-paste {:type item-type
                               :data data})))

          (= item-type "text/html")
          (dispatch-paste {:type item-type
                           :data (.getData transfer item-type)})

          (= item-type "image/png")
          (let [clip (.getData transfer item-type)]
            (if-not (empty? clip) ;; empty string when no value per spec
              (let [_ (timbre/debug "->" clip (type clip)(some? clip))
                    url nil]
                (dispatch-paste {:type item-type
                                 :data clip
                                 :url url}))
              (timbre/warn "No clip content for type:" item-type)))

          true ;; images etc
          (timbre/warn "Type not supported:" item-type))))))


(defn paste-handler [event]
  (timbre/info "Paste from clipboard" event)
  (if-let [clip-data (.-clipboardData event)]
    (do
      (load-datatransfer clip-data)
      (.preventDefault event))
    (let [text (.. js/navigator -clipboard readText)]
      (timbre/info "Text=" text)
      (.preventDefault event)
      (-> text
          (.then (fn [text]
                   (timbre/info "Pasted:" text)
                   (dispatch-paste {:type "text/plain"
                                    :data text})))))))

(defstate paste-target
  :start (.addEventListener js/document goog.events.EventType.PASTE
                            paste-handler)
  :stop (.removeEventListener js/document goog.events.EventType.PASTE
                              paste-handler))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; COPY

(defn copy-handler [event]
  (let [board-items @(rf/subscribe [:board])
        [selection] (filter :selected board-items)]
    (timbre/info "Copy to clipboard:" selection)
    (when selection
      (.preventDefault event) ;; if not just use default like selected text
      (or
        (clipboard/insert-clip (:data selection) {:type (:type selection)})
        (clipboard/legacy-insert-clip event (:data selection) {:type (:type selection)})))))

(defstate copy-target
    :start (.addEventListener js/document goog.events.EventType.COPY
                              copy-handler)
    :stop (.removeEventListener js/document goog.events.EventType.COPY
                               copy-handler))
