(ns toascii.models.image
  "Largely based on the Claskii library: https://github.com/LauJensen/Claskii"
  (:import (java.awt RenderingHints Graphics2D Image)
           (java.awt.image BufferedImage)
           (javax.imageio ImageIO)
           (java.io File))
  (:require [clojure.string :as str]
            [cemerick.url :as url]
            [toascii.util :refer [query-param-url->java-url]])
  (:use hiccup.core))

(def ascii-chars [\# \A \@ \% \$ \+ \= \* \: \, \. \space])
(def num-ascii-chars (count ascii-chars))

(defn- get-css-color-attr [r g b]
  (format "color: #%02x%02x%02x;" r g b))

(defn- get-color-brightness [r g b]
  (int
    (Math/sqrt
      (+ (* r r 0.241)
         (* g g 0.691)
         (* b b 0.068)))))

(defn- get-pixel [^BufferedImage image x y]
  (let [argb (.getRGB image x y)]
    [(bit-shift-right (bit-and 0xff000000 argb) 24)
     (bit-shift-right (bit-and 0x00ff0000 argb) 16)
     (bit-shift-right (bit-and 0x0000ff00 argb) 8)
     (bit-and 0x000000ff argb)]))

(defn- get-ascii-pixel [^BufferedImage image x y color?]
  (let [[a r g b]  (get-pixel image x y)
        peak       (get-color-brightness r g b)
        char-index (if (zero? peak)
                     (dec num-ascii-chars)
                     (dec (int (* num-ascii-chars (/ peak 255)))))
        pixel-char (nth ascii-chars (if (pos? char-index) char-index 0))]
    (if color?
      [:span {:style (get-css-color-attr r g b)} pixel-char]
      pixel-char)))

(defn- pixels->ascii [^BufferedImage image color?]
  (let [width       (.getWidth image)
        ascii-image (for [y (range (.getHeight image))
                          x (range (.getWidth image))]
                      (get-ascii-pixel image x y color?))
        output      (->> ascii-image
                         (partition width)
                         (map #(conj % (if color? [:br] \newline)))
                         (apply concat))]
    (if color?
      (html output)
      (str/join output))))

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