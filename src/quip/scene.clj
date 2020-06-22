(ns quip.scene
  (:require [quil.core :as q]))

(defn get-current-scene
  [{:keys [current-scene] :as state}]
  (-> state :scenes current-scene))

(defn fade-to-black
  [state progress max]
  (q/fill 0 (int (* 255 (/ progress max))))
  (q/rect 0 0 (q/width) (q/height)))

(defn transition
  "Temporarily replace `parent-update-fn` and `parent-draw-fn` with
  transition handdling versions which will set themselves back to the
  originals on completion."
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

(defn update-scene-sprites
  [{:keys [sprites] :as scene}]
  (assoc scene :sprites (map (fn [s]
                               ((:update-fn s) s))
                             sprites)))

(defn draw-scene-sprites
  [{:keys [sprites]}]
  (map (fn [s]
         ((:draw-fn s) s))
       sprites))
