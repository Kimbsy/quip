(ns quip.tween)

;; @TODO: do we want to only store the normalized deltas? why not the scaled ones?

(def ease-linear identity)

(defn ease-sigmoid
  [x]
  (if (<= x 0.5)
    0
    1))

;;; Sinusoidal easing functions

(defn ease-in-sine
  [x]
  (- 1 (Math/cos (* x Math/PI 1/2))))

(defn ease-out-sine
  [x]
  (Math/sin (* x Math/PI 1/2)))

(defn ease-in-out-sine
  [x]
  (* (- (Math/cos (* x Math/PI)) 1) -1/2))

;;; Quadratic easing functions

(defn ease-in-quad
  [x]
  (* x x))

(defn ease-out-quad
  [x]
  (- 1 (* (- 1 x) (- 1 x))))

(defn ease-in-out-quad
  [x]
  (cond
    (< x 0.5) (* 2 x x)
    :else (- 1 (/ (Math/pow (+ (* x -2) 2) 2) 2))))

;;; Cubic easing functions

(defn ease-in-cubic
  [x]
  (* x x x))

(defn ease-out-cubic
  [x]
  (- 1 (Math/pow (- 1 x) 3)))

(defn ease-in-out-cubic
  [x]
  (cond
    (< x 0.5) (* 4 x x x)
    :else (- 1 (/ (Math/pow (+ (* x -2) 2) 3) 2))))

;;; Quartic easing functions

(defn ease-in-quart
  [x]
  (* x x x x))

(defn ease-out-quart
  [x]
  (- 1 (Math/pow (- 1 x) 4)))

(defn ease-in-out-quart
  [x]
  (cond
    (< x 0.5) (* 8 x x x x)
    :else (- 1 (/ (Math/pow (+ (* x -2) 2) 4) 2))))

;;; Quintic easing functions

(defn ease-in-quint
  [x]
  (* x x x x x))

(defn ease-out-quint
  [x]
  (- 1 (Math/pow (- 1 x) 5)))

(defn ease-in-out-quint
  [x]
  (cond
    (< x 0.5) (* 16 x x x x x)
    :else (- 1 (/ (Math/pow (+ (* x -2) 2) 5) 2))))

;;; Exponential easing functions

(defn ease-in-expo
  [x]
  (cond
    (zero? x) 0
    :else (Math/pow 2 (- (* x 10) 10))))

(defn ease-out-expo
  [x]
  (cond
    (= 1 x) 1
    :else (- 1 (Math/pow 2, (* x -10)))))

(defn ease-in-out-expo
  [x]
  (cond
    (zero? x) 0
    (= 1 x) 1
    (< x 0.5) (/ (Math/pow 2 (- (* x 20) 10)) 2)
    :else (/ (- 2 (Math/pow 2 (+ (* x -20) 10))) 2)))

;;; Circular easing functions

(defn ease-in-circ
  [x]
  (- 1 (Math/sqrt (- 1 (Math/pow x 2)))))

(defn ease-out-circ
  [x]
  (Math/sqrt (- 1 (Math/pow (- x 1) 2))))

(defn ease-in-out-circ
  [x]
  (cond
    (< x 0.5) (/ (- 1 (Math/sqrt (- 1 (Math/pow (* x 2) 2)))) 2)
    :else (/ (+ (Math/sqrt (- 1 (Math/pow (+ (* x -2) 2) 2))) 1) 2)))

;;; Back easing functions

(defn ease-in-back
  [x]
  (let [c1 1.70158
        c2 (+ c1 1)]
    (- (* c2 x x x)
       (* c1 x x))))

(defn ease-out-back
  [x]
  (let [c1 1.70158
        c2 (+ c1 1)]
    (+ 1
       (* c2 (Math/pow (- x 1) 3))
       (* c1 (Math/pow (- x 1) 2)))))

(defn ease-in-out-back
  [x]
  (let [c1 1.70158
        c2 (+ c1 1.525)]
    (cond
      (< x 0.5) (/ (* (Math/pow (* x 2) 2) (- (* (+ c2 1) x 2) c2)) 2)
      :else (/ (+ (* (Math/pow (- (* x 2) 2) 2) (+ (* (+ c2 1) (- (* x 2) 2)) c2)) 2) 2))))

;;; Elastic easing functions

(defn ease-in-elastic
  [x]
  (cond
    (zero? x) 0
    (= 1 x) 1
    :else (* (- (Math/pow 2 (- (* x 10) 10))) (Math/sin (* (- (* x 10) 10.75) (* 2 Math/PI 1/3))))))

(defn ease-out-elastic
  [x]
  (cond
    (zero? x) 0
    (= 1 x) 1
    :else (+ (* (Math/pow 2 (* x -10)) (Math/sin (* (- (* x 10) 0.75) (* 2 Math/PI 1/3)))) 1)))

(defn ease-in-out-elastic
  [x]
  (cond
    (zero? x) 0
    (= 1 x) 1
    (< x 0.5) (/ (- (* (Math/pow 2 (- (* x 20) 10)) (Math/sin (* (- (* x 10) 11.125) (* 2 Math/PI 5/4))))) 2)
    :else (+ (/ (* (Math/pow 2 (+ (* x -20) 10)) (Math/sin (* (- (* x 10) 11.125) (* 2 Math/PI 5/4)))) 2) 1)))

;;; Bouncing easing functions

(declare ease-out-bounce)

(defn ease-in-bounce
  [x]
  (- 1 (ease-out-bounce (- 1 x))))

(defn ease-out-bounce
  [x]
  (let [n 7.5625
        d 2.75]
    (cond
      (< x (/ 1 d)) (* n x x)
      (< x (/ 2 d)) (+ (* n (- x (/ 1.5 d)) (- x (/ 1.5 d))) 0.75)
      (< x (/ 2.5 d)) (+ (* n (- x (/ 2.25 d)) (- x (/ 2.25 d))) 0.9375)
      :else (+ (* n (- x (/ 2.625 d)) (- x (/ 2.625 d))) 0.984375))))

(defn ease-in-out-bounce
  [x]
  (cond
    (< x 0.5) (/ (- 1 (ease-out-bounce (- 1 (* x 2)))) 2)
    :else (/ (+ 1 (ease-out-bounce (- (* x 2) 1))) 2)))

;; Utility functions for tweening [x y] tuples
(defn tween-x-fn
  [[x y] d]
  [(+ x d) y])
(defn tween-y-fn
  [[x y] d]
  [x (+ y d)])
(defn tween-x-yoyo-fn
  [[x y] d]
  [(- x d) y])
(defn tween-y-yoyo-fn
  [[x y] d]
  [x (- y d)])

(defn normalized-deltas
  [easing-fn step-count]
  ;; get step-count [s-min s-max] pairs from 0 to 1
  (let [steps (map (fn [i]
                     [(* i (/ 1 step-count))
                      (* (inc i) (/ 1 step-count))])
                   (range step-count))]
    ;; delta = f(s-max) - f(s-min)
    (map (fn [[s-min s-max]]
           (- (easing-fn s-max)
              (easing-fn s-min)))
         steps)))

(defn ->tween
  "Create a new tween for modifying a field on a sprite over time."
  [field to-value
   & {:keys [from-value
             easing-fn
             update-fn
             step-count
             yoyo?
             yoyo-update-fn
             on-yoyo-fn
             repeat-times
             on-repeat-fn
             on-complete-fn]
      :or   {from-value     0
             easing-fn      ease-linear
             update-fn      +
             step-count     100
             yoyo?          false
             yoyo-update-fn -
             on-yoyo-fn     identity
             repeat-times   1
             on-repeat-fn   identity
             on-complete-fn identity}}]
  {:field             field
   :total-change      (- to-value from-value)
   :normalized-deltas (normalized-deltas easing-fn step-count)
   :easing-fn         easing-fn
   :update-fn         update-fn
   :progress          0
   :step-count        step-count
   :yoyo?             yoyo?
   :yoyo-update-fn    yoyo-update-fn
   :yoyoing?          false
   :on-yoyo-fn        on-yoyo-fn
   :repeat-times      (max 1 repeat-times)
   :on-repeat-fn      on-repeat-fn
   :completed?        false
   :on-complete-fn    on-complete-fn})

(defn add-tween
  [{:keys [tweens] :as sprite} tween]
  (if (seq tweens)
    (update sprite :tweens #(conj % tween))
    (assoc sprite :tweens [tween])))

(defn complete-repetition
  [{:keys [repeat-times resetting?] :as tween}]
  (-> tween
      (assoc :progress 0)
      (assoc :resetting? true)
      (#(if (<= repeat-times 1)
          (assoc % :completed? true)
          (update % :repeat-times dec)))))

(defn update-tween
  [{:keys [progress step-count yoyo? yoyoing? repeat-times] :as tween}]
  (let [tween (dissoc tween :resetting?)]
    (when (<= 1 repeat-times)
      (if yoyo?
        (if yoyoing?
          (if (zero? progress)
            (-> tween
                (assoc :yoyoing? false)
                complete-repetition)
            (update tween :progress dec))
          (if (= (inc progress) step-count)
            (assoc tween :yoyoing? true)
            (update tween :progress inc)))
        (if (= (inc progress) step-count)
          (complete-repetition tween)
          (update tween :progress inc))))))

(defn apply-tween
  [sprite
   {:keys [field
           update-fn
           yoyo?
           yoyoing?
           yoyo-update-fn
           total-change
           normalized-deltas
           progress
           resetting?]}]
  (update sprite
          field
          (fn [v]
            (let [f (if yoyoing?
                      yoyo-update-fn
                      update-fn)
                  value (if (and resetting?
                                 (not yoyo?))
                          (f v (- total-change))
                          v)]
              (f value (* total-change (nth normalized-deltas progress)))))))

(defn update-sprite
  [{:keys [tweens] :as sprite}]
  (if (seq tweens)
    (let [updated-tweens (map update-tween tweens)]
      (-> (reduce apply-tween
                  sprite
                  tweens)
          (assoc :tweens updated-tweens)))
    sprite))

(defn handle-on-yoyos
  [{:keys [tweens] :as sprite}]
  (if (seq tweens)
    (reduce (fn [s {:keys [yoyo? yoyoing? progress step-count on-yoyo-fn]}]
              (if (and yoyo? yoyoing? (= (inc progress) step-count))
                (on-yoyo-fn s)
                s))
            sprite
            tweens)
    sprite))

(defn handle-on-repeats
  [{:keys [tweens] :as sprite}]
  (if (seq tweens)
    (reduce (fn [s {:keys [repeat-times yoyo? yoyoing? progress step-count on-repeat-fn]}]
              (if (and (< 1 repeat-times) (or (and yoyo? yoyoing? (zero? progress))
                                              (and (not yoyo?) (= (inc progress) step-count))))
                (on-repeat-fn s)
                s))
            sprite
            tweens)
    sprite))

(defn handle-on-completes
  [{:keys [tweens] :as sprite}]
  (if (seq tweens)
    (reduce (fn [s {:keys [completed? on-complete-fn]}]
              (if completed?
                (on-complete-fn s)
                s))
            sprite
            tweens)
    sprite))

(defn remove-completed-tweens
  [sprites]
  (map (fn [s]
         (update s :tweens #(remove :completed? %)))
       sprites))

(defn update-sprite-tweens
  [{:keys [current-scene] :as state}]
  (let [sprites         (get-in state [:scenes current-scene :sprites])
        updated-sprites (transduce (comp (map update-sprite)
                                         (map handle-on-yoyos)
                                         (map handle-on-repeats)
                                         (map handle-on-completes))
                                   conj
                                   sprites)
        cleaned-sprites (remove-completed-tweens updated-sprites)]
    (assoc-in state [:scenes current-scene :sprites]
              cleaned-sprites)))
