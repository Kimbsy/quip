(ns collision-detection.scenes.level-01
  (:require [quil.core :as q]
            [quip.collision :as collision]
            [quip.sprite :as sprite]
            [quip.util :as u]))

(def grey [44 49 44])
(def green [154 225 157])
(def pink [255 116 119])

(defn square
  "A simple sprite"
  [group pos vel size color]
  (sprite/sprite
   group
   pos
   :vel vel
   :w size
   :h size
   :update-fn (fn [{p :pos v :vel :as b}]
                (assoc b :pos (mapv + p v)))
   :draw-fn (fn [{[x y] :pos c :color w :w h :h}]
              (u/fill c)
              (let [offset (/ w 2)]
                (q/rect (- x offset) (- y offset) w h)))
   :extra {:color color}))

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(square :greens [400 300] [5 0] 100 green)
   (square :reds [300 300] [0 0] 100 pink)
   (square :reds [500 300] [0 0] 100 pink)])

(defn colliders
  "Returns the list of colliders for the scene"
  []
  [(collision/collider
    :greens  ;; collision group `a`
    :reds    ;; collision group `b`

    ;; collision function `a`, returns updated `a`
    (fn [g r]
      ;; reverse x direction of green
      (update-in g [:vel 0] #(* % -1)))

    ;; collision function `b`, retuns updated `b`
    (fn [r g]
      ;; bump red a little
      (let [distance (- (get-in r [:pos 0])
                        (get-in g [:pos 0]))
            direction (if (neg? distance) -1 1)]
        (update-in r [:pos 0] #(+ % (* direction 10))))))

   ;; ... other colliders
   ])

(defn draw-level-01!
  "Called each frame, draws the current scene to the screen"
  [state]
  (u/background grey)
  (sprite/draw-scene-sprites! state))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      sprite/update-state
      ;; NOTE: you must update collisions for them to work
      collision/update-state))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :colliders (colliders)
   :draw-fn draw-level-01!
   :update-fn update-level-01})
