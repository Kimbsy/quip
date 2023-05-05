(ns quip.utils
  (:require [quil.core :as q]
            [clojure.math.combinatorics :as combo]
            [clojure.set :as s]))

(def black [0])
(def white [255])
(def grey [122])
(def gray grey)
(def red [255 0 0])
(def green [0 255 0])
(def blue [0 0 255])

(defn darken
  [color]
  (map #(max 0 (- % 30)) color))

(defn lighten
  [color]
  (map #(min 255 (+ % 30)) color))

(defn hex->rgb
  [hex-string]
  (->> hex-string
       (partition 2)
       (map (partial apply str "0x"))
       (map read-string)))

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

(defn wrap-trans-rot
  "Perform a translation, a rotation, invoke the supplied
  function (probably drawing a sprite, then reset the transform matrix
  to the identity."
  [[x y] r f]
  (q/push-matrix)
  (q/translate x y)
  (q/rotate (q/radians r))
  (f)
  (q/pop-matrix))

;;; Vector utils

(defn zero-vector?
  "Predicate to check if a vector has length 0."
  [v]
  (every? zero? v))

(defn magnitude
  "Calculate the length of a vector."
  [v]
  (Math/sqrt (reduce + (map #(Math/pow % 2)
                            v))))

(defn unit-vector
  "Calculate the unit vector of a given 2D vector."
  [v]
  (when-not (zero-vector? v)
    (map #(/ % (magnitude v)) v)))

(defn rotate-vector
  "Rotate a vector about the origin by `r` degrees."
  [[x y] r]
  (let [radians (q/radians r)]
    [(- (* x (q/cos radians))
        (* y (q/sin radians)))
     (+ (* x (q/sin radians))
        (* y (q/cos radians)))]))

(defn orthogonals
  "Calculate the two orthogonal vectors to a given 2D vector.

  Y axis is inverted so this returns [90-degrees-right-vector
                                      90-degrees-left-vector]"
  [[x y]]
  [[(- y) x]
   [y (- x)]])

(defn direction-vector
  "Calculate the unit direction vector based on the rotation angle."
  [r]
  [(q/sin (q/radians r))
   (- (q/cos (q/radians r)))])

(defn rotation-angle
  "Calculate the rotation angle of a vector."
  [[x y]]
  (q/degrees (q/atan2 x y)))

;;; Geometric collision predicates

(defn equal-pos?
  "Predicate to check if two positions are equal."
  [pos-a pos-b]
  (and (seq pos-a)
       (seq pos-b)
       (every? true? (map = pos-a pos-b))))

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

(defn pos-in-rect?
  "Predicate to check if a position is inside a rectangle."
  [[ax ay]
   [bx1 by1 bx2 by2]]
  (and (<= bx1 ax bx2)
       (<= by1 ay by2)))

(defn coarse-pos-in-poly?
  "Predicate to determine if a point is possibly inside a polygon.

  Checks if the point is contanied by the minimum rectangle containing
  the polygon. If the point is inside this rectangle we should use
  `fine-pos-in-poly?` to check properly."
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
  (partition 2 1 (take (inc (count poly))
                       (cycle poly))))

(defn lines-intersect?
  "Predicate to determine if two lines intersect.

  We have decided that zero-length lines do not intersect as the
  complexity in determining their intersection is not worth the
  performance hit.

  line a: (x1, y1) -> (x2, y2)
  line b: (x3, y3) -> (x4, y4)

  lines intersect iff:
       0.0 <= numerator-t/denominator-t <= 1.0
  and  0.0 <= numerator-u/denominator-u <= 1.0

  We can just assert that the fraction is bottom-heavy."
  [[[x1 y1 :as p1] [x2 y2 :as p2] :as l1]
   [[x3 y3 :as p3] [x4 y4 :as p4] :as l2]]
  ;; We ignore zero-length lines
  (when-not (or (= p1 p2) (= p3 p4))
    (let [numerator-t (- (* (- x1 x3) (- y3 y4))
                   (* (- y1 y3) (- x3 x4)))
          denominator-t (- (* (- x1 x2) (- y3 y4))
                   (* (- y1 y2) (- x3 x4)))
          numerator-u (- (* (- x2 x1) (- y1 y3))
                   (* (- y2 y1) (- x1 x3)))
          denominator-u (- (* (- x1 x2) (- y3 y4))
                   (* (- y1 y2) (- x3 x4)))]
      (and (or (<= 0 numerator-t denominator-t)
               (<= denominator-t numerator-t 0))
           (or (<= 0 numerator-u denominator-u)
               (<= denominator-u numerator-u 0))))))

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

(defn pos-in-poly?
  "Predicate to check if a pos is inside a polygon.

  The `fine-pos-in-poly?` predicate is expensive so we only do it if
  the cheaper `coarse-pos-in-poly?` says this is a possible
  collision."
  [pos poly]
  (when (and (seq poly)
             (coarse-pos-in-poly? pos poly))
    (fine-pos-in-poly? pos poly)))

(defn poly-w-h
  [poly]
  (let [xs (map first poly)
        ys (map second poly)]
    [(- (max xs) (min xs))
     (- (max ys) (min ys))]))

(defn coarse-polys-collide?
  "Predicate to determine if two polygons possibly collide.

  Checks if the minimum rectangles containing the polygons overlap. If
  they do we should use `fine-polys-collide?` to check properly."
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
   (or (pos-in-poly? (first poly-a) poly-b)
       (pos-in-poly? (first poly-b) poly-a))))

(defn polys-collide?
  "Predicate to check if two polygons overlap.

  The `fine-polys-collide?` predicate is expensive so we only do it if
  the cheaper `coarse-polys-collide?` says this is a possible
  collision."
  [poly-a poly-b]
  (when (coarse-polys-collide? poly-a poly-b)
    (fine-polys-collide? poly-a poly-b)))



;; Box building ascii characters

;; ─ │ ┌ ┐ └ ┘ ┼ ├ ┤ ┴ ┬

;; ═ ║ ╔ ╗ ╚ ╝ ╬ ╠ ╣ ╩ ╦
