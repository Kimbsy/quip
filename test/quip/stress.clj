(ns quip.stress
  (:require [quil.core :as q]
            [quip.core :as qp]
            [quip.profiling :as qpprofiling]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]))

(def test-id (str "stress-test_" (System/currentTimeMillis)))

(def stress-test-file (str "/tmp/" test-id ".edn"))

(defn stress-test-draw
  [{:keys [frame-times] :as state}]
  (q/background 0 153 255)
  (qpscene/draw-scene-sprites (qpscene/get-current-scene state))
  (q/fill 0)
  (q/rect 0 0 100 40)
  (q/fill 255)
  (when frame-times
    (when-let [fps (:average-fps (qpprofiling/profiling-info frame-times))]
      (q/text (str "fps: " (int fps)) 10 25))))

(defn sprite-count
  [{:keys [current-scene] :as state}]
  (count (get-in state [:scenes current-scene :sprites])))

(defn big-captain
  []
  (let [animations {:jump {:frames      7
                           :y-offset    3
                           :frame-delay 3}}
        rand-pos   [(- (rand-int (q/width)) 120)
                    (- (rand-int (q/height)) 180)]]
    (qpsprite/animated-sprite rand-pos
                              240
                              360
                              "captain-big.png"
                              :animations animations
                              :current-animation :jump)))

(defn add-sprite
  [sprites]
  (cons (big-captain) sprites))

(defn stress-test-update
  [{:keys [current-scene global-frame test-id frame-times] :as state}]
  (if (zero? (mod global-frame 100))
    (let [results (read-string (slurp stress-test-file))]
      (spit stress-test-file
            (conj results {:sprite-count (sprite-count state)
                           :profiling    (qpprofiling/profiling-info frame-times)}))
      (-> state
          (update-in [:scenes current-scene :sprites] add-sprite)
          (update-in [:scenes current-scene] qpscene/update-scene-sprites)))
    (update-in state [:scenes current-scene] qpscene/update-scene-sprites)))

(defn init-scenes
  []
  {:stress-test {:sprites   []
                 :draw-fn   stress-test-draw
                 :update-fn stress-test-update}})

(defn setup
  []
  (q/text-font (q/create-font "Ubuntu Mono Bold" 20))
  (prn "++++++++ INITIALISING STRESS TEST FILE ++++++++")
  (prn stress-test-file)
  (spit stress-test-file "[]")
  {})

;;;;;;;; Game definition

(def stress-test (qp/game {:title          "Performance Stress Test"
                           :size           [1200 800]
                           :profiling?     true
                           :current-scene  :stress-test
                           :init-scenes-fn init-scenes
                           :setup          setup}))

(qp/run stress-test)
