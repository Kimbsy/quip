(ns quip.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [quip.input :as qpinput]))

(def event-identity
  (fn [state e] state))

(defn default-update
  [state]
  state)

(defn default-draw
  [{:keys [scenes current-scene] :as state}]
  (q/background 0)
  (q/fill 255)
  (q/text (str "No draw-fn found for current scene " (str current-scene)) 200 200))

(defn update-state
  [{:keys [scenes current-scene] :as state}]
  (if-let [scene-update (get-in scenes [current-scene :update-fn])]
    (scene-update state)
    state))

(defn draw-state
  [{:keys [scenes current-scene] :as state}]
  (if-let [scene-draw (get-in scenes [current-scene :draw-fn])]
    (scene-draw state)
    (default-draw state)))

(def default-opts
  {:title          "title"
   :size           [600 400]
   :setup          (constantly {})
   :update         update-state
   :draw           draw-state
   :key-pressed    qpinput/key-pressed
   :key-released   qpinput/key-released
   :mouse-pressed  event-identity
   :mouse-released event-identity
   :middleware     [m/fun-mode]
   :frame-rate     30})

(def default-initial-state {:held-keys    #{}
                            :scene-paused? false})

(defn game
  [{:keys [scenes current-scene] :as override-opts}]
  (let [opts (merge default-opts override-opts)]
    (-> opts
        (update :setup (fn [setup]
                         (fn []
                           (q/frame-rate (:frame-rate opts))
                           (-> (merge default-initial-state (setup))
                               (assoc :scenes scenes)
                               (assoc :current-scene current-scene))))))))

(defn run
  [{:keys [title size setup update draw key-pressed key-released
           mouse-pressed mouse-released middleware]
    :as game}]
  (q/sketch
   :title title
   :size size
   :setup setup
   :update update
   :draw draw
   :key-pressed key-pressed
   :key-released key-released
   :mouse-pressed mouse-pressed
   :mouse-released mouse-released
   :middleware middleware))





;;;;;;;;

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
                          state))]
   :key-released-fns [(fn [state e]
                        (if (= :space (:key e))
                          (update state :y #(- % 10))
                          state))]})

(def test-game (game {:title         "some title"
                      :setup         (fn [] {:x 100 :y 300 :color [0 0 255]})
                      :scenes        {:menu    (init-menu)
                                      :level-1 {}}
                      :current-scene :menu}))

(run test-game)
