(defproject fabrik "0.1.0"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [quip "2.0.6"]]
  :main ^:skip-aot fabrik.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
