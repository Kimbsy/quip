(defproject quip "2.0.3"
  :description "A 2D game library based on Quil"
  :url "https://github.com/Kimbsy/quip"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [quil "3.1.0"]
                 [org.clojure/math.combinatorics "0.1.6"]]
  :plugins [[lein-codox "0.10.8"]]
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo/"
                                    :username      :env/clojars_user
                                    :password      :env/clojars_pass
                                    :sign-releases false}]])
