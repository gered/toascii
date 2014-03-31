(ns toascii.models.image
  "Largely based on the Claskii library: https://github.com/LauJensen/Claskii"
  (:import (java.awt RenderingHints Graphics2D Image)
           (java.awt.image BufferedImage Raster)
           (javax.imageio ImageIO)
           (java.io File))
  (:require [clojure.string :as str]
            [cemerick.url :as url]
            [toascii.util :refer [query-param-url->java-url]])
  (:use hiccup.core))

(def ascii-chars [\# \A \@ \% \$ \+ \= \* \: \, \. \space])
(def num-ascii-chars (count ascii-chars))

(defn- add-pixel [argb ^StringBuilder sb color?]
  (let [r          (bit-shift-right (bit-and 0x00ff0000 argb) 16)
        g          (bit-shift-right (bit-and 0x0000ff00 argb) 8)
        b          (bit-and 0x000000ff argb)
        peak       (int
                     (Math/sqrt
                       (+ (* r r 0.241)
                          (* g g 0.691)
                          (* b b 0.068))))
        char-index (if (zero? peak)
                     (dec num-ascii-chars)
                     (dec (int (* num-ascii-chars (/ peak 255)))))
        pixel-char (nth ascii-chars (if (pos? char-index) char-index 0))]
    (if color?
      ; <span style="color: rgb(255, 255, 255);">X</span>
      (doto sb
        (.append "<span style=\"color: rgb(")
        (.append r)
        (.append ",")
        (.append g)
        (.append ",")
        (.append b)
        (.append ");\">")
        (.append pixel-char)
        (.append "</span>"))
      pixel-char)))

(defn- pixels->ascii [^BufferedImage image color?]
  (let [width  (.getWidth image)
        height (.getHeight image)
        sb     (StringBuilder.)
        pixels (.getDataElements (.getRaster image) 0 0 width height nil)]
    (dotimes [y height]
      (dotimes [x width]
        (add-pixel (aget pixels (+ x (* y width))) sb color?))
      (.append sb (if color? "<br>" \newline)))
    (.toString sb)))

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
     (pixels->ascii final-image color?))))

(defn wrap-pre-tag [s]
  (str "<pre style=\"font-size:6pt; letter-spacing:1px; line-height:5pt; font-weight:bold;\">" s "</pre>"))



#_(require '[criterium.core :as c])

#_(c/bench
  (let [f (File. "./test/images/test.png")
        image (get-image-by-file f)
        ascii (convert-image image true)]
    (count ascii)))

#_(let [f (File. "./test/images/test.png")
      image (get-image-by-file f)
      ascii (convert-image image true)]
  (count ascii))