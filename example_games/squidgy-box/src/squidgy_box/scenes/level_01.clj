(ns squidgy-box.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as sprite]
            [quip.tween :as tween]
            [quip.util :as u]))

(def gunmetal [0 43 54])
(def jet [60 60 60])
(def cultured [245 245 245])

(defn draw-box!
  [{[x y] :pos :keys [w h l-offset r-offset u-offset d-offset] :as b}]
  (u/stroke cultured)
  (u/fill jet)
  (q/rect (- x (/ w 2) l-offset)
          (- y (/ h 2) u-offset)
          (+ w l-offset r-offset)
          (+ h u-offset d-offset)
          10))

(defn box
  [pos]
  {:sprite-group :box
   ::uuid (java.util.UUID/randomUUID)
   :pos pos
   :w 100
   :h 100
   :l-offset 0
   :r-offset 0
   :u-offset 0
   :d-offset 0
   :animated? false
   :update-fn identity
   :draw-fn draw-box!})

(defn add-squish-tween
  [state offset-key]
  (update-in state
             [:scenes :level-01 :sprites]
             (fn [sprites]
               [(tween/add-tween
                 (first sprites)
                 (tween/tween offset-key 50
                              :step-count 10
                              :yoyo? true
                              :easing-fn tween/ease-in-out-sine))])))

(defn handle-key-pressed
  [state e]
  (case (:key e)
    :space (-> state
               (add-squish-tween :w)
               (add-squish-tween :h))
    :left  (add-squish-tween state :l-offset)
    :right (add-squish-tween state :r-offset)
    :up    (add-squish-tween state :u-offset)
    :down  (add-squish-tween state :d-offset)
    state))

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(box [300 200])])

(defn draw-level-01!
  "Called each frame, draws the current scene to the screen"
  [state]
  (u/background gunmetal)
  (sprite/draw-scene-sprites! state))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      sprite/update-state
      tween/update-state))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01!
   :update-fn update-level-01
   :key-pressed-fns [handle-key-pressed]})
