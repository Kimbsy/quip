(ns spider-legs.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as sprite]
            [quip.scene :as scene]
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

(defn update-spider
  [spider]
  (update spider
          :feet
          (fn [feet]
            (map (fn [{:keys [pos reset-offset] :as foot}]
                   (if (< max-distance (distance foot spider))
                     (assoc foot
                            :pos
                            (map + (:pos spider) reset-offset))
                     foot))
                 feet))))

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
             :feet (map (fn [offset]
                          {:reset-offset offset
                           :pos (map + pos offset)})
                        initial-offsets)})))

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
      sprite/update-state))

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
             (update :pos #(map + [dx dy] %))
             #_(update :feet #(map (partial map + [dx dy]) %))))))))

(defn init
  "Initialise this scene"
  []
  {:sprites   (sprites)
   :draw-fn   draw-level-01!
   :update-fn update-level-01
   :key-pressed-fns [key-pressed]
   :mouse-dragged-fns [dragged]})
