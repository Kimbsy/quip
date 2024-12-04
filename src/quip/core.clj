(ns quip.core
  "Creating and running a game, along with management of update and draw
  functions."
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [quip.input :as input]
            [quip.sound :as sound]
            [quip.util :as u]))

(defn default-update
  [state]
  state)

(defn default-draw!
  [{:keys [scenes current-scene] :as state}]
  (q/background 0)
  (q/fill 255)
  (q/text-align :left :baseline)
  (q/text-font (q/create-font u/default-font u/default-text-size))
  (q/text (str "No draw-fn found for current scene " (str current-scene)) 200 200))

(defn update-framerate
  "Keep track of the current average framerate."
  [{:keys [previous-frame-time fr-window] :as state}]
  (let [current-time (System/currentTimeMillis)
        fr (q/current-frame-rate)
        fr-n (count fr-window)]
    ;; record the delta-time of 30 frames and then average them
    (if (< fr-n 30)
      (update state :fr-window conj fr)
      (-> state
          (assoc :fr-window [])
          (assoc :average-fps (/ (apply + fr-window) fr-n))))))

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

(defn draw-fps-counter!
  [{:keys [average-fps] :as state}]
  (let [text-h u/default-text-size
        text-w (/ u/default-text-size 2)
        content (str "FPS: " (format "%.2f" (float average-fps)))]
    ;; draw black box
    (q/fill 0)
    (q/rect 0 0 (* text-w (count content)) (* text-h 1))

    ;; draw white fps text (rounded to 2dp)
    (q/text-align :left :center)
    (q/fill 255)
    (q/text-font (q/create-font u/default-font text-h))
    (q/text content 0 (/ text-h 2))))

(defn draw-state!
  [{:keys [display-fps? scenes current-scene] :as state}]
  (if-let [scene-draw (get-in scenes [current-scene :draw-fn])]
    (scene-draw state)
    (default-draw! state))
  (when display-fps?
    (draw-fps-counter! state)))

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
   :focus-gained   input/focus-gained
   :focus-lost     input/focus-lost
   :key-pressed    input/key-pressed
   :key-released   input/key-released
   :mouse-pressed  input/mouse-pressed
   :mouse-released input/mouse-released
   :mouse-entered  input/mouse-entered
   :mouse-exited   input/mouse-exited
   :mouse-clicked  input/mouse-clicked
   :mouse-moved    input/mouse-moved
   :mouse-dragged  input/mouse-dragged
   :mouse-wheel    input/mouse-wheel
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
   :parent-draw-fn      draw-state!})

(defn game
  "Create a quip game configuration.

  Takes a single `override-opts` map argument which contains overrides
  for `default-opts`.

  Works with an empty `override-opts`, but needs a `:init-scenes-fn`
  and a `:current-scene` to start doing anything useful."
  [{:keys [init-scenes-fn restart-fn current-scene]
    :or   {init-scenes-fn (constantly {})
           restart-fn       identity
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
                           {:restart-fn restart-fn}
                           ;; invoke the supplied `:setup` function to
                           ;; allow overriding the initial `state` map
                           (setup)]]

                      (-> (apply merge initial-state-maps)
                          (assoc :scenes (init-scenes-fn))
                          (assoc :current-scene current-scene))))))

        ;; wrap the existing `:on-close` to stop music playing
        (update :on-close (fn [on-close]
                            (fn [state]
                              (sound/stop-music!)
                              (on-close state)))))))

(defn start!
  "Run a quip game configuration as a quil sketch."
  [{:keys [title size setup update draw focus-gained focus-lost
           key-pressed key-released mouse-pressed mouse-released
           mouse-entered mouse-exited mouse-clicked mouse-moved
           mouse-dragged mouse-wheel middleware on-close]
    :as game}]
  (q/sketch
   :title title
   :size size
   :setup setup
   :update update
   :draw draw
   :focus-gained focus-gained
   :focus-lost focus-lost
   :key-pressed key-pressed
   :key-released key-released
   :mouse-pressed mouse-pressed
   :mouse-released mouse-released
   :mouse-entered mouse-entered
   :mouse-exited mouse-exited
   :mouse-clicked mouse-clicked
   :mouse-moved mouse-moved
   :mouse-dragged mouse-dragged
   :mouse-wheel mouse-wheel
   :middleware middleware
   :on-close on-close))

(defn restart
  [{:keys [restart-fn] :as state}]
  (restart-fn state))
