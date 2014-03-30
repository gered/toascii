(ns toascii.models.image
  "Largely based on the Claskii library: https://github.com/LauJensen/Claskii"
  (:import (java.awt Color RenderingHints Graphics2D Image)
           (java.awt.image BufferedImage)
           (javax.swing ImageIcon)
           (javax.imageio ImageIO)
           (java.io File)
           (java.net URL URLEncoder URI))
  (:require [cemerick.url :as url]
            [toascii.util :refer [query-param-url->java-url]])
  (:use hiccup.core))

(def ascii-chars [\# \A \@ \% \$ \+ \= \* \: \, \. \space])
(def num-ascii-chars (count ascii-chars))

(defn get-image-by-url
  (^BufferedImage [^String url]
   (try
     (let [java-url (query-param-url->java-url url)]
       (ImageIO/read java-url))
     (catch Exception ex))))

(defn get-image-by-file
  (^BufferedImage [^File file]
   (try
     (ImageIO/read file)
     (catch Exception ex))))

(defn scale-image
  "takes a source image specified by the uri (a filename or a URL) and scales it proportionally
   using the new width, returning the newly scaled image."
  (^BufferedImage [url new-width]
   (let [^Image image (ImageIO/read url)
         new-height    (* (/ new-width (.getWidth image))
                          (.getHeight image))
         scaled-image (BufferedImage. new-width new-height BufferedImage/TYPE_INT_RGB)
         gfx2d        (doto (.createGraphics scaled-image)
                        (.setRenderingHint RenderingHints/KEY_INTERPOLATION
                                           RenderingHints/VALUE_INTERPOLATION_BILINEAR)
                        (.drawImage image 0 0 new-width new-height nil)
                        (.dispose))]
     scaled-image)))

(defn- get-css-color-attr [r g b]
  (format "color: #%02x%02x%02x;" r g b))

(defn- get-pixel [^BufferedImage image x y]
  (let [argb (.getRGB image x y)]
    [(bit-shift-right (bit-and 0xff000000 argb) 24)
     (bit-shift-right (bit-and 0x00ff0000 argb) 16)
     (bit-shift-right (bit-and 0x0000ff00 argb) 8)
     (bit-and 0x000000ff argb)]))

(defn get-ascii-pixel
  ""
  [^BufferedImage image x y color?]
  (let [[a r g b]  (get-pixel image x y)
        peak       (apply max [r g b])
        char-index (if (zero? peak)
                     (dec num-ascii-chars)
                     (dec (int (+ 0.5 (* num-ascii-chars (/ peak 255))))))
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
      (html
        [:pre
         {:style "font-size:5pt; letter-spacing:1px; line-height:4pt; font-weight:bold;"}
         output])
      output)))

(defn convert-image
  ([^BufferedImage image color?]
   (convert-image image nil color?))
  ([^BufferedImage image scale-width color?]
   (let [current-width (.getWidth image)
         new-width     (or scale-width current-width)
         final-image   (if-not (= new-width current-width)
                         (scale-image image new-width)
                         image)]
     (pixels->ascii final-image color?))))