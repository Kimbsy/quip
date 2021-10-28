(ns quip.sprite
  (:require [quil.core :as q]
            [quip.utils :as qpu]))

(defn offset-pos
  [[x y] w h]
  [(- x (/ w 2))
   (- y (/ h 2))])

(defn update-pos
  [{:keys [pos vel] :as s}]
  (assoc s :pos (map + pos vel)))

(defn update-frame-delay
  [{:keys [current-animation] :as s}]
  (let [animation   (current-animation (:animations s))
        frame-delay (:frame-delay animation)]
    (update s :delay-count #(mod (inc %) frame-delay))))

(defn update-animation
  [{:keys [current-animation delay-count] :as s}]
  (if (zero? delay-count)
    (let [animation (current-animation (:animations s))
          max-frame (:frames animation)]
      (update s :animation-frame #(mod (inc %) max-frame)))
    s))

(defn update-image-sprite
  [s]
  (some-> s
          update-pos))

(defn update-animated-sprite
  [s]
  (some-> s
          update-frame-delay
          update-animation
          update-pos))

(defn draw-image-sprite
  [{:keys [pos w h image]}]
  (let [[x y] (offset-pos pos w h)]
    (q/image image x y)))

(def memo-graphics (memoize (fn [w h] (q/create-graphics w h))))

(defn draw-animated-sprite
  [{:keys [pos rotation w h spritesheet current-animation animation-frame] :as s}]
  (let [animation (current-animation (:animations s))
        x-offset  (* animation-frame w)
        y-offset  (* (:y-offset animation) h)
        g         (memo-graphics w h)]
    (q/with-graphics g
      (.clear g)
      (q/image spritesheet (- x-offset) (- y-offset)))
    (qpu/wrap-trans-rot pos rotation
                       #(q/image g (- (/ w 2)) (- (/ h 2))))))

(defn set-animation
  [s animation]
  (-> s
      (assoc :current-animation animation)
      (assoc :animation-frame 0)))

(defn default-bounding-poly
  "Generates a bounding polygon based off the `w` by `h` rectangle of
  the sprite."
  [{:keys [w h]}]
  [[0 0]
   [w 0]
   [w h]
   [0 h]])

;;; Basic Sprite types

(defn static-sprite
  [sprite-group pos w h image-file &
   {:keys [rotation
           update-fn
           draw-fn
           points
           bounds-fn]
    :or   {rotation  0
           update-fn identity
           draw-fn   draw-image-sprite}}]
  {:sprite-group sprite-group
   :uuid         (java.util.UUID/randomUUID)
   :pos          pos
   :rotation     rotation
   :w            w
   :h            h
   :animated?    false
   :static?      true
   :update-fn    update-fn
   :draw-fn      draw-fn
   :points       points
   :bounds-fn    (or bounds-fn
                     (if (seq points)
                       :points
                       default-bounding-poly))
   :image        (q/load-image image-file)})

(defn image-sprite
  [sprite-group pos w h image-file &
   {:keys [rotation
           vel
           update-fn
           draw-fn
           points
           bounds-fn]
    :or   {rotation  0
           vel       [0 0]
           update-fn update-image-sprite
           draw-fn   draw-image-sprite}}]
  {:sprite-group sprite-group
   :uuid         (java.util.UUID/randomUUID)
   :pos          pos
   :rotation     rotation
   :vel          vel
   :w            w
   :h            h
   :animated?    false
   :static?      false
   :update-fn    update-fn
   :draw-fn      draw-fn
   :points       points
   :bounds-fn    (or bounds-fn
                     (if (seq points)
                       :points
                       default-bounding-poly))
   :image        (q/load-image image-file)})

(defn animated-sprite
  [sprite-group pos w h spritesheet-file &
   {:keys [rotation
           vel
           update-fn
           draw-fn
           animations
           current-animation
           points
           bounds-fn]
    :or   {rotation          0
           vel               [0 0]
           update-fn         update-animated-sprite
           draw-fn           draw-animated-sprite
           animations        {:none {:frames      1
                                     :y-offset    0
                                     :frame-delay 100}}
           current-animation :none}}]
  {:sprite-group      sprite-group
   :uuid              (java.util.UUID/randomUUID)
   :pos               pos
   :rotation          rotation
   :vel               vel
   :w                 w
   :h                 h
   :animated?         true
   :static?           false
   :update-fn         update-fn
   :draw-fn           draw-fn
   :points            points
   :bounds-fn         (or bounds-fn
                          (if (seq points)
                            :points
                            default-bounding-poly))
   :spritesheet       (q/load-image spritesheet-file)
   :animations        animations
   :current-animation current-animation
   :delay-count       0
   :animation-frame   0})

(defn draw-text-sprite
  [{:keys [content pos offsets font size color]}]
  (let [[x y] pos]
    (apply q/text-align offsets)
    (q/text-font font)
    (qpu/fill color)
    (q/text content x y)))

(defn text-sprite
  [content pos &
   {:keys [offsets
           sprite-group
           font
           size
           color
           update-fn
           draw-fn]
    :or   {offsets      [:center]
           sprite-group :text
           font         qpu/default-font
           size         qpu/default-text-size
           color        qpu/black
           update-fn    identity
           draw-fn      draw-text-sprite}}]
  {:sprite-group sprite-group
   :uuid         (java.util.UUID/randomUUID)
   :content      content
   :pos          pos
   :offsets      offsets
   :font         (q/create-font font size)
   :color        color
   :update-fn    update-fn
   :draw-fn      draw-fn})

(defn update-scene-sprites
  "Update each sprite in the current scene using its `:update-fn`.

  Optionally accepts a key specifying the name of the sprite
  collection on the scene."
  [{:keys [current-scene] :as state} &
   {:keys [sprite-key] :or {sprite-key :sprites}}]
  (update-in state [:scenes current-scene sprite-key]
             (fn [sprites]
               (map (fn [s]
                      ((:update-fn s) s))
                    sprites))))

(defn draw-scene-sprites
  "Draw each sprite in the current scene using its `:draw-fn`.

  Optionally accepts a key specifying the name of the sprite
  collection on the scene."
  [{:keys [current-scene] :as state} &
   {:keys [sprite-key] :or {sprite-key :sprites}}]
  (let [sprites (get-in state [:scenes current-scene sprite-key])]
    (doall
     (map (fn [s]
            ((:draw-fn s) s))
          sprites))))

(defn draw-scene-sprites-by-layers
  "Draw each sprite in the current scene using its `:draw-fn` in the
  order their `:sprite-group` appears in the `layers` list.

  Any sprites with groups not found in `layers` will be drawn last.

  Optionally accepts a key specifying the name of the sprite
  collection on the scene."
  [{:keys [current-scene] :as state} layers &
   {:keys [sprite-key] :or {sprite-key :sprites}}]
  (let [sprites     (get-in state [:scenes current-scene sprite-key])
        unspecified (filter #(not ((set layers) (:sprite-group %))) sprites)]
    (doall
     (map (fn [group]
            (doall
             (map (fn [s]
                    ((:draw-fn s) s))
                  (filter #(= group (:sprite-group %))
                          sprites))))
          layers))
    (doall
     (map (fn [s]
            ((:draw-fn s) s))
          unspecified))))

(defn update-sprites-by-pred
  "Update sprites in the current scene with the update function `f`
  filtering by a predicate function `pred`.

  Optionally accepts a key specifying the name of the sprite
  collection on the scene."
  [{:keys [current-scene] :as state} pred f &
   {:keys [sprite-key] :or {sprite-key :sprites}}]
  (update-in state [:scenes current-scene sprite-key]
             (fn [sprites]
               (pmap (fn [s]
                       (if (pred s)
                         (f s)
                         s))
                     sprites))))

(defn group-pred
  "Defines a predicate that filters sprites based on their
  sprite-group.

  Commonly used alongside `update-sprites-by-pred`:

  (qpsprite/update-sprites-by-pred
    state
    (qpsprite/group-pred :asteroids)
    sprite-update-fn)"
  [sprite-group]
  (fn [s]
    (= sprite-group (:sprite-group s))))
