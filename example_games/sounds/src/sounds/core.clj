(ns sounds.core
  (:gen-class)
  (:require [quil.core :as q]
            [quip.core :as qp]
            [quip.sound :as qpsound]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

;; Nicer than actual white and black
(def white [245 245 245])
(def black [60 60 60])

(defn draw-level-01
  "Draw the background and then the scene sprites."
  [state]
  (qpu/background black)
  (qpsprite/draw-scene-sprites state))

(defn sprites
  "Define the sprites in the scene."
  []
  [(qpsprite/text-sprite
    "Press <m> to restart music"
    [(* 0.5 (q/width)) (* 0.4 (q/height))]
    :color white)
   (qpsprite/text-sprite
    "Press <s> to play sound effect"
    [(* 0.5 (q/width)) (* 0.6 (q/height))]
    :color white)])

(defn handle-music
  "Handle a key-pressed event `e`.
  When the key was `m` reset the music."
  [state e]
  (if (= :m (:key e))
    (do
      (qpsound/loop-music "music/music.wav")
      state)
    state))

;; Available sound effects
(def blips
  ["sfx/blip-1.wav"
   "sfx/blip-2.wav"
   "sfx/blip-3.wav"])

(defn handle-sfx
  "handle a key-pressed event `e`.
  When the key was `s` play a random sound effect."
  [state e]
  (if (= :s (:key e))
    (do
      (qpsound/play (rand-nth blips))
      state)
    state))

(defn key-pressed-fns
  "Define the key-pressed handler functions for the scene."
  []
  [handle-music
   handle-sfx])

(defn init-scenes
  "Define the scenes in the game."
  []
  {:level-01 {:draw-fn draw-level-01
              :sprites (sprites)
              :key-pressed-fns (key-pressed-fns)}})

(defn setup
  "Initialise the state of the game.
  Starts the music playing."
  []
  (qpsound/loop-music "music/music.wav")
  ;; Empty state
  {})

(def sounds-game
  "Define the game."
  (qp/game {:title "Music and sound effects"
            :init-scenes-fn init-scenes
            :setup setup
            :current-scene :level-01}))

(defn -main
  "Run the game."
  [& args]
  (qp/run sounds-game))
