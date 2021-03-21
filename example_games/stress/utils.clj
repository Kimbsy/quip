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

(defn big-captain-complex
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
                                  :current-animation :jump
                                  :points (map #(map + % rand-pos)
                                               [[0 0] [14 0] [14 9] [12 9] [5 1] [12 1] [4 9] [0 9]]))
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

(defn complex-collision-sprite
  []
  (let [rand-pos [(- (rand-int (q/width)) 120)
                  (- (rand-int (q/height)) 180)]]
    {:sprite-group :hit-me
     :pos          rand-pos
     :collisions   0
     :w            20
     :h            20
     :update-fn    identity
     :draw-fn      draw-box
     :points       (map #(map + % rand-pos)
                        [[0 0] [10 0] [20 0] [20 20] [10 20] [0 20]])}))

(defn add-captain
  [sprites]
  (cons (big-captain) sprites))

(defn add-captain-complex
  [sprites]
  (cons (big-captain-complex) sprites))

(defn add-collision-sprite
  [sprites]
  (cons (basic-collision-sprite) sprites))

(defn add-collision-sprite-complex
  [sprites]
  (cons (complex-collision-sprite) sprites))

(defn basic-collider-fn
  [s _]
  (update s :collisions inc))

(defn removing-collider-fn
  [_]
  nil)

(defn basic-collider
  ([]
   (basic-collider qpcollision/w-h-rects-collide?))
  ([collision-detection-fn]
   (qpcollision/collider :big-captain :hit-me basic-collider-fn basic-collider-fn
                         :collision-detection-fn collision-detection-fn)))

(defn remove-a-collider
  []
  (qpcollision/collider :big-captain :hit-me removing-collider-fn basic-collider-fn))

(defn remove-ab-collider
  []
  (qpcollision/collider :big-captain :hit-me removing-collider-fn removing-collider-fn))
