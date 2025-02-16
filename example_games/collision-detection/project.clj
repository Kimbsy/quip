(defproject collision-detection "0.1.0"
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [com.kimbsy/quip "4.0.4"]]
  :main ^:skip-aot collision-detection.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
