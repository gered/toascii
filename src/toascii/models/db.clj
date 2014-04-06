(ns toascii.models.db
  (:require [com.ashafa.clutch :as couch]
            [cemerick.url :as url]
            [toascii.config :refer [config-val]]))

(defn db-url [db-name]
  (let [db-config (config-val :database)
        url       (:url db-config)
        user      (:user db-config)
        pass      (:pass db-config)]
    (if (and user pass)
      (assoc (url/url url db-name)
        :username user
        :password pass)
      (url/url url db-name))))

(defn db-library []
  (db-url "ascii_library"))

(defn check-status []
  (as-> {} x
        (assoc x (str (db-library)) (couch/database-info-with-db (db-library)))
        (filter #(nil? (second %)) x)
        (map first x)
        (seq x)))

