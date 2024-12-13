(ns mouse-controls.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as sprite]
            [quip.tween :as tween]
            [quip.input :as input]
            [quip.util :as u]))

(def grey [57 57 58])
(def white [192 214 223])
(def alpha-green [140 215 144 100])

(defn deselect-all
  [state]
  (sprite/update-sprites
   state
   (sprite/has-group :heart)
   (fn [s]
     (sprite/set-animation s :none))))

(defn heart
  [pos]
  (-> (sprite/animated-sprite
       :heart   ; sprite-group, used for group collision detection
       pos
       28   ; <- width and
       24   ; <- height of each animation frame
       "img/heart.png"   ; spritesheet location in `resources` directory
       :animations {:none     {:frames      1
                               :y-offset    0
                               :frame-delay 100}
                    :selected {:frames      1
                               :y-offset    1
                               :frame-delay 100}})
      (input/on-click
       (fn [{:keys [held-keys] :as state} h0]
         ;; Hold shift to select multiple hearts
         (let [state (if (held-keys :shift)
                       state
                       (deselect-all state))]
           (-> state
               (sprite/update-sprites
                (sprite/is-sprite h0)
                #(sprite/set-animation % :selected))))))))

(defn selected?
  [sprite]
  (= :selected (:current-animation sprite)))

(defn move-to-target
  [state [tx ty]]
  (update-in state [:scenes :level-01 :sprites]
             (fn [sprites]
               (let [selected (filter selected? sprites)
                     other (remove selected? sprites)]
                 (concat other
                         (map (fn [{[sx sy] :pos :as sprite}]
                                (-> sprite
                                    (tween/add-tween
                                     (tween/tween
                                      :pos
                                      (- tx sx)
                                      :update-fn tween/tween-x-fn))
                                    (tween/add-tween
                                     (tween/tween
                                      :pos
                                      (- ty sy)
                                      :update-fn tween/tween-y-fn))))
                              selected))))))

(defn handle-mouse-pressed
  [{:keys [current-scene] :as state} e]
  ;; left clicks are handled by the `on-click` function of the sprites
  (if (= :right (q/mouse-button))
    (if (some selected? (get-in state [:scenes current-scene :sprites]))
      (-> state
          (move-to-target [(:x e) (:y e)])
          deselect-all)
      state)
    state))

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(heart [100 100])
   (heart [150 110])
   (heart [200 200])])

(defn draw-level-01!
  "Called each frame, draws the current scene to the screen"
  [state]
  (u/background grey)
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
   :mouse-pressed-fns [handle-mouse-pressed]})
