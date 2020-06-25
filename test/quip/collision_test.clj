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

(defn ab-collide-fn-a
  [a]
  (update a :collide-count inc))
(defn ab-collide-fn-b
  [b]
  (update b :collide-count inc))

(defn aa-collide-fn
  [a]
  (update a :collide-count inc))

(deftest standard-collisions-ab-collider
  (let [collider-ab (sut/collider :foo :bar ab-collide-fn-a ab-collide-fn-b
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
  (let [collider-aa (sut/collider :foo :foo aa-collide-fn aa-collide-fn
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
    (let [collider         (sut/collider :foo :non-existent-group ab-collide-fn-a ab-collide-fn-b)
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
    (let [removing-a-collider (sut/collider :foo :bar removing-collide-fn identity
                                            :collision-detection-fn sut/equal-pos?)

         state {:current-scene :test
                :scenes        {:test {:sprites   sprites
                                       :colliders [removing-a-collider]}}}

         results (sut/update-collisions state)
         result-sprites (get-in results [:scenes :test :sprites])]
      (is (= 9 (count result-sprites)))
      (let [foo-sprites (filter #(#{:foo} (:sprite-group %)) result-sprites)
              bar-sprites (filter #(#{:bar} (:sprite-group %)) result-sprites)
              baz-sprites (filter #(#{:baz} (:sprite-group %)) result-sprites)]
          (is (= 2 (count foo-sprites)))
          (is (= 4 (count bar-sprites)))
          (is (= 3 (count baz-sprites)))
          (is (= [0 0] (map :collide-count foo-sprites)))
          (is (= [0 0 0 0] (map :collide-count bar-sprites)))
          (is (= [0 0 0] (map :collide-count baz-sprites))))))

  (testing "sprites in both groups are removed on collision"
    (let [removing-a-collider (sut/collider :foo :bar removing-collide-fn removing-collide-fn
                                            :collision-detection-fn sut/equal-pos?)

         state {:current-scene :test
                :scenes        {:test {:sprites   sprites
                                       :colliders [removing-a-collider]}}}

         results (sut/update-collisions state)
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
          (is (= [0 0 0] (map :collide-count baz-sprites)))))))

(deftest provided-collision-detection-function
  (testing "equal-pos?"
    (let [a {:pos [1 3]}
          b {:pos [1 3]}
          c {:pos [4 4]}
          d {:pos []}]
      (is (true? (sut/equal-pos? a b)))
      (is (false? (sut/equal-pos? a c)))
      (is (false? (sut/equal-pos? b c)))
      (is (not (true? (sut/equal-pos? a d))))))
  
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
        (is (true? (sut/w-h-rects-collide? a b)))
        (is (true? (sut/w-h-rects-collide? a c)))
        (is (true? (sut/w-h-rects-collide? a d)))
        (is (true? (sut/w-h-rects-collide? a e)))

        ;; b collides with no other sprite
        (is (false? (sut/w-h-rects-collide? b c)))
        (is (false? (sut/w-h-rects-collide? b d)))
        (is (false? (sut/w-h-rects-collide? b e)))

        ;; c collides with no other sprite
        (is (false? (sut/w-h-rects-collide? c d)))
        (is (false? (sut/w-h-rects-collide? c e)))

        ;; d collides with no other sprite
        (is (false? (sut/w-h-rects-collide? d e)))))
    
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
        (is (true? (sut/w-h-rects-collide? a b)))
        (is (true? (sut/w-h-rects-collide? a c)))
        (is (true? (sut/w-h-rects-collide? b c)))))
    
    (testing "full overlap"
      ;; ╔══════╗
      ;; ║      ║
      ;; ║ a  b ║
      ;; ║      ║
      ;; ╚══════╝
      (let [a {:pos [1 5] :w 8 :h 5}
            b {:pos [1 5] :w 8 :h 5}]
        (is (true? (sut/w-h-rects-collide? a b)))))))
