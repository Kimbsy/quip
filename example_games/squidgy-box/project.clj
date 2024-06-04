(defproject squidgy-box "0.1.0"
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [quip "2.0.7"]]
  :main ^:skip-aot squidgy-box.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
