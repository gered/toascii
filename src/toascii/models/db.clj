(ns toascii.models.db
  (:require [com.ashafa.clutch :as couch]
            [cemerick.url :as url]
            [toascii.config :refer [config-val]]
            [toascii.util :refer [now sha256]]))

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

(defn existing-ascii-art? [name hash]
  (->> (couch/get-view-with-db (db-library) "search" "hashes" {:key [name hash]})
       (seq)))

(defn add-ascii-art [name ascii ip]
  (let [hash (sha256 ascii)]
    (println name hash)
    (if (existing-ascii-art? name hash)
      (throw (new Exception (str "Existing ASCII art with same name and art found")))
      (couch/put-document-with-db
        (db-library)
        {:name    name
         :date    (now)
         :ip      ip
         :format  "ascii"
         :hash    (sha256 ascii)
         :content ascii}))))