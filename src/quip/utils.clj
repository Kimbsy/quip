(ns quip.utils
  (:require [quil.core :as q]
            [clojure.math.combinatorics :as combo]
            [clojure.set :as s]))

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

;; @TODO: should we be drawing sprites at their center? if so, this
;; should take it into account.

(defn rects-overlap?
  "Predicate to determine if two rectangles overlap."
  [[ax1 ay1 ax2 ay2]
   [bx1 by1 bx2 by2]]
  (let [x-preds [(<= ax1 bx1 ax2)
                 (<= ax1 bx2 ax2)
                 (<= bx1 ax1 ax2 bx2)]
        y-preds [(<= ay1 by1 ay2)
                 (<= ay1 by2 ay2)
                 (<= by1 ay1 ay2 by2)]]
    (and (some true? x-preds)
         (some true? y-preds))))

(defn w-h-rects-collide?
  "Predicate to check if the `w` by `h` rects of two sprites overlap."
  [{[ax1 ay1] :pos
    aw        :w
    ah        :h}
   {[bx1 by1] :pos
    bw        :w
    bh        :h}]
  (let [ax2     (+ ax1 aw)
        ay2     (+ ay1 ah)
        bx2     (+ bx1 bw)
        by2     (+ by1 bh)]
    (rects-overlap? [ax1 ay1 ax2 ay2]
                    [bx1 by1 bx2 by2])))

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

(defn coarse-pos-in-poly?
  "Predicate to determine if a point is possibly inside a polygon.

  Checks if the point is contanied by the minimum rectangle containing
  the polygon. If the point is inside this rectangle we should use
  `fine-poly-encloses` to check properly."
  [[x y] poly]
  (let [xs (map first poly)
        ys (map second poly)]
    (and (<= (apply min xs) x (apply max xs))
         (<= (apply min ys) y (apply max ys)))))

(defn pos->ray
  "Creates an arbitrarily long line starting at the specified pos.

  When doing poly->point collision detection a point lying on a
  horizontal edge of a poly would cause a division by zero if we used
  a horizontal ray.

  This would be handled, but would not count as a collision so we
  increment y to make it much less likely that the intersecting lines
  are parallel."
  [[x y]]
  [[x y] [(+ x 100000) (+ y 1)]])

(defn poly-lines
  "Construct the lines that make up a polygon from its points."
  [poly]
  (partition 2 1 (conj poly
                       (first poly))))

(defn lines-intersect?
  "Predicate to determine if two lines intersect.

  line a: (x1, y1) -> (x2, y2)
  line b: (x3, y3) -> (x4, y4)"
  [[[x1 y1] [x2 y2]] [[x3 y3] [x4 y4]]]

  ;; Division by zero protection, if this should have been an
  ;; intersection we'll likely get it on the next frame.
  (let [denom-a (- (* (- y4 y3) (- x2 x1))
                   (* (- x4 x3) (- y2 y1)))

        denom-b (- (* (- y4 y3) (- x2 x1))
                   (* (- x4 x3) (- y2 y1)))]
    (when-not (or (zero? denom-a)
                  (zero? denom-b))
      (let [intersection-a (/ (- (* (- x4 x3) (- y1 y3))
                                 (* (- y4 y3) (- x1 x3)))
                              denom-a)
            intersection-b (/ (- (* (- x2 x1) (- y1 y3))
                                 (* (- y2 y1) (- x1 x3)))
                              denom-b)]
        (and (<= 0 intersection-a)
             (< intersection-a 1)
             (<= 0 intersection-b)
             (< intersection-b 1))))))

(defn fine-pos-in-poly?
  "Uses ray casting to check if a polygon encloses a pos.

  We construct a line starting at our point and count how many of the
  polygon lines it intersects, an odd number of intersections means
  the point is inside the polygon.

  Our line should be infinite, but in practice any large number will
  suffice."
  [pos poly]
  (let [ray (pos->ray pos)]
    (->> (poly-lines poly)
         (filter #(lines-intersect? % ray))
         count
         odd?)))

(defn pos-in-poly?*
  "The `fine-pos-in-poly?` predicate is expensive so we only do it if
  the cheaper `coarse-pos-in-poly?` says this is a possible
  collision."
  [pos poly]
  (when (and (seq poly)
             (coarse-pos-in-poly? pos poly))
    (fine-pos-in-poly? pos poly)))

(defn pos-in-poly?
  "Predicate to check if the position of sprite `a` is inside the
  bounding polygon of sprite `b`."
  [{:keys [pos] :as a} {:keys [bounds-fn] :as b}]
  (let [bounding-poly (bounds-fn b)]
    (pos-in-poly?* pos bounding-poly)))

(defn poly-contains-pos?
  "Predicate to check if the position of sprite `b` is inside the
  bounding polygon of sprite `a`."
  [a b]
  (pos-in-poly? b a))

(defn poly-w-h
  [poly]
  (let [xs (map first poly)
        ys (map second poly)]
    [(- (max xs) (min xs))
     (- (max ys) (min ys))]))

(defn coarse-polys-collide?
  [poly-a poly-b]
  (let [a-xs (map first poly-a)
        a-ys (map second poly-a)
        b-xs (map first poly-b)
        b-ys (map second poly-b)]
    (rects-overlap? [(apply min a-xs) (apply min a-ys) (apply max a-xs) (apply max a-ys)]
                    [(apply min b-xs) (apply min b-ys) (apply max b-xs) (apply max b-ys)])))

(defn fine-polys-collide?
  "Predicate to determine if two polygons overlap.

  We first check if there are any points shared by the polygons, then
  we check if any of the lines intersect.

  If no lines intersect it is still possible that one polygon is fully
  containing the other. In this case one polygon will contain all the
  points of the other. So we can just check if the first point of
  poly-a is contained in poly-b or vice versa."
  [poly-a poly-b]
  (or
   ;; any identical points
   (seq (s/intersection (set poly-a) (set poly-b)))
   ;; any intersecting lines
   (some (partial apply lines-intersect?)
         (combo/cartesian-product (poly-lines poly-a)
                                  (poly-lines poly-b)))
   ;; either fully contains the other
   (or (pos-in-poly?* (first poly-a) poly-b)
       (pos-in-poly?* (first poly-b) poly-a))))

(defn polys-collide?
  "Predicate to check if the bounding polygons of sprites `a` and `b`
  overlap.
  
  The `fine-polys-collide?` predicate is expensive so we only do it if
  the cheaper `coarse-polys-collide?` says this is a possible
  collision."
  [{bounds-fn-a :bounds-fn :as a} {bounds-fn-b :bounds-fn :as b}]
  (let [poly-a (bounds-fn-a a)
        poly-b (bounds-fn-b b)]
    (when (coarse-polys-collide? poly-a poly-b)
      (fine-polys-collide? poly-a poly-b))))

;; @TODO: implement these:

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

(defn rotating-polys-collide?
  "Predicate to check if the bounding polys of sprites `a` and `b`
  intersect, taking into account the rotation of both sprites."
  [a b]
  (throw (new Exception "Unimplemented collision detection function")))








;; Box building ascii characters

;; ─ │ ┌ ┐ └ ┘ ┼ ├ ┤ ┴ ┬

;; ═ ║ ╔ ╗ ╚ ╝ ╬ ╠ ╣ ╩ ╦
