(ns quip.sound
  "Music and sound effects.

  There's no support for audio in Quil, so we're relying on interop
  with `javax.sound.sampled`."
  (:require [clojure.java.io :as io])
  (:import (javax.sound.sampled AudioSystem Clip DataLine$Info LineListener)))

(defonce ^:dynamic *music* (atom nil))

(def close-line-on-end-listener
  (reify LineListener
    (update [this line-event]
      (when (= "Stop" (.toString (.getType line-event)))
        (future (.close (.getLine line-event)))))))

(defn get-line
  [line-info mixer-info]
  (or (->> (map (fn [mi]
                  (try (.getLine (AudioSystem/getMixer mi) line-info)
                       (catch Exception e)))
                mixer-info)
           (remove nil?)
           first)
      (AudioSystem/getLine line-info)))

(defn play!
  ([sound]
   (play! sound false))
  ([sound loop?]
   (let [input-stream (io/input-stream (io/resource sound))
         audio-stream (AudioSystem/getAudioInputStream input-stream)
         audio-format (.getFormat audio-stream)
         line-info    (DataLine$Info. Clip audio-format)
         mixer-info   (AudioSystem/getMixerInfo)
         line         (get-line line-info mixer-info)
         audio-clip   (cast Clip line)]
     (.open audio-clip audio-stream)
     (if loop?
       (.loop audio-clip Clip/LOOP_CONTINUOUSLY)
       (.addLineListener line close-line-on-end-listener))
     (.start audio-clip)
     audio-clip)))

(defn stop!
  [clip]
  (.stop clip))

(defn stop-music!
  []
  (when @*music*
    (stop! @*music*)))

(defn loop-music!
  [track]
  (stop-music!)
  (reset! *music* (play! track true)))
