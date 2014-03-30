(ns toascii.routes.api.image
  (:require [clojure.string :as str]
            [liberator.core :refer [defresource]]
            [compojure.core :refer [ANY]]
            [toascii.route-utils :refer [register-routes]]
            [toascii.models.image :refer [convert-image]]))

(defresource render-image [{:keys [url width color format] :as params}]
  :media-type-available?
  (fn [ctx]
    (let [type (condp = format
                 "html" "text/html"
                 "text" "text/plain"
                 "text/html")]
      {:representation {:media-type type}}))
  :malformed?
  (fn [_]
    (cond
      (str/blank? url) {:error "Missing image url"}))
  :handle-ok
  (fn [ctx]
    (let [rendered (convert-image url 64 true)]
      (if (= "text/html" (get-in ctx [:representation :media-type]))
        rendered
        rendered)))
  :handle-malformed
  (fn [ctx]
    (:error ctx)))

(register-routes api-image-routes
  (ANY "/api/image" {params :params} (render-image params)))