(ns toascii.routes.api.art
  (:require [liberator.core :refer [defresource]]
            [compojure.core :refer [ANY]]
            [toascii.route-utils :refer [register-routes]]
            [toascii.models.art :as art]))

(defresource art-search [q]
  :available-media-types ["application/json"]
  :malformed?
  (fn [_]
    (not (art/valid-name? q)))
  :handle-ok
  (fn [_]
    (art/search q)))

(register-routes api-art-routes
  (ANY "/api/art" {{q :q} :params} (art-search q)))
