(defproject input-handling "0.1.0"
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [quip "4.0.0"]]
  :main ^:skip-aot input-handling.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})