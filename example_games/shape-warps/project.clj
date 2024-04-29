(defproject shape-warps "0.1.0"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [quip "2.0.4"]]
  :main ^:skip-aot shape-warps.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
