(defproject tweens "0.1.0"
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [com.kimbsy/quip "4.0.5"]
                 [criterium "0.4.6"]]
  :main ^:skip-aot tweens.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
