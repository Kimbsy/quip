(ns quip.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [quip.input :as qpinput]
            [quip.profiling :as qpprofiling]
            [quip.sound :as qpsound]))

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

(defn default-on-close
  [& _]
  (prn "******** SHUTTING DOWN ********"))

(def default-opts
  "Default game configuration options."
  {:title          "title"
   :size           [600 400]
   :setup          (constantly {})
   :update         update-wrapper
   :draw           draw-wrapper
   :key-pressed    qpinput/key-pressed
   :key-released   qpinput/key-released
   :mouse-pressed  qpinput/mouse-pressed
   :mouse-released qpinput/mouse-released
   :middleware     [m/fun-mode]
   :on-close       default-on-close
   :frame-rate     60})

(def default-initial-state
  "Default initial values for the `state` map. The result of the game's
  `:setup` function will be merged on top."
  {:held-keys        #{}
   :input-enabled?   true
   :global-frame     1
   :parent-update-fn update-state
   :parent-draw-fn   draw-state})

(defn game
  "Create a quip game configuration.

  Takes a single `override-opts` map argument which contains overrides
  for `default-opts`.

  Works with an empty `override-opts`, but needs a `:init-scenes-fn`
  and a `:current-scene` to start doing anything useful."
  [{:keys [init-scenes-fn current-scene profiling?]
    :or   {init-scenes-fn (constantly {})
           current-scene  :none
           profiling?     false}
    :as   override-opts}]
  (let [opts-maps [default-opts
                   (if profiling?
                     qpprofiling/profiling-opts
                     {}) ; @TODO: this is a little icky
                   override-opts]
        opts (apply merge opts-maps)]
    (-> opts

        ;; wrap the supplied `:setup` function (which returns the
        ;; desired initial `state` map) so we can supply defaults.
        (update :setup
                (fn [setup]
                  (fn []
                    (q/frame-rate (:frame-rate opts))
                    (let [initial-state-maps
                          [default-initial-state

                           (if profiling?
                             qpprofiling/profiling-initial-state
                             {}) ; @TODO: this is a little icky

                           ;; invoke the supplied `:setup` function to
                           ;; allow overriding the initial `state` map
                           (setup)]]

                      (-> (apply merge initial-state-maps)
                          (assoc :scenes (init-scenes-fn))
                          (assoc :current-scene current-scene))))))

        ;; wrap the existing `:on-close` to stop music playing
        (update :on-close (fn [on-close]
                            (fn [state]
                              (qpsound/stop-music)
                              (on-close state)))))))

(defn run
  "Run a quip game configuration as a quil sketch."
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
