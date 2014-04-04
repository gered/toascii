(ns toascii.models.image
  (:import (java.awt.image BufferedImage))
  (:require [toascii.util :refer [query-param-url->java-url]]
            [clj-image2ascii.core :as i2a])
  (:use hiccup.core))

(defn get-image [^String url]
  (let [java-url (query-param-url->java-url url)]
    (i2a/get-image-by-url java-url)))

(defn wrap-pre-tag [s]
  (str "<pre style=\"font-size:6pt; letter-spacing:1px; line-height:5pt; font-weight:bold;\">" s "</pre>"))

(defn image->ascii [^BufferedImage image scale-to-width color? html?]
  (let [converted (i2a/convert-image image scale-to-width color?)
        ascii     (:image converted)]
    (if html?
      (wrap-pre-tag ascii)
      ascii)))
