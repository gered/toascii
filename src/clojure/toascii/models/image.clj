(ns toascii.models.image
  (:import (java.awt RenderingHints Graphics2D Image)
           (java.awt.image BufferedImage Raster)
           (javax.imageio ImageIO)
           (java.io File)
           (java.net URL)
           (toascii.images ImageToAscii))
  (:require [clojure.string :as str]
            [cemerick.url :as url]
            [toascii.util :refer [query-param-url->java-url]])
  (:use hiccup.core))

(defn get-image-by-url
  (^BufferedImage [^String url]
   (try
     (let [java-url (query-param-url->java-url url)]
       (ImageIO/read java-url))
     (catch Exception ex))))

(defn get-image-by-file
  "returns a BufferedImage loaded from the file specified, or null if an error occurs."
  (^BufferedImage [^File file]
   (try
     (ImageIO/read file)
     (catch Exception ex))))

(defn scale-image
  "takes a source image specified by the uri (a filename or a URL) and scales it proportionally
   using the new width, returning the newly scaled image."
  (^BufferedImage [^BufferedImage image new-width]
   (let [new-height    (* (/ new-width (.getWidth image))
                          (.getHeight image))
         scaled-image (BufferedImage. new-width new-height BufferedImage/TYPE_INT_RGB)
         gfx2d        (doto (.createGraphics scaled-image)
                        (.setRenderingHint RenderingHints/KEY_INTERPOLATION
                                           RenderingHints/VALUE_INTERPOLATION_BILINEAR)
                        (.drawImage image 0 0 new-width new-height nil)
                        (.dispose))]
     scaled-image)))

(defn convert-image
  "converts the image to an ascii representation. a multiline string will be returned,
   which will be formatted for html if color? is true, or plain text if false. if
   scale-to-width is specified the resulting image will be scaled proportionally from
   the source image."
  ([^BufferedImage image color?]
   (convert-image image nil color?))
  ([^BufferedImage image scale-to-width color?]
   (let [current-width (.getWidth image)
         new-width     (or scale-to-width current-width)
         final-image   (if-not (= new-width current-width)
                         (scale-image image new-width)
                         image)]
     (ImageToAscii/convert final-image color?))))

(defn wrap-pre-tag [s]
  (str "<pre style=\"font-size:6pt; letter-spacing:1px; line-height:5pt; font-weight:bold;\">" s "</pre>"))