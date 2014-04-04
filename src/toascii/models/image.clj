(ns toascii.models.image
  (:import (java.awt.image BufferedImage)
           (javax.imageio.stream ImageInputStream))
  (:require [clojure.string :as str]
            [clj-image2ascii.core :as i2a]
            [toascii.util :refer [query-param-url->java-url]])
  (:use hiccup.core))

(def ascii-pre-css "font-size:6pt; letter-spacing:1px; line-height:5pt; font-weight:bold;")

(defn get-image [^String url]
  (let [java-url (query-param-url->java-url url)]
    (i2a/get-image-by-url java-url)))

(defn get-image-stream [^String url]
  (let [java-url (query-param-url->java-url url)]
    (i2a/get-image-stream-by-url java-url)))

(defn wrap-pre-tag
  ([s]
   (str "<pre style=\"" ascii-pre-css "\">" s "</pre>"))
  ([s delay]
   (str "<pre style=\"" ascii-pre-css "\" data-delay=\"" delay "\">" s "</pre>")))

(defn image->ascii [^BufferedImage image scale-to-width color? html?]
  (let [converted (i2a/convert-image image scale-to-width color?)
        ascii     (:image converted)]
    (if html?
      (wrap-pre-tag ascii)
      ascii)))

(defn gif->ascii [^ImageInputStream image-stream scale-to-width color?]
  (as-> image-stream x
        (i2a/convert-animated-gif-frames x scale-to-width color?)
        (:frames x)
        (map
          (fn [frame]
            (wrap-pre-tag (:image frame) (:delay frame)))
          x)
        (str/join x)))