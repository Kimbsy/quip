(ns basic-sprite.scenes.level-01
  (:require [quip.sprite :as sprite]
            [quip.util :as u]))

(def blue [0 153 255])

(defn captain
  [pos current-animation]
  (sprite/animated-sprite
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

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(captain [150 180] :run)
   (captain [450 180] :jump)])

(defn draw-level-01!
  "Called each frame, draws the current scene to the screen"
  [state]
  (u/background blue)
  (sprite/draw-scene-sprites! state))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      sprite/update-state))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01!
   :update-fn update-level-01})
