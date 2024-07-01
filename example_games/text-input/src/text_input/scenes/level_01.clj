(ns text-input.scenes.level-01
  (:require [clojure.string :as str]
            [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

(def light-green [133 255 199])
(def dark-blue [0 43 54])
(def white [245 245 245])
(def orange [255 90 96])

(defn draw-text-input-sprite
  [{:keys [pos w h offsets font size content content-color background-color highlight-color placeholder placeholder-color in-focus?]}]
  (let [[x y] pos]

    ;; background box
    (q/no-stroke)
    (qpu/fill background-color)
    (q/rect x (- y h) w h)

    ;; content or placeholder
    (q/text-font font)
    (if (and placeholder (str/blank? content))
      (do (qpu/fill placeholder-color)
          (q/text placeholder x y))
      (do (qpu/fill content-color)
          (q/text content x y)))

    ;; highlight input box
    (when in-focus?
      (q/no-fill)
      (qpu/stroke highlight-color)
      (q/rect x (- y h) w h))))

(defn text-input
  [pos path &
   {:keys [w
           h
           offsets
           sprite-group
           font
           size
           content
           content-color
           background-color
           highlight-color
           placeholder
           placeholder-color
           in-focus?
           update-fn
           draw-fn]
    :or   {w                 (* qpu/default-text-size 12)
           h                 qpu/default-text-size
           offsets           [:center]
           sprite-group      :input
           font              qpu/default-font
           size              qpu/default-text-size
           content           ""
           content-color     qpu/white
           background-color  qpu/black
           highlight-color   qpu/white
           placeholder       nil
           placeholder-color qpu/grey
           in-focus?         false
           update-fn         identity
           draw-fn           draw-text-input-sprite}}]
  {:sprite-group      sprite-group
   :uuid              (java.util.UUID/randomUUID)
   :pos               pos
   :w                 w
   :h                 h
   :offsets           offsets
   :font              (q/create-font font size)
   :path              path
   :content           content
   :content-color     content-color
   :background-color  background-color
   :highlight-color   highlight-color
   :placeholder       placeholder
   :placeholder-color placeholder-color
   :in-focus?         in-focus?
   :update-fn         update-fn
   :draw-fn           draw-fn})

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(text-input [100 100] :top :content "foo" :in-focus? true)
   (text-input [100 200] :bottom :placeholder "something")])

(defn draw-level-01
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background dark-blue)
  (qpsprite/draw-scene-sprites state))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      qpsprite/update-scene-sprites))

;; @TODO: I don't love the fact that we need to add special mouse and key handlers if we add a text-input sprite o the game. It might be nice if handlers lived on the sprites themselves and the scene used them.

(def ascii? (set "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~ "))

(defn key-pressed
  [state {:keys [raw-key] :as e}]
  (if (ascii? raw-key)
    (-> state
        ;; @TODO: maybe quip should use `keep` instead of `map` when we update sprites so we can easily remove dead ones by setting them `nil`. Is there a `pkeep` analogue? is there a performance overhead?
        (qpsprite/update-sprites-by-pred
         (fn [{:keys [sprite-group in-focus?]}]
           (and (= :input sprite-group)
                in-focus?))
         (fn [s]
           (update s :content str (:raw-key e)))))
    state))

(defn mouse-pressed
  [state e]
  (prn e)

  ;; if we've clicked on an input

  ;; make it in-focus  

  ;; make all other not in-focus

  ;; @TODO: I don't love that someone overriding the :sprite-group of a text-input would stop the unfocusing from working. how else would we know what sprites to process? a :type field?
  
  state)

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01
   :update-fn update-level-01
   :key-pressed-fns [key-pressed]
   :mouse-pressed-fns [mouse-pressed]})
