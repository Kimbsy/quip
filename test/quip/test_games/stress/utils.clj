(ns quip.test-games.stress.utils
  (:require [quil.core :as q]
            [quip.collision :as qpcollision]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(defn big-captain
  []
  (let [animations {:jump {:frames      7
                           :y-offset    3
                           :frame-delay 3}}
        rand-pos   [(- (rand-int (q/width)) 120)
                    (- (rand-int (q/height)) 180)]]
    (-> (qpsprite/animated-sprite :big-captain
                                  rand-pos
                                  240
                                  360
                                  "img/captain-big.png"
                                  :animations animations
                                  :current-animation :jump)
        (assoc :collisions 0))))

(defn draw-box
  [{[x y] :pos}]
  (q/fill 255 0 0)
  (q/rect x y 20 20))

(defn basic-collision-sprite
  []
  {:sprite-group :hit-me
   :pos          [(- (rand-int (q/width)) 120)
                  (- (rand-int (q/height)) 180)]
   :collisions   0
   :w            20
   :h            20
   :update-fn    identity
   :draw-fn      draw-box})

(defn add-captain
  [sprites]
  (cons (big-captain) sprites))

(defn add-collision-sprite
  [sprites]
  (cons (basic-collision-sprite) sprites))

(defn basic-collider-fn
  [s]
  (update s :collisions inc))

(defn removing-collider-fn
  [_]
  nil)

(defn basic-collider
  ([]
   (basic-collider qpu/w-h-rects-collide?))
  ([collision-detection-fn]
   (qpcollision/collider :big-captain :hit-me basic-collider-fn basic-collider-fn
                         :collision-detection-fn collision-detection-fn)))

(defn remove-a-collider
  []
  (qpcollision/collider :big-captain :hit-me removing-collider-fn basic-collider-fn))

(defn remove-ab-collider
  []
  (qpcollision/collider :big-captain :hit-me removing-collider-fn removing-collider-fn))
