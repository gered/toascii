(ns toascii.models.image
  (:import (java.awt.image BufferedImage)
           (java.io Writer)
           (javax.imageio.stream ImageInputStream))
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [ring.util.io :refer [piped-input-stream]]
            [clj-image2ascii.core :as i2a]
            [toascii.util :refer [query-param-url->java-url]])
  (:use hiccup.core))

(def js-gif-animation (slurp (io/resource "gif-animation.js")))

(def ascii-pre-css "font-size:6pt; letter-spacing:1px; line-height:5pt; font-weight:bold;")

(defn get-image [^String url]
  (let [java-url (query-param-url->java-url url)]
    (i2a/get-image-by-url java-url)))

(defn get-image-stream [^String url]
  (let [java-url (query-param-url->java-url url)]
    (i2a/get-image-stream-by-url java-url)))

(defn- wrap-pre-tag [s & {:keys [hidden? delay]}]
  (str
    "<pre style=\""
    ascii-pre-css
    (if hidden?
      " display: none;")
    "\""
    (if delay
      (str " data-delay=\"" delay "\""))
    ">" s "</pre>"))

(defn- get-open-pre-tag [hidden? delay]
  (str
    "<pre style=\""
    ascii-pre-css
    (if hidden?
      " display: none;")
    "\""
    (if delay
      (str " data-delay=\"" delay "\""))
    ">"))

(defn image->ascii [^BufferedImage image scale-to-width color? html?]
  (let [converted (i2a/convert-image image scale-to-width color?)
        ascii     (:image converted)]
    (if html?
      (wrap-pre-tag ascii)
      ascii)))

(defn- write-out [^Writer w s]
  (.write w (str s)))

(defn gif->ascii [^ImageInputStream image-stream scale-to-width color?]
  (piped-input-stream
    (fn [output-stream]
      (with-open [^Writer w (io/writer output-stream :append true)]
        (write-out w "<div class=\"animated-gif-frames\">")
        (i2a/stream-animated-gif-frames!
          image-stream scale-to-width color?
          (fn [{:keys [image delay]}]
              (write-out w (get-open-pre-tag true delay))
              (write-out w image)
              (write-out w "</pre>")))
        (write-out w "</div>")
        (write-out w "<script type=\"text/javascript\">")
        (write-out w js-gif-animation)
        (write-out w "</script>")))))