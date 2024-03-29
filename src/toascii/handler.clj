(ns toascii.handler
  (:require [clojure.string :as str]
            [compojure.core :refer [defroutes]]
            [compojure.route :as route]
            [noir.util.middleware :refer [app-handler]]
            [taoensso.timbre :refer [log set-config!]]
            [environ.core :refer [env]]
            [clj-jtwig.core :as jtwig]
            [clj-jtwig.web.middleware :refer [wrap-servlet-context-path]]
            [toascii.route-utils :refer [find-routes]]
            [toascii.models.db :as db]
            [toascii.models.flf :as flf]
            [toascii.util :refer [log-formatter]]
            [toascii.config :refer [load-config!]]
            [toascii.middleware :refer [wrap-exceptions]]))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defonce ring-app (atom nil))

(defn handle-app [request]
  (@ring-app request))

(defn init []
  (set-config! [:shared-appender-config :spit-filename] "toascii.log")
  (set-config! [:appenders :spit :enabled?] true)
  (set-config! [:fmt-output-fn] log-formatter)

  (log :info "Starting up ...")

  (load-config!)

  (log :info "Checking DB status ...")
  (if-let [missing-dbs (db/check-status)]
    (log :error (str "Databases missing or not available: " (str/join "\n" missing-dbs)))
    (log :info "DB status check passed."))

  (reset! ring-app
          (app-handler
            (find-routes "toascii.routes." app-routes)
            :middleware [wrap-exceptions wrap-servlet-context-path]
            :access-rules []
            :formats [:json-kw :edn]))

  (when (env :dev)
    (log :info "Dev environment. Template caching disabled.")
    (jtwig/toggle-compiled-template-caching! false))

  (flf/load-all!))

(defn destroy []
  (log :info "Shutting down ..."))
