(ns spider-legs.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as sprite]
            [quip.scene :as scene]
            [quip.tween :as tween]
            [quip.util :as u]))

(def light-green [133 255 199])
(def dark-green [41 115 115])

(def min-length 50)
(def max-length 160)
(def max-angle 45)

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

(defn draw-spider
  [{[x y] :pos
    :keys [feet]}]
  (q/stroke u/white)
  (q/stroke-weight 3)
  (u/fill u/white)

  ;; draw body
  (q/ellipse x y 30 30)

  (doseq [{[fx fy] :pos} feet]
    ;; draw leg
    (q/line x y fx fy)
    
    ;; draw foot
    (q/ellipse fx fy 10 10)))

(defn spider
  [pos]
  (let [initial-offsets [[50 -100]
                         [100 -50]
                         [100 50]
                         [50 100]
                         [-50 -100]
                         [-100 -50]
                         [-100 50]
                         [-50 100]]]
    (sprite/sprite
     :spider
     pos
     :update-fn update-spider
     :draw-fn draw-spider
     :extra {:initial-offsets initial-offsets
             :feet (mapv (fn [offset]
                           {:reset-offset offset
                            :pos (mapv + pos offset)})
                         initial-offsets)
             :moving-feet {}})))

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

(defn key-pressed
  [state e]
  (if (= :space (:key e))
    (update state :moving? not)
    state))

(defn dragged
  [{:keys [moving?] :as state}
   {:keys [x y p-x p-y] :as e}]
  (if (every? zero? [p-x p-y])
    state
    (let [dx (- x p-x)
          dy (- y p-y)]
      (sprite/update-sprites
       state
       (sprite/has-group :spider)
       (fn [spider]
         (-> spider
             (update :pos #(mapv + [dx dy] %))
             #_(update :feet #(map (partial map + [dx dy]) %))))))))

(defn init
  "Initialise this scene"
  []
  {:sprites   (sprites)
   :draw-fn   draw-level-01!
   :update-fn update-level-01
   :key-pressed-fns [key-pressed]
   :mouse-dragged-fns [dragged]})
