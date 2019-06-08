(ns app.lib.clipboard
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<! chan close! alts! timeout put!]]
   [taoensso.timbre :as timbre]))

"Interface with the clipboard (generalized)"

;; https://www.w3.org/TR/clipboard-apis/#async-clipboard-api
;; https://w3c.github.io/clipboard-apis/

; Legacy clipboard functions only in context of event handlers and cannot be
; used in async context... so careful if attempting to combine

; Chrome will soon allow copy/paste of images (6/30 2019! or now by enabling "Experimental Web Platform features")
; fixing a long standing bug: https://bugs.chromium.org/p/chromium/issues/detail?id=150835
; https://docs.google.com/document/d/1sMcajoYAQ_M_VOIn1RLokG1sVB7skjoeviqt9aZOcDk/edit

(defn modern-clipboard []
  ; see discussion at https://developers.google.com/web/updates/2018/03/clipboardapi
  (boolean js/navigator.clipboard))

(defn insert-clip
  ([data {:keys [type] :as options}]
   (timbre/debug "Clip:" data options)
   (cond
     (not (modern-clipboard)) nil

     (and (= type "text/plain")
          js/navigator.clipboard.writeText)
     (let [clipboard js/navigator.clipboard]
       (-> (.writeText clipboard data)
           (.then (fn [response](timbre/debug "Copied to clipboard -" response))
                  (fn [err] (timbre/error "Failed to copy to clipboard" err)))))

     js/navigator.clipboard.write
     (let [clipboard js/navigator.clipboard
           js-options #js{:type type}
           ; item (new js/Blob [data] js-options)
           item (new js/ClipboardItem (clj->js {type data}))]
       (assert js/navigator.clipboard.write)
       (-> (.write clipboard [item])
           (.then (fn [response](timbre/debug "Copied to clipboard -" response))
                  (fn [err] (timbre/error "Failed to copy to clipboard" err))))
       true))))

(defn legacy-insert-clip [event data {:keys [type] :as options}]
  ; Important: Has to be in the context of the event, not async
  (timbre/debug "Legacy insert clip:" type data)
  (.setData (.. event -clipboardData) type data))
