(ns quip.scene
  (:require [quil.core :as q]))

(defn fade-to-black
  [state progress max]
  (q/fill 0 (int (* 255 (/ progress max))))
  (q/rect 0 0 (q/width) (q/height)))

(defn transition
  [{:keys [parent-update-fn parent-draw-fn] :as state}
   target-scene
   & {:keys [transition-fn
             transition-length]
      :or   {transition-fn     fade-to-black
             transition-length 20}}]
  (-> state
      (assoc :transition-progress 0)
      (assoc :input-enabled? false)
      (assoc :parent-update-fn (fn [{:keys [transition-progress] :as state}]
                                 (if (< (or transition-progress 0) transition-length)
                                   (update state :transition-progress inc)
                                   (-> state
                                       (assoc :current-scene target-scene)
                                       (assoc :parent-update-fn parent-update-fn)
                                       (assoc :parent-draw-fn parent-draw-fn)
                                       (assoc :input-enabled? true)
                                       (dissoc :transition-progress)))))
      (assoc :parent-draw-fn (fn [{:keys [transition-progress] :as state}]
                               (transition-fn state transition-progress transition-length)))))
