(ns input-handling.core
  (:gen-class)
  (:require [quip.core :as qp]
            [input-handling.scenes.level-01 :as level-01]))

(defn setup
  "The initial state of the game"
  []
  {})

(defn init-scenes
  "Map of scenes in the game"
  []
  {:level-01 (level-01/init)})

;; Configure the game
(def input-handling-game
  (qp/game {:title          "input-handling"
            :size           [800 600]
            :setup          setup
            :init-scenes-fn init-scenes
            :current-scene  :level-01}))

(defn -main
  "Run the game"
  [& args]
  (qp/start! input-handling-game))
