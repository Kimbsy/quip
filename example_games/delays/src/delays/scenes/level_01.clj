(ns delays.scenes.level-01
  (:require [quil.core :as q]
            [quip.delay :as delay]
            [quip.sprite :as sprite]
            [quip.tween :as tween]
            [quip.util :as u]))

(def blue [0 153 255])

(def spin-tween
  (tween/tween
   :rotation
   360))

(defn captain
  [pos]
  (sprite/animated-sprite
   :captain
   pos
   240
   360
   "img/captain.png"
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
   :current-animation :idle))

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(captain [150 180])])

(defn print-current-frame
  "All delayed functions should take the game state and return an
  (optionally) modified version of it."
  [state]
  (prn (str "Current frame: " (:global-frame state)))
  state)

(defn delays
  "Define the delays which exist as the scene begins."
  []
  [;; We can have a simple side-effecting delayed function:
   (delay/delay 100 print-current-frame)

   ;; We can add new sprites into the scene:
   (delay/add-sprites-to-scene 200 [(captain [(rand-int (q/width))
                                              (rand-int (q/height))])])

   ;; We can add tweens to sprites in the scene:
   (delay/add-tween-to-sprites 300
                               spin-tween
                               (fn [s]
                                 (= :captain (:sprite-group s))))

   ;; We can even delay adding new delays to the scene:
   (delay/delay
     400
     (fn [state]
       (reduce delay/add-delay
               state
               ;; add the same delays again!
               (delays))))])

(defn draw-level-01
  "Called each frame, draws the current scene to the screen"
  [state]
  (u/background blue)
  (sprite/draw-scene-sprites state))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      sprite/update-state
      tween/update-state
      delay/update-state))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01
   :update-fn update-level-01
   :delays    (delays)})
