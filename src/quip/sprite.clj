(ns quip.sprite
  (:require [quil.core :as q]
            [quil.applet :as ap]))

(defn update-pos
  [{:keys [pos vel] :as s}]
  (assoc s (map + pos vel)))

(defn update-static-sprite
  [s]
  s)

(defn update-animated-sprite
  [s]
  s)

(defn draw-static-sprite
  [s]
  )

(defn draw-animated-sprite
  [s]
  (prn "drawing animated spritesheet-file"))

(defn set-animation
  [s animation]
  (-> s
      (assoc :current-animation animation)
      (assoc :animation-frame 0)))


(defn static-sprite
  [pos w h image-file &
   {:keys [vel update-fn draw-fn]
    :or   {vel       [0 0]
           update-fn update-static-sprite
           draw-fn   draw-static-sprite}}]
  {:pos       pos
   :vel       vel
   :w         w
   :h         h
   :animated? false
   :update-fn update-fn
   :draw-fn   draw-fn
   :image     (q/load-image image-file)})

(defn animated-sprite
  [pos w h spritesheet-file &
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
  {:pos               pos
   :vel               vel
   :w                 w
   :h                 h
   :animated?         true
   :update-fn         update-fn
   :draw-fn           draw-fn
   :spritesheet       (q/load-image spritesheet-file)
   :animations        animations
   :current-animation current-animation})
