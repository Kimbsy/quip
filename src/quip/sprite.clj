(ns quip.sprite
  "Sprites, for drawing and animating game objects.

  The basic sprites provided are useful if simple, feel free to
  implement your own."
  (:require [quil.core :as q]
            [quip.util :as u]))

(defn pos-offsets
 "Determine the x and y offsets for a sprite based on it's `:w`, `:h`
  and `:offsets` configuration.

  Defaults to `[:center :center]`."
  [{[x-off y-off] :offsets
    :keys [w h]}]
  (let [dx (cond
             (= :left x-off) 0
             (= :right x-off) (- w)
             (#{:center :centre} x-off) (- (/ w 2))
             :else (- (/ w 2)))
        dy (cond
             (= :top y-off) 0
             (= :bottom y-off) (- h)
             (#{:center :centre} y-off) (- (/ h 2))
             :else (- (/ h 2)))]
    [dx dy]))

(defn update-pos
  [{:keys [pos vel] :as s}]
  (assoc s :pos (map + pos vel)))

(defn update-frame-delay
  [{:keys [current-animation] :as s}]
  (let [animation   (current-animation (:animations s))
        frame-delay (:frame-delay animation)]
    (update s :delay-count #(mod (inc %) frame-delay))))

(defn update-animation
  [{:keys [current-animation delay-count] :as s}]
  (if (zero? delay-count)
    (let [animation (current-animation (:animations s))
          max-frame (:frames animation)]
      (update s :animation-frame #(mod (inc %) max-frame)))
    s))

(defn update-animated-sprite
  [s]
  (some-> s
          update-frame-delay
          update-animation
          update-pos))

(defn draw-image-sprite!
  [{:keys [pos rotation image] :as sprite}]
  (u/wrap-trans-rot pos rotation
                    #(let [[x y] (pos-offsets sprite)]
                      (q/image image x y))))

(def memo-graphics (memoize (fn [w h] (q/create-graphics w h))))

(defn draw-animated-sprite!
  [{:keys [pos rotation w h spritesheet current-animation animation-frame] :as s}]
  (let [animation (current-animation (:animations s))
        x-offset  (* animation-frame w)
        y-offset  (* (:y-offset animation) h)
        g         (memo-graphics w h)]
    (q/with-graphics g
      (.clear g)
      (q/image spritesheet (- x-offset) (- y-offset)))
    (u/wrap-trans-rot pos rotation
                      #(let [[x y] (pos-offsets s)]
                         (q/image g x y)))))

(defn set-animation
  [s animation]
  (-> s
      (assoc :current-animation animation)
      (assoc :animation-frame 0)))

(defn default-bounding-poly
  "Generates a bounding polygon based off the `w` by `h` rectangle of
  the sprite."
  [{:keys [w h]}]
  [[0 0]
   [w 0]
   [w h]
   [0 h]])

(defn draw-default!
  [{:keys [pos w h] :as s}]
  (q/stroke-weight 2)
  (q/stroke 0 255 0)
  (q/fill 0)
  (let [[x y] (map + pos (pos-offsets s))]
    (q/rect x y w h)
    (q/no-fill)
    (q/line x y (+ x w) (+ y h))
    (q/line (+ x w) y x (+ y h))))

;;; Basic Sprite types

(defn sprite
  "The simplest sensible sprite.

  Contains a position, velocity, dimensions for collision detection,
  can be enriched with any custom fields via the `:extra` kwarg."
  [sprite-group pos &
   {:keys [w
           h
           vel
           update-fn
           draw-fn
           points
           bounds-fn
           offsets
           extra]
    :or   {w         20
           h         20
           vel       [0 0]
           update-fn update-pos
           draw-fn   draw-default!
           offsets   [:center]
           extra     {}}}]
  (merge
   {:sprite-group sprite-group
    :uuid         (java.util.UUID/randomUUID)
    :pos          pos
    :w            w
    :h            h
    :vel          vel
    :animated?    false
    :update-fn    update-fn
    :draw-fn      draw-fn
    :points       points
    :bounds-fn    (or bounds-fn
                      (if (seq points)
                        :points
                        default-bounding-poly))
    :offsets      offsets}
   extra))

(defn image-sprite
  [sprite-group pos w h image-file &
   {:keys [rotation
           vel
           update-fn
           draw-fn
           points
           bounds-fn
           offsets
           extra]
    :or   {rotation  0
           vel       [0 0]
           update-fn update-pos
           draw-fn   draw-image-sprite!
           offsets   [:center]
           extra     {}}}]
  (merge
   (sprite sprite-group pos)
   {:w         w
    :h         h
    :image     (q/load-image image-file)
    :rotation  rotation
    :vel       vel
    :update-fn update-fn
    :draw-fn   draw-fn
    :points    points
    :bounds-fn (or bounds-fn
                   (if (seq points)
                     :points
                     default-bounding-poly))
    :offsets   offsets}
   extra))

(defn animated-sprite
  [sprite-group pos w h spritesheet-file &
   {:keys [rotation
           vel
           update-fn
           draw-fn
           animations
           current-animation
           points
           bounds-fn
           offsets
           extra]
    :or   {rotation          0
           vel               [0 0]
           update-fn         update-animated-sprite
           draw-fn           draw-animated-sprite!
           animations        {:none {:frames      1
                                     :y-offset    0
                                     :frame-delay 100}}
           current-animation :none
           offsets           [:center]
           extra             {}}}]
  (merge
   (sprite sprite-group pos)
   {:w                 w
    :h                 h
    :spritesheet       (q/load-image spritesheet-file)
    :rotation          rotation
    :vel               vel
    :animated?         true
    :update-fn         update-fn
    :draw-fn           draw-fn
    :points            points
    :bounds-fn         (or bounds-fn
                           (if (seq points)
                             :points
                             default-bounding-poly))
    :offsets           offsets
    :animations        animations
    :current-animation current-animation
    :delay-count       0
    :animation-frame   0}
   extra))

(defn draw-text-sprite
  [{:keys [content pos offsets font size color]}]
  (let [[x y] pos]
    (apply q/text-align offsets)
    (q/text-font font)
    (u/fill color)
    (q/text content x y)))

(defn text-sprite
  [content pos &
   {:keys [offsets
           sprite-group
           font
           size
           color
           update-fn
           draw-fn
           extra]
    :or   {offsets      [:center]
           sprite-group :text
           font         u/default-font
           size         u/default-text-size
           color        u/black
           update-fn    identity
           draw-fn      draw-text-sprite
           extra        {}}}]
  (merge
   (sprite sprite-group pos)
   {:content   content
    :offsets   offsets
    :font      (q/create-font font size)
    :color     color
    :update-fn update-fn
    :draw-fn   draw-fn}
   extra))

(defn update-state
  "Update each sprite in the current scene using its `:update-fn`."
  [{:keys [current-scene] :as state}]
  (update-in state [:scenes current-scene :sprites]
             (fn [sprites]
               (map (fn [s]
                      ((:update-fn s) s))
                    sprites))))

(defn draw-scene-sprites!
  "Draw each sprite in the current scene using its `:draw-fn`."
  [{:keys [current-scene] :as state}]
  (let [sprites (get-in state [:scenes current-scene :sprites])]
    (doall
     (map (fn [s]
            ((:draw-fn s) s))
          sprites))))

(defn draw-scene-sprites-by-layers!
  "Draw each sprite in the current scene using its `:draw-fn` in the
  order their `:sprite-group` appears in the `layers` list.

  Any sprites with groups not found in `layers` will be drawn last."
  [{:keys [current-scene] :as state} layers]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        unspecified (filter #(not ((set layers) (:sprite-group %))) sprites)]
    (doall
     (map (fn [group]
            (doall
             (map (fn [s]
                    ((:draw-fn s) s))
                  (filter #(= group (:sprite-group %))
                          sprites))))
          layers))
    (doall
     (map (fn [s]
            ((:draw-fn s) s))
          unspecified))))

(defn update-sprites
  "Update sprites in the current scene with the update function `f`.

  Optionally takes a filtering function `pred`."
  ([state f]
   (update-sprites state (constantly true) f))
  ([{:keys [current-scene] :as state} pred f]
   (update-in state [:scenes current-scene :sprites]
              (fn [sprites]
                (pmap (fn [s]
                        (if (pred s)
                          (f s)
                          s))
                      sprites)))))

(defn has-group
  "Creates a predicate function that filters sprites based on their
  `:sprite-group.`

  Takes either a single `:sprite-group` keyword, or a collection of
  them.

  Commonly used alongside `update-sprites`:

  (sprite/update-sprites
    state
    (sprite/has-group :asteroids)
    sprite-update-fn)

  (sprite/update-sprites
    state
    (sprite/has-group [:asteroids :ships])
    sprite-update-fn)"
  [sprite-group]
  (if (coll? sprite-group)
    (fn [s]
      ((set sprite-group) (:sprite-group s)))
    (fn [s]
      (= sprite-group (:sprite-group s)))))

(defn is-sprite
  [{:keys [uuid]}]
  "Creates a predicate function that filters sprites based on their
  `:uuid`.

  Takes a map with a `:uuid` key (such as a sprite).

  Commonly used alongside `update-sprites`:

  (sprite/update-sprites
    state
    (sprite/is-sprite player)
    sprite-update-fn)"
  (fn [s]
    (= uuid (:uuid s))))
