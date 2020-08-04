(ns quip.sprite
  (:require [quil.core :as q]
            [quip.utils :as qpu]))

(defn offset-pos
  [[x y] w h [offset-x offset-y]]
  [(case offset-x
     :left   x
     :center (- x (/ w 2))
     :right  (- x w)
     x)
   (case offset-y
     :top y
     :center (- y (/ h 2))
     :bottom (- y h)
     y)])

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
  [{[x y] :pos image :image}]
  (q/image image x y))

(def memo-graphics (memoize (fn [w h] (q/create-graphics w h))))

(defn draw-animated-sprite
  [{:keys [pos w h spritesheet current-animation animation-frame] :as s}]
  (let [[x y]     pos
        animation (current-animation (:animations s))
        x-offset  (* animation-frame w)
        y-offset  (* (:y-offset animation) h)
        g         (memo-graphics w h)]
    (q/with-graphics g
      (.clear g)
      (q/image spritesheet (- x-offset) (- y-offset)))
    (q/image g x y)))

(defn set-animation
  [s animation]
  (-> s
      (assoc :current-animation animation)
      (assoc :animation-frame 0)))

(defn default-bounding-poly
  "Generates a bounding polygon based off the `w` by `h` rectangle of
  the sprite."
  [{[x y] :pos
    w     :w
    h     :h}]
  [[x       y]
   [(+ x w) y]
   [(+ x w) (+ y h)]
   [x       (+ y h)]])

;;; Basic Sprite types

(defn static-sprite
  [sprite-group pos w h image-file &
   {:keys [offsets
           update-fn
           draw-fn
           points
           bounds-fn]
    :or   {offsets   [:left :top]
           update-fn identity
           draw-fn   draw-image-sprite}}]
  {:sprite-group sprite-group
   :uuid         (java.util.UUID/randomUUID)
   :pos          (offset-pos pos w h offsets)
   :offsets      offsets
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
   {:keys [offsets
           vel
           update-fn
           draw-fn
           points
           bounds-fn]
    :or   {offsets   [:left :top]
           vel       [0 0]
           update-fn update-image-sprite
           draw-fn   draw-image-sprite}}]
  {:sprite-group sprite-group
   :uuid         (java.util.UUID/randomUUID)
   :pos          (offset-pos pos w h offsets)
   :offsets      offsets
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
   {:keys [offsets
           vel
           update-fn
           draw-fn
           animations
           current-animation
           points
           bounds-fn]
    :or   {offsets           [:left :top]
           vel               [0 0]
           update-fn         update-animated-sprite
           draw-fn           draw-animated-sprite
           animations        {:none {:frames      1
                                     :y-offset    0
                                     :frame-delay 100}}
           current-animation :none}}]
  {:sprite-group      sprite-group
   :uuid              (java.util.UUID/randomUUID)
   :pos               (offset-pos pos w h offsets)
   :offsets           offsets
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
           draw-fn]
    :or   {offsets      [:center]
           sprite-group :text
           font         qpu/default-font
           size         qpu/default-text-size
           color        qpu/black
           draw-fn      draw-text-sprite}}]
  {:sprite-group sprite-group
   :uuid         (java.util.UUID/randomUUID)
   :content      content
   :pos          pos
   :offsets      offsets
   :font         (q/create-font font size)
   :color        color
   :draw-fn      draw-fn})
