(ns clickable-sprites.core
  (:gen-class)
  (:require [clickable-sprites.scenes.level-01 :as level-01]
            [clickable-sprites.scenes.menu :as menu]
            [quip.core :as qp]))

(defn setup
  "The initial state of the game"
  []
  {})

(defn init-scenes
  "Map of scenes in the game"
  []
  {:menu     (menu/init)
   :level-01 (level-01/init)})

;; Configure the game
(def clickable-sprites-game
  (qp/game {:title          "clickable-sprites"
            :size           [800 600]
            :setup          setup
            :init-scenes-fn init-scenes
            :current-scene  :menu}))

(defn -main
  "Run the game"
  [& args]
  (qp/start! clickable-sprites-game))
