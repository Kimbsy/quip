(ns game-restart.core
  (:gen-class)
  (:require [quip.core :as qp]
            [game-restart.scenes.menu :as menu]
            [game-restart.scenes.level-01 :as level-01]))

(defn setup
  "The initial state of the game"
  []
  {:high-score 0
   :current-score 0})

(defn init-scenes
  "Map of scenes in the game"
  []
  {:menu     (menu/init)
   :level-01 (level-01/init)})

(defn restart
  "Define how to restart the game, resetting scenes and sprites
  etc. Make sure you keep hold of any state that needs to persist
  between playthroughs."
  [{:keys [current-score] :as state}]
  (-> state
      (assoc :scenes (init-scenes))
      (assoc :current-scene :menu)
      (assoc :current-score 0)
      (update :high-score max current-score)))

;; Configure the game
(def game-restart-game
  (qp/game {:title          "game-restart"
            :size           [800 600]
            :setup          setup
            :restart-fn     restart
            :init-scenes-fn init-scenes
            :current-scene  :menu}))

(defn -main
  "Run the game"
  [& args]
  (qp/start! game-restart-game))
