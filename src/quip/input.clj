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
  [{:keys [scenes current-scene] :as state} e]
  (let [default-handled-state (default-key-pressed state e)
        scene-handlers (get-in scenes [current-scene :key-pressed-fns])]
    (reduce (fn [acc-state f]
              (f acc-state e))
            default-handled-state
            scene-handlers)))

(defn key-released
  [{:keys [scenes current-scene] :as state} e]
  (let [default-handled-state (default-key-released state e)
        scene-handlers (get-in scenes [current-scene :key-released-fns])]
    (reduce (fn [acc-state f]
              (f acc-state e))
            default-handled-state
            scene-handlers)))

(defn mouse-pressed
  [{:keys [scenes current-scene] :as state} e]
  (let [default-handled-state (default-mouse-pressed state e)
        scene-handlers (get-in scenes [current-scene :mouse-pressed-fns])]
    (reduce (fn [acc-state f]
              (f acc-state e))
            default-handled-state
            scene-handlers)))

(defn mouse-released
  [{:keys [scenes current-scene] :as state} e]
  (let [default-handled-state (default-mouse-released state e)
        scene-handlers (get-in scenes [current-scene :mouse-released-fns])]
    (reduce (fn [acc-state f]
              (f acc-state e))
            default-handled-state
            scene-handlers)))
