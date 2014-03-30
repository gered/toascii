(ns toascii.routes.api.image
  (:import (java.awt.image BufferedImage))
  (:require [clojure.string :as str]
            [liberator.core :refer [defresource]]
            [compojure.core :refer [ANY]]
            [toascii.route-utils :refer [register-routes]]
            [toascii.models.image :refer [convert-image get-image-by-url]]
            [toascii.util :refer [parse-int parse-boolean]]))

(defn- color? [color]
  (if (nil? color)
    true
    (parse-boolean color)))

(defresource render-image [{:keys [url width color format] :as params}]
  :media-type-available?
  (fn [ctx]
    (if (color? color)
      {:representation {:media-type "text/html"}}
      (let [type (condp = format
                   "html" "text/html"
                   "text" "text/plain"
                   "text/html")]
        {:representation {:media-type type}})))
  :malformed?
  (fn [_]
    (cond
      (str/blank? url)
      {:error "Missing image url"}

      (and width (nil? (parse-int width)))
      {:error "Invalid image width."}))
  :exists?
  (fn [_]
    (if-let [image (get-image-by-url url)]
      {:image image}
      [false {:error "Image could not be loaded."}]))
  :handle-ok
  (fn [ctx]
    (convert-image
      (:image ctx)
      (parse-int width)
      (color? color)))
  :handle-malformed
  (fn [ctx]
    (:error ctx))
  :handle-not-found
  (fn [ctx]
    (:error ctx)))

(register-routes api-image-routes
  (ANY "/api/image" {params :params} (render-image params)))