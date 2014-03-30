(ns toascii.routes.home
  (:require [toascii.route-utils :refer [register-routes]]
            [toascii.views.layout :as layout])
  (:use compojure.core))

(defn home-page []
  (layout/render
    "home.html"))

(register-routes home-routes
  (GET "/" [] (home-page)))
