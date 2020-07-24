(ns quip.collision
  (:require [quil.core :as q]
            [quip.utils :as qpu]))

;;; @TODO: Is this too specific? Is collision detection just a
;;; concrete example of a more abstract `interaction` or
;;; `relationship`?
;;;
;;; If we want sprites to do something when they get near each other
;;; or have the same x or y coordinate we can use a collider to model
;;; this by using a custom `collision-detection-fn`, similarly if we
;;; want sprites to inteact with each other when their health is
;;; equal, or their total gold is greater than an amount we can do
;;; this in the same way.
;;;
;;; Should rename, probably wouldn't even need much refactoring.


(defn collider
  "Define a check for collision between to groups of sprites with
  functions to be invoked on the sprites when collision is detected."
  [group-a-key group-b-key collide-fn-a collide-fn-b &
   {:keys [collision-detection-fn]
    :or   {collision-detection-fn qpu/w-h-rects-collide?}}]
  {:group-a-key            group-a-key
   :group-b-key            group-b-key
   :collision-detection-fn collision-detection-fn
   :collide-fn-a           collide-fn-a
   :collide-fn-b           collide-fn-b})

(defn collide-sprites
  "Check two sprites for collision and update them with the appropriate
  `collide-fn-<a|b>` provided by the collider.

  In the case that we're checking a group of sprites for collisions in
  the same group we need to check the uuid on the sprites to ensure
  they're not colliding with themselves."
  [a b {:keys [group-a-key
               group-b-key
               collision-detection-fn
               collide-fn-a
               collide-fn-b]}]
  (let [collision-predicate (if (= group-a-key group-b-key)
                              #(and (not= (:uuid a) (:uuid b))
                                    (collision-detection-fn %1 %2))
                              #(collision-detection-fn %1 %2))]
    (if (and a b (collision-predicate a b))
      {:a (collide-fn-a a)
       :b (collide-fn-b b)}
      {:a a
       :b b})))

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

(defn update-collisions
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
