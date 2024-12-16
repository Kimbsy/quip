(defproject clickable-sprites "0.1.0"
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [com.kimbsy/quip "4.0.2"]]
  :main ^:skip-aot clickable-sprites.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
