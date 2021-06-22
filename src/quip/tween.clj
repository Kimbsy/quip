(ns quip.tween)

;; @TODO: do we want to only store the normalized deltas? why not the scaled ones?

(def linear-easing-fn identity)

(defn sigmoidal-easing-fn
  [x]
  (if (<= x 0.5)
    0
    1))

(defn exponential-easing-fn
  [x]
  (Math/pow x 2))

(defn asymptotic-easing-fn
  [x]
  (Math/sqrt x))

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
             easing-fn      linear-easing-fn
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
