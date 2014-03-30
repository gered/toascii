(ns toascii.middleware
  (:require [taoensso.timbre :refer [log]]))

(defn wrap-exceptions [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception ex
        (log :error ex "Unhandled exception lol.")
        (println (:uri request) (:context request))
        {:status 500
         :body "An error occurred! oh noes!"}))))