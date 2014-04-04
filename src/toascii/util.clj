(ns toascii.util
  (:import (java.net URL))
  (:require [clojure.string :as str]
            [clojure.stacktrace :refer [print-stack-trace]]
            [cemerick.url :as url]))

(defn get-filename-without-ext [^String filename]
  (let [idx (.lastIndexOf filename ".")]
    (if-not (neg? idx)
      (subs filename 0 idx)
      filename)))

(defn get-throwable-stack-trace [throwable]
  (if throwable
    (with-out-str
      (print-stack-trace throwable))))

(defn log-formatter [{:keys [level throwable message timestamp hostname ns]} & [{:keys [nofonts?] :as appender-fmt-output-opts}]]
  (format "%s %s %s [%s] - %s%s"
          timestamp hostname (-> level name str/upper-case) ns (or message "")
          (or (get-throwable-stack-trace throwable) "")))

(defn encode-url-path-components
  "Gotta love the Java standard library. If you try to create a java.net.URL or java.net.URI using a URL string that
   has some part of the path component _not_ url encoded already (e.g. space characters instead of %20), then it throws
   an exception. There is no easy way to URL encode only that part of the URL (you end up fucking up the 'http://' if
   you use any of the normal URL encoding methods). In addition! If you are accepting URL parameters in the query
   string, the path component will be decoded (space characters instead of %20) ... so you can't use URL parameters
   received via the query string directly with a java.net.URL or java.net.URI! wowee!

   This function takes a cemerick.url/URL and is basically a copy of the toString function from that protocol, except
   that the :path value gets encoded (but we are careful not to fuck up the forward slashes!)"
  [{:keys [protocol username password host port path query anchor] :as url}]
  (when url
    (let [
          creds (if username (str username ":" password))]
      (str protocol "://"
           creds
           (when creds \@)
           host
           (when (and (not= nil port)
                      (not= -1 port)
                      (not (and (== port 80) (= protocol "http")))
                      (not (and (== port 443) (= protocol "https"))))
             (str ":" port))
           (when path
             (as-> path x
                   (str/split x #"/")
                   (map url/url-encode x)
                   (str/join "/" x)))
           (when (seq query)
             (str \? (if (string? query)
                       query
                       (url/map->query query))))
           (when anchor (str \# anchor))))))

(defn query-param-url->java-url
  (^URL [^String url]
  (->> url
       (url/url)
       (encode-url-path-components)
       (URL.))))

(defn parse-int [s]
  (try
    (Integer/parseInt s)
    (catch Exception ex)))

(defn parse-boolean [x]
  (let [x (if (string? x)
            (-> x (.toLowerCase) (.trim))
            x)]
    (condp = x
      "false" false
      false   false
      "0"     false
      0       false
      nil     false
      true)))