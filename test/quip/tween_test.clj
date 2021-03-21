(ns quip.tween-test
  (:require [clojure.test :refer :all]
            [quip.tween :as sut]))

(defn n-times
  [n f]
  (->> (repeat f)
       (take n)
       (apply comp)))

(defn equiv
  [& xs]
  (apply = (map double xs)))

(deftest deltas-are-normalized
  (testing "the linear-easing-fn deltas are normalized"
    (let [step-count 4
          deltas (sut/normalized-deltas sut/linear-easing-fn step-count)]
      (is (= step-count (count deltas)))
      (is (equiv 1 (reduce + deltas))))))

(deftest can-add-tweens-to-sprite
  (testing "can add a tween to a sprite with no tweens"
    (let [s  {:foo 10}
          t  (sut/->tween :foo 5)
          s' (sut/add-tween s t)]
      (is (zero? (count (:tweens s))))
      (is (= 1 (count (:tweens s'))))))

  (testing "can add a tween to a sprite with existing tweens"
    (let [t1 (sut/->tween :foo 5)
          t2 (sut/->tween :foo 7)
          s  {:foo 10 :tweens [t1]}
          s' (sut/add-tween s t2)]
      (is (= 1 (count (:tweens s))))
      (is (= 2 (count (:tweens s')))))))

(deftest tweens-are-updated
  (testing "a simple tween has it's progress incremented on update"
    (let [t   (sut/->tween :foo 7)
          t'  (sut/update-tween t)
          t'' (sut/update-tween t')]
      (is (zero? (:progress t)))
      (is (= 1 (:progress t')))
      (is (= 2 (:progress t'')))))

  (testing "after :step-counts updates, the tween is flagged for removal"
    (let [t   (sut/->tween :foo 7
                           :step-count 10)
          t'  ((n-times 9 sut/update-tween) t)
          t'' ((n-times 10 sut/update-tween) t)]
      (is (zero? (:progress t)))
      (is (= 9 (:progress t')))
      (is (not (:completed? t)))
      (is (not (:completed? t')))
      (is (:completed? t''))))

  (testing "sprites have their field updated by tweens"
    (let [t   (sut/->tween :foo 5 :step-count 4)
          s   (sut/add-tween {:foo 1} t)
          s'  ((n-times 3 sut/update-sprite) s)
          s'' ((n-times 4 sut/update-sprite) s)]
      (is (equiv 4.75 (:foo s')))
      (is (equiv 6 (:foo s'')))))

  (testing "a state with a basic tweening sprite is updated appropriately"
    (let [t        (sut/->tween :foo 12)
          s        (sut/add-tween {:foo 10} t)
          state    {:scenes        {:test {:sprites [s]}}
                    :current-scene :test}
          state1   (sut/update-sprite-tweens state)
          state99  ((n-times 99 sut/update-sprite-tweens) state)
          state100 ((n-times 100 sut/update-sprite-tweens) state)
          state200 ((n-times 200 sut/update-sprite-tweens) state)]
      (is (= 1 (count (get-in state [:scenes :test :sprites]))))
      (is (= 1 (count (get-in state1 [:scenes :test :sprites]))))
      (is (= 1 (count (get-in state99 [:scenes :test :sprites]))))
      (is (= 1 (count (get-in state100 [:scenes :test :sprites]))))
      (is (= 1 (count (get-in state200 [:scenes :test :sprites]))))
      (is (equiv 10
                 (-> state (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (+ 10 (* 1 12/100))
                 (-> state1 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (+ 10 (* 99 12/100))
                 (-> state99 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv 22
                 (-> state100 (get-in [:scenes :test :sprites]) first :foo)))
      (is (= state100 state200))))

  (testing "a sprite can have multiple tweens affecting the same field"
    (let [t1       (sut/->tween :foo 20)
          t2       (sut/->tween :foo 15)
          s        (-> {:foo 10}
                       (sut/add-tween t1)
                       (sut/add-tween t2))
          state    {:scenes        {:test {:sprites [s]}}
                    :current-scene :test}
          state1   (sut/update-sprite-tweens state)
          state50  ((n-times 50 sut/update-sprite-tweens) state)
          state99  ((n-times 99 sut/update-sprite-tweens) state)
          state100 ((n-times 100 sut/update-sprite-tweens) state)
          state200 ((n-times 200 sut/update-sprite-tweens) state)]
      (is (equiv 10
                 (-> state (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (+ 10 (* 1 20/100) (* 1 15/100))
                 (-> state1 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (+ 10 (* 50 20/100) (* 50 15/100))
                 (-> state50 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (+ 10 (* 99 20/100) (* 99 15/100))
                 (-> state99 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv 45
                 (-> state100 (get-in [:scenes :test :sprites]) first :foo)))
      (is (= state100 state200)))))

(deftest yoyo
  (testing "a yoyoing tween should inc progress up to step-count and then dec back to zero"
    (let [t   (sut/->tween :foo 5
                           :step-count 5
                           :yoyo? true)
          s   (sut/add-tween {:foo 1} t)
          s4  ((n-times 4 sut/update-sprite) s)
          s5  ((n-times 5 sut/update-sprite) s)
          s6  ((n-times 6 sut/update-sprite) s)
          s10 ((n-times 10 sut/update-sprite) s)]
      (is (equiv 5 (:foo s4)))
      (is (equiv 6 (:foo s5)))
      (is (equiv 5 (:foo s6)))
      (is (equiv 1 (:foo s10)))))

  (testing "yoyoing tweens are removed once complete"
    (let [t        (sut/->tween :foo 37
                                :yoyo? true)
          s        (sut/add-tween {:foo 1} t)
          state    {:scenes        {:test {:sprites [s]}}
                    :current-scene :test}
          state1   (sut/update-sprite-tweens state)
          state99  ((n-times 99 sut/update-sprite-tweens) state)
          state100 ((n-times 100 sut/update-sprite-tweens) state)
          state101 ((n-times 101 sut/update-sprite-tweens) state)
          state199 ((n-times 199 sut/update-sprite-tweens) state)
          state200 ((n-times 200 sut/update-sprite-tweens) state)
          state250 ((n-times 250 sut/update-sprite-tweens) state)]
      (is (= 1 (count (get-in state [:scenes :test :sprites]))))
      (is (= 1 (count (get-in state1 [:scenes :test :sprites]))))
      (is (= 1 (count (get-in state99 [:scenes :test :sprites]))))
      (is (= 1 (count (get-in state100 [:scenes :test :sprites]))))
      (is (= 1 (count (get-in state101 [:scenes :test :sprites]))))
      (is (= 1 (count (get-in state199 [:scenes :test :sprites]))))
      (is (= 1 (count (get-in state200 [:scenes :test :sprites]))))
      (is (= 1 (count (get-in state250 [:scenes :test :sprites]))))
      (is (equiv 1
                 (-> state (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (+ 1 (* 1 37/100))
                 (-> state1 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (+ 1 (* 99 37/100))
                 (-> state99 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv 38
                 (-> state100 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (+ 1 (* 99 37/100))
                 (-> state101 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (+ 1 (* 1 37/100))
                 (-> state199 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv 1
                 (-> state200 (get-in [:scenes :test :sprites]) first :foo)))
      (is (= 1 (-> state199
                   (get-in [:scenes :test :sprites])
                   first
                   :tweens
                   count)))
      (is (zero? (-> state200
                     (get-in [:scenes :test :sprites])
                     first
                     :tweens
                     count)))
      (is (= state200 state250)))))

(deftest repeating
  (testing "a tween can repeat any number of times"
    (let [t        (sut/->tween :foo 300
                                :repeat-times 3
                                :step-count 10)
          s        (sut/add-tween {:foo 1} t)
          state    {:scenes        {:test {:sprites [s]}}
                    :current-scene :test}
          state1   (sut/update-sprite-tweens state)
          state5  ((n-times 5 sut/update-sprite-tweens) state)
          state10 ((n-times 10 sut/update-sprite-tweens) state)
          state15 ((n-times 15 sut/update-sprite-tweens) state)
          state20 ((n-times 20 sut/update-sprite-tweens) state)
          state25 ((n-times 25 sut/update-sprite-tweens) state)
          state30 ((n-times 30 sut/update-sprite-tweens) state)
          state35 ((n-times 35 sut/update-sprite-tweens) state)]
      (is (equiv 1
                 (-> state (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (+ 1 (* 1 300/10))
                 (-> state1 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (+ 1 (* 5 300/10))
                 (-> state5 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (+ 1 (* 10 300/10))
                 (-> state10 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (-> state5 (get-in [:scenes :test :sprites]) first :foo)
                 (-> state15 (get-in [:scenes :test :sprites]) first :foo)
                 (-> state25 (get-in [:scenes :test :sprites]) first :foo)))
      (is (equiv (-> state10 (get-in [:scenes :test :sprites]) first :foo)
                 (-> state20 (get-in [:scenes :test :sprites]) first :foo)
                 (-> state30 (get-in [:scenes :test :sprites]) first :foo)))
      (is (not (equiv (-> state5 (get-in [:scenes :test :sprites]) first :foo)
                      (-> state35 (get-in [:scenes :test :sprites]) first :foo))))))

  (testing "a tween can repeat indefinitely"
    (let [t        (sut/->tween :foo 300
                                :repeat-times ##Inf
                                :step-count 10)
          s        (sut/add-tween {:foo 1} t)
          state    {:scenes        {:test {:sprites [s]}}
                    :current-scene :test}
          state50  ((n-times 50 sut/update-sprite-tweens) state)
          state100 ((n-times 50 sut/update-sprite-tweens) state50)
          state150 ((n-times 50 sut/update-sprite-tweens) state100)
          state200 ((n-times 50 sut/update-sprite-tweens) state150)]
      (is (equiv (-> state50 (get-in [:scenes :test :sprites]) first :foo)
                 (-> state100 (get-in [:scenes :test :sprites]) first :foo)
                 (-> state150 (get-in [:scenes :test :sprites]) first :foo)
                 (-> state200 (get-in [:scenes :test :sprites]) first :foo))))))

(deftest complex-update-fns
  (testing "can tween the x coordinate of a sprite"
    (let [t (sut/->tween :pos 20
                         :yoyo? true
                         :update-fn (fn [[x y] d]
                                      [(+ x d) y])
                         :yoyo-update-fn (fn [[x y] d]
                                           [(- x d) y]))
          s (sut/add-tween {:pos [10 10]} t)
          s1 (sut/update-sprite s)
          s100 ((n-times 100 sut/update-sprite) s)
          s200 ((n-times 200 sut/update-sprite) s)]
      (is (equiv 10.2 (get-in s1 [:pos 0])))
      (is (equiv 10 (get-in s1 [:pos 1])))
      (is (equiv 30 (get-in s100 [:pos 0])))
      (is (equiv 10 (get-in s200 [:pos 0]))))))

(deftest complex-easing-fns
  (testing "sigmoidal easing"
    (dotimes [_ 100]
      (let [n  (inc (rand-int 1000))
            ds (sut/normalized-deltas sut/sigmoidal-easing-fn n)]
        (is (equiv 1 (reduce + ds))))))

  (testing "exponential easing"
    (dotimes [_ 100]
      (let [n  (inc (rand-int 1000))
            ds (sut/normalized-deltas sut/exponential-easing-fn n)]
        (is (equiv 1 (reduce + ds))))))

  (testing "asymptotic easing"
    (dotimes [_ 100]
      (let [n  (inc (rand-int 1000))
            ds (sut/normalized-deltas sut/asymptotic-easing-fn n)]
        (is (equiv 1 (reduce + ds)))))))

(deftest on-x-fns
  (testing "on-yoyo-fn is called"
    (let [t      (sut/->tween :foo 20
                              :yoyo? true
                              :on-yoyo-fn #(assoc % :yoyoed? true)
                              :step-count 5)
          s      (sut/add-tween {:foo 1} t)
          state  {:scenes        {:test {:sprites [s]}}
                  :current-scene :test}
          state4 ((n-times 4 sut/update-sprite-tweens) state)
          state5 ((n-times 5 sut/update-sprite-tweens) state)]
      (is (not (-> state4 (get-in [:scenes :test :sprites]) first :yoyoed?)))
      (is (-> state5 (get-in [:scenes :test :sprites]) first :yoyoed?))))

  (testing "on-repeat-fn is called"
    (let [t       (sut/->tween :foo 20
                               :repeat-times 3
                               :on-repeat-fn #(update % :counter inc)
                               :step-count 5)
          s       (sut/add-tween {:foo 1 :counter 0} t)
          state   {:scenes        {:test {:sprites [s]}}
                   :current-scene :test}
          state3  ((n-times 3 sut/update-sprite-tweens) state)
          state5  ((n-times 5 sut/update-sprite-tweens) state)
          state10 ((n-times 10 sut/update-sprite-tweens) state)
          state15 ((n-times 15 sut/update-sprite-tweens) state)]
      (is (zero? (-> state3 (get-in [:scenes :test :sprites]) first :counter)))
      (is (= 1 (-> state5 (get-in [:scenes :test :sprites]) first :counter)))
      (is (= 2 (-> state10 (get-in [:scenes :test :sprites]) first :counter)))
      (is (= 3 (-> state15 (get-in [:scenes :test :sprites]) first :counter)))))

  (testing "on-complete-fn is called"
    (let [t       (sut/->tween :foo 20
                               :on-complete-fn #(assoc % :completed? true)
                               :step-count 10)
          s       (sut/add-tween {:foo 1} t)
          state   {:scenes        {:test {:sprites [s]}}
                   :current-scene :test}
          state9  ((n-times 9 sut/update-sprite-tweens) state)
          state10 ((n-times 10 sut/update-sprite-tweens) state)]
      (is (not (-> state9 (get-in [:scenes :test :sprites]) first :completed?)))
      (is (-> state10 (get-in [:scenes :test :sprites]) first :completed?))))

  (testing "all on-x-fns are called"
    (let [t       (sut/->tween :foo 20
                               :yoyo? true
                               :on-yoyo-fn #(update % :yoyo-counter inc)
                               :repeat-times 2
                               :on-repeat-fn #(update % :repeat-counter inc)
                               :step-count 5
                               :on-complete-fn #(assoc % :completed? true))
          s       (sut/add-tween {:foo 1 :yoyo-counter 0 :repeat-counter 0} t)
          state   {:scenes        {:test {:sprites [s]}}
                   :current-scene :test}
          state4  ((n-times 4 sut/update-sprite-tweens) state)
          state5  ((n-times 5 sut/update-sprite-tweens) state)
          state14 ((n-times 14 sut/update-sprite-tweens) state)
          state15 ((n-times 15 sut/update-sprite-tweens) state)
          state20 ((n-times 20 sut/update-sprite-tweens) state)]
      (is (= 0 (-> state4 (get-in [:scenes :test :sprites]) first :yoyo-counter)))
      (is (= 1 (-> state5 (get-in [:scenes :test :sprites]) first :yoyo-counter)))
      (is (= 1 (-> state14 (get-in [:scenes :test :sprites]) first :yoyo-counter)))
      (is (= 2 (-> state15 (get-in [:scenes :test :sprites]) first :yoyo-counter)))
      (is (not (-> state15 (get-in [:scenes :test :sprites]) first :completed?)))
      (is (-> state20 (get-in [:scenes :test :sprites]) first :completed?)))))
