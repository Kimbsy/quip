(ns quip.collision-test
  (:require [clojure.test :refer :all]
            [quip.collision :as sut]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]))

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
                                  :collision-detection-fn qpu/equal-pos?)]
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
                                  :collision-detection-fn qpu/equal-pos?)]
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
                                            :collision-detection-fn qpu/equal-pos?)

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
                                            :collision-detection-fn qpu/equal-pos?)

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
                                            :collision-detection-fn qpu/equal-pos?)

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

(deftest equal-pos?
  (let [a {:pos [1 3]}
        b {:pos [1 3]}
        c {:pos [4 4]}
        d {:pos []}]
    (is (and (qpu/equal-pos? a b)
             (qpu/equal-pos? b a)))
    (is (and (not (qpu/equal-pos? a c))
             (not (qpu/equal-pos? c a))))
    (is (and (not (qpu/equal-pos? b c))
             (not (qpu/equal-pos? c b))))
    (is (not (qpu/equal-pos? a d)))
    (is (not (qpu/equal-pos? d a)))))

(deftest w-h-rects-collide?
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
    (let [a {:pos [2 2] :w 7 :h 4}
          b {:pos [0 0] :w 4 :h 3}
          c {:pos [7 0] :w 4 :h 3}
          d {:pos [0 5] :w 4 :h 3}
          e {:pos [7 5] :w 4 :h 3}]
      ;; a collides with every other sprite
      (is (and (qpu/w-h-rects-collide? a b)
               (qpu/w-h-rects-collide? b a)))
      (is (and (qpu/w-h-rects-collide? a c)
               (qpu/w-h-rects-collide? c a)))
      (is (and (qpu/w-h-rects-collide? a d)
               (qpu/w-h-rects-collide? d a)))
      (is (and (qpu/w-h-rects-collide? a e)
               (qpu/w-h-rects-collide? e a)))

      ;; b collides with no other sprite
      (is (and (not (qpu/w-h-rects-collide? b c))
               (not (qpu/w-h-rects-collide? c b))))
      (is (and (not (qpu/w-h-rects-collide? b d))
               (not (qpu/w-h-rects-collide? d b))))
      (is (and (not (qpu/w-h-rects-collide? b e))
               (not (qpu/w-h-rects-collide? e b))))

      ;; c collides with no other sprite
      (is (and (not (qpu/w-h-rects-collide? c d))
               (not (qpu/w-h-rects-collide? d c))))
      (is (and (not (qpu/w-h-rects-collide? c e))
               (not (qpu/w-h-rects-collide? e c))))

      ;; d collides with no other sprite
      (is (and (not (qpu/w-h-rects-collide? d e))
               (not (qpu/w-h-rects-collide? e d))))))
  
  (testing "partial overlaps"
    ;; ┌────┬─┬────┐
    ;; │ a  │ │ b  │
    ;; ├────┼─┤    │
    ;; ├────┴─┼────┘
    ;; │ c    │
    ;; └──────┘
    (let [a {:pos [0 0] :w 7 :h 3}
          b {:pos [5 0] :w 7 :h 3}
          c {:pos [0 2] :w 7 :h 3}]
      ;; all sprites collide with each other
      (is (and (qpu/w-h-rects-collide? a b)
               (qpu/w-h-rects-collide? b a)))
      (is (and (qpu/w-h-rects-collide? a c)
               (qpu/w-h-rects-collide? c a)))
      (is (and (qpu/w-h-rects-collide? b c)
               (qpu/w-h-rects-collide? c b)))))
  
  (testing "overlaps exactly"
    ;; ╔══════╗
    ;; ║      ║
    ;; ║ a  b ║
    ;; ║      ║
    ;; ╚══════╝
    (let [a {:pos [0 0] :w 7 :h 4}
          b {:pos [0 0] :w 7 :h 4}]
      (is (and (qpu/w-h-rects-collide? a b)
               (qpu/w-h-rects-collide? b a)))))

  (testing "fully contains"
    ;; ┌────────┐
    ;; │   a    │
    ;; │┌──────┐│
    ;; ││  b   ││
    ;; │└──────┘│
    ;; └────────┘
    (let [a {:pos [0 0] :w 9 :h 5}
          b {:pos [1 2] :w 7 :h 2}]
      (is (and (qpu/w-h-rects-collide? a b)
               (qpu/w-h-rects-collide? b a))))))

(deftest pos-in-rect?
  (testing "pos-in-rect? and rect-contains-pos?"
    ;; ┌────────┐
    ;; │ .b     │
    ;; │   a    │
    ;; │        │ .d
    ;; └───.c───┘
    (let [a {:pos [0 0] :w 9 :h 4}
          b {:pos [2 1]}
          c {:pos [4 4]}
          d {:pos [11 3]}]
      (is (and (qpu/pos-in-rect? b a)
               (qpu/rect-contains-pos? a b)))
      (is (and (qpu/pos-in-rect? c a)
               (qpu/rect-contains-pos? a c)))
      (is (and (not (qpu/pos-in-rect? d a))
               (not (qpu/rect-contains-pos? a d)))))))

(deftest pos-in-poly?
  (testing "pos-in-poly? and poly-contains-pos?"
    (testing "coarse collision detection"
      ;; ┌─────────┐
      ;; │ a      /
      ;; │       /       .d
      ;; │      /
      ;; │ .b  / .c
      ;; └────┘
      (let [a [[0 0] [10 0] [5 5] [0 5]]
            b [2 4]
            c [8 4]
            d [16 2]]
        (is (qpu/coarse-pos-in-poly? b a))
        (is (qpu/coarse-pos-in-poly? c a))
        (is (not (qpu/coarse-pos-in-poly? d a)))))

    (testing "fine collision detection"
      ;; ┌─────────┐
      ;; │ a      /
      ;; │       /       .d
      ;; │      /
      ;; │ .b  / .c
      ;; └────┘
      (let [a [[0 0] [10 0] [5 5] [0 5]]
            b [2 4]
            c [8 4]
            d [16 2]]
        (is (qpu/fine-pos-in-poly? b a))
        (is (not (qpu/fine-pos-in-poly? c a)))
        (is (not (qpu/fine-pos-in-poly? d a)))))

    (testing "works on rectangles"
      ;; ┌────────┐
      ;; │ .b     │
      ;; │   a    │
      ;; │        │ .d
      ;; └───.c───┘
      (let [a {:pos [0 0] :w 9 :h 4 :bounds-fn qpsprite/default-bounding-poly}
            b {:pos [2 1]}
            c {:pos [4 4]}
            d {:pos [11 3]}]
        (is (and (qpu/pos-in-poly? b a)
                 (qpu/poly-contains-pos? a b)))
        (is (and (qpu/pos-in-poly? c a)
                 (qpu/poly-contains-pos? a c)))
        (is (and (not (qpu/pos-in-poly? d a))
                 (not (qpu/poly-contains-pos? a d))))))

    (testing "works on simple polygons"
      ;; ┌─────────┐
      ;; │ a      /
      ;; │       /       .d
      ;; │      /
      ;; │ .b  / .c
      ;; └────┘
      (let [points [[0 0] [10 0] [5 5] [0 5]]
            a      {:pos [0 0] :bounds-fn :points :points points}
            b      {:pos [2 4]}
            c      {:pos [8 4]}
            d      {:pos [16 2]}]
        (is (and (qpu/pos-in-poly? b a)
                 (qpu/poly-contains-pos? a b)))
        (is (and (not (qpu/pos-in-poly? c a))
                 (not (qpu/poly-contains-pos? a c))))
        (is (and (not (qpu/pos-in-poly? d a))
                 (not (qpu/poly-contains-pos? a d))))))

    (testing "works on complex polygons"
      ;; ┌─────────────┐
      ;; │ a     .     │
      ;; │      / \    │ .d
      ;; │     /   \   │
      ;; │ .b /  .c \  │
      ;; └───┘       └─┘
      (let [points [[0 0] [14 0] [14 5] [12 5] [8 1] [4 5] [0 5]]
            a      {:pos [0 0] :bounds-fn :points :points points}
            b      {:pos [2 4]}
            c      {:pos [8 4]}
            d      {:pos [16 2]}]
        (is (and (qpu/pos-in-poly? b a)
                 (qpu/poly-contains-pos? a b)))
        (is (and (not (qpu/pos-in-poly? c a))
                 (not (qpu/poly-contains-pos? a c))))
        (is (and (not (qpu/pos-in-poly? d a))
                 (not (qpu/poly-contains-pos? a d))))))

    (testing "works on very complex polygons"
      ;; ┌─────────────┐
      ;; │    ┌──────┐ │
      ;; │ a   \ .e /  │
      ;; │      \  /   │
      ;; │       \/    │
      ;; │       X     │
      ;; │      / \    │ .d
      ;; │     /   \   │
      ;; │ .b /  .c \  │
      ;; └───┘       └─┘
      (let [points [[0 0] [14 0] [14 9] [12 9] [5 1] [12 1] [4 9] [0 9]]
            a      {:pos [0 0] :bounds-fn :points :points points}
            b      {:pos [2 8]}
            c      {:pos [8 8]}
            d      {:pos [16 6]}
            e      {:pos [8 2]}]
        (is (and (qpu/pos-in-poly? b a)
                 (qpu/poly-contains-pos? a b)))
        (is (and (not (qpu/pos-in-poly? c a))
                 (not (qpu/poly-contains-pos? a c))))
        (is (and (not (qpu/pos-in-poly? d a))
                 (not (qpu/poly-contains-pos? a d))))
        (is (and (not (qpu/pos-in-poly? e a))
                 (not (qpu/poly-contains-pos? a e))))))))

(deftest polys-collide
  (testing "rectangular polygons"
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
      (let [a {:pos [2 2] :w 7 :h 4 :bounds-fn qpsprite/default-bounding-poly}
            b {:pos [0 0] :w 4 :h 3 :bounds-fn qpsprite/default-bounding-poly}
            c {:pos [7 0] :w 4 :h 3 :bounds-fn qpsprite/default-bounding-poly}
            d {:pos [0 5] :w 4 :h 3 :bounds-fn qpsprite/default-bounding-poly}
            e {:pos [7 5] :w 4 :h 3 :bounds-fn qpsprite/default-bounding-poly}]
        ;; a collides with every other sprite
        (is (and (qpu/polys-collide? a b)
                 (qpu/polys-collide? b a)))
        (is (and (qpu/polys-collide? a c)
                 (qpu/polys-collide? c a)))
        (is (and (qpu/polys-collide? a d)
                 (qpu/polys-collide? d a)))
        (is (and (qpu/polys-collide? a e)
                 (qpu/polys-collide? e a)))

        ;; b collides with no other sprite
        (is (and (not (qpu/polys-collide? b c))
                 (not (qpu/polys-collide? c b))))
        (is (and (not (qpu/polys-collide? b d))
                 (not (qpu/polys-collide? d b))))
        (is (and (not (qpu/polys-collide? b e))
                 (not (qpu/polys-collide? e b))))

        ;; c collides with no other sprite
        (is (and (not (qpu/polys-collide? c d))
                 (not (qpu/polys-collide? d c))))
        (is (and (not (qpu/polys-collide? c e))
                 (not (qpu/polys-collide? e c))))

        ;; d collides with no other sprite
        (is (and (not (qpu/polys-collide? d e))
                 (not (qpu/polys-collide? e d))))))
    
    (testing "partial overlaps"
      ;; ┌────┬─┬────┐
      ;; │ a  │ │ b  │
      ;; ├────┼─┤    │
      ;; ├────┴─┼────┘
      ;; │ c    │
      ;; └──────┘
      (let [a {:pos [0 0] :w 7 :h 3 :bounds-fn qpsprite/default-bounding-poly}
            b {:pos [5 0] :w 7 :h 3 :bounds-fn qpsprite/default-bounding-poly}
            c {:pos [0 2] :w 7 :h 3 :bounds-fn qpsprite/default-bounding-poly}]
        ;; all sprites collide with each other
        (is (and (qpu/polys-collide? a b)
                 (qpu/polys-collide? b a)))
        (is (and (qpu/polys-collide? a c)
                 (qpu/polys-collide? c a)))
        (is (and (qpu/polys-collide? b c)
                 (qpu/polys-collide? c b)))))
    
    (testing "overlaps exactly"
      ;; ╔══════╗
      ;; ║      ║
      ;; ║ a  b ║
      ;; ║      ║
      ;; ╚══════╝
      (let [a {:pos [0 0] :w 7 :h 4 :bounds-fn qpsprite/default-bounding-poly}
            b {:pos [0 0] :w 7 :h 4 :bounds-fn qpsprite/default-bounding-poly}]
        (is (and (qpu/polys-collide? a b)
                 (qpu/polys-collide? b a)))))

    (testing "fully contains"
      ;; ┌────────┐
      ;; │   a    │
      ;; │┌──────┐│
      ;; ││  b   ││
      ;; │└──────┘│
      ;; └────────┘
      (let [a {:pos [0 0] :w 9 :h 5 :bounds-fn qpsprite/default-bounding-poly}
            b {:pos [1 2] :w 7 :h 2 :bounds-fn qpsprite/default-bounding-poly}]
        (is (and (qpu/polys-collide? a b)
                 (qpu/polys-collide? b a))))))

  (testing "simple polygons"
    ;; ┌──────┐
    ;;  \  a  │
    ;;   \    │
    ;;    \   │
    ;;     \  │
    ;; ┌────┼─┼───┐
    ;; │     \│   │
    ;; │  b   │   │
    ;; │          │
    ;; └──────────┘
    (let [a-points [[0 0] [7 0] [7 7]]
          b-points [[0 5] [11 5] [11 9] [9 0]]
          a        {:pos [0 0] :points a-points :bounds-fn :points}
          b        {:pos [0 5] :points b-points :bounds-fn :points}]
      (is (and (qpu/polys-collide? a b)
               (qpu/polys-collide? b a)))))
  (testing "irregular polygons"
    ;; ┌──────┐  ┌─────┐
    ;; │      └──┼─┐   │
    ;; │  a      │ │ b │
    ;; │         └─┼─┐ │
    ;; │      ┌────┘ │ │
    ;; └──────┘ ┌────┘ │
    ;;          └──────┘
    (let [a-points [[0 0] [7 0] [7 1] [12 1] [12 4] [7 4] [7 5] [0 5]]
          b-points [[10 0] [16 0] [16 6] [9 6] [9 5] [14 5] [14 3] [10 3]]
          a        {:pos [0 0] :points a-points :bounds-fn :points}
          b        {:pos [10 0] :points b-points :bounds-fn :points}]
      (is (and (qpu/polys-collide? a b)
               (qpu/polys-collide? b a))))))
