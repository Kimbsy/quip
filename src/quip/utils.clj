(ns quip.utils
  (:require [quil.core :as q]))

(def black [0])
(def white [255])
(def grey [122])
(def gray grey)

(defn darken
  [color]
  (map #(max 0 (- % 30)) color))

(defn lighten
  [color]
  (map #(min 255 (+ % 30)) color))

(def title-text-size 120)
(def large-text-size 50)
(def default-text-size 25)
(def small-text-size 15)

(def default-font "Ubuntu Mono")
(def bold-font "Ubuntu Mono Bold")
(def italic-font "Ubuntu Mono Italic")

(def background (partial apply q/background))
(def fill (partial apply q/fill))
(def stroke (partial apply q/stroke))


;;; Geometric collision predicates

(defn equal-pos?
  "Predicate to check if two sprites have the same position."
  [a b]
  (and (seq (:pos a))
       (seq (:pos b))
       (every? true? (map = (:pos a) (:pos b)))))

(defn w-h-rects-collide?
  "Predicate to check if the `w` by `h` rects of two sprites intersect."
  [{[ax1 ay1] :pos
    aw        :w
    ah        :h}
   {[bx1 by1] :pos
    bw        :w
    bh        :h}]
  ;; @TODO: should we be drawing sprites at their center? if so, this
  ;; should take it into account.
  (let [ax2     (+ ax1 aw)
        ay2     (+ ay1 ah)
        bx2     (+ bx1 bw)
        by2     (+ by1 bh)
        x-preds [(<= ax1 bx1 ax2)
                 (<= ax1 bx2 ax2)
                 (<= bx1 ax1 ax2 bx2)]
        y-preds [(<= ay1 by1 ay2)
                 (<= ay1 by2 ay2)
                 (<= by1 ay1 ay2 by2)]]
    (and (some true? x-preds)
         (some true? y-preds))))

(defn pos-in-rect?
  "Predicate to check if the position of sprite `a` is inside the `w` by
  `h` rect of sprite `b`."
  [{[ax ay] :pos}
   {[bx by] :pos
    bw      :w
    bh      :h}]
  (and (<= bx ax (+ bx bw))
       (<= by ay (+ by bh))))

(defn rect-contains-pos?
  "Predicate to check if the position of sprite `b` is inside the `w` by
  `h` rect of sprite `a`."
  [a b]
  (pos-in-rect? b a))

;; @TODO: implement the following:

(defn pos-in-poly?
  "Predicate to check if the position of sprite `a` is inside the
  bounding polygon of sprite `b`."
  [a b]
  (throw (new Exception "Unimplemented collision detection function")))

(defn poly-contains-pos?
  "Predicate to check if the position of sprite `b` is inside the
  bounding polygon of sprite `a`."
  [a b]
  (pos-in-poly? b a))

(defn pos-in-rotating-poly?
  "Predicate to check if the position of sprite `a` is inside the
  bounding polygon of sprite `b`, taking into account the rotation of
  sprite `b`."
  [a b]
  (throw (new Exception "Unimplemented collision detection function")))

(defn rotating-poly-contains-pos?
  "Predicate to check if the position of sprite `b` is inside the
  bounding polygon of sprite `a`, taking into account the rotation of
  sprite `a`."
  [a b]
  (pos-in-rotating-poly? b a))

(defn polys-intersect?
  "Predicate to check if the bounding polys of sprites `a` and `b`
  intersect."
  [a b]
  (throw (new Exception "Unimplemented collision detection function")))

(defn rotating-polys-intersect?
  "Predicate to check if the bounding polys of sprites `a` and `b`
  intersect, taking into account the rotation of both sprites."
  [a b]
  (throw (new Exception "Unimplemented collision detection function")))








;; Box building ascii characters

;; ─ │ ┌ ┐ └ ┘ ┼ ├ ┤ ┴ ┬

;; ═ ║ ╔ ╗ ╚ ╝ ╬ ╠ ╣ ╩ ╦
