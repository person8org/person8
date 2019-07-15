(ns app.store
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<! chan close! alts! timeout put!]]
   [clojure.spec.alpha :as s]
   ["elliptic" :as elliptic]
   ["buffer" :as buffer :refer [Buffer]]
   ["bitcoinjs-lib" :as btclib] ;; see https://github.com/blockstack/blockstack.js/blob/src/keys.ts
   [goog.fs
    :refer [getBlob getBlobWithProperties]]
   [taoensso.timbre :as timbre]
   [re-frame.core :as rf] ;; ## FIX: remove
   [app.datatransfer
    :refer [file-as-blob-async]]
   [app.lib.url
    :refer [as-url]]
   [app.state :as state]))

;; GOAL: Abstract out blockstack specifics

;; ## for testing only
(def user-session (rf/subscribe [:blockstack/user-session]))

;; Can use hashes as filenames if the goal is to reuse
;; but it has privacy implications (minor)

(s/def ::image (s/and string? #(clojure.string/starts-with? % "img/")))

(s/def ::private-key (s/and string? #(re-matches #"[0-9a-fA-F]+" %)))

(s/def ::public-key (s/and string? #(re-matches #"[0-9a-fA-F]+" %)))

(s/def ::item (s/keys :req-un [::id]
                      :opt-un [::image ::private-key ::public-key ::path ::label ::description])) ; ::expandable ::edit ::share

(s/def ::index (s/coll-of ::item))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FILE

;; Represent a (potential) file in persistent storage

(defn generate-file-path [prefix]
  (str prefix "/" (random-uuid)))

(defn doc-path? [path]
  (boolean (re-matches #"doc/(.+)" path)))

(defn gen-file-url [user-session path & [options]]
  {:pre [(string? path)]}
  (let [out (async/promise-chan)]
    (-> user-session
        (.getFileUrl path (clj->js options))
        (.then (fn [result]
                 (put! out result)
                 (timbre/debug path "=>>>" result))))
    out))

(defrecord File [user-session path encryption spec])

(def test-image-path "img/5887d1a0-e09e-4223-92dc-0f358257fe4b")

#_
(gen-file-url @user-session test-image-path)

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

(defn all-files [user-session]
  "Returns a channel containing the path of all files"
  (let [out (async/chan)]
    (-> (.listFiles user-session (fn [file] (put! out file) true))
        (.catch (fn [err] (timbre/error "Failed to list files:" err)))
        (.then (fn [val] (timbre/info "File count:" val)))
        (.finally (fn []
                     (timbre/info "Done listing files")
                     (close! out))))
    out))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PERSISTENT DOCS

(defn fetch-edn [{:keys [user-session path options spec] :as target}]
  ; Should always close on fails but return empty map if doesn't exist
  ; Consider ignoring spec on reading?
  (let [out (async/promise-chan)]
    (-> (.getFile user-session path)
        (.then (fn [content]
                 (let [result (cljs.reader/read-string (or content "{}"))]
                   (put! out (if (some? result) result {})))))
        (.catch (fn [err]
                  (timbre/warn "Failed to load:" err)
                  (close! out))))
    out))

(defn store-edn [{:keys [user-session path options spec] :as target} content]
  {:pre [user-session (string? path) spec]}
  (let [out (async/promise-chan)]
    (if (s/valid? spec content)
      (-> (.putFile user-session path (prn-str content))
          (.then (fn [response]
                   (put! out (or response true))))
          (.catch (fn [err]
                    (timbre/warn "Failed to store: " err)
                    (close! out))))
      (do
        (timbre/warn "Not storing due to invalid content:" content)
        (close! out)))
    out))

(defn p-assoc [{:keys [user-session path options spec] :as target} & {:as key-args}]
  {:pre [user-session path (some? spec)]}
  ; Careful to avoid overwriting existing file on retrieval errors
  (timbre/debug "Persistent doc:" target key-args)
  (let [out (async/chan)]
    (go-loop [content (<! (fetch-edn target))]
      (if (nil? content)
        (timbre/warn "Failed to update " path)
        (->> (merge content key-args)
             (store-edn target)
             (<!))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ENCRYPTION

(defrecord KeyPair [private public])

(defn public-key-from-private [private-key]
  {:pre [(string? private-key) (some? btclib/ECPair)]}
  "Replicates blockstack/getPublicKeyFromPrivate (unofficial)"
  (let [key-pair (.fromPrivateKey btclib/ECPair (.from Buffer private-key "hex"))]
    (-> (.-publicKey key-pair)
        (.toString "hex"))))

(defn generate-keys [{:keys [secret]}]
  "Generates a ECDSA keypair used when encrypting files, based on a secret (typically appPrivateKey from UserData)"
  ;; using https://www.npmjs.com/package/elliptic
  ;; Consider instead using bitcoinjs-lib as blockstack?
  ;; ## TODO: Use EdDSA with secret?
  {:pre [(string? secret)]}
  (case :btclib
    :elliptic ;; failing
    (let [ec (new elliptic/ec "curve25519")
          key-pair (.genKeyPair ec)
          private (-> key-pair (.getPrivate)(.toString "hex"))
          public (-> key-pair (.getPublic) (.encode "hex"))]
      (->KeyPair private public))
    :btclib
    (let [key-pair (.makeRandom btclib/ECPair)
          private (-> (.-privateKey key-pair)
                      (.toString "hex"))
          ; public (public-key-from-private private)
          public (-> (.-publicKey key-pair)
                     (.toString "hex"))]
      (->KeyPair private public))))

#_
(let [key-pair (generate-keys)]
  (timbre/debug "Private:" (:private key-pair))
  (timbre/debug "Public:" (:public key-pair)))

(defn encrypt-content [user-session content & [options]]
  {:pre [(string? (:public options))]}
  "Encrypts the data provided with the app public key or ECDSA public key hex string"
  (.encryptContent user-session content #js{:publicKey (:public options)}))

(defn decrypt-content [user-session content & [options]]
  {:pre [(string? (:private options))]}
  (.decryptContent user-session content #js{:privateKey (:private options)}))

#_ ;; for test
(let [user-data @(re-frame.core/subscribe [:blockstack/user-data])
      secret (:appPrivateKey user-data)]
  (let [{:keys [public private] :as key-pair}
        (generate-keys {:secret secret})
        content "abcdefg"
        encrypted (encrypt-content @user-session content {:public public})
        decrypted (decrypt-content @user-session encrypted {:private private})]
    [decrypted key-pair]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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

(defn encode-image [{:keys [type data] :as content}]
  ;; ## TODO: Move to datatransfer?
  (timbre/debug "Encode image:" content)
  (let [out (async/promise-chan)
        collect (fn [encoded]
                  #_  {:pre [(or (string? encoded)
                                 (js/Buffer.isBuffer encoded))]}
                  (put! out encoded))]
    (if (string? data)
       (collect data)
       ;; assume file object
       (file-as-blob-async data collect))
    out))

(defn store-image [user-session {:keys [path options] :as target} {:keys [type data] :as content}]
  ;; should never store on top of existing file
  (let [out (async/promise-chan)
        collect #(put! out %)]
    (go-loop [image-content (<! (encode-image content))]
      (timbre/debug "Store Image:" target image-content)
      (-> (.putFile user-session path image-content (clj->js options))
          (.then #(collect true))
          (.catch #(do (timbre/error "Failed to store:" %)
                     (collect false)))))
    out))

#_ ; for test
(defn verify-image [user-session img-path {:keys [private public] :as key-pair} content]
  {:pre [(string? private)(string? public)]}
  (-> (.getFile user-session img-path #js{:decrypt private})
      (.then (fn [stored]
               (timbre/debug "VERIFY:" (= content stored))
               (when-not (= content stored)
                 (timbre/warn content)
                 (timbre/warn stored))))
      (.catch (fn [err]
                (timbre/error err)))
    #_
    (let [decrypted (.decryptContent user-session stored private)]
      (timbre/debug "VERIFY:" (subseq decrypted 0 10)))))

(defn update-image [user-session id {:keys [type data] :as content}]
  {:pre [(some? user-session) (string? id)]}
  "After: Image is stored with custom encryption and index is updated in storage.
   Success: Returns channel with non-nil content.
   Failure: Returns closed channel - assume storage is not updated."
  (let [; secret (.-appPrivateKey user-session)
        ; key-pair (generate-keys {:secret secret})
        ;{:keys [public private]} key-pair
        public true
        private true
        img-path (str "img/" id "/" (random-uuid))
        doc-path (str "doc/" id)
        options {:public public :contentType type}
        target {:path img-path :options options}
        stored-chan (store-image user-session target content)]
    (go-loop [stored? (<! stored-chan)]
       (when stored?
         (let [doc (<! (p-assoc {:user-session user-session
                                 :path doc-path
                                 :spec ::item}
                               :id id
                               :image img-path))]
           (if doc
             (timbre/debug "Updated:" id doc)
             (timbre/warn "Failed to update image:" id))
           doc)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn delete-all-files! [user-session]
  (let [delete-file (fn [path]
                      (-> user-session
                          (.deleteFile path)
                          (.then (fn [file]
                                   (timbre/info "Deleted file:" path)))
                          (.catch (fn [err]
                                    (timbre/warn "Failed to delete file:" path err)))))]
    (-> user-session
        (.listFiles delete-file)
        (.then (fn [] (timbre/info "Deleted all files")))
        (.catch (fn [e] (timbre/warn "Failed while deleting files:" e))))))

#_ ;; Careful!!!!
(delete-all-files! @user-session)

(defn load-index [user-session]
  "Returns a chan containing all docs"
  (let [in (all-files user-session)
        out (async/chan)
        af (fn [path out]
             (if (doc-path? path)
               (go-loop [doc (<! (fetch-edn {:user-session user-session
                                             :path path}))]
                 (timbre/debug "DOC:" path doc)
                 (when doc
                   (put! out doc))
                 (close! out))
               (close! out)))]
    (async/pipeline-async 10 out af in)
    out))
