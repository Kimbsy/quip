(ns quip.collision
  "Group-based sprite collision tools and sprite collision detection
  predicates."
  (:require [quip.sprite :as sprite]
            [quip.util :as u]))

(defn equal-pos?
  "Predicate to check if two sprites have the same position."
  [{pos-a :pos} {pos-b :pos}]
  (u/equal-pos? pos-a pos-b))

(defn w-h-rects-collide?
  "Predicate to check for overlap of the `w` by `h` rects of two sprites
  centered on their positions.

  Accounts for the respective `:offsets` configuration of each sprite."
  [{[ax ay] :pos
    aw      :w
    ah      :h
    :as     a}
   {[bx by] :pos
    bw      :w
    bh      :h
    :as     b}]
  (let [[adx ady] (sprite/pos-offsets a)
        [bdx bdy] (sprite/pos-offsets b)
        ax1 (+ ax adx)
        ay1 (+ ay ady)
        ax2 (+ ax adx aw)
        ay2 (+ ay ady ah)
        bx1 (+ bx bdx)
        by1 (+ by bdy)
        bx2 (+ bx bdx bw)
        by2 (+ by bdy bh)]
    (u/rects-overlap? [ax1 ay1 ax2 ay2]
                      [bx1 by1 bx2 by2])))

(defn pos-in-rect?
  "Predicate to check if the position of sprite `a` is inside the `w` by
  `h` rect of sprite `b` centered on its position.

  Accounts for the respective `:offsets` configuration of each sprite."
  [{pos-a :pos
    :as a}
   {[bx by] :pos
    bw      :w
    bh      :h
    :as     b}]
  (let [a-offsets (sprite/pos-offsets a)
        [bdx bdy] (sprite/pos-offsets b)
        rect-b [(+ bx bdx)
                (+ by bdy)
                (+ bx bdx bw)
                (+ by bdy bh)]]
    (u/pos-in-rect? (map + pos-a a-offsets) rect-b)))

(defn rect-contains-pos?
  "Predicate to check if the position of sprite `b` is inside the `w` by
  `h` rect of sprite `a` centered on its position.

  Accounts for the respective `:offsets` configuration of each sprite."
  [a b]
  (pos-in-rect? b a))

(defn pos-in-poly?
  "Predicate to check if the position of sprite `a` is inside the
  bounding polygon of sprite `b` centered on its position.

  Accounts for the respective `:offsets` configuration of each sprite."
  [{pos-a :pos :as a}
   {bounds-fn :bounds-fn pos-b :pos :as b}]
  (let [bounding-poly (->> (bounds-fn b)
                           (map (fn [p] (map + p pos-b (sprite/pos-offsets b)))))]
    (u/pos-in-poly? pos-a bounding-poly)))

(defn poly-contains-pos?
  "Predicate to check if the position of sprite `b` is inside the
  bounding polygon of sprite `a` centered on its position.

  Accounts for the respective `:offsets` configuration of each sprite."
  [a b]
  (pos-in-poly? b a))

(defn polys-collide?
  "Predicate to check an intersection of the bounding polygons of
  sprites `a` and `b` centered on their positions.

  Accounts for the respective `:offsets` configuration of each sprite."
  [{bounds-fn-a :bounds-fn pos-a :pos :as a}
   {bounds-fn-b :bounds-fn pos-b :pos :as b}]
  (let [poly-a (->> (bounds-fn-a a)
                    (map (fn [p] (map + p pos-a (sprite/pos-offsets a)))))
        poly-b (->> (bounds-fn-b b)
                    (map (fn [p] (map + p pos-b (sprite/pos-offsets b)))))]
    (u/polys-collide? poly-a poly-b)))

(defn pos-in-rotating-poly?
  "Predicate to check if the position of sprite `a` is inside the
  bounding polygon of sprite `b` centered on its position, taking into
  account its rotation.

  Accounts for the respective `:offsets` configuration of each sprite."
  [{pos-a :pos :as a}
   {bounds-fn :bounds-fn pos-b :pos rotation :rotation :as b}]
  (let [bounding-poly (->> (bounds-fn b)
                           (map (fn [p] (map + p (sprite/pos-offsets b))))
                           (map #(u/rotate-vector % rotation))
                           (map (fn [p] (map + p pos-b))))]
    (u/pos-in-poly? pos-a bounding-poly)))

(defn rotating-poly-contains-pos?
  "Predicate to check if the position of sprite `b` is inside the
  bounding polygon of sprite `a` centered on its position, taking into
  account its rotation.

  Accounts for the respective `:offsets` configuration of each sprite."
  [a b]
  (pos-in-rotating-poly? b a))

(defn rotating-polys-collide?
  "Predicate to check for an intersection of the bounding polys of
  sprites `a` and `b` centered on their positions, taking into account
  the rotation of both sprites.

  Accounts for the respective `:offsets` configuration of each sprite."
  [{bounds-fn-a :bounds-fn pos-a :pos rotation-a :rotation wa :w ha :h :as a}
   {bounds-fn-b :bounds-fn pos-b :pos rotation-b :rotation wb :w hb :h :as b}]
  (let [poly-a (->> (bounds-fn-a a)
                    (map (fn [p] (map + p (sprite/pos-offsets a))))
                    (map #(u/rotate-vector % rotation-a))
                    (map (fn [p] (map + p pos-a))))
        poly-b (->> (bounds-fn-b b)
                    (map (fn [p] (map + p (sprite/pos-offsets b))))
                    (map #(u/rotate-vector % rotation-b))
                    (map (fn [p] (map + p pos-b))))]
    (u/polys-collide? poly-a poly-b)))

;;; Applying colliders across sprites in current scene

;;; @TODO: Is this too specific? Is collision detection just a
;;; concrete example of a more abstract `interaction` or
;;; `relationship`?
;;;
;;; If we want sprites to do something when they get near each other
;;; or have the same x or y coordinate we can use a collider to model
;;; this by using a custom `collision-detection-fn`, similarly if we
;;; want sprites to interact with each other when their health is
;;; equal, or their total gold is greater than an amount we can do
;;; this in the same way.
;;;
;;; Could rename, probably wouldn't even need much refactoring.

(defn identity-collide-fn
  "Collide functions should return an optionally modified `a` sprite."
  [a b]
  a)

(defn collider
  "Define a check for collision between to groups of sprites with
  functions to be invoked on the sprites when collision is detected."
  [group-a-key group-b-key collide-fn-a collide-fn-b &
   {:keys [collision-detection-fn
           non-collide-fn-a
           non-collide-fn-b]
    :or   {collision-detection-fn w-h-rects-collide?
           non-collide-fn-a       identity-collide-fn
           non-collide-fn-b       identity-collide-fn}}]
  {:group-a-key            group-a-key
   :group-b-key            group-b-key
   :collision-detection-fn collision-detection-fn
   :collide-fn-a           collide-fn-a
   :collide-fn-b           collide-fn-b
   :non-collide-fn-a       non-collide-fn-a
   :non-collide-fn-b       non-collide-fn-b})

(defn collide-sprites
  "Check two sprites for collision and update them with the appropriate
  `collide-fn-<a|b>` provided by the collider. These functions should
  return an optionally modified version of their first argument, the
  second is passed in only as a reference.

  In the case that we're checking a group of sprites for collisions in
  the same group we need to check the uuid on the sprites to ensure
  they're not colliding with themselves."
  [a b {:keys [group-a-key
               group-b-key
               collision-detection-fn
               collide-fn-a
               collide-fn-b
               non-collide-fn-a
               non-collide-fn-b]}]
  (let [collision-predicate (if (= group-a-key group-b-key)
                              #(and (not= (:uuid a) (:uuid b))
                                    (collision-detection-fn %1 %2))
                              #(collision-detection-fn %1 %2))]
    (if (and a b (collision-predicate a b))
      {:a (collide-fn-a a b)
       :b (collide-fn-b b a)}
      {:a (non-collide-fn-a a b)
       :b (non-collide-fn-b b a)})))

(defn collide-group
  "Check a sprite from one group for collisions with all sprites from
  another group, updating both as necessary.

  Reducing over group-b lets us build up a new version of group-b,
  updating the value of a as we go.

  We filter out any b that returns `nil` after colliding to allow
  collide functions to kill sprites."
  [a group-b collider]
  (reduce (fn [acc b]
            (let [results (collide-sprites (:a acc) b collider)]
              (-> acc
                  (assoc :a (:a results))
                  (update :group-b #(->> (conj % (:b results))
                                         (filter some?)
                                         vec)))))
          {:a       a
           :group-b []}
          group-b))

(defn collide-groups
  "Check a group of sprites for collisions with another group of
  sprites, updating all sprites as necessary.

  We're iterating using a reducing function over the first group, this
  means that each time we check an `a` against `group-b` we get the
  new value for a, and the new values for each sprite in `group-b`.

  We filter out any a that returns `nil` after colliding to allow
  collide functions to kill sprites.

  We build our results map using the threading macro to handle the
  case where `group-a-key` and `group-b-key` are the same."
  [sprite-groups {:keys [group-a-key group-b-key]
                  :as   collider}]
  (let [group-a (filter some? (group-a-key sprite-groups))
        group-b (filter some? (group-b-key sprite-groups))
        results (reduce (fn [acc a]
                          (let [group-result (collide-group a (:group-b acc) collider)]
                            (-> acc
                                (assoc :group-b (:group-b group-result))
                                (update :group-a #(->> (conj % (:a group-result))
                                                       (filter some?)
                                                       vec)))))
                        {:group-a []
                         :group-b group-b}
                        group-a)]
    (-> {}
        (assoc group-b-key (:group-b results))
        (assoc group-a-key (:group-a results)))))

;; @TODO: we could make the `collide-fn-a|b` functions take the whole game state and the sprite (like how we do with `quip.input/on-click`). Would require a bit of refactoring for uncertain benefit.

(defn update-state
  "Update the sprites in the current scene based on the scene colliders."
  [{:keys [current-scene] :as state}]
  (let [sprites               (get-in state [:scenes current-scene :sprites])
        sprite-groups         (group-by :sprite-group sprites)
        colliders             (get-in state [:scenes current-scene :colliders])
        colliding-group-keys  (set (mapcat (juxt :group-a-key :group-b-key)
                                           colliders))
        colliding-groups      (select-keys sprite-groups colliding-group-keys)
        non-colliding-sprites (remove #(colliding-group-keys (:sprite-group %)) sprites)]
    (assoc-in state [:scenes current-scene :sprites]
              (concat non-colliding-sprites
                      (->> colliders
                           (reduce (fn [acc-groups {:keys [group-a-key group-b-key]
                                                    :as   collider}]
                                     (let [results (collide-groups acc-groups collider)]
                                       (-> acc-groups
                                           (assoc group-b-key (group-b-key results))
                                           (assoc group-a-key (group-a-key results)))))
                                   colliding-groups)
                           vals
                           (apply concat))))))
