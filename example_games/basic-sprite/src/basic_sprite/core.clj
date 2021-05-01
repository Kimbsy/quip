(ns basic-sprite.core
  (:gen-class)
  (:require [quip.core :as qp]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(defn captain
  [pos current-animation]
  (qpsprite/animated-sprite
   :captain   ; sprite-group, used for group collision detection
   pos
   240   ; <- width and
   360   ; <- height of each animation frame
   "img/captain.png"   ; spritesheet location in `resources` directory
   :animations {:none {:frames      1
                       :y-offset    0
                       :frame-delay 100}
                :idle {:frames      4
                       :y-offset    1
                       :frame-delay 15}
                :run  {:frames      4
                       :y-offset    2
                       :frame-delay 8}
                :jump {:frames      7
                       :y-offset    3
                       :frame-delay 8}}
   :current-animation current-animation))

(defn update-level-01
  "Update each sprite in the scene using its own `:update-fn`."
  [state]
  (qpscene/update-scene-sprites state))

(defn draw-level-01
  "Draw each sprite in the scene using its own `:draw-fn`."
  [state]
  (qpu/background [0 153 255])
  (qpscene/draw-scene-sprites state))

(defn init-scenes
  "Returns a map of scenes in the game."
  []
  {:level-01 {:sprites   [(captain [150 180] :run)
                          (captain [450 180] :jump)]
              :update-fn update-level-01
              :draw-fn   draw-level-01}})

(def basic-sprite-game
  (qp/game {:title          "Basic Sprite Example"
            :init-scenes-fn init-scenes
            :current-scene  :level-01}))

(defn -main
  "Run the game."
  [& args]
  (qp/run basic-sprite-game))
