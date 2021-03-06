(ns tweens.core
  (:gen-class)
  (:require [quip.core :as qp]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [quip.utils :as qpu]))

(defn captain
  [pos current-animation]
  (qpsprite/animated-sprite :captain
                            pos
                            240    ; <- width and
                            360    ; <- height of each animation frame
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
                            :current-animation current-animation))

(defn tween
  "A tween requires a key of the field to modify and a value to modify it by.

  For non-numerical fields (such as `:pos`, a vector of 2 values), we
  need to supply an `:update-fn` which takes the current value of the
  field and updates it by a delta `d`.

  We can specify the tween's progression curve using the `:easing-fn`
  option, a number of default easing functions are provided, but you
  can write your own if needed.

  We can make the tween yoyo with the `:yoyo?` option. This will
  return the field to it's orginal untweened value by applying the
  tween in reverse. For non-numerical fields we will also need to
  supply a `:yoyo-update-fn`.

  We can specify a positive number of times to repeat the tween with
  the `:repeat-times` option (defaults to 1). We can supply the `##Inf`
  special symbolic value for continuous repetition.

  We can alter the number of deltas generated and applied by the tween
  with the `:step-count` option. This defaults to 100 and determines
  how many updates will be applied to the sprite to achieve the
  modified field value.

  We can specify `:on-<x>-fn` functions to modify the sprite when it
  `repeat`s, `yoyo`s or `complete`s."
  []
  (qptween/->tween :pos
                   300
                   :update-fn (fn [[x y] d]
                                [(+ x d) y])
                   :easing-fn qptween/exponential-easing-fn
                   :repeat-times 3
                   :on-repeat-fn (fn [s]
                                   (prn "REPEAT!")
                                   (qpsprite/set-animation s :run))
                   :yoyo? true
                   :yoyo-update-fn (fn [[x y] d]
                                     [(- x d) y])
                   :on-yoyo-fn (fn [s]
                                 (prn "YOYO!")
                                 (qpsprite/set-animation s :jump))
                   :on-complete-fn (fn [s]
                                     (prn "COMPLETE!")
                                     (qpsprite/set-animation s :idle))))

(defn random-pos-tween
  "Creates a non-repeating yoyoing tween which chooses a random axis and
  magnitude.

  When it yoyos a new `random-pos-tween` is added to the sprite."
  []
  (let [axis (rand-int 2)]
    (qptween/->tween :pos
                     (- (rand-int 200) 100)
                     :update-fn (if (zero? axis)
                                  (fn [[x y] d]
                                    [(+ x d) y])
                                  (fn [[x y] d]
                                    [x (+ y d)]))
                     :yoyo? true
                     :yoyo-update-fn (if (zero? axis)
                                       (fn [[x y] d]
                                         [(- x d) y])
                                       (fn [[x y] d]
                                         [x (- y d)]))
                     :on-yoyo-fn (fn [s] (qptween/add-tween s (random-pos-tween))))))

(defn sprites
  "A basic sprite and a tweened version."
  []
  (let [tween           (tween)
        captain-sprite  (captain [150 180] :run)
        tweened-captain (qptween/add-tween captain-sprite tween)]
    [captain-sprite
     tweened-captain]))

(defn update-level-01
  "Update each sprite in the scene using its own `:update-fn`."
  [state]
  (-> state
      qpscene/update-scene-sprites
      qptween/update-sprite-tweens))

(defn draw-level-01
  "Draw each sprite in the scene using its own `:draw-fn`."
  [state]
  (qpu/background [0 153 255])
  (qpscene/draw-scene-sprites state))

(defn quit-on-esc
  [state e]
  (when (= 27 (:key-code e))
    (quil.core/exit))
  state)

(defn print-state
  [state e]
  (when (= 32 (:key-code e))
    (prn state))
  state)

(defn init-scenes
  []
  {:level-01 {:sprites   (sprites)
              :update-fn update-level-01
              :draw-fn   draw-level-01
              :key-pressed-fns [quit-on-esc
                                print-state]}})

;; Game definition
(def tweens-game
  (qp/game {:title          "Tweens example"
            :init-scenes-fn init-scenes
            :current-scene  :level-01}))

(defn -main
  "Run the game"
  [& args]
  (qp/run tweens-game))
