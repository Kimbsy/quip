(ns quip.test-games.lambdahoy.scenes.menu
  (:require [quil.core :as q]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(defn draw-menu
  [state]
  (q/background 190)
  (qpscene/draw-scene-sprites state))

(defn update-menu
  [state]
  state)

(defn main-title
  []
  (qpsprite/text-sprite "Some title" [(/ (q/width) 2) 150]
                        :color qpu/white
                        :size 70))

(defn start-button
  []
  (qpsprite/button-sprite "press" [(/ (q/width) 2)
                                   (/ (q/height) 2)]
                          :on-click (fn [state e] (prn "CLICK") state)))

(defn handle-buttons
  [{:keys [current-scene] :as state} {ex :x ey :y :as e}]
  (let [sprites (get-in state [:scenes current-scene :sprites])
        buttons (filter #(= :button (:sprite-group %)) sprites)]
    (reduce (fn [acc-state {:keys [collision-detection-fn
                                   on-click]
                            :as   b}]
              (if (collision-detection-fn {:pos [ex ey]} b)
                (on-click acc-state e)
                acc-state))
            state
            buttons)))

(defn init
  []
  {:draw-fn   draw-menu
   :update-fn update-menu
   :sprites   [(main-title)
               (start-button)]
   :mouse-pressed-fns [(fn [state e]
                         (handle-buttons state e))]})

