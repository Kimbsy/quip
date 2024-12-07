(ns quip.sprites.button
  (:require [quil.core :as q]
            [quip.sprite :as sprite]
            [quip.util :as u]))

(defn draw-button-sprite!
  [{:keys [content pos w h color font content-color content-pos] :as button}]
  (q/no-stroke)
  (q/text-align :center :center)
  (q/text-font font)
  (let [[x y]   (map + pos (sprite/pos-offsets button))
        [cx cy] content-pos]
    (u/fill (u/darken color))
    (q/rect (+ 2 x) (+ 2 y) w h)
    (u/fill color)
    (q/rect x y w h)
    (u/fill content-color)
    (q/text content (+ x cx) (+ y cy))))

(defn button-sprite
  [content pos & {:keys [offsets
                         size
                         color
                         font
                         font-size
                         content-color
                         content-pos
                         update-fn
                         draw-fn
                         collision-detection-fn]
                  :or   {offsets       [:center :center]
                         size          [200 100]
                         color         u/grey
                         font          u/default-font
                         font-size     u/large-text-size
                         content-color u/black
                         content-pos   [100 50]
                         update-fn     identity
                         draw-fn       draw-button-sprite!}}]
  (let [[w h] size]
    {:sprite-group  :button
     :uuid          (java.util.UUID/randomUUID)
     :content       content
     :pos           pos
     :w             w
     :h             h
     :update-fn     update-fn
     :color         color
     :font          (q/create-font font font-size)
     :content-color content-color
     :content-pos   content-pos
     :draw-fn       draw-fn
     :bounds-fn     sprite/default-bounding-poly
     :offsets       offsets}))
