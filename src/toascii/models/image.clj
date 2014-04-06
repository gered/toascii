(ns toascii.models.image
  (:import (java.awt.image BufferedImage)
           (javax.imageio.stream ImageInputStream)
           (java.io Writer))
  (:require [clojure.java.io :as io]
            [ring.util.io :refer [piped-input-stream]]
            [clj-image2ascii.core :as i2a]
            [toascii.util :refer [query-param-url->java-url]]))

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
            (write-out w (str "<pre style=\"" ascii-pre-css " display: none;\" data-delay=\"" delay "\">"))
            (write-out w image)
            (write-out w "</pre>")))
        (write-out w "</div>")
        (write-out w "<script type=\"text/javascript\">")
        (write-out w js-gif-animation)
        (write-out w "</script>")))))