(ns quip.sprite
  (:require [quil.core :as q]
            [quip.utils :as qpu]))

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

(defn static-sprite
  [sprite-group pos w h image-file &
   {:keys [update-fn draw-fn]
    :or   {update-fn identity
           draw-fn   draw-image-sprite}}]
  {:sprite-group sprite-group
   :uuid         (java.util.UUID/randomUUID)
   :pos          pos
   :w            w
   :h            h
   :animated?    false
   :static?      true
   :update-fn    update-fn
   :draw-fn      draw-fn
   :image        (q/load-image image-file)})

(defn image-sprite
  [sprite-group pos w h image-file &
   {:keys [vel
           update-fn
           draw-fn]
    :or   {vel           [0 0]
           update-fn     update-image-sprite
           draw-fn       draw-image-sprite}}]
  {:sprite-group sprite-group
   :uuid         (java.util.UUID/randomUUID)
   :pos          pos
   :vel          vel
   :w            w
   :h            h
   :animated?    false
   :static?      false
   :update-fn    update-fn
   :draw-fn      draw-fn
   :image        (q/load-image image-file)})

(defn animated-sprite
  [sprite-group pos w h spritesheet-file &
   {:keys [vel
           update-fn
           draw-fn
           animations
           current-animation]
    :or   {vel               [0 0]
           update-fn         update-animated-sprite
           draw-fn           draw-animated-sprite
           animations        {:none {:frames      1
                                     :y-offset    0
                                     :frame-delay 100}}
           current-animation :none}}]
  {:sprite-group      sprite-group
   :uuid              (java.util.UUID/randomUUID)
   :pos               pos
   :vel               vel
   :w                 w
   :h                 h
   :animated?         true
   :static?           false
   :update-fn         update-fn
   :draw-fn           draw-fn
   :spritesheet       (q/load-image spritesheet-file)
   :animations        animations
   :current-animation current-animation
   :delay-count       0
   :animation-frame   0})

(defn draw-text-sprite
  [{:keys [content pos font size color]}]
  (let [[x y] pos]
    (q/text-align :center)
    (q/text-font font)
    (qpu/fill color)
    (q/text content x y)))

(defn text-sprite
  [content pos &
   {:keys [sprite-group
           font
           size
           color
           draw-fn]
    :or   {sprite-group :text
           font         qpu/default-font
           size         qpu/default-text-size
           color        qpu/black
           draw-fn      draw-text-sprite}}]
  {:sprite-group sprite-group
   :content      content
   :pos          pos
   :font         (q/create-font font size)
   :color        color
   :draw-fn      draw-fn})

(defn draw-button-sprite
  [{:keys [content pos size color font content-color content-pos held?]}]
  (q/no-stroke)
  (q/text-align :center :center)
  (q/text-font font)
  (let [[x y]   pos
        [w h]   size
        [cx cy] content-pos]
    (if held?
      (do (qpu/fill color)
          (q/rect (+ 2 x) (+2 y) w h)
          (qpu/fill content-color)
          (q/text content (+ 2 x cx) (+ 2 y cy)))
      (do (qpu/fill (qpu/darken color))
          (q/rect (+ 2 x) (+ 2 y) w h)
          (qpu/fill color)
          (q/rect x y w h)
          (qpu/fill content-color)
          (q/text content (+ x cx) (+ y cy))))))

(defn button-sprite
  [content pos & {:keys [on-click
                         size
                         color
                         font
                         font-size
                         content-color
                         content-pos
                         held?
                         draw-fn]
                  :or   {on-click      identity
                         size          [100 70]
                         color         qpu/grey
                         font          qpu/default-font
                         font-size     qpu/default-text-size
                         content-color qpu/black
                         content-pos   [50 35]
                         held?         false
                         draw-fn       draw-button-sprite}}]
  {:sprite-group  :button
   :content       content
   :pos           pos
   :on-click      on-click
   :size          size
   :color         color
   :font          (q/create-font font font-size)
   :content-color content-color
   :content-pos   content-pos
   :held?         held?
   :draw-fn       draw-fn})
