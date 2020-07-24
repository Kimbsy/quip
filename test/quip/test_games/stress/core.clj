(ns quip.test-games.stress.core
  (:require [quil.core :as q]
            [quip.collision :as qpcollision]
            [quip.core :as qp]
            [quip.profiling :as qpprofiling]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [oz.core :as oz]
            [quip.test-games.stress.utils :as u]))

(def test-id (str "stress-test_" (System/currentTimeMillis)))
(def stress-test-file (str "/tmp/" test-id ".edn"))
(def stage-length 1000)

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

;;; Stage update functions

(defn increasing-animated-sprites-update
  [{:keys [current-scene] :as state}]
  (update-in state [:scenes current-scene :sprites] u/add-captain))

(defn increasing-as-collide-single-b-update
  [{:keys [current-scene] :as state}]
  (update-in state [:scenes current-scene :sprites] u/add-captain))

(defn increasing-as-collide-increasing-bs-update
  [{:keys [current-scene] :as state}]
  (-> state
      (update-in [:scenes current-scene :sprites] u/add-captain)
      (update-in [:scenes current-scene :sprites] u/add-collision-sprite)))

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
                     (assoc-in [:scenes current-scene :sprites] [(u/basic-collision-sprite)])
                     (assoc-in [:scenes current-scene :colliders] [(u/basic-collider)])
                     (assoc :frame-times [])))}
   {:name      "increasing-as-collide-increasing-bs"
    :intensify-fn increasing-as-collide-increasing-bs-update
    :init-fn   (fn [{:keys [current-scene] :as state}]
                 (-> state
                     (assoc-in [:scenes current-scene :sprites] [])
                     (assoc-in [:scenes current-scene :colliders] [(u/basic-collider)])
                     (assoc :frame-times [])))}])

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
