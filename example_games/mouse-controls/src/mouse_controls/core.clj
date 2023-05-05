(ns mouse-controls.core
  (:gen-class)
  (:require [quil.core :as q]
            [quip.core :as qp]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [quip.utils :as qpu]
            [mouse-controls.common :as common]))

(defn heart
  [pos]
  (qpsprite/animated-sprite
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
                           :frame-delay 100}}))

(defn bounding-rect
  [{[x y] :pos :keys [w h]}]
  [(- x (/ w 2))
   (- y (/ h 2))
   (- (+ x w) (/ w 2))
   (- (+ y h) (/ h 2))])

(defn in-selection?
  [{[sx sy] :selection-start} sprite]
  (let [ex (q/mouse-x)
        ey (q/mouse-y)]
    (qpu/rects-overlap? (bounding-rect sprite)
                        [(min sx ex) (min sy ey) (max sx ex) (max sy ey)])))

(defn update-selected-sprites
  [{:keys [selecting?] :as state}]
  (if selecting?
    (update-in state [:scenes :level-01 :sprites]
               (fn [sprites]
                 (map (fn [s]
                        (if (in-selection? state s)
                          (qpsprite/set-animation s :selected)
                          (qpsprite/set-animation s :none)))
                      sprites)))
    state))

(defn update-level-01
  "Update each sprite in the scene using its own `:update-fn`."
  [state]
  (-> state
      update-selected-sprites
      qpsprite/update-scene-sprites
      qptween/update-sprite-tweens))

(defn selected?
  [sprite]
  (= :selected (:current-animation sprite)))

(defn draw-level-01
  "Draw each sprite in the scene using its own `:draw-fn`."
  [{:keys [selecting? selection-start] :as state}]
  (qpu/background common/grey)
  (qpsprite/draw-scene-sprites state)

  (if selecting?
    ;; draw the selection box
    (do
      (qpu/stroke common/white)
      (q/no-fill)
      (let [x (first selection-start)
            y (second selection-start)
            w (- (q/mouse-x) x)
            h (- (q/mouse-y) y)]
        (q/rect x y w h)))

    ;; draw the target area
    (when (some selected? (get-in state [:scenes :level-01 :sprites]))
      (q/no-stroke)
      (qpu/fill common/alpha-green)
      (q/ellipse (q/mouse-x) (q/mouse-y) 30 30))))

(defn move-to-target
  [state [tx ty]]
  (update-in state [:scenes :level-01 :sprites]
             (fn [sprites]
               (let [selected (filter selected? sprites)
                     other (remove selected? sprites)]
                 (concat other
                         (map (fn [{[sx sy] :pos :as sprite}]
                                (-> sprite
                                    (qptween/add-tween
                                     (qptween/->tween
                                      :pos
                                      (- tx sx)
                                      :update-fn qptween/tween-x-fn))
                                    (qptween/add-tween
                                     (qptween/->tween
                                      :pos
                                      (- ty sy)
                                      :update-fn qptween/tween-y-fn))))
                              selected))))))

(defn deselect-all
  [state]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :heart)
   (fn [s]
     (qpsprite/set-animation s :none))))

(defn handle-mouse-pressed
  [state e]
  (case (q/mouse-button)
    :left
    (-> state
        deselect-all
        (assoc :selecting? true)
        (assoc :selection-start [(:x e) (:y e)]))
    :right
    (if (some selected? (get-in state [:scenes :level-01 :sprites]))
      (-> state
          (move-to-target [(:x e) (:y e)])
          deselect-all)
      state)
    state))

(defn handle-mouse-released
  [state e]
  (-> state
      (assoc :selecting? false)))

(defn init-scenes
  "Returns a map of scenes in the game."
  []
  {:level-01 {:sprites   [(heart [100 100])
                          (heart [150 110])
                          (heart [200 200])]
              :update-fn update-level-01
              :draw-fn   draw-level-01
              :mouse-pressed-fns [handle-mouse-pressed]
              :mouse-released-fns [handle-mouse-released]}})

(def mouse-controls-game
  (qp/game {;; quip config
            :title          "Mouse Controls Example"
            :init-scenes-fn init-scenes
            :current-scene  :level-01

            ;; custom state
            :selecting?     false
            :selection-start [0 0]}))

(defn -main
  "Run the game."
  [& args]
  (qp/run mouse-controls-game))
