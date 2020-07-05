(ns quip.test-games.stress
  (:require [quil.core :as q]
            [quip.collision :as qpcollision]
            [quip.core :as qp]
            [quip.profiling :as qpprofiling]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [oz.core :as oz]))

(def test-id (str "stress-test_" (System/currentTimeMillis)))

(def stress-test-file (str "/tmp/" test-id ".edn"))

(defn stress-test-draw
  [{:keys [frame-times] :as state}]
  (q/background 0 153 255)
  (qpscene/draw-scene-sprites state)
  (q/fill 0)
  (q/rect 0 0 100 40)
  (q/fill 255)
  (when frame-times
    (when-let [fps (:average-fps (qpprofiling/profiling-info frame-times))]
      (q/text (str "fps: " (int fps)) 10 25))))

(defn big-captain
  []
  (let [animations {:jump {:frames      7
                           :y-offset    3
                           :frame-delay 3}}
        rand-pos   [(- (rand-int (q/width)) 120)
                    (- (rand-int (q/height)) 180)]]
    (-> (qpsprite/animated-sprite :big-captain
                                  rand-pos
                                  240
                                  360
                                  "img/captain-big.png"
                                  :animations animations
                                  :current-animation :jump)
        (assoc :collisions 0))))

(defn draw-box
  [{[x y] :pos}]
  (q/fill 255 0 0)
  (q/rect x y 20 20))

(defn basic-collision-sprite
  []
  {:sprite-group :hit-me
   :pos          [(- (rand-int (q/width)) 120)
                  (- (rand-int (q/height)) 180)]
   :collisions   0
   :w            20
   :h            20
   :update-fn    identity
   :draw-fn      draw-box})

(defn add-captain
  [sprites]
  (cons (big-captain) sprites))

(defn add-collision-sprite
  [sprites]
  (cons (basic-collision-sprite) sprites))

(defn basic-collider-fn
  [s]
  (update s :collisions inc))

(defn removing-collider-fn
  [_]
  nil)

(defn basic-collider
  []
  (qpcollision/collider :big-captain :hit-me basic-collider-fn basic-collider-fn))

(defn remove-a-collider
  []
  (qpcollision/collider :big-captain :hit-me removing-collider-fn basic-collider-fn))

(defn remove-ab-collider
  []
  (qpcollision/collider :big-captain :hit-me removing-collider-fn removing-collider-fn))



;;; Stage update functions

(defn increasing-animated-sprites-update
  [{:keys [current-scene] :as state}]
  (update-in state [:scenes current-scene :sprites] add-captain))

(defn increasing-as-collide-single-b-update
  [{:keys [current-scene] :as state}]
  (update-in state [:scenes current-scene :sprites] add-captain))

(defn increasing-as-collide-increasing-bs-update
  [{:keys [current-scene] :as state}]
  (-> state
      (update-in [:scenes current-scene :sprites] add-captain)
      (update-in [:scenes current-scene :sprites] add-collision-sprite)))



;;; Defining the stages of the stress test.

(def stages
  [{:name      "empty"
    :intensify-fn identity
    :init-fn   identity}
   {:name      "increasing-animated-sprites"
    :intensify-fn increasing-animated-sprites-update
    :init-fn   (fn [{:keys [current-scene] :as state}]
                 (-> state
                     (assoc-in [:scenes current-scene :sprites] [])
                     (assoc-in [:scenes current-scene :colliders] [])
                     (assoc :frame-times [])))}
   {:name      "increasing-as-collide-single-b"
    :intensify-fn increasing-as-collide-single-b-update
    :init-fn   (fn [{:keys [current-scene] :as state}]
                 (-> state
                     (assoc-in [:scenes current-scene :sprites] [(basic-collision-sprite)])
                     (assoc-in [:scenes current-scene :colliders] [(basic-collider)])
                     (assoc :frame-times [])))}
   {:name      "increasing-as-collide-increasing-bs"
    :intensify-fn increasing-as-collide-increasing-bs-update
    :init-fn   (fn [{:keys [current-scene] :as state}]
                 (-> state
                     (assoc-in [:scenes current-scene :sprites] [])
                     (assoc-in [:scenes current-scene :colliders] [(basic-collider)])
                     (assoc :frame-times [])))}])

(def stage-length 5000)

(defn reset-state
  [{:keys [stage-idx] :as state}]
  (when-let [init-fn (get-in stages [stage-idx :init-fn])]
    (init-fn state)))

(defn stress-test-update
  [{:keys [current-scene global-frame test-id frame-times] :as state}]
  (if-let [staged (if (zero? (mod global-frame stage-length))
                    (-> state
                        (update :stage-idx inc)
                        reset-state)
                    state)]

    (let [stage-idx    (:stage-idx staged)
          stage        (get stages stage-idx)
          intensify-fn (:intensify-fn stage)
          stage-name   (:name stage)]
      (if (zero? (mod global-frame 100))
        (let [results (read-string (slurp stress-test-file))]
          (spit stress-test-file
                (conj results {:stage-frame (mod global-frame stage-length)
                               :stage-name  stage-name
                               :profiling   (qpprofiling/profiling-info frame-times)}))
          (-> staged
              intensify-fn
              qpscene/update-scene-sprites
              qpcollision/update-collisions))
        (-> staged
            qpscene/update-scene-sprites
            qpcollision/update-collisions)))

    (let [viz {:data     {:values (read-string (slurp stress-test-file))}
               :encoding {:x     {:field "stage-frame" :type "quantitative"}
                          :y     {:field "profiling.average-fps" :type "quantitative"}
                          :color {:field "stage-name" :type "nominal"}}
               :mark     "line"
               :width    1500
               :height   400}]
      (oz/view! viz)
      (q/exit)
      state)))

(defn init-scenes
  []
  {:stress-test {:sprites   []
                 :draw-fn   stress-test-draw
                 :update-fn stress-test-update
                 :colliders []}})

(defn setup
  []
  (oz/start-server!)
  (q/text-font (q/create-font "Ubuntu Mono Bold" 20))
  (prn "++++++++ INITIALISING STRESS TEST FILE ++++++++")
  (prn stress-test-file)
  (spit stress-test-file "[]")
  {:stage-idx 0})

;;;;;;;; Game definition

(def stress-test (qp/game {:title          "Performance Stress Test"
                           :size           [1200 800]
                           :profiling?     true
                           :current-scene  :stress-test
                           :init-scenes-fn init-scenes
                           :setup          setup}))

(qp/run stress-test)
