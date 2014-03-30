(ns toascii.routes.api.text
  (:require [clojure.string :as str]
            [liberator.core :refer [defresource]]
            [compojure.core :refer [ANY]]
            [clj-figlet.core :as figlet]
            [toascii.route-utils :refer [register-routes]]
            [toascii.models.flf :refer [get-font]]))

(defresource render-text [{:keys [s font format] :as params}]
  :media-type-available?
  (fn [ctx]
    (let [type (condp = format
                 "html" "text/html"
                 "text" "text/plain"
                 "text/plain")]
      {:representation {:media-type type}}))
  :malformed?
  (fn [_]
    (cond
      (str/blank? s) {:error "Missing text to render"}))
  :exists?
  (fn [ctx]
    (let [font-name (or font "standard")]
      (if-let [flf (get-font font-name)]
        {:flf flf}
        [false {:error "Font not found."}])))
  :handle-ok
  (fn [ctx]
    (let [rendered (figlet/render-to-string (:flf ctx) s)]
      (if (= "text/html" (get-in ctx [:representation :media-type]))
        (str "<pre>" rendered "</pre>")
        rendered)))
  :handle-malformed
  (fn [ctx]
    (:error ctx))
  :handle-not-found
  (fn [ctx]
    (:error ctx)))

(register-routes api-text-routes
  (ANY "/api/text" {params :params} (render-text params)))
