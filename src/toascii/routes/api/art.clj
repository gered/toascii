(ns toascii.routes.api.art
  (:require [liberator.core :refer [defresource]]
            [compojure.core :refer [ANY]]
            [toascii.route-utils :refer [register-routes]]
            [toascii.models.art :as art]))

(defresource art-search [q]
  :available-media-types ["application/json"]
  :malformed?
  (fn [_]
    (if-not (art/valid-name? q)
      {:error "Invalid name."}))
  :handle-ok
  (fn [_]
    (art/search q))
  :handle-malformed
  (fn [ctx]
    (:error ctx)))

(defresource get-random-art [{:keys [name format]}]
  :media-type-available?
  (fn [ctx]
    (let [type (condp = format
                 "html" "text/html"
                 "text" "text/plain"
                 "text/plain")]
      {:representation {:media-type type}}))
  :malformed?
  (fn [_]
    (if-not (art/valid-name? name)
      {:error "Invalid name."}))
  :exists?
  (fn [_]
    (if-let [ascii (art/get-random name)]
      {:ascii ascii}
      [false {:error "No art found under that name."}]))
  :handle-ok
  (fn [ctx]
    (if (= "text/html" (get-in ctx [:representation :media-type]))
      (str "<pre>" (:ascii ctx) "</pre>")
      (:ascii ctx)))
  :handle-malformed
  (fn [ctx]
    (:error ctx))
  :handle-not-found
  (fn [ctx]
    (:error ctx)))

(defresource get-art-count-by [{:keys [name]}]
  :available-media-types ["application/json"]
  :malformed?
  (fn [_]
    (if-not (art/valid-name? name)
      {:error "Invalid name."}))
  :exists?
  (fn [_]
    (if-let [num (art/get-count name)]
      {:count num}
      [false {:error "No art found under that name."}]))
  :handle-ok
  (fn [ctx]
    (str (:count ctx)))
  :handle-malformed
  (fn [ctx]
    (:error ctx))
  :handle-not-found
  (fn [ctx]
    (:error ctx)))

(register-routes api-art-routes
  (ANY "/api/art/:name/count" {params :params} (get-art-count-by params))
  (ANY "/api/art/:name" {params :params} (get-random-art params))
  (ANY "/api/art" {{q :q} :params} (art-search q)))
