(ns toascii.models.image
  (:import (java.awt.image BufferedImage)
           (javax.imageio.stream ImageInputStream)
           (java.io Writer))
  (:require [clojure.java.io :as io]
            [clj-image2ascii.core :as i2a]
            [toascii.util :refer [query-param-url->java-url stream-response]]))

(def js-gif-animation (slurp (io/resource "gif-animation.js")))

(def ascii-pre-css "font-size:6pt; letter-spacing:1px; line-height:5pt; font-weight:bold;")

(defn get-image [^String url]
  (let [java-url (query-param-url->java-url url)]
    (i2a/get-image-by-url java-url)))

(defn get-image-stream [^String url]
  (let [java-url (query-param-url->java-url url)]
    (i2a/get-image-stream-by-url java-url)))

(defn image->ascii [^BufferedImage image scale-to-width color? html?]
  (let [converted (i2a/convert-image image scale-to-width color?)
        ascii     (:image converted)]
    (if html?
      (str "<pre style=\"" ascii-pre-css "\">" ascii "</pre>")
      ascii)))

(defn gif->ascii [^ImageInputStream image-stream scale-to-width color?]
  (stream-response
    (fn [^Writer w]
      (.write w "<div class=\"animated-gif-frames\">")
      (i2a/stream-animated-gif-frames!
        image-stream scale-to-width color?
        (fn [{:keys [^String image delay]}]
          (.write w (str "<pre style=\"" ascii-pre-css " display: none;\" data-delay=\"" delay "\">"))
          (.write w image)
          (.write w "</pre>")))
      (.write w "</div>")
      (.write w "<script type=\"text/javascript\">")
      (.write w js-gif-animation)
      (.write w "</script>"))))