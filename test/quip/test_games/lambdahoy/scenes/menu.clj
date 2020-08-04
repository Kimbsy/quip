(ns quip.test-games.lambdahoy.scenes.menu
  (:require [quil.core :as q]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.sprites.button :as qpbutton]
            [quip.utils :as qpu]))

(defn draw-menu
  [state]
  (q/background 190)
  (qpscene/draw-scene-sprites state))

(defn update-menu
  [state]
  state)

(defn text-sprites
  []
  [(qpsprite/text-sprite "Lambda-hoy!"
                         [(* (q/width) 1/2)
                          (* (q/height) 1/5)]
                         :color qpu/white
                         :size qpu/title-text-size)
   (qpsprite/text-sprite "functional adventures on the high seas"
                         [(* (q/width) 1/2)
                          (* (q/height) 5/20)]
                         :font qpu/italic-font
                         :color qpu/white)])

(defn button-sprites
  []
  [(qpbutton/button-sprite "Play"
                           [(* (q/width) 1/2) (* (q/height) 1/2)]
                           :on-click (fn [state e] (prn "PLAY") state)
                           :offsets [:right :center])
   (qpbutton/button-sprite "Don't"
                           [(* (q/width) 1/2) (* (q/height) 2/3)]
                           :on-click (fn [state e] (prn "DONT") state))
   (qpbutton/button-sprite "Quit"
                           [(* (q/width) 1/2) (* (q/height) 5/6)]
                           :on-click (fn [state e] (prn "QUIT") state)
                           :offsets [:left :center])])

(defn init
  []
  {:draw-fn            draw-menu
   :update-fn          update-menu
   :sprites            (concat (text-sprites)
                               (button-sprites))
   :mouse-pressed-fns  [qpbutton/handle-buttons-pressed]
   :mouse-released-fns [qpbutton/handle-buttons-released]})

