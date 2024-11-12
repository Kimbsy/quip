(ns sounds.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.sound :as qpsound]
            [quip.utils :as qpu]))

;; Nicer than actual white and black
(def white [245 245 245])
(def black [60 60 60])

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

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(qpsprite/text-sprite
    "Press <m> to restart music"
    [(* 0.5 (q/width)) (* 0.4 (q/height))]
    :color white)
   (qpsprite/text-sprite
    "Press <s> to play sound effect"
    [(* 0.5 (q/width)) (* 0.6 (q/height))]
    :color white)])

(defn draw-level-01
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background black)
  (qpsprite/draw-scene-sprites state))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      qpsprite/update-state))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01
   :update-fn update-level-01
   :key-pressed-fns (key-pressed-fns)})
