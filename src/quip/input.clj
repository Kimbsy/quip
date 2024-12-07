(ns quip.input
  "Keyboard and mouse input handling.

  We allow scenes to define a collection of their own handlers for
  each event type:
  - `:focus-gained-fns`
  - `:focus-lost-fns`
  - `:mouse-pressed-fns`
  - `:mouse-released-fns`
  - `:mouse-entered-fns`
  - `:mouse-exited-fns`
  - `:mouse-clicked-fns`
  - `:mouse-moved-fns`
  - `:mouse-dragged-fns`
  - `:mouse-wheel-fns`

  When an event occurs the handlers for that type are applied to the
  state in order. Most of the time you'll likely have at most one
  handler for each type, but it's sometimes very helpful to be able to
  split them out."
  (:require [quil.core :as q]
            [quip.collision :as collision]))

(defn identity-handler
  "Returns the state unchanged, optionally takes any number of
  additional arguments which it ignores."
  [state & _args]
  state)

(defn handler-reducer
  "Returns a function which reduces across the collection of
  `handler-fns-key` functions in the current scene, applying each to
  the accumulating state."
  [handler-fns-key &
   {:keys [default-handler]
    :or {default-handler identity-handler}}]
  (fn [{:keys [scenes current-scene] :as state}]
    (let [default-handled-state (default-handler state)
          scene-handlers        (get-in scenes [current-scene handler-fns-key])]
      (reduce (fn [acc-state f]
                (f acc-state))
              default-handled-state
              scene-handlers))))

(defn handler-reducer-with-events
  "Returns a function which reduces across the collection of
  `handler-fns-key` functions in the current scene, applying each to
  the accumulating state, passing in the event each time."
  [handler-fns-key &
   {:keys [default-handler]
    :or {default-handler identity-handler}}]
  (fn [{:keys [scenes current-scene] :as state} e]
    (let [default-handled-state (default-handler state e)
          scene-handlers        (get-in scenes [current-scene handler-fns-key])]
      (reduce (fn [acc-state f]
                (f acc-state e))
              default-handled-state
              scene-handlers))))

;;; Default handlers for key events.

;;; We use these to manage the set of currently held keys, which
;;; allows us to make decisions during scene update functions, useful
;;; for character control etc.

(defn default-key-pressed
  "Prevent the default behaviour of esc closing the sketch and add the
  pressed key to the list of currently held keys."
  [state e]
  (if (= 27 (q/key-code))
    (set! (.key (quil.applet/current-applet)) (char 0)))
  (update state :held-keys #(conj % (:key e))))

(defn default-key-released
  "Remove the released key from the list of currently held keys."
  [state e]
  (update state :held-keys #(disj % (:key e))))

;;; Default mouse-pressed handler

;;; Looks for sprites that are `:clickable?` and invokes their
;;; `:on-click-fn`.

(defn default-mouse-pressed
  "Check all `:clickable?` sprites for collision with the mouse event,
  apply the `:on-click-fn` of all that have been licked on."
  [{:keys [scenes current-scene] :as state} e]
  (let [sprites   (get-in scenes [current-scene :sprites])
        clickable (filter :clickable? sprites)]
    (reduce (fn [acc {:keys [on-click-fn] :as s}]
              ;; Using our most powerful (albeit expensive) collision detection.
              (if (collision/pos-in-rotating-poly? {:pos ((juxt :x :y) e)} s)
                (on-click-fn state s)
                state))
            state
            clickable)))

(def focus-gained (handler-reducer :focus-gained-fns))
(def focus-lost (handler-reducer :focus-lost-fns))

(def key-pressed (handler-reducer-with-events :key-pressed-fns :default-handler default-key-pressed))
(def key-released (handler-reducer-with-events :key-released-fns :default-handler default-key-released))

(def mouse-pressed (handler-reducer-with-events :mouse-pressed-fns :default-handler default-mouse-pressed))
(def mouse-released (handler-reducer-with-events :mouse-released-fns))
(def mouse-entered (handler-reducer-with-events :mouse-entered-fns))
(def mouse-exited (handler-reducer-with-events :mouse-exited-fns))
(def mouse-clicked (handler-reducer-with-events :mouse-clicked-fns))
(def mouse-moved (handler-reducer-with-events :mouse-moved-fns))
(def mouse-dragged (handler-reducer-with-events :mouse-dragged-fns))
;; "Called every time mouse wheel is rotated. Takes 1 argument - wheel rotation, an int. Negative values if the mouse wheel was rotated up/away from the user, and positive values if the mouse wheel was rotated down/towards the user."
(def mouse-wheel (handler-reducer-with-events :mouse-wheel-fns))

(defn on-click
  "Make a sprite `:clickable?` by adding an `:on-click-fn` to be invoked
  by the default mouse-pressed handler."
  [sprite f]
  (-> sprite
      (assoc :clickable? true)
      (assoc :on-click-fn f)))
