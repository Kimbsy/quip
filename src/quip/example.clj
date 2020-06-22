(ns quip.example
  (:require [quil.core :as q]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]))

(defn init-menu
  []
  {:draw-fn   (fn [{:keys [x y color] :as state}]
                (q/background 100)
                (apply q/fill color)
                (q/rect x y 10 10))
   :update-fn (fn [{:keys [held-keys] :as state}]                
                (cond (held-keys :left)
                      (update state :x dec)
                      
                      (held-keys :right)
                      (update state :x inc)

                      :default
                      state))
   :key-pressed-fns  [(fn [{:keys [held-keys] :as state} e]
                        (if (= :space (:key e))
                          (letfn [(shift [coll] (concat (rest coll) (take 1 coll)))]
                            (update state :color shift))
                          state))
                      (fn [state e]
                        (if (= 10 (:key-code e))
                          (qpscene/transition state :level-1
                                              :transition-length 30
                                              :transition-fn
                                              (fn [state progress max]
                                                (q/fill 0)
                                                (q/rect 0 0 (-> progress
                                                                (/ max)
                                                                (* (q/width))
                                                                int)
                                                        (q/height))))
                          state))]
   :key-released-fns [(fn [state e]
                        (if (= :space (:key e))
                          (update state :y #(- % 10))
                          state))]})

(defn big-captain
  [pos]
  (qpsprite/animated-sprite pos
                            240
                            360
                            "captain-big.png"
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
                                                :frame-delay 5}}))

(defn init-level-1
  []
  {:key-pressed-fns [(fn [state e]
                       (if (= 10 (:key-code e))
                         (qpscene/transition state :menu)
                         state))]
   :sprites [(big-captain [200 200])]
   :draw-fn (fn [{:keys [current-scene] :as state}]
              (qpscene/draw-scene-sprites (qpscene/get-current-scene state)))
   :update-fn (fn [{:keys [current-scene] :as state}]
                (update-in state [:scenes current-scene] qpscene/update-scene-sprites))})
