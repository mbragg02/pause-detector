(defproject pause-detector "0.1.0-SNAPSHOT"
  :description "A program to detect abnormal pauses in pre-recorded audio files"
  :url "https://github.com/mbragg02/pause-detector"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha19"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/data.csv "0.1.4"]
                 [org.clojure/core.async "0.3.443"]
                 [environ "1.1.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.apache.commons/commons-math3 "3.6.1"]
                 [incanter "1.5.7"]
                 [clj-ml "0.0.3-SNAPSHOT"]]
  :main ^:skip-aot pause-detector.app
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/test.check "0.9.0"]]}})
