(ns collision-detection.core
  (:gen-class)
  (:require [quip.core :as qp]
            [collision-detection.scenes.level-01 :as level-01]))

(defn init-scenes
  "Map of scenes in the game"
  []
  {:level-01 (level-01/init)})

;; Configure the game
(def collision-detection-game
  (qp/game {:title          "Collision Detection Example"
            :init-scenes-fn init-scenes
            :current-scene  :level-01}))

(defn -main
  "Run the game"
  [& args]
  (qp/start! collision-detection-game))
