(ns toascii.models.art
  (:require [toascii.models.db :as db]
            [toascii.util :refer [convert-line-endings remove-leading-and-trailing-blank-lines parse-index]]))

(defn- prep-ascii-art [s]
  (-> s
      (convert-line-endings)
      (remove-leading-and-trailing-blank-lines)))

(defn valid-name? [name]
  (if-not (nil? name)
    (re-matches #"[a-z0-9\-]+" name)))

(defn search [query]
  (if-not (valid-name? query)
    (throw (new Exception "Invalid search term. Can only use characters valid in names.")))
  (db/find-art-names query))

(defn get-count [name]
  (if-not (valid-name? name)
    (throw (new Exception "Invalid name")))
  (db/get-art-count-by name))

(defn get-index [name index]
  (if-not (valid-name? name)
    (throw (new Exception "Invalid name")))
  (if-let [idx (parse-index index)]
    (db/get-ascii-art name idx)
    (throw (new Exception "Invalid index"))))

(defn get-random [name]
  (if-not (valid-name? name)
    (throw (new Exception "Invalid name")))
  (db/get-random-ascii-art name))

(defn add [name s ip]
  (if-not (valid-name? name)
    (throw (new Exception "Invalid name")))
  (let [prepped-ascii (prep-ascii-art s)]
    (db/add-ascii-art name prepped-ascii ip)))
