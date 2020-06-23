(ns quip.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [quip.input :as qpinput]
            [quip.scene :as qpscene]
            [quip.profiling :as qpprofiling]

            [quip.example :as example]))

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
  (let [new-frame (update state :global-frame inc)]
    (if-let [scene-update (get-in scenes [current-scene :update-fn])]
      (scene-update new-frame)
      new-frame)))

(defn draw-state
  [{:keys [scenes current-scene] :as state}]
  (if-let [scene-draw (get-in scenes [current-scene :draw-fn])]
    (scene-draw state)
    (default-draw state)))

(defn update-wrapper
  "Allow us to change our update function."
  [{:keys [parent-update-fn] :as state}]
  (parent-update-fn state))

(defn draw-wrapper
  [{:keys [parent-draw-fn] :as state}]
  (parent-draw-fn state))

(def default-opts
  {:title          "title"
   :size           [600 400]
   :setup          (constantly {})
   :update         update-wrapper
   :draw           draw-wrapper
   :key-pressed    qpinput/key-pressed
   :key-released   qpinput/key-released
   :mouse-pressed  event-identity
   :mouse-released event-identity
   :middleware     [m/fun-mode]
   :on-close       #_(constantly nil) (fn [& _] (prn "******** SHUTTING DOWN ********"))
   :frame-rate     60})

(def default-initial-state
  {:held-keys        #{}
   :input-enabled?   true
   :global-frame     0
   :parent-update-fn update-state
   :parent-draw-fn   draw-state})

(defn game
  [{:keys [init-scenes-fn current-scene profiling?] :as override-opts}]
  (let [opts-maps [default-opts
                   (if profiling?
                     qpprofiling/profiling-opts
                     {})
                   override-opts]
        opts      (apply merge opts-maps)]
    (-> opts
        (update :setup (fn [setup]
                         (fn []
                           (q/frame-rate (:frame-rate opts))
                           (let [initial-state-maps [default-initial-state
                                                     (if profiling?
                                                       qpprofiling/profiling-initial-state
                                                       {})
                                                     (setup)]]
                             (-> (apply merge initial-state-maps)
                                 (assoc :scenes (init-scenes-fn))
                                 (assoc :current-scene current-scene)))))))))

(defn run
  [{:keys [title size setup update draw key-pressed key-released
           mouse-pressed mouse-released middleware on-close]
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
   :middleware middleware
   :on-close on-close))





;;;;;;;;

(def test-game (game {:title          "some title"
                      :setup          (fn [] {:x 100 :y 300 :color [0 0 255]})
                      :init-scenes-fn (fn []
                                        {:menu    (example/init-menu)
                                         :level-1 (example/init-level-1)})
                      :current-scene  :level-1
                      :profiling?     true}))

(run test-game)
