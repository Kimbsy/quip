(ns quip.scene
  "Transitioning between game scenes."
  (:require [quil.core :as q]))

(defn current-scene
  [{:keys [current-scene] :as state}]
  (get-in state [:scenes current-scene]))

(defn fade-to-black
  [state progress max]
  (q/fill 0 (int (* 255 (/ progress max))))
  (q/rect 0 0 (q/width) (q/height)))

(defn transition
  "Temporarily replace `parent-update-fn` and `parent-draw-fn` with
  transition handling versions which will set themselves back to the
  originals on completion."
  [{:keys [parent-update-fn parent-draw-fn] :as state}
   target-scene
   & {:keys [transition-fn
             transition-length
             init-fn]
      :or   {transition-fn     fade-to-black
             transition-length 20
             init-fn           identity}}]
  (-> state
      (assoc :transition-progress 0)
      (assoc :parent-update-fn (fn [{:keys [transition-progress] :as state}]
                                 (if (< (or transition-progress 0) transition-length)
                                   (update state :transition-progress inc)
                                   (-> state
                                       (assoc :current-scene target-scene)
                                       (assoc :parent-update-fn parent-update-fn)
                                       (assoc :parent-draw-fn parent-draw-fn)
                                       (dissoc :transition-progress)
                                       init-fn))))
      (assoc :parent-draw-fn (fn [{:keys [transition-progress] :as state}]
                               (transition-fn state transition-progress transition-length)))))
