(ns clickable-sprites.scenes.level-01
  (:require [quil.core :as q]
            [quip.input :as input]
            [quip.sprite :as sprite]
            [quip.util :as u]))

(def light-green [133 255 199])

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

(def animations [:none :idle :run :jump])

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(-> (captain
        [(* 0.5 (q/width))
         (* 0.5 (q/height))]
        :idle)
       ;; @TODO: would be nice to generify this `draw-bounding-box`
       ;; function and have it part of the base sprites.
       (assoc :draw-fn (fn [{:keys [pos w h] :as s}]
                         (sprite/draw-animated-sprite! s)
                         (u/stroke u/red)
                         (q/no-fill)
                         (let [offsets (sprite/pos-offsets s)
                               [x y] (map + pos offsets)]
                           (q/rect x y w h))
                         (q/no-stroke)))
       (input/on-click
        (fn [state {:keys [current-animation] :as captain}]
          (sprite/update-sprites-by-pred
           state
           (sprite/group-pred :captain)
           (fn [s]
             (let [anim (rand-nth animations)]
               (prn "new animation: " anim)
               (sprite/set-animation s anim)))))))])

(defn draw-level-01
  "Called each frame, draws the current scene to the screen"
  [state]
  (u/background light-green)
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
   :draw-fn draw-level-01
   :update-fn update-level-01})
