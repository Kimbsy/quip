(ns shape-warps.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [quip.utils :as qpu]))

(def dark-green [48 58 43])
(def cyan [88 252 236])
(def pink [255 153 201])

(def max-n 12)

(defn angles
  [n]
  (vec (take max-n
             (concat 
              (range 0 360 (/ 360 n))
              (repeat 360)))))

(defn points
  [pos radius point-angles]
  (->> point-angles
       (map (fn [angle]
              (map (partial * radius) (qpu/direction-vector angle))))
       (map (fn [p]
              (map + pos p)))))

(defn draw-multigon
  "We have 12 points (regardless of shape), we use as many as required
  to draw the shape, and keep the rest at 360."
  [{:keys [pos radius point-angles]} & {:keys [color] :or {color cyan}}]
  (qpu/fill color)
  (q/begin-shape)
  (doseq [[x y] (points pos radius point-angles)]
    (q/vertex x y))
  (q/end-shape))

(defn multigon-tween
  [i a]
  (qptween/tween
   :point-angles
   a
   :update-fn (fn [point-angles d]
                (update point-angles i (partial + d)))
   :step-count 50
   :easing-fn qptween/ease-in-out-cubic))

(defn tween-to
  "Add tweens to each point-angle in the multigon based on the angles
  for an `n` sided multigon."
  [multigon n]
  (reduce (fn [m [i a]]
            (qptween/add-tween m (multigon-tween i a)))
          multigon
          (map vector (range) (map - (angles n) (:point-angles multigon)))))

(defn handle-multigon-key
  [state e]
  (let [n (read-string (name (:key e)))
        n (if (< n 3) (+ n 10) n)]
    (prn n)
    (qpsprite/update-sprites-by-pred
     state
     (qpsprite/group-pred :multigon)
     (fn [m]
       (tween-to m n)))))

(defn multigon
  [pos]
  {:sprite-group :multigon
   :uuid         (java.util.UUID/randomUUID)
   :pos          pos
   :radius       200
   :point-angles (angles 3)
   :update-fn    identity
   :draw-fn      draw-multigon})

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(multigon [(* (q/width) 0.5) (* (q/height) 0.5)])])

(defn draw-level-01
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background dark-green)
  (qpsprite/draw-scene-sprites state))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      qpsprite/update-scene-sprites
      qptween/update-sprite-tweens))

(defn key-pressed
  [state e]
  (cond
    (#{:0 :1 :2 :3 :4 :5 :6 :7 :8 :9} (:key e))
    (handle-multigon-key state e)
    :else state))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01
   :update-fn update-level-01
   :key-pressed-fns [key-pressed]})
