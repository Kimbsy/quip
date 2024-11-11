(ns quip.core
  "Creating and running a game, along with management of update and draw
  functions."
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [quip.input :as qpinput]
            [quip.sound :as qpsound]
            [quip.utils :as qpu]))

(defn default-update
  [state]
  state)

(defn default-draw
  [{:keys [scenes current-scene] :as state}]
  (q/background 0)
  (q/fill 255)
  (q/text-align :left :baseline)
  (q/text-font (q/create-font qpu/default-font qpu/default-text-size))
  (q/text (str "No draw-fn found for current scene " (str current-scene)) 200 200))

(defn update-framerate
  "Keep track of the current average framerate."
  [{:keys [display-fps? previous-frame-time dt-window] :as state}]
  (if display-fps?
    (let [current-time (System/currentTimeMillis)
          dt (- current-time previous-frame-time)
          dt-n (count dt-window)]
      ;; record the delta-time of 30 frames and then average them
      (if (< dt-n 30)
        (-> state
            (update :dt-window conj dt)
            (assoc :previous-frame-time current-time))
        (-> state
            (assoc :dt-window [])
            (assoc :average-fps (/ 1000 (/ (apply + dt-window) dt-n)))
            (assoc :previous-frame-time current-time))))
    state))

(defn update-state
  [{:keys [scenes current-scene] :as state}]
  ;; quip updates
  (let [new-frame (-> state
                      (update :global-frame inc)
                      update-framerate)]
    ;; scene updates
    (if-let [scene-update (get-in scenes [current-scene :update-fn])]
      (scene-update new-frame)
      new-frame)))

(defn draw-fps-counter
  [{:keys [average-fps] :as state}]
  (let [text-h qpu/default-text-size
        text-w (/ qpu/default-text-size 2)]
    ;; draw black box
    (q/fill 0)
    (q/rect 0 0 (* text-w 10) (* text-h 1))

    ;; draw white fps text (rounded to 2dp)
    (q/text-align :left :center)
    (q/fill 255)
    (q/text-font (q/create-font qpu/default-font text-h))
    (q/text (str "FPS: " (format "%.2f" (float average-fps))) 0 (/ text-h 2))))

(defn draw-state
  [{:keys [display-fps? scenes current-scene] :as state}]
  (if-let [scene-draw (get-in scenes [current-scene :draw-fn])]
    (scene-draw state)
    (default-draw state))
  (when display-fps?
    (draw-fps-counter state)))

(defn update-wrapper
  "Allow us to change our update function."
  [{:keys [parent-update-fn] :as state}]
  (parent-update-fn state))

(defn draw-wrapper
  "Allow us to change our draw function."
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
  {:held-keys           #{}
   :input-enabled?      true
   :global-frame        1
   :display-fps?        false ;; @TODO: should this be part of a broader `debug` mode?
   :previous-frame-time (System/currentTimeMillis)
   :average-fps         0
   :dt-window           []
   :parent-update-fn    update-state
   :parent-draw-fn      draw-state})

(defn game
  "Create a quip game configuration.

  Takes a single `override-opts` map argument which contains overrides
  for `default-opts`.

  Works with an empty `override-opts`, but needs a `:init-scenes-fn`
  and a `:current-scene` to start doing anything useful."
  [{:keys [init-scenes-fn current-scene]
    :or   {init-scenes-fn (constantly {})
           current-scene  :none}
    :as   override-opts}]
  (let [opts (merge default-opts override-opts)]
    (-> opts

        ;; wrap the supplied `:setup` function (which returns the
        ;; desired initial `state` map) so we can supply defaults.
        (update :setup
                (fn [setup]
                  (fn []
                    (q/frame-rate (:frame-rate opts))
                    (let [initial-state-maps
                          [default-initial-state
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
