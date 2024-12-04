(ns game-restart.scenes.menu
  (:require [quil.core :as q]
            [quip.scene :as scene]
            [quip.sprite :as sprite]
            [quip.sprites.button :as button]
            [quip.util :as u]))

(def white [230 230 230])
(def grey [57 57 58])
(def dark-green [41 115 115])

(defn on-click-play
  "Transition from this scene to `:level-01` with a 30 frame fade-out"
  [state e]
  (scene/transition state :level-01 :transition-length 30))

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(button/button-sprite "Play"
                         [(* 0.5 (q/width))
                          (* 0.5 (q/height))]
                         :color grey
                         :content-color white
                         :on-click on-click-play)
   (sprite/text-sprite "Highscore: 0"
                       [(* 0.5 (q/width))
                        (* 0.7 (q/height))]
                       :sprite-group :highscore-text
                       :color u/white)])

(defn draw-menu!
  "Called each frame, draws the current scene to the screen"
  [state]
  (u/background dark-green)
  (sprite/draw-scene-sprites! state))

(defn update-menu
  "Called each frame, update the sprites in the current scene"
  [{:keys [high-score] :as state}]
  (-> state
      sprite/update-state
      (sprite/update-sprites-by-pred
       (sprite/group-pred :highscore-text)
       (fn [s]
         (assoc s :content (str "Highscore: " high-score))))))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-menu!
   :update-fn update-menu
   :mouse-pressed-fns [button/handle-buttons-pressed]
   :mouse-released-fns [button/handle-buttons-released]})
