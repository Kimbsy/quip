(defproject delays "0.1.0-SNAPSHOT"
  :description "An example game for quip"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [quip "2.0.2"]]
  :main ^:skip-aot delays.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
