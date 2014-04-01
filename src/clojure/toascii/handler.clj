(ns toascii.handler
  (:require [compojure.core :refer [defroutes]]
            [compojure.route :as route]
            [noir.util.middleware :refer [app-handler]]
            [taoensso.timbre :refer [log set-config!]]
            [environ.core :refer [env]]
            [clj-jtwig.core :as jtwig]
            [clj-jtwig.web.middleware :refer [wrap-servlet-context-path]]
            [toascii.route-utils :refer [find-routes]]
            [toascii.models.flf :as flf]
            [toascii.util :refer [log-formatter]]
            [toascii.middleware :refer [wrap-exceptions]]))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defonce routes (find-routes "toascii.routes." app-routes))

(def app (app-handler
           routes
           :middleware [wrap-exceptions wrap-servlet-context-path]
           :access-rules []
           :formats [:json-kw :edn]))

(defn init []
  (set-config! [:shared-appender-config :spit-filename] "toascii.log")
  (set-config! [:appenders :spit :enabled?] true)
  (set-config! [:fmt-output-fn] log-formatter)

  (log :info "toascii started successfully")

  (when (env :dev)
    (log :info "Dev environment. Template caching disabled.")
    (jtwig/toggle-compiled-template-caching! false))

  (flf/load-all!))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (log :info "toascii is shutting down..."))
