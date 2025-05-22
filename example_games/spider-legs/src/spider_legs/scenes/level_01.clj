(ns spider-legs.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as sprite]
            [quip.scene :as scene]
            [quip.tween :as tween]
            [quip.util :as u]))

(def light-green [133 255 199])
(def dark-green [41 115 115])
(defn rand-color [] [(rand-int 255) (rand-int 255) (rand-int 255)])

(def leg-color (u/hex->rgb "#8ACDEA"))
(def foot-color (u/hex->rgb "#F84AA7"))
(def body-color (u/hex->rgb "#43B929"))
(def eye-color (u/hex->rgb "#18430F"))

(def min-length 50)
(def max-length 160)
(def max-angle 45)

(def ls 100)
(def lt 60)

(defn move-foot-tween-x
  [idx [fx _] [tx _]]
  (tween/tween :feet
               tx
               :from-value fx
               :step-count (u/ms->frames 80)
               :update-fn (fn [feet d]
                            (update-in feet
                                       [idx :pos 0]
                                       + d))
               :on-complete-fn (fn [spider]
                                 (update spider :moving-feet
                                         assoc idx false))))

(defn move-foot-tween-y
  [idx [_ fy] [_ ty]]
  (tween/tween :feet
               ty
               :from-value fy
               :step-count (u/ms->frames 80)
               :update-fn (fn [feet d]
                            (update-in feet
                                       [idx :pos 1]
                                       + d))
               ;; @NOTE don't need the on-complete for Y, since X
               ;; already does it.
               ))

(defn length
  [[ax ay] [bx by]]
  (Math/sqrt (+ (Math/pow (- ax bx) 2)
                (Math/pow (- ay by) 2))))

(defn should-move?
  [spider-pos foot-pos reset-pos]
  (let [leg-length (length spider-pos foot-pos)]
    (or (< leg-length min-length)
        (< max-length leg-length)
        (< max-angle
           (abs (- (u/rotation-angle (map - foot-pos spider-pos))
                   (u/rotation-angle (map - reset-pos spider-pos))))))))

(defn update-spider
  [{:keys [feet pos moving-feet] :as spider}]
  (let [movements (keep-indexed
                   (fn [i {:keys [reset-offset]
                           foot-pos :pos
                           :as foot}]
                     (let [reset-pos (map + pos reset-offset)]
                       (when (and (not (moving-feet i))
                                  (should-move? pos foot-pos reset-pos))
                         (let [target-pos (map +
                                               foot-pos
                                               (map #(* 1.5 %)
                                                    (map - reset-pos foot-pos)))]
                           [i [(move-foot-tween-x i foot-pos target-pos)
                               (move-foot-tween-y i foot-pos target-pos)]]))))
                   feet)]
    (reduce (fn [acc-spider [i [tween-x tween-y]]]
              (-> acc-spider
                  (tween/add-tween tween-x)
                  (tween/add-tween tween-y)
                  (update :moving-feet assoc i true)))
            spider
            movements)))

(defn draw-leg
  [ls lt [fx fy :as f] [bx by :as b] k1?]
  (let [l (length f b)

        ;; d is the unit vector from f to b
        [dx dy :as d] [(/ (- bx fx) l)
                       (/ (- by fy) l)]

        ;; a is the distance form f to the intersection midpoint
        a (/ (+ (* ls ls)
                (- (* lt lt))
                (* l l))
             (* l 2))

        ;; p is the point along the line f->b at the intersection point
        [px py :as p] (mapv + f (mapv #(* a %) d))

        ;; perp is perpendicular to p
        perp [(- dy) dx]

        ;; h is height of k above p
        h (Math/sqrt (- (* ls ls)
                        (* a a)))

        ;; kn are the points for the knee
        [k1x k1y :as k1] (mapv + p (mapv #(* h %) perp))
        [k2x k2y :as k2] (mapv - p (mapv #(* h %) perp))]

    ;; draw leg
    (q/stroke leg-color)
    (q/stroke-weight 4)
    (if k1?
      (do (q/line bx by k1x k1y)
          (q/line bx by k1x k1y)
          (q/line k1x k1y fx fy)
          (q/line k1x k1y fx fy))
      (do (q/line bx by k2x k2y)
          (q/line bx by k2x k2y)
          (q/line k2x k2y fx fy)
          (q/line k2x k2y fx fy)))
    (q/no-stroke)

    ;; draw foot
    (q/fill foot-color)
    (q/ellipse fx fy 10 10)
    
    ;; ;; draw intersection point
    ;; (q/ellipse px py 10 10)

    ;; ;; draw knee points
    ;; (u/fill u/red)
    ;; (q/ellipse k1x k1y 10 10)
    ;; (u/fill u/blue)
    ;; (q/ellipse k2x k2y 10 10)
    ))

(defn draw-spider
  [{[x y :as b] :pos
    :keys [feet]}]
  (q/stroke u/white)
  (q/stroke-weight 3)
  (u/fill u/white)

  (doseq [[i {[fx fy :as f] :pos}] (zipmap (range) feet)]
    ;; draw leg
    (draw-leg ls lt f b (< i 4)))

  ;; draw body
  (q/fill body-color)
  (q/ellipse x (- y 10) 30 30)

  (u/fill eye-color)
  (q/rect (- x 5) (- y 10) 2 4)
  (q/rect (+ x 5) (- y 10) 2 4))

(defn spider
  [pos]
  (let [initial-offsets [[100 0]
                         [100 25]
                         [80 50]
                         [60 75]
                         [-100 0]
                         [-100 25]
                         [-80 50]
                         [-60 75]]
        s (sprite/sprite
           :spider
           pos
           :update-fn update-spider
           :draw-fn draw-spider
           :extra {:initial-offsets initial-offsets
                   :feet (mapv (fn [offset]
                                 {:reset-offset offset
                                  :pos (mapv + pos offset)})
                               initial-offsets)
                   :moving-feet {}})]
    (tween/add-tween
     s
     (tween/tween
      :pos
      7
      :step-count (u/ms->frames 800)
      :easing-fn tween/ease-in-out-sine
      :update-fn tween/tween-y-fn
      :yoyo? true
      :yoyo-update-fn tween/tween-y-yoyo-fn
      :repeat-times ##Inf))))

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(spider (u/center))])

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

(defn clicked
  [state {:keys [x y button] :as e}]
  (if (= button :right)
    (sprite/update-sprites
     state
     (sprite/has-group :spider)
     (fn [spider]
       (-> spider
           (update :tweens (fn [tweens]
                             (remove #(= :current-move (:tag %))
                                     tweens)))
           (tween/add-tween
            (assoc (tween/tween
                    :pos
                    x
                    :from-value (get-in spider [:pos 0])
                    :update-fn tween/tween-x-fn
                    :easing-fn tween/ease-out-quad)
                   :tag :current-move))
           (tween/add-tween
            (assoc (tween/tween
                    :pos
                    y
                    :from-value (get-in spider [:pos 1])
                    :update-fn tween/tween-y-fn
                    :easing-fn tween/ease-out-quad)
                   :tag :current-move)))))
    state))

(defn dragged
  [state
   {:keys [x y p-x p-y button] :as e}]
  (if (or (every? zero? [p-x p-y])
          (not= button :left))
    state
    (let [dx (- x p-x)
          dy (- y p-y)]
      (sprite/update-sprites
       state
       (sprite/has-group :spider)
       (fn [spider]
         (-> spider
             (update :pos #(mapv + [dx dy] %))))))))

(defn init
  "Initialise this scene"
  []
  {:sprites   (sprites)
   :draw-fn   draw-level-01!
   :update-fn update-level-01
   :mouse-dragged-fns [dragged]
   :mouse-pressed-fns [clicked]})
