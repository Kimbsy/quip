(ns quip.sound
  (:require [clojure.java.io :as io])
  (:import javax.sound.sampled.AudioSystem
           javax.sound.sampled.Clip
           javax.sound.sampled.DataLine$Info
           javax.sound.sampled.LineListener))

(defonce ^:dynamic *music* (atom nil))

(def close-line-on-end-listener
  (reify LineListener
    (update [this line-event]
      (when (= "Stop" (.toString (.getType line-event)))
        (future (.close (.getLine line-event)))))))

(defn play
  ([sound]
   (play sound false))
  ([sound loop?]
   (let [input-stream (io/input-stream (io/resource (str "sound/" sound)))
         audio-stream (AudioSystem/getAudioInputStream input-stream)
         audio-format (.getFormat audio-stream)
         audio-info (DataLine$Info. Clip audio-format)
         mixer-info   (AudioSystem/getMixerInfo)
         mixer        (AudioSystem/getMixer (first mixer-info))
         line         (.getLine mixer audio-info)
         audio-clip   (cast Clip line)]
     (.addLineListener line close-line-on-end-listener)
     (.open audio-clip audio-stream)
     (when loop?
       (.loop audio-clip Clip/LOOP_CONTINUOUSLY))
     (.start audio-clip)
     audio-clip)))

(defn stop
  [clip]
  (.stop clip))

(defn stop-music
  []
  (stop @*music*))

(defn loop-music
  [track]
  (when @*music*
    (stop-music))
  (reset! *music* (play track true)))
