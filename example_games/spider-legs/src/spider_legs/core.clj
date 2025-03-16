(ns spider-legs.core
  (:gen-class)
  (:require [quip.core :as qp]
            [spider-legs.scenes.level-01 :as level-01]))

(defn setup
  "The initial state of the game"
  []
  {:moving? false})

(defn init-scenes
  "Map of scenes in the game"
  []
  {:level-01 (level-01/init)})

;; Configure the game
(def spider-legs-game
  (qp/game {:title          "spider-legs"
            :size           [800 600]
            :setup          setup
            :init-scenes-fn init-scenes
            :current-scene  :level-01}))

(defn -main
  "Run the game"
  [& args]
  (qp/start! spider-legs-game))
