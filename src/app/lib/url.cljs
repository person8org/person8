(ns app.lib.url
  (:require
   [taoensso.timbre :as timbre]
   [goog.fs :as fs]))

(defn as-url [object]
  {:pre [(some? object)]
   :post [string?]}
  (if (goog.fs.url.browserSupportsObjectUrls)
    (goog.fs.url.createObjectUrl object)
    (timbre/error "Browser doesn't support object URLs")))
