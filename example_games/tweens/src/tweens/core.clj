(ns tweens.core
  (:gen-class)
  (:require [quip.core :as qp]
            [tweens.scenes.level-01 :as level-01]))

(defn init-scenes
  "Map of scenes in the game"
  []
  {:level-01 (level-01/init)})

;; Configure the game
(def tweens-game
  (qp/game {:title          "tweens"
            :init-scenes-fn init-scenes
            :current-scene  :level-01}))

(defn -main
  "Run the game"
  [& args]
  (qp/run! tweens-game))
