(ns game-restart.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as sprite]
            [quip.util :as u]
            [quip.core :as qp]))

(def light-green [133 255 199])

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(sprite/text-sprite "Current Score: 0"
                       [(* 0.5 (q/width))
                        (* 0.7 (q/height))]
                       :sprite-group :current-score-text)])

(defn draw-level-01!
  "Called each frame, draws the current scene to the screen"
  [state]
  (u/background light-green)
  (sprite/draw-scene-sprites! state))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [{:keys [current-score] :as state}]
  (-> state
      sprite/update-state
      (sprite/update-sprites-by-pred
       (sprite/group-pred :current-score-text)
       (fn [s]
         (assoc s :content (str "Current Score: " current-score))))))

(defn kp
  [state e]
  (if (= 10 (:key-code e))
    (qp/restart state)
    (update state :current-score inc)))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01!
   :update-fn update-level-01
   :key-pressed-fns [kp]})
