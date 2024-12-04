(ns delays.core
  (:gen-class)
  (:require [quip.core :as qp]
            [delays.scenes.level-01 :as level-01]))

(defn init-scenes
  "Map of scenes in the game"
  []
  {:level-01 (level-01/init)})

;; Configure the game
(def delays-game
  (qp/game {:title          "Delays Example"
            :init-scenes-fn init-scenes
            :current-scene  :level-01}))

(defn -main
  "Run the game"
  [& args]
  (qp/start! delays-game))
