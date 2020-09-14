(ns quip.test-games.lambdahoy.scenes.ocean
  (:require [quil.core :as q]
            [quip.collision :as qpcollision]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(defn draw-ocean
  [state]
  (q/background 100)
  (qpscene/draw-scene-sprites state)
  (qpu/fill qpu/red)
  (q/rect 300 300 10 10)
  (qpu/fill qpu/blue)
  (q/rect 500 300 10 10))

(defn rotate-player
  [sprites held-keys]
  (map (fn [s]
         (if (= :player (:sprite-group s))
           (if-let [direction (first (clojure.set/intersection #{:left :right} held-keys))]
             (case direction
               :left (update s :rotation #(- % 5))
               :right (update s :rotation #(+ % 5)))
             s)
           s))
       sprites))

(defn update-ocean
  [{:keys [current-scene held-keys] :as state}]
  (-> state
      (update-in [:scenes current-scene :sprites] #(rotate-player % held-keys))
      qpscene/update-scene-sprites
      qpcollision/update-collisions))

(defn player-sprite
  []
  [(qpsprite/animated-sprite :player
                             [300 300]
                             240
                             360
                             "img/captain-big.png"
                             :animations {:none {:frames      1
                                                 :y-offset    0
                                                 :frame-delay 100}
                                          :idle {:frames      4
                                                 :y-offset    1
                                                 :frame-delay 10}
                                          :run  {:frames      4
                                                 :y-offset    2
                                                 :frame-delay 5}
                                          :jump {:frames      7
                                                 :y-offset    3
                                                 :frame-delay 5}}
                             :current-animation :idle)])

(defn other-sprite
  []
  [(qpsprite/image-sprite :other [500 300] 112 96 "img/a0218040a060000.png")])

(defn init
  []
  {:draw-fn         draw-ocean
   :update-fn       update-ocean
   :sprites         (concat (player-sprite)
                            (other-sprite))
   :mouse-pressed-fns [(fn [state e]
                         (prn e)
                         state)]
   :key-pressed-fns [(fn [state e]
                       (if (= 27 (:key-code e))
                         (qpscene/transition state :menu)
                         state))
                     (fn [{:keys [current-scene held-keys] :as state} e]
                       (if (#{:left :right} (:key e))
                         (update-in state [:scenes current-scene :sprites] #(rotate-player % held-keys))
                         state))]
   :colliders [(qpcollision/collider :player :other (fn [{:keys [rotation] :as s}]
                                                      (prn rotation)
                                                      s)
                                     identity
                                     :collision-detection-fn qpcollision/rotating-polys-collide?)]})
