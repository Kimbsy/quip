(defproject sounds "0.1.0"
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [com.kimbsy/quip "4.0.3"]]
  :main ^:skip-aot sounds.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
