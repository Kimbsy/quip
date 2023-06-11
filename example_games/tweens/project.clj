(defproject tweens "0.1.0"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [quip "2.0.4"]
                 [criterium "0.4.6"]]
  :main ^:skip-aot tweens.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
