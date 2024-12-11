(ns clickable-sprites.scenes.menu
  (:require [quil.core :as q]
            [quip.input :as input]
            [quip.scene :as scene]
            [quip.sprite :as sprite]
            [quip.sprites.button :as button]
            [quip.util :as u]))

(def white [230 230 230])
(def grey [57 57 58])
(def dark-green [41 115 115])

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(-> (button/button-sprite "play" [(* 0.5 (q/width)) (* 0.5 (q/height))])
       (input/on-click
        (fn [state button]
          (-> state
              (scene/transition :level-01 :transition-length 30)))))])

(defn draw-menu!
  "Called each frame, draws the current scene to the screen"
  [state]
  (u/background dark-green)
  (sprite/draw-scene-sprites! state))

(defn update-menu
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      sprite/update-state))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-menu!
   :update-fn update-menu})
