(ns quip.delay
  "Frame-based delays for executing arbitrary code, useful for cutscenes
  and complex animations."
  (:require [quip.tween :as tween]))

;;;; @TODO: Should delays be flagged for removeal/persistence across
;;;; scene transitions?

(defn delay
  "Create a delay which will execute a function in a specified number of
  frames."
  [remaining f & {:keys [tag] :or {tag :none}}]
  {:remaining      remaining
   :on-complete-fn f
   :tag            tag})

;; @NOTE: deprecated name
(def ->delay delay)

(defn add-delay
  "Add a delay into the current scene."
  [{:keys [current-scene] :as state} d]
  (let [path [:scenes current-scene :delays]]
    (if (seq (get-in state path))
      (update-in state path conj d)
      (assoc-in state path [d]))))

(defn apply-all
  "Apply a sequence of delayed functions to the game state."
  [state ds]
  (reduce (fn [state d]
            ((:on-complete-fn d) state))
          state
          ds))

(defn update-delay
  "Decrement `:remaining` but stop at 0."
  [d]
  (update d :remaining (comp (partial max 0) dec)))

(def finished? (comp zero? :remaining))

(defn update-state
  "Update all the delays in the current scene and apply the ones that
  have finished."
  [{:keys [current-scene] :as state}]
  (let [path   [:scenes current-scene :delays]
        delays (get-in state path)]
    (if (seq delays)
      (let [updated-delays (map update-delay delays)
            finished       (filter finished? updated-delays)
            unfinished     (remove finished? updated-delays)]
        (-> state
            (assoc-in path unfinished)
            (apply-all finished)))
      state)))

(defn sequential-delays
  "Creates a sequence of delays from a given list of [time
  on-complete-fn] tuples where each time is relative to the ending of
  the previous delay."
  [delays & {:keys [initial-delay] :or {initial-delay 0}}]
  (:ds (reduce (fn [{:keys [ds curr] :as acc}
                    [d f]]
                 (-> acc
                     (update :ds conj (->delay (+ curr d) f))
                     (update :curr + d)))
               {:ds   []
                :curr initial-delay}
               delays)))

;;; Useful utility delays.

(defn add-sprites-to-scene
  "Create a delay which adds new sprites to the current scene."
  [remaining new-sprites]
  (->delay
   remaining
   (fn [{:keys [current-scene] :as state}]
     (update-in state [:scenes current-scene :sprites]
                concat new-sprites))))

(defn add-tween-to-sprites
  "Create a delay which adds a tween to the collection of sprites which
  satisfy the `sprite-selection-fn`."
  [remaining tween sprite-selection-fn]
  (->delay
   remaining
   (fn [{:keys [current-scene] :as state}]
     (let [path       [:scenes current-scene :sprites]
           sprites    (get-in state path)
           relevant   (filter sprite-selection-fn sprites)
           irrelevant (remove sprite-selection-fn sprites)]
       (assoc-in state path (concat irrelevant
                                    (map #(tween/add-tween % tween)
                                         relevant)))))))
