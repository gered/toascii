(ns toascii.routes.home
  (:require [toascii.route-utils :refer [register-routes]]
            [toascii.views.layout :as layout]
            [toascii.models.flf :refer [get-font-names]])
  (:use compojure.core))

(defn home-page []
  (layout/render
    "home.html"
    :params {:fontNames (get-font-names)}))

(register-routes home-routes
  (GET "/" [] (home-page)))
