(ns input-handling.scenes.level-01
  (:require [quip.sprite :as sprite]
            [quip.util :as u]))

(def light-green [133 255 199])

(defn sprites
  "The initial list of sprites for this scene"
  []
  [])

(defn draw-level-01!
  "Called each frame, draws the current scene to the screen"
  [state]
  (u/background light-green)
  (sprite/draw-scene-sprites! state))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      sprite/update-state))

(defn fg [state] (prn "FOCUS_GAINED") state)
(defn fl [state] (prn "FOCUS_LOST") state)
(defn kp [state e] (prn "KEY_PRESSED" e) state)
(defn kr [state e] (prn "KEY_RELEASED" e) state)
(defn mp [state e] (prn "MOUSE_PRESSED" e) state)
(defn mr [state e] (prn "MOUSE_RELEASED" e) state)
(defn mn [state e] (prn "MOUSE_ENTERED" e) state)
(defn mx [state e] (prn "MOUSE_EXITED" e) state)
(defn mc [state e] (prn "MOUSE_CLICKED" e) state)
(defn mm [state e] (prn "MOUSE_MOVED" e) state)
(defn md [state e] (prn "MOUSE_DRAGGED" e) state)
(defn mw [state e] (prn "MOUSE_WHEEL" e) state)

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01!
   :update-fn update-level-01
   :focus-gained-fns [fg]
   :focus-lost-fns [fl]
   :key-pressed-fns [kp]
   :key-released-fns [kr]
   :mouse-pressed-fns [mp]
   :mouse-released-fns [mr]
   :mouse-entered-fns [mn]
   :mouse-exited-fns [mx]
   :mouse-clicked-fns [mc]
   :mouse-moved-fns [mm]
   :mouse-dragged-fns [md]
   :mouse-wheel-fns [mw]})
