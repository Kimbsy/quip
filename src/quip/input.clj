(ns quip.input)

;;; Default handlers for key events.

;;; We use these to manage the set of currently held keys, which
;;; allows us to make decisions during scene update functions, useful
;;; for character control etc.

(defn default-key-pressed
  [state e]
  (update state :held-keys #(conj % (:key e))))

(defn default-key-released
  [state e]
  (update state :held-keys #(disj % (:key e))))

;;; Default handlers for mouse events.

;;; Currently haven't needed these to do anything, but they could
;;; similarly be used to keep track of the currently held mouse
;;; buttons.

(defn default-mouse-pressed
  [state e]
  state)

(defn default-mouse-released
  [state e]
  state)


;;; We allow scenes to define a collection of their own handlers for
;;; each event type.

;;; We can then reduce accross this collection using an anonymous
;;; apply-handler function (which is a closure over the event `e`) as
;;; our reducing function.

;;; Most of the time only one of the handlers will modifiy the state
;;; as it goes through, but its totally fine for multiple to.

(defn key-pressed
  "Reduce applying a handler function:
    (f state e)
  accross the collection of `:key-pressed-fns` in the current scene."
  [{:keys [input-enabled? scenes current-scene] :as state} e]
  (if input-enabled?
    (let [default-handled-state (default-key-pressed state e)
          scene-handlers        (get-in scenes [current-scene :key-pressed-fns])]
      (reduce (fn [acc-state f]
                (f acc-state e))
              default-handled-state
              scene-handlers))
    state))

(defn key-released
  "Reduce applying a handler function:
    (f state e)
  accross the collection of `:key-released-fns` in the current scene."
  [{:keys [input-enabled? scenes current-scene] :as state} e]
  (if input-enabled?
    (let [default-handled-state (default-key-released state e)
          scene-handlers        (get-in scenes [current-scene :key-released-fns])]
      (reduce (fn [acc-state f]
                (f acc-state e))
              default-handled-state
              scene-handlers))
    state))

(defn mouse-pressed
  "Reduce applying a handler function:
    (f state e)
  accross the collection of `:mouse-pressed-fns` in the current scene."
  [{:keys [input-enabled? scenes current-scene] :as state} e]
  (if input-enabled?
    (let [default-handled-state (default-mouse-pressed state e)
          scene-handlers        (get-in scenes [current-scene :mouse-pressed-fns])]
      (reduce (fn [acc-state f]
                (f acc-state e))
              default-handled-state
              scene-handlers))
    state))

(defn mouse-released
  "Reduce applying a handler function:
    (f state e)
  accross the collection of `:mouse-released-fns` in the current scene."
  [{:keys [input-enabled? scenes current-scene] :as state} e]
  (if input-enabled?
    (let [default-handled-state (default-mouse-released state e)
          scene-handlers        (get-in scenes [current-scene :mouse-released-fns])]
      (reduce (fn [acc-state f]
                (f acc-state e))
              default-handled-state
              scene-handlers))
    state))
