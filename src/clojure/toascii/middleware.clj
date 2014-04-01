(ns toascii.middleware
  (:require [taoensso.timbre :refer [log]]
            [toascii.util :refer [get-throwable-stack-trace]]
            [toascii.views.layout :as layout]))

(defn api-request? [request]
  (let [api-route-prefix (-> (:context request)
                             (str "/api")
                             (.replace "//" "/"))
        uri              (:uri request)]
    (.startsWith uri api-route-prefix)))

(defn wrap-exceptions [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception ex
        (log :error ex "Unhandled exception.")
        (if (api-request? request)
          {:status 500
           :content-type "text/plain"
           :body (.toString ex)}
          (layout/render-response
            request
            "error.html"
            :params {:stacktrace (get-throwable-stack-trace ex)}
            :status 500))))))