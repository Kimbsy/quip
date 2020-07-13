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

;; Box building ascii characters

;; ─ │ ┌ ┐ └ ┘ ┼ ├ ┤ ┴ ┬

;; ═ ║ ╔ ╗ ╚ ╝ ╬ ╠ ╣ ╩ ╦
