(defproject toascii "0.1.0-SNAPSHOT"
  :description "Web site and REST api for http://toascii.net/"
  :url "https://github.com/gered/toascii"
  :dependencies
  [[org.clojure/clojure "1.6.0"]
   [lib-noir "0.8.1"]
   [compojure "1.1.6"]
   [ring-server "0.3.1"]
   [liberator "0.11.0"]
   [cheshire "5.3.1"]
   [com.taoensso/timbre "3.1.6"]
   [clj-jtwig-java6 "0.3.2"]
   [environ "0.4.0"]
   [clj-metasearch "0.1.1"]
   [clj-figlet "0.1.1"]]
  :main         main
  :plugins      [[lein-ring "0.8.10"]
                 [lein-environ "0.4.0"]]
  :ring         {:handler toascii.handler/app
                 :init    toascii.handler/init
                 :destroy toascii.handler/destroy}
  :profiles     {:uberjar    {:aot :all}
                 :production {:ring {:open-browser? false
                                     :stacktraces?  false
                                     :auto-reload?  false}}
                 :repl       {:source-paths ["dev"]}
                 :dev        {:env          {:dev true}
                              :dependencies [[ring-mock "0.1.5"]
                                             [ring/ring-devel "1.2.2"]]}}
  :min-lein-version "2.0.0")