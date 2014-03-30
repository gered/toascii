(ns toascii.util
  (:require [clojure.string :as str]
            [clojure.stacktrace :refer [print-stack-trace]]))

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