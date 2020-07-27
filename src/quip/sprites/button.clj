(ns quip.sprites.button
  (:require [quil.core :as q]
            [quip.utils :as qpu]))

(defn draw-button-sprite
  [{:keys [content pos w h color font content-color content-pos held?]}]
  (q/no-stroke)
  (q/text-align :center :center)
  (q/text-font font)
  (let [[x y]   pos
        [cx cy] content-pos]
    (if held?
      (do (qpu/fill color)
          (q/rect (+ 2 x) (+ 2 y) w h)
          (qpu/fill content-color)
          (q/text content (+ 2 x cx) (+ 2 y cy)))
      (do (qpu/fill (qpu/darken color))
          (q/rect (+ 2 x) (+ 2 y) w h)
          (qpu/fill color)
          (q/rect x y w h)
          (qpu/fill content-color)
          (q/text content (+ x cx) (+ y cy))))))

(defn button-sprite
  [content pos & {:keys [on-click
                         size
                         color
                         font
                         font-size
                         content-color
                         content-pos
                         held?
                         draw-fn
                         collision-detection-fn]
                  :or   {on-click               identity
                         size                   [200 100]
                         color                  qpu/grey
                         font                   qpu/default-font
                         font-size              qpu/large-text-size
                         content-color          qpu/black
                         content-pos            [50 35]
                         held?                  false
                         draw-fn                draw-button-sprite
                         collision-detection-fn qpu/pos-in-rect?}}]
  (let [[w h] size]
    {:sprite-group           :button
     :uuid                   (java.util.UUID/randomUUID)
     :content                content
     :pos                    pos
     :on-click               on-click
     :w                      w
     :h                      h
     :color                  color
     :font                   (q/create-font font font-size)
     :content-color          content-color
     :content-pos            content-pos
     :held?                  held?
     :draw-fn                draw-fn
     :collision-detection-fn collision-detection-fn}))

(defn update-held
  "Update the `:held?` attribute of a specific button to indicate it is
  being pressed."
  [{:keys [current-scene] :as state} {:keys [uuid] :as b}]
  (let [sprites       (get-in state [:scenes current-scene :sprites])
        other-sprites (remove #(= uuid (:uuid %)) sprites)]
    (assoc-in state [:scenes current-scene :sprites]
              (conj other-sprites (assoc b :held? true)))))


;;; @TODO: is this the best way of doing this? Seems like we're
;;; potentially duplicating some of the effort of our mouse event
;;; handler reduction in input.clj?

;;;  Maybe we could register buttons in the state map in our scene
;;;  init which would construct a mouse event handler for each button
;;;  that could check the button position and invoke it's on-click?

;;; Need to put some hammock time into this.

(defn handle-buttons-pressed
  "Determine if any button sprites have been clicked on. If so invoke
  their `on-click` function and set their `:held?` attribute to
  `true`."
  [{:keys [current-scene] :as state} {ex :x ey :y :as e}]
  (let [sprites (get-in state [:scenes current-scene :sprites])
        buttons (filter #(= :button (:sprite-group %)) sprites)]
    (reduce (fn [acc-state {:keys [collision-detection-fn
                                   on-click]
                            :as   b}]
              (if (collision-detection-fn {:pos [ex ey]} b)
                (-> acc-state
                    (on-click e)
                    (update-held b))
                acc-state))
            state
            buttons)))

(defn handle-buttons-released
  "Set the `:held?` atribute to `false` for all sprites in the :button
  sprite-group."
  [{:keys [current-scene] :as state} _]
  (let [sprites (get-in state [:scenes current-scene :sprites])
        buttons (filter #(= :button (:sprite-group %)) sprites)
        other-sprites (remove #(= :button (:sprite-group %)) sprites)]
    (assoc-in state [:scenes current-scene :sprites]
              (concat other-sprites
                      (map #(assoc % :held? false)
                           buttons)))))
