(ns quip.input)

(defn default-key-pressed
  [state e]
  (update state :held-keys #(conj % (:key e))))

(defn default-key-released
  [state e]
  (update state :held-keys #(disj % (:key e))))

(defn default-mouse-pressed
  [state e]
  state)

(defn default-mouse-released
  [state e]
  state)

(defn key-pressed
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
  [{:keys [input-enabled? scenes current-scene] :as state} e]
  (if input-enabled?
    (let [default-handled-state (default-mouse-released state e)
          scene-handlers        (get-in scenes [current-scene :mouse-released-fns])]
      (reduce (fn [acc-state f]
                (f acc-state e))
              default-handled-state
              scene-handlers))
    state))
