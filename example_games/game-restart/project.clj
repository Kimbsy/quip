(defproject game-restart "0.1.0"
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [com.kimbsy/quip "4.0.5"]]
  :main ^:skip-aot game-restart.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
