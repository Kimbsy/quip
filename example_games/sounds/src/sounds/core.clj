(ns sounds.core
  (:gen-class)
  (:require [quip.core :as qp]
            [quip.sound :as qpsound]
            [sounds.scenes.level-01 :as level-01]))

(defn setup
  "The initial state of the game"
  []
  (qpsound/loop-music "music/music.wav")
  ;; Empty state
  {})

(defn init-scenes
  "Map of scenes in the game"
  []
  {:level-01 (level-01/init)})

;; Configure the game
(def sounds-game
  (qp/game {:title          "Music and sound effects example"
            :setup          setup
            :init-scenes-fn init-scenes
            :current-scene  :level-01}))

(defn -main
  "Run the game"
  [& args]
  (qp/run sounds-game))
