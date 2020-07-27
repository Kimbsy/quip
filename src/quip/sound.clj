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

;;;; This is not the focus of this project, it'll do for now.

(defn ->player
  [resource-name]
  (-> resource-name
      io/resource
      io/input-stream
      java.io.BufferedInputStream.
      javazoom.jl.player.Player.))

(defonce ^:dynamic *main-music-thread* (atom nil))
(defonce ^:dynamic *main-music-player* (atom nil))

(defn loop-music
  "Continuously loop a music track using the main music thread."
  [music-file]
  (when @*main-music-player*
    (.close @*main-music-player*))
  (reset! *main-music-player* (->player music-file))
  (reset! *main-music-thread* (Thread. #(doto @*main-music-player*
                                          (.play)
                                          (.close))))
  (.start @*main-music-thread*))

(defn stop-music
  "Stop the music on the main music thread."
  []
  (when @*main-music-player*
    (.close @*main-music-player*))
  (when @*main-music-thread*
    (.stop @*main-music-thread*)))

(defn play-sound
  "Play a sound effect on it's own thread so that we don't kill the
  music when we call `.close`."
  [sound-file]
  (.start (Thread. #(doto (->player sound-file)
                      (.play)
                      (.close)))))
