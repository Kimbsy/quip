(ns mouse-controls.core
  (:gen-class)
  (:require [quip.core :as qp]
            [mouse-controls.scenes.level-01 :as level-01]))

(defn setup
  "The initial state of the game"
  []
  {:selecting?      false
   :selection-start [0 0]})

(defn init-scenes
  "Map of scenes in the game"
  []
  {:level-01 (level-01/init)})

;; Configure the game
(def mouse-controls-game
  (qp/game {:title          "Mouse Controls Example"
            :init-scenes-fn init-scenes
            :current-scene  :level-01
            :setup          setup}))

(defn -main
  "Run the game"
  [& args]
  (qp/run mouse-controls-game))
