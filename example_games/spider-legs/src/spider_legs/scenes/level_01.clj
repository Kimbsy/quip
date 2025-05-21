(ns spider-legs.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as sprite]
            [quip.scene :as scene]
            [quip.tween :as tween]
            [quip.util :as u]))

(def light-green [133 255 199])
(def dark-green [41 115 115])

(defn distance
  [{[fx fy] :pos
    [ox oy] :reset-offset}
   {[sx sy] :pos}]
  (Math/sqrt (+ (Math/pow (- (/ (+ sx (+ sx ox)) 2) fx) 2)
                (Math/pow (- (/ (+ sy (+ sy oy)) 2) fy) 2))))

(def max-distance 120)

(defn move-foot-tween-x
  [idx spider-pos foot-pos reset-offset]
  (tween/tween :feet (first (map + spider-pos reset-offset))
               :from-value (first foot-pos)
               :step-count (u/ms->frames 80)
               :update-fn (fn [feet d]
                            (update-in feet
                                       [idx :pos 0]
                                       + d))
               :on-complete-fn (fn [spider]
                                 (update spider :moving-feet
                                         assoc idx false))))

(defn move-foot-tween-y
  [idx spider-pos foot-pos reset-offset]
  (tween/tween :feet (second (map + spider-pos reset-offset))
               :from-value (second foot-pos)
               :step-count (u/ms->frames 80)
               :update-fn (fn [feet d]
                            (update-in feet
                                       [idx :pos 1]
                                       + d))
               ;; @NOTE don't need the on-complete for Y, since X
               ;; already does it.
               ))

(defn update-spider
  [{:keys [feet pos moving-feet] :as spider}]
  (let [movements (keep-indexed
                   (fn [i {:keys [reset-offset]
                           foot-pos :pos
                           :as foot}]
                     (when (and (not (moving-feet i))
                                (< max-distance (distance foot spider)))
                       [i [(move-foot-tween-x i pos foot-pos reset-offset)
                           (move-foot-tween-y i pos foot-pos reset-offset)]]))
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
