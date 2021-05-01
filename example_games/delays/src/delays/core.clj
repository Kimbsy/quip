(ns delays.core
  (:gen-class)
  (:require [quil.core :as q]
            [quip.core :as qp]
            [quip.delay :as qpdelay]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [quip.utils :as qpu]))

(def spin-tween
  (qptween/->tween
   :rotation
   360))

(defn captain
  [pos]
  (qpsprite/animated-sprite
   :captain
   pos
   240
   360
   "img/captain.png"
   :animations {:none {:frames      1
                       :y-offset    0
                       :frame-delay 100}
                :idle {:frames      4
                       :y-offset    1
                       :frame-delay 15}
                :run  {:frames      4
                       :y-offset    2
                       :frame-delay 8}
                :jump {:frames      7
                       :y-offset    3
                       :frame-delay 8}}
   :current-animation :idle))

(defn update-level-01
  "Update each sprite in the scene using its own `:update-fn`."
  [state]
  (-> state
      qpscene/update-scene-sprites
      qptween/update-sprite-tweens
      qpdelay/update-delays))

(defn draw-level-01
  "Draw each sprite in the scene using its own `:draw-fn`."
  [state]
  (qpu/background [0 153 255])
  (qpscene/draw-scene-sprites state))

(defn print-current-frame
  "All delayed functions should take the game state and return an
  (optionally) modified version of it."
  [state]
  (prn (str "Current frame: " (:global-frame state)))
  state)

(defn delays
  "Define the delays which exist as the scene begins."
  []
  [;; We can have a simple side-effecting delayed function:
   (qpdelay/->delay 100 print-current-frame)

   ;; We can add new sprites into the scene:
   (qpdelay/add-sprites-to-scene 200 [(captain [(rand-int (q/width))
                                                (rand-int (q/height))])])

   ;; We can add tweens to sprites in the scene:
   (qpdelay/add-tween-to-sprites 300
                                 spin-tween
                                 (fn [s]
                                   (= :captain (:sprite-group s))))

   ;; We can even delay adding new delays to the scene:
   (qpdelay/->delay
    400
    (fn [state]
      (reduce qpdelay/add-delay
              state
              ;; add the same delays again!
              (delays))))])

(defn init-scenes
  "Returns a map of scenes in the game."
  []
  {:level-01 {:sprites   [(captain [150 180])]
              :update-fn update-level-01
              :draw-fn   draw-level-01
              :delays    (delays)}})

(def delays-game
  (qp/game {:title          "Delays Example"
            :init-scenes-fn init-scenes
            :current-scene  :level-01}))

(defn -main
  "Run the game."
  [& args]
  (qp/run delays-game))
