(ns sprite-sampler.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [quil.core :as q]
            [quip.core :as qp]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(def colors [qpu/black qpu/grey qpu/white])

(defn setup
  []
  {:bg-color 0
   :animation 0})

(defn single-sprite
  []
  (if-let [config (-> "./config.edn"
                      slurp
                      read-string)]
    (let [w (:width config)
          h (:height config)]
      [{:sprite-group      :foo
        :uuid              (java.util.UUID/randomUUID)
        :pos               [250 250]
        :rotation          0
        :vel               [0 0]
        :w                 w
        :h                 h
        :animated?         true
        :static?           false
        :update-fn         qpsprite/update-animated-sprite
        :draw-fn           qpsprite/draw-animated-sprite
        :points            nil
        :bounds-fn         qpsprite/default-bounding-poly
        :spritesheet       (q/load-image (:file config))
        :animations        (:animations config)
        :current-animation (last (keys (:animations config)))
        :delay-count       0
        :animation-frame   0}])
    []))

(defn update-fn
  [state]
  (qpscene/update-scene-sprites state))

(defn draw-fn
  [{:keys [bg-color] :as state}]
  (qpu/background (nth colors bg-color))
  (qpscene/draw-scene-sprites state))

(defn quit-on-esc
  [state e]
  (when (= 27 (:key-code e))
    (quil.core/exit))
  state)

(defn change-bg-color
  [{:keys [bg-color] :as state} e]
  (case (:key e)
    :up (assoc state :bg-color (mod (inc bg-color) 3))
    :down (assoc state :bg-color (mod (dec bg-color) 3))
    state))

(defn change-animation
  [{:keys [animation] :as state} e]
  (if (= :space (:key e))
    (let [anims (-> (get-in state [:scenes :sampler :sprites])
                    first
                    :animations)]
      (-> state
          (assoc :animation (mod (inc animation) (count anims)))
          (update-in [:scenes :sampler :sprites]
                     (fn [[s]]
                       [(qpsprite/set-animation s (nth (keys anims) animation))]))))
    state))

(defn init-scenes
  []
  {:sampler {:sprites         (single-sprite)
             :update-fn       update-fn
             :draw-fn         draw-fn
             :key-pressed-fns [quit-on-esc
                               change-bg-color
                               change-animation]}})

(def sprite-sampler-game
  (qp/game {:title          "Sprite Sampler"
            :size           [500 500]
            :setup          setup
            :init-scenes-fn init-scenes
            :current-scene  :sampler}))

(defn -main
  [& args]
  (qp/run sprite-sampler-game))
