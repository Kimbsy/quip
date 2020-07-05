(ns quip.sound
  (:require [clojure.java.io :as io]))

;;;; Reeeeeeally basic sound stuff using the javazoom Player class
;;;; (only supports .mp3 files).

;;;; It would be great to be able to queue up a list of music tracks to
;;;; play/suffle through.

;;;; It would be great to be able to invoke callbacks when a track
;;;; finishes.

;;;; It would also be great if it didn't crash when you play too many
;;;; sounds too quickly.

;;;; It would be great to support other file formats.

;;;; It'll do for now.

(defonce ^:dynamic *main-music-thread* (atom nil))
(def ^:dynamic *sound-enabled* true)

(defn ->player
  [resource-name]
  (-> resource-name
      io/resource
      io/input-stream
      java.io.BufferedInputStream.
      javazoom.jl.player.Player.))

(defn loop-music
  "Continuously loop a music track using the main music thread.

  Make sure you first call `(stop-music)` or the tracks will play on
  top of each other."
  [music-file]
  (when *sound-enabled*
    (reset! *main-music-thread* (Thread. #(while true (doto (->player music-file)
                                                        (.play)
                                                        (.close)))))
    (.start @*main-music-thread*)))

(defn stop-music
  "Stop the music on the main music thread.

  Pretty sure this will throw if the thread hasn't been started,
  sorry."
  []
  (when *sound-enabled*
    (.stop @*main-music-thread*)))

(defn play-sound
  "Play a sound effect on it's own thread so that we don't kill the
  music when we call `.close`."
  [sound-file]
  (when *sound-enabled*
    (.start (Thread. #(doto (->player sound-file)
                        (.play)
                        (.close))))))
