(ns collision-detection.core
  (:gen-class)
  (:require [quil.core :as q]
            [quip.core :as qp]
            [quip.collision :as qpcollision]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(def grey [44 49 44])
(def green [154 225 157])
(def pink [255 116 119])

(defn square
  "A simple sprite"
  [group pos vel size color]
  {:sprite-group group
   :pos pos
   :vel vel
   :w size
   :h size
   :color color
   :update-fn (fn [{p :pos v :vel :as b}]
                (assoc b :pos (mapv + p v)))
   :draw-fn (fn [{[x y] :pos c :color w :w h :h}]
              (qpu/fill c)
              (let [offset (/ w 2)]
                (q/rect (- x offset) (- y offset) w h)))})


(defn colliders
  "Returns the list of colliders for the scene"
  []
  [(qpcollision/collider
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

(defn update-level-01
  [state]
  (-> state
      qpsprite/update-scene-sprites
      ;; NOTE: you must update collisions for them to work
      qpcollision/update-collisions))

(defn draw-level-01
  [state]
  (qpu/background grey)
  (qpsprite/draw-scene-sprites state))

(defn init-scenes
  "Map of scenes in the game"
  []
  {:level-01 {:sprites [(square :greens [400 300] [5 0] 100 green)
                        (square :reds [300 300] [0 0] 100 pink)
                        (square :reds [500 300] [0 0] 100 pink)]
              :colliders (colliders)
              :update-fn update-level-01
              :draw-fn draw-level-01}})

(def collision-detection-game
  (qp/game {:title          "collision-detection"
            :size           [800 600]
            :init-scenes-fn init-scenes
            :current-scene  :level-01}))

(defn -main
  [& args]
  (qp/run collision-detection-game))
