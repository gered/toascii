(ns toascii.routes.api.image
  (:import (java.awt.image BufferedImage)
           (javax.imageio IIOException))
  (:require [clojure.string :as str]
            [liberator.core :refer [defresource]]
            [compojure.core :refer [ANY]]
            [toascii.route-utils :refer [register-routes]]
            [toascii.models.image :refer [image->ascii gif->ascii get-image get-image-stream]]
            [toascii.util :refer [parse-int parse-boolean]]))

(defresource render-image [{:keys [url width color format] :as params}]
  :media-type-available?
  (fn [ctx]
    (let [type (condp = format
                 "html" "text/html"
                 "text" "text/plain"
                 "text/html")]
      {:representation {:media-type type}}))
  :malformed?
  (fn [ctx]
    (cond
      (str/blank? url)                     {:error "Missing image url"}
      (and width
           (nil? (parse-int width)))       {:error "Invalid image width."}
      (and (= "text" format)
           (not (nil? color))
           (parse-boolean color))          {:error "Cannot output colored plain text version of image."}))
  :exists?
  (fn [_]
    (if-let [image (get-image url)]
      {:image image}
      [false {:error "Image could not be loaded."}]))
  :handle-ok
  (fn [ctx]
    (let [html? (= "text/html" (get-in ctx [:representation :media-type]))
          color? (or (and html? (nil? color))
                     (parse-boolean color))]
      (image->ascii (:image ctx) (parse-int width) color? html?)))
  :handle-malformed
  (fn [ctx]
    (:error ctx))
  :handle-not-found
  (fn [ctx]
    (:error ctx)))

(defresource render-animated-image [{:keys [url width color] :as params}]
  :available-media-types ["text/html"]
  :malformed?
  (fn [ctx]
    (cond
      (str/blank? url)                     {:error "Missing image url"}
      (and width
           (nil? (parse-int width)))       {:error "Invalid image width."}))
  :exists?
  (fn [_]
    (if-let [stream (get-image-stream url)]
      {:image stream}
      [false {:error "Image could not be loaded."}]))
  :handle-ok
  (fn [ctx]
    (let [color? (or (nil? color)
                     (parse-boolean color))]
      (try
        (gif->ascii (:image ctx) (parse-int width) color?)
        (catch IIOException ex
          (throw (new Exception "Invalid GIF image."))))))
  :handle-malformed
  (fn [ctx]
    (:error ctx))
  :handle-not-found
  (fn [ctx]
    (:error ctx)))

(register-routes api-image-routes
  (ANY "/api/image" {params :params} (render-image params))
  (ANY "/api/animated" {params :params} (render-animated-image params)))