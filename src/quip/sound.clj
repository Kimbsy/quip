(ns quip.sound
  (:require [clojure.java.io :as io])
  (:import javax.sound.sampled.AudioSystem
           javax.sound.sampled.Clip
           javax.sound.sampled.DataLine$Info))

(defonce ^:dynamic *music* (atom nil))

(defn play
  ([sound]
   (play sound false))
  ([sound loop?]
   (let [input-stream (io/input-stream (io/resource (str "sound/" sound)))
         audio-stream (AudioSystem/getAudioInputStream input-stream)
         audio-format (.getFormat audio-stream)
         audio-info (DataLine$Info. Clip audio-format)
         audio-clip (cast Clip (AudioSystem/getLine audio-info))]
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
