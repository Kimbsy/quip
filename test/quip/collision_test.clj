(ns quip.collision-test
  (:require [quip.collision :as sut]
            [clojure.test :refer :all]))

(def sprite-group-a [{:sprite-group :a :pos [0 0] :collide-count 0}
                     {:sprite-group :a :pos [0 1] :collide-count 0}
                     {:sprite-group :a :pos [0 1] :collide-count 0}
                     {:sprite-group :a :pos [0 3] :collide-count 0}])
(def sprite-group-b [{:sprite-group :b :pos [0 0] :collide-count 0}
                     {:sprite-group :b :pos [0 2] :collide-count 0}
                     {:sprite-group :b :pos [0 3] :collide-count 0}
                     {:sprite-group :b :pos [0 3] :collide-count 0}])
(def sprite-group-c [{:sprite-group :c :pos [0 0] :collide-count 0}
                     {:sprite-group :c :pos [0 0] :collide-count 0}
                     {:sprite-group :c :pos [0 0] :collide-count 0}])

(def sprites (concat sprite-group-a sprite-group-b sprite-group-c))

(defn ab-collide-fn-a
  [a]
  (update a :collide-count inc))
(defn ab-collide-fn-b
  [b]
  (update b :collide-count inc))

(defn aa-collide-fn-a
  [a]
  (update a :collide-count inc))

(deftest sprite-collision
  (testing "Sprite a collides with sprite b if they are at the same :pos"
    (is (true? (sut/collides? (first sprite-group-a)
                              (first sprite-group-b))))
    (is (false? (sut/collides? (first sprite-group-a)
                               (second sprite-group-a))))))

(deftest standard-collisions-ab-collider
  (let [collider-ab (sut/collider :a :b ab-collide-fn-a ab-collide-fn-b)]
    (testing "colliding a with group-b"
      (let [results (sut/collide-group (first sprite-group-a)
                                       sprite-group-b
                                       collider-ab)]
        (is (= 1 (-> results
                     :a
                     :collide-count)))
        (is (= [1 0 0 0] (->> (:group-b results)
                              (map :collide-count))))
        (is (= 4 (count (:group-b results))))))

    (testing "colliding group-a with group-b"
      (let [results (sut/collide-groups {:a sprite-group-a
                                         :b sprite-group-b}
                                        collider-ab)]
        (is (= 4 (count (:a results))))
        (is (= 4 (count (:b results))))
        (is (= [1 0 0 2] (->> (:a results)
                              (map :collide-count))))
        (is (= [1 0 1 1] (->> (:b results)
                              (map :collide-count))))))

    (testing "applying our collider at the top level"
      (let [state {:current-scene :test
                   :scenes        {:test {:sprites   sprites
                                          :colliders [collider-ab]}}}

            results        (sut/update-collisions state)
            result-sprites (get-in results [:scenes :test :sprites])]
        (is (= 11 (count result-sprites)))
        (let [a-sprites (filter #(#{:a} (:sprite-group %)) result-sprites)
              b-sprites (filter #(#{:b} (:sprite-group %)) result-sprites)
              c-sprites (filter #(#{:c} (:sprite-group %)) result-sprites)]
          (is (= 4 (count a-sprites)))
          (is (= 4 (count b-sprites)))
          (is (= 3 (count c-sprites)))
          (is (= [1 0 0 2] (map :collide-count a-sprites)))
          (is (= [1 0 1 1] (map :collide-count b-sprites)))
          (is (= [0 0 0] (map :collide-count c-sprites))))))))


(deftest self-collisions-aa-collider
  (let [collider-aa (sut/collider :a aa-collide-fn-a)]
    (testing "colliding a with group-a"
      (let [results (sut/collide-group (second sprite-group-a)
                                       sprite-group-a
                                       collider-aa)]        
        (is (= 2 (-> results
                     :a
                     :collide-count)))
        (is (= [0 1 1 0] (->> (:group-b results)
                              (map :collide-count))))
        (is (= 4 (count (:group-b results))))))

    (testing "colliding group-a with group-a"
      (let [results (sut/collide-groups {:a sprite-group-a
                                         :b sprite-group-a}
                                        collider-aa)]
        (is (= 1 (count results)))
        (is (= 4 (count (:a results))))
        (is (= [0 1 1 0] (->> (:a results)
                              (map :collide-count))))))

    (testing "applying our collider at the top level"
      (let [state {:current-scene :test
                   :scenes        {:test {:sprites   sprites
                                          :colliders [collider-aa]}}}

            results        (sut/update-collisions state)
            result-sprites (get-in results [:scenes :test :sprites])]
        (prn "***********************")
        (clojure.pprint/pprint results)
        (is (= 11 (count result-sprites)))
        (let [a-sprites (filter #(#{:a} (:sprite-group %)) result-sprites)
              b-sprites (filter #(#{:b} (:sprite-group %)) result-sprites)
              c-sprites (filter #(#{:c} (:sprite-group %)) result-sprites)]
          (is (= 4 (count a-sprites)))
          (is (= 4 (count b-sprites)))
          (is (= 3 (count c-sprites)))
          (is (= [1 0 0 2] (map :collide-count a-sprites)))
          (is (= [0 0 0 0] (map :collide-count b-sprites)))
          (is (= [0 0 0] (map :collide-count c-sprites))))))
    ))
