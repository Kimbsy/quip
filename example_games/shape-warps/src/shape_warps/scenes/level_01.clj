(ns shape-warps.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as sprite]
            [quip.tween :as tween]
            [quip.util :as u]))

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
              (map (partial * radius) (u/direction-vector angle))))
       (map (fn [p]
              (map + pos p)))))

(defn draw-multigon!
  "We have 12 points (regardless of shape), we use as many as required
  to draw the shape, and keep the rest at 360."
  [{:keys [pos radius point-angles]} & {:keys [color] :or {color cyan}}]
  (u/fill color)
  (q/begin-shape)
  (doseq [[x y] (points pos radius point-angles)]
    (q/vertex x y))
  (q/end-shape))

(defn multigon-tween
  [i a]
  (tween/tween
   :point-angles
   a
   :update-fn (fn [point-angles d]
                (update point-angles i (partial + d)))
   :step-count 50
   :easing-fn tween/ease-in-out-cubic))

(defn tween-to
  "Add tweens to each point-angle in the multigon based on the angles
  for an `n` sided multigon."
  [multigon n]
  (reduce (fn [m [i a]]
            (tween/add-tween m (multigon-tween i a)))
          multigon
          (map vector (range) (map - (angles n) (:point-angles multigon)))))

(defn handle-multigon-key
  [state e]
  (let [n (read-string (name (:key e)))
        n (if (< n 3) (+ n 10) n)]
    (prn n)
    (sprite/update-sprites
     state
     (sprite/has-group :multigon)
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
   :draw-fn      draw-multigon!})

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(multigon (u/make-pos [0.5 0.5]))])

(defn draw-level-01!
  "Called each frame, draws the current scene to the screen"
  [state]
  (u/background dark-green)
  (sprite/draw-scene-sprites! state))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      sprite/update-state
      tween/update-state))

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
   :draw-fn draw-level-01!
   :update-fn update-level-01
   :key-pressed-fns [key-pressed]})
