(ns app.store
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<! chan close! alts! timeout put!]]
   [clojure.spec.alpha :as s]
   [goog.fs
    :refer [getBlob getBlobWithProperties]]
   [taoensso.timbre :as timbre]
   [app.datatransfer
    :refer [file-as-blob-async]]
   [app.lib.url
    :refer [as-url]]
   [app.state :as state]))

;; GOAL: Abstract out blockstack specifics

;; Can use hashes as filenames if the goal is to reuse
;; but it has privacy implications (minor)

(s/def ::item (s/keys :req-un [::id]
                      :opt-un [::path ::label ::description])) ; ::expandable ::edit ::share

(s/def ::index (s/coll-of ::item))

(def data-storage ;; should be in settings
  {:path "v10/data.edn"
   :options {:decrypt true} ;; decrypt?
   :spec ::index
   :reader cljs.reader/read-string
   :writer prn-str})

(s/check-asserts true)

(defn check-data-storage [content]
  ;; assert round-trip
  (let [{:keys [spec reader writer]} data-storage]
    (s/assert spec content)
    (s/assert spec (-> content writer reader))))

(try ;; only for debug, remove in production
  (check-data-storage state/profile-fields)
  (catch :default e
    (timbre/warn e)
    (let [{:keys [spec reader writer]} data-storage]
      (timbre/info state/profile-fields writer reader)
      (timbre/warn (-> state/profile-fields writer reader)))))

;; PERSISTENT IMAGES

;; Representation of image presumedly stored in a file
;; Consider as immutable

(defrecord Image [path url])

;; Should store the type of the image as well!
;; Use http headers with content-type?

(defn make-image [path filedata]
  ; filedata is string or .ArrayBuffer
  ; status is an atom representing current save status
  (timbre/debug "make image:" path filedata)
  (let [path (or path (str (random-uuid)))]
    (->Image path (if filedata (as-url filedata)))))

(defn as-blob [data {:keys [type]}]
  (if (some? type)
    (getBlobWithProperties [data] type)
    (getBlob data))
  #_ ; better use goog for compatibility
  (new js/Blob [data] #js{:type type}))

(defn load-image [{:keys [user-session path options]}]
  (timbre/debug "Load image:" path)
  (let [out (async/promise-chan)
        collect #(put! out (make-image path (if % (as-blob % {:type "image/png"}))))]
    (-> (.getFile user-session path (clj->js options))
        (.then collect)
        (.catch #(do (timbre/error "Failed to load image" path ". " %)
                   (close! out))))
    out))

(defn encode-image [content]
  ; {:post [(string? %)(js/Buffer.isBuffer %)]}
  (timbre/debug "Encode image:" content)
  (let [out (async/promise-chan)
        collect #(put! out %)]
    (if (string? content)
       (collect content)
       ;; assume file object
       (file-as-blob-async content collect))
    out))

(defn store-image-data [{:keys [user-session path options]} content]
  ;; should never store on top of existing file
  (let [out (async/promise-chan)
        collect #(put! out %)]
    (go
     (-> (.putFile user-session path (<!(encode-image content)) (clj->js options))
         (.finally #(collect content))
         (.catch #(do (timbre/error "Failed to store:" %)
                    (close! out)))))
    out))

(defn store-image [{:keys [user-session path options] :as target} content]
  "Returns an image record, storing the content as a side effect"
  {:post [#(instance? Image %)]}
  (timbre/debug "Store image:" path content)
  (go (<! (store-image-data target content)))
  (make-image path (as-url content)))

(defn encode-index [content]
  {:post [#(s/valid? ::index %)]}
  ; encodes a board to the index spec for storing
  (->> content
       (map #(select-keys % [:id :path :label :description]))))

#_
(defn decode-index [user-session index]
  ; actually decodes from persistent file format to semi-valid board format
  (timbre/debug "Decode Index:" index)
  (let [in (async/chan)
        out (async/chan)
        af (fn [input out]
             (timbre/debug "Decoding:" input)
             (go-loop [collect #(do (put! out %)(close! out))]
              (cond

                (:path input)
                (->>
                 (load-image {:user-session user-session
                              :path (:path input)})
                 (<!)
                 (.-url)
                 (assoc input :image)
                 (collect))

                true
                (collect input))))]
    (async/onto-chan in index)
    (async/pipeline-async 10 out af in)
    (async/into [] out)))

(defn use-default [{:keys [id image] :as item}]
  (if (some? image) item
    (let [[default] (filter #(= id (:id %)) state/profile-fields)]
      (assoc item :image (:image default)))))

(defn merge-defaults [decoded]
  (timbre/debug "Ready to merge defaults:" decoded)
  (if (empty? decoded) ;; fallback, should perhaps not revert to original
     state/profile-fields
     (map use-default decoded)))

#_
(defn call-resolved-images [user-session index dispatch]
  (go (-> (decode-index user-session index)
          (<!)
          (merge-defaults)
          (dispatch))))
