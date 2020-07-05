(ns quip.collision-test
  (:require [quip.collision :as sut]
            [clojure.test :refer :all]))

(defn test-sprite
  [group pos]
  {:sprite-group group :pos pos :collide-count 0 :uuid (java.util.UUID/randomUUID)})

(def sprite-group-foo [(test-sprite :foo [0 0])
                       (test-sprite :foo [0 1])
                       (test-sprite :foo [0 1])
                       (test-sprite :foo [0 3])])
(def sprite-group-bar [(test-sprite :bar [0 0])
                       (test-sprite :bar [0 2])
                       (test-sprite :bar [0 3])
                       (test-sprite :bar [0 3])])
(def sprite-group-baz [(test-sprite :baz [0 0])
                       (test-sprite :baz [0 0])
                       (test-sprite :baz [0 0])])

(def sprites (concat sprite-group-foo sprite-group-bar sprite-group-baz))

(defn increment-collide-count
  [s]
  (update s :collide-count inc))

(deftest standard-collisions-ab-collider
  (let [collider-ab (sut/collider :foo :bar increment-collide-count increment-collide-count
                                  :collision-detection-fn sut/equal-pos?)]
    (testing "colliding a with group-b"
      (let [results (sut/collide-group (first sprite-group-foo)
                                       sprite-group-bar
                                       collider-ab)]
        (is (= 1 (-> results
                     :a
                     :collide-count)))
        (is (= [1 0 0 0] (->> (:group-b results)
                              (map :collide-count))))
        (is (= 4 (count (:group-b results))))))

    (testing "colliding group-a with group-b"
      (let [results (sut/collide-groups {:foo sprite-group-foo
                                         :bar sprite-group-bar}
                                        collider-ab)]
        (is (= 4 (count (:foo results))))
        (is (= 4 (count (:bar results))))
        (is (= [1 0 0 2] (->> (:foo results)
                              (map :collide-count))))
        (is (= [1 0 1 1] (->> (:bar results)
                              (map :collide-count))))))

    (testing "applying our collider at the top level"
      (let [state {:current-scene :test
                   :scenes        {:test {:sprites   sprites
                                          :colliders [collider-ab]}}}

            results        (sut/update-collisions state)
            result-sprites (get-in results [:scenes :test :sprites])]
        (is (= 11 (count result-sprites)))
        (let [foo-sprites (filter #(#{:foo} (:sprite-group %)) result-sprites)
              bar-sprites (filter #(#{:bar} (:sprite-group %)) result-sprites)
              baz-sprites (filter #(#{:baz} (:sprite-group %)) result-sprites)]
          (is (= 4 (count foo-sprites)))
          (is (= 4 (count bar-sprites)))
          (is (= 3 (count baz-sprites)))
          (is (= [1 0 0 2] (map :collide-count foo-sprites)))
          (is (= [1 0 1 1] (map :collide-count bar-sprites)))
          (is (= [0 0 0] (map :collide-count baz-sprites))))))))


(deftest self-collisions-aa-collider
  (let [collider-aa (sut/collider :foo :foo increment-collide-count increment-collide-count
                                  :collision-detection-fn sut/equal-pos?)]
    (testing "colliding a with group-a"
      (let [results (sut/collide-group (second sprite-group-foo)
                                       sprite-group-foo
                                       collider-aa)]        
        (is (= 1 (-> results
                     :a
                     :collide-count)))
        (is (= [0 0 1 0] (->> (:group-b results)
                              (map :collide-count))))
        (is (= 4 (count (:group-b results))))))

    (testing "colliding group-a with group-a"
      (let [results (sut/collide-groups {:foo sprite-group-foo
                                         :bar sprite-group-foo}
                                        collider-aa)]
        (is (= 1 (count results)))
        (is (= 4 (count (:foo results))))
        (is (= [0 1 1 0] (->> (:foo results)
                              (map :collide-count))))))

    (testing "applying our collider at the top level"
      (let [state {:current-scene :test
                   :scenes        {:test {:sprites   sprites
                                          :colliders [collider-aa]}}}

            results        (sut/update-collisions state)
            result-sprites (get-in results [:scenes :test :sprites])]
        (is (= 11 (count result-sprites)))
        (let [foo-sprites (filter #(#{:foo} (:sprite-group %)) result-sprites)
              bar-sprites (filter #(#{:bar} (:sprite-group %)) result-sprites)
              baz-sprites (filter #(#{:baz} (:sprite-group %)) result-sprites)]
          (is (= 4 (count foo-sprites)))
          (is (= 4 (count bar-sprites)))
          (is (= 3 (count baz-sprites)))
          (is (= [0 1 1 0] (map :collide-count foo-sprites)))
          (is (= [0 0 0 0] (map :collide-count bar-sprites)))
          (is (= [0 0 0] (map :collide-count baz-sprites))))))))

(deftest empty-group-collider
  (testing "when group b is empty, group a should be unchanged."
    (let [collider         (sut/collider :foo :non-existent-group increment-collide-count increment-collide-count)
          only-foo-sprites (filter #(#{:foo} (:sprite-group %)) sprites)

          state {:current-scene :test
                 :scenes        {:test {:sprites   only-foo-sprites
                                        :colliders [collider]}}}
          
          results        (sut/update-collisions state)
          result-sprites (get-in results [:scenes :test :sprites])]
      (is (= result-sprites only-foo-sprites)))))

(defn removing-collide-fn
  [x]
  nil)

(deftest removing-sprites-collider
  (testing "sprites in group a are removed on collision"
    (let [removing-a-collider (sut/collider :foo :bar removing-collide-fn increment-collide-count
                                            :collision-detection-fn sut/equal-pos?)

          state {:current-scene :test
                 :scenes        {:test {:sprites   sprites
                                        :colliders [removing-a-collider]}}}

          results        (sut/update-collisions state)
          result-sprites (get-in results [:scenes :test :sprites])]
      (is (= 9 (count result-sprites)))
      (let [foo-sprites (filter #(#{:foo} (:sprite-group %)) result-sprites)
            bar-sprites (filter #(#{:bar} (:sprite-group %)) result-sprites)
            baz-sprites (filter #(#{:baz} (:sprite-group %)) result-sprites)]
        (is (= 2 (count foo-sprites)))
        (is (= 4 (count bar-sprites)))
        (is (= 3 (count baz-sprites)))
        (is (= [0 0] (map :collide-count foo-sprites)))
        (is (= [1 0 1 0] (map :collide-count bar-sprites)))
        (is (= [0 0 0] (map :collide-count baz-sprites))))))

  (testing "sprites in both groups are removed on collision"
    (let [removing-a-collider (sut/collider :foo :bar removing-collide-fn removing-collide-fn
                                            :collision-detection-fn sut/equal-pos?)

          state {:current-scene :test
                 :scenes        {:test {:sprites   sprites
                                        :colliders [removing-a-collider]}}}

          results        (sut/update-collisions state)
          result-sprites (get-in results [:scenes :test :sprites])]
      (is (= 7 (count result-sprites)))
      (let [foo-sprites (filter #(#{:foo} (:sprite-group %)) result-sprites)
            bar-sprites (filter #(#{:bar} (:sprite-group %)) result-sprites)
            baz-sprites (filter #(#{:baz} (:sprite-group %)) result-sprites)]
        (is (= 2 (count foo-sprites)))
        (is (= 2 (count bar-sprites)))
        (is (= 3 (count baz-sprites)))
        (is (= [0 0] (map :collide-count foo-sprites)))
        (is (= [0 0] (map :collide-count bar-sprites)))
        (is (= [0 0 0] (map :collide-count baz-sprites))))))

  (testing "sprite in group a is removed when hitting the first of multiple sprites in b"
    (let [removing-a-collider (sut/collider :foo :baz removing-collide-fn increment-collide-count
                                            :collision-detection-fn sut/equal-pos?)

          state {:current-scene :test
                 :scenes        {:test {:sprites   sprites
                                        :colliders [removing-a-collider]}}}

          results        (sut/update-collisions state)
          result-sprites (get-in results [:scenes :test :sprites])]
      (is (= 10 (count result-sprites)))
      (let [foo-sprites (filter #(#{:foo} (:sprite-group %)) result-sprites)
            bar-sprites (filter #(#{:bar} (:sprite-group %)) result-sprites)
            baz-sprites (filter #(#{:baz} (:sprite-group %)) result-sprites)]
        (is (= 3 (count foo-sprites)))
        (is (= 4 (count bar-sprites)))
        (is (= 3 (count baz-sprites)))
        (is (= [0 0 0] (map :collide-count foo-sprites)))
        (is (= [0 0 0 0] (map :collide-count bar-sprites)))
        (is (= [1 0 0] (map :collide-count baz-sprites)))))))

(deftest provided-collision-detection-function
  (testing "equal-pos?"
    (let [a {:pos [1 3]}
          b {:pos [1 3]}
          c {:pos [4 4]}
          d {:pos []}]
      (is (and (sut/equal-pos? a b)
               (sut/equal-pos? b a)))
      (is (false? (and (sut/equal-pos? a c)
                       (sut/equal-pos? c a))))
      (is (false? (and (sut/equal-pos? b c)
                       (sut/equal-pos? c b))))
      (is (not (true? (sut/equal-pos? a d))))
      (is (not (true? (sut/equal-pos? d a))))))
  
  (testing "w-h-rects-collide?"
    (testing "intersections"
      ;; ┌───┐  ┌───┐
      ;; │ b │  │ c │
      ;; │ ┌─┼──┼─┐ │
      ;; └─┼─┘  └─┼─┘
      ;;   │  a   │
      ;; ┌─┼─┐  ┌─┼─┐
      ;; │ └─┼──┼─┘ │
      ;; │ d │  │ e │
      ;; └───┘  └───┘
      (let [a {:pos [3 7] :w 7 :h 5}
            b {:pos [1 9] :w 5 :h 4}
            c {:pos [8 9] :w 5 :h 4}
            d {:pos [1 4] :w 5 :h 4}
            e {:pos [8 4] :w 5 :h 4}]
        ;; a collides with every other sprite
        (is (and (sut/w-h-rects-collide? a b)
                 (sut/w-h-rects-collide? b a)))
        (is (and (sut/w-h-rects-collide? a c)
                 (sut/w-h-rects-collide? c a)))
        (is (and (sut/w-h-rects-collide? a d)
                 (sut/w-h-rects-collide? d a)))
        (is (and (sut/w-h-rects-collide? a e)
                 (sut/w-h-rects-collide? e a)))

        ;; b collides with no other sprite
        (is (false? (and (sut/w-h-rects-collide? b c)
                         (sut/w-h-rects-collide? c b))))
        (is (false? (and (sut/w-h-rects-collide? b d)
                         (sut/w-h-rects-collide? d b))))
        (is (false? (and (sut/w-h-rects-collide? b e)
                         (sut/w-h-rects-collide? e b))))

        ;; c collides with no other sprite
        (is (false? (and (sut/w-h-rects-collide? c d)
                         (sut/w-h-rects-collide? d c))))
        (is (false? (and (sut/w-h-rects-collide? c e)
                         (sut/w-h-rects-collide? e c))))

        ;; d collides with no other sprite
        (is (false? (and (sut/w-h-rects-collide? d e)
                         (sut/w-h-rects-collide? e d))))))
    
    (testing "partial overlaps"
      ;; ┌────┬─┬────┐
      ;; │ a  │ │ b  │
      ;; ├────┼─┤    │
      ;; ├────┴─┼────┘
      ;; │ c    │
      ;; └──────┘
      (let [a {:pos [1 6] :w 8 :h 4}
            b {:pos [6 6] :w 8 :h 4}
            c {:pos [1 4] :w 8 :h 4}]
        ;; all sprites collide with each other
        (is (and (sut/w-h-rects-collide? a b)
                 (sut/w-h-rects-collide? b a)))
        (is (and (sut/w-h-rects-collide? a c)
                 (sut/w-h-rects-collide? c a)))
        (is (and (sut/w-h-rects-collide? b c)
                 (sut/w-h-rects-collide? c b)))))
    
    (testing "overlaps exactly"
      ;; ╔══════╗
      ;; ║      ║
      ;; ║ a  b ║
      ;; ║      ║
      ;; ╚══════╝
      (let [a {:pos [1 5] :w 8 :h 5}
            b {:pos [1 5] :w 8 :h 5}]
        (is (and (sut/w-h-rects-collide? a b)
                 (sut/w-h-rects-collide? b a)))))

    (testing "fully contains"
      ;; ┌────────┐
      ;; │   a    │
      ;; │┌──────┐│
      ;; ││  b   ││
      ;; │└──────┘│
      ;; └────────┘
      (let [a {:pos [1 6] :w 10 :h 6}
            b {:pos [2 4] :w 8 :h 3}]
        (is (and (sut/w-h-rects-collide? a b)
                 (sut/w-h-rects-collide? b a)))))))
