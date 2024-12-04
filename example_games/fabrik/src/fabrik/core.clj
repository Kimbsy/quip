(ns fabrik.core
  (:gen-class)
  (:require [quip.core :as qp]
            [fabrik.scenes.level-01 :as level-01]))

(defn setup
  "The initial state of the game"
  []
  {})

(defn init-scenes
  "Map of scenes in the game"
  []
  {:level-01 (level-01/init)})

;; Configure the game
(def fabrik-game
  (qp/game {:title          "fabrik"
            :size           [800 600]
            :setup          setup
            :init-scenes-fn init-scenes
            :current-scene  :level-01}))

(defn -main
  "Run the game"
  [& args]
  (qp/run! fabrik-game))
