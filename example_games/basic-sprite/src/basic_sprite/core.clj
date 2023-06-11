(ns basic-sprite.core
  (:gen-class)
  (:require [quip.core :as qp]
            [basic-sprite.scenes.level-01 :as level-01]))

(defn init-scenes
  "Map of scenes in the game"
  []
  {:level-01 (level-01/init)})

;; Configure the game
(def basic-sprite-game
  (qp/game {:title          "Basic Sprite Example"
            :init-scenes-fn init-scenes
            :current-scene  :level-01}))

(defn -main
  "Run the game"
  [& args]
  (qp/run basic-sprite-game))
