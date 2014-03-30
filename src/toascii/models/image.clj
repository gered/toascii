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

(defmacro get-properties [obj & properties]
  (let [target (gensym)]
    `(let [~target ~obj]
       (vector ~@(for [property properties]
                   `(~property ~target))))))

(defn scale-image
  "takes a source image specified by the uri (a filename or a URL) and scales it proportionally
   using the new width, returning the newly scaled image."
  [url new-width]
  (let [^Image image (ImageIO/read url)
        new-height   (* (/ new-width (.getWidth image))
                        (.getHeight image))
        scaled-image (BufferedImage. new-width new-height BufferedImage/TYPE_INT_RGB)
        gfx2d        (doto (.createGraphics scaled-image)
                       (.setRenderingHint RenderingHints/KEY_INTERPOLATION
                                          RenderingHints/VALUE_INTERPOLATION_BILINEAR)
                       (.drawImage image 0 0 new-width new-height nil)
                       (.dispose))]
    scaled-image))

(defn ascii [^BufferedImage img x y color?]
  (let [[red green blue] (get-properties (Color. (.getRGB img x y))
                                         .getRed .getGreen .getBlue)
        peak    (apply max [red green blue])
        idx     (if (zero? peak)
                  (dec (count ascii-chars))
                  (dec (int (+ 1/2 (* (count ascii-chars) (/ peak 255))))))
        output  (nth ascii-chars (if (pos? idx) idx 0))	]
    (if color?
      (html [:span {:style (format "color: rgb(%s,%s,%s);" red green blue)} output])
      output)))

(defn convert-image [url w color?]
  (let [java-url         (query-param-url->java-url url)
        ^Image raw-image (scale-image java-url w)
        ascii-image      (->> (for [y (range (.getHeight raw-image))
                                    x (range (.getWidth  raw-image))]
                                (ascii raw-image x y color?))
                              (partition w))
        output           (->> ascii-image
                              (interpose (if color? "<BR/>" \newline))
                              flatten)]
    (if color?
      (html [:pre {:style "font-size:5pt; letter-spacing:1px;
                           line-height:4pt; font-weight:bold;"}
             output])
      (println output))))