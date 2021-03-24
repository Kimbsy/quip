(ns tweens.benchmarking
  (:require [criterium.core :as c]))

(def deltas
  [2.777777777777779E-4 8.333333333333333E-4 0.0013888888888888894
   0.001944444444444444 0.0024999999999999996 0.003055555555555558
   0.0036111111111111153 0.004166666666666654 0.004722222222222228
   0.005277777777777794 0.005833333333333305 0.006388888888888909
   0.0069444444444444545 0.007499999999999972 0.008055555555555566
   0.008611111111111139 0.009166666666666629 0.00972222222222223
   0.010277777777777802 0.010833333333333306 0.011388888888888879
   0.011944444444444494 0.012499999999999956 0.013055555555555598
   0.013611111111111102 0.014166666666666605 0.014722222222222275
   0.015277777777777779 0.015833333333333255 0.016388888888888953
   0.016944444444444484 0.01749999999999996 0.018055555555555602
   0.018611111111111023 0.01916666666666661 0.019722222222222308
   0.02027777777777784 0.02083333333333326 0.021388888888888957
   0.02194444444444449 0.02249999999999991 0.023055555555555496
   0.023611111111111194 0.02416666666666656 0.024722222222222312
   0.025277777777777843 0.025833333333333264 0.026388888888889017
   0.026944444444444327 0.027499999999999858 0.02805555555555561
   0.028611111111111254 0.029166666666666563 0.029722222222222316
   0.030277777777777848 0.030833333333333268 0.0313888888888888
   0.03194444444444444 0.03249999999999997 0.033055555555555616])

(def base-tween
  {:on-complete-fn identity,
   :completed? false,
   :normalized-deltas deltas,
   :yoyo-update-fn (fn [[x y] d] [(- x d) y])
   :total-change 300,
   :yoyoing? true,
   :field :pos,
   :easing-fn quip.tween/exponential-easing-fn,
   :update-fn (fn [[x y] d] [(+ x d) y]),
   :on-repeat-fn identity,
   :repeat-times 2,
   :on-yoyo-fn identity,
   :yoyo? true,
   :progress 37,
   :step-count 60})

(defn base-sprite
  []
  {:animations
   {:none {:frames 1, :y-offset 0, :frame-delay 100},
    :idle {:frames 4, :y-offset 1, :frame-delay 15},
    :run {:frames 4, :y-offset 2, :frame-delay 8},
    :jump {:frames 7, :y-offset 3, :frame-delay 8}},
   :tweens []
   :spritesheet nil
   :animated? true,
   :rotation 0,
   :animation-frame 1,
   :w 240,
   :pos [270.3333333333334 180],
   :points nil,
   :update-fn quip.sprite/update-animated-sprite,
   :delay-count 7,
   :h 360,
   :bounds-fn quip.sprite/default-bounding-poly,
   :sprite-group :captain,
   :uuid (java.util.UUID/randomUUID)
   :vel [0 0],
   :static? false,
   :current-animation :idle,
   :draw-fn quip.sprite/draw-animated-sprite})

(def base-state
  {:held-keys #{:space},
   :input-enabled? true,
   :global-frame 431,
   :parent-update-fn quip.core/update-state,
   :parent-draw-fn quip.core/draw-state,
   :scenes
   {:level-01
    {:sprites []
     :update-fn tweens.core/update-level-01,
     :draw-fn tweens.core/draw-level-01,
     :key-pressed-fns
     [tweens.core/quit-on-esc
      tweens.core/print-state]}},
   :current-scene :level-01})

(defn construct-state
  [sprite-count tween-count]
  (assoc-in base-state
            [:scenes :level-01 :sprites]
            (for [_ (range sprite-count)]
              (assoc (base-sprite)
                     :tweens
                     (repeat tween-count base-tween)))))

(defn get-mean
  [sprite-count tween-count]
  (let [state (construct-state sprite-count tween-count)]
    (-> (c/quick-benchmark (quip.core/update-state state) {:verbose false})
        :mean
        first)))

(defn benchmarking
  []
  (let [xs (map #(* % 2) (range 10))]
    (spit "bench-results.csv"
          (str (clojure.string/join ", " xs) "\n"))
    (doall
     (for [tween-count xs]
       (let [row (for [sprite-count xs]
                   (format "%.12f" (get-mean sprite-count tween-count)))]
         (spit "bench-results.csv"
               (str (clojure.string/join ", " row) "\n")
               :append true))))))
