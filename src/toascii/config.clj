(ns toascii.config
  (:import (java.io PushbackReader))
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def site-config (atom nil))

(defn load-config! []
  (reset! site-config
          (with-open [r (PushbackReader. (io/reader (io/resource "site.config")))]
            (edn/read r))))

(defn config-val [key]
  (get @site-config key))

(defn config-val-in [ks]
  (get-in @site-config [ks]))
