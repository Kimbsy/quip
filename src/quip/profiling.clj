(ns quip.profiling)

(def frame-group-size 10)

(defn profiling-info
  [frame-times]
  (when (seq frame-times)
    (let [average-frame-group   (/ (reduce + frame-times)
                                   (count frame-times))
          average-frame-time-ms (float (/ average-frame-group frame-group-size))
          average-fps           (/ 1000 average-frame-time-ms)]
      {:average-frame-time-ms average-frame-time-ms
       :average-fps           average-fps})))

(defn output-profiling-info
  [{:keys [out-file frame-times] :as state}]
  (prn "======== OUTPUTTING PROFILING INFO ========")
  (prn out-file)
  (prn (profiling-info frame-times))
  (spit out-file (profiling-info frame-times)))

(defn record-fps
  [{:keys [last-frame] :as state}]
  (let [now (System/currentTimeMillis)]
    (if last-frame
      (-> state
          (update :frame-times #(conj % (- now last-frame)))
          (assoc :last-frame now))
      (assoc state :last-frame now))))

(defn profiling-update-wrapper
  [{:keys [parent-update-fn global-frame] :as state}]
  (parent-update-fn (if (zero? (mod global-frame frame-group-size))
                      (record-fps state)
                      state)))

(def profiling-opts
  {:update     profiling-update-wrapper
   :on-close   output-profiling-info
   :frame-rate 99999})

(def profiling-initial-state
  {:frame-times []
   :out-file    (str "/tmp/quip-profiling_" (System/currentTimeMillis) ".out")})
