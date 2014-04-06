(ns toascii.models.art
  (:require [toascii.models.db :as db]
            [toascii.util :refer [convert-line-endings remove-leading-and-trailing-blank-lines]]))

(defn- prep-ascii-art [s]
  (-> s
      (convert-line-endings)
      (remove-leading-and-trailing-blank-lines)))

(defn valid-name? [name]
  (if-not (nil? name)
    (re-matches #"[A-Za-z0-9\-]+" name)))

(defn search [query]
  )

(defn get-count [name]
  )

(defn get-index [name index]
  )

(defn get-random [name]
  )

(defn add [name s ip]
  (if-not (valid-name? name)
    (throw (new Exception "Invalid name")))
  (let [prepped-ascii (prep-ascii-art s)]
    (db/add-ascii-art name prepped-ascii ip)))
