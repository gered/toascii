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

(defn get-ascii-art
  ([doc-id]
   (->> (couch/get-document-with-db (db-library) doc-id)
        :content))
  ([name index]
   (if-let [doc-id (as-> (couch/get-view-with-db (db-library) "list" "byDate" {:key name}) x
                         (sort-by :date x)
                         (nth x index nil)
                         (:id x))]
     (get-ascii-art doc-id)
     (throw (new IndexOutOfBoundsException)))))

(defn get-random-ascii-art [name]
  (->> (couch/get-view-with-db (db-library) "list" "ids" {:key name})
       (rand-nth)
       :value
       (get-ascii-art)))

(defn get-art-count []
  (->> (couch/get-view-with-db (db-library) "list" "count")
       (first)
       :value))

(defn get-art-count-by [name]
  (->> (couch/get-view-with-db (db-library) "list" "count" {:key name :group true})
       (first)
       :value))

(defn find-art-names [query]
  ; HACK: to mimic a "starts with prefix" type of search with couchdb, we append unicode character \u9999
  ;       to the search term and use it as the end key, effectively meaning "find all matches with keys
  ;       between 'prefix' and 'prefix\u9999'" which works because keys will be sorted lexicographically
  ;       by couchdb
  (->> (couch/get-view-with-db (db-library) "list" "uniqueNames" {:startkey query :endkey (str query "\u9999") :group true})
       (map :key)))