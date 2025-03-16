(defproject com.kimbsy/quip "4.0.5"
  :description "A 2D game library based on Quil"
  :url "https://github.com/Kimbsy/quip"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [quil "4.3.1563"]
                 [org.clojure/math.combinatorics "0.3.0"]]
  :plugins [[lein-codox "0.10.8"]
            [lein-ancient "1.0.0-RC3"]]
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo/"
                                    :username      :env/CLOJARS_USER
                                    :password      :env/CLOJARS_PASS
                                    :sign-releases false}]])
