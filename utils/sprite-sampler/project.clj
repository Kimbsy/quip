(defproject sprite-sampler "1.0.0"
  :description "Utility for testing sprites"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [quip "1.0.5"]]
  :main ^:skip-aot sprite-sampler.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
