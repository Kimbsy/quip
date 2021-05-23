(ns quip.utils-test
  (:require [clojure.test :refer :all]
            [quip.utils :as sut]))

(deftest lines-intersect?
  (testing "simple intersection"
    ;;   |
    ;; --┼--
    ;;   |
    (let [l1 [[0 0] [10 0]]
          l2 [[5 -5] [5 5]]]
      (is (sut/lines-intersect? l1 l2))))

  (testing "direction of line doesn't matter"
    ;;   |
    ;; --┼--
    ;;   |
    (let [l1 [[0 0] [10 0]]
          l2 [[5 -5] [5 5]]
          l3 (reverse l1)
          l4 (reverse l2)]
      (is (sut/lines-intersect? l1 l2))
      (is (sut/lines-intersect? l3 l4))
      (is (sut/lines-intersect? l1 l4))
      (is (sut/lines-intersect? l3 l2))))

  (testing "line intersects with itself"
    ;; ====
    (let [l1 [[0 0] [10 0]]]
      (is (sut/lines-intersect? l1 l1)))
    ;; ║
    ;; ║
    (let [l1 [[0 0] [0 10]]]
      (is (sut/lines-intersect? l1 l1))))

  (testing "intersection with same start point"
    ;; ┌--
    ;; |
    (let [l1 [[0 0] [10 0]]
          l2 [[0 0] [0 10]]]
      (is (sut/lines-intersect? l1 l2))))

  (testing "intersection starting on line"
    ;; --┬--
    ;;   |
    (let [l1 [[0 0] [10 0]]
          l2 [[5 0] [5 10]]]
      (is (sut/lines-intersect? l1 l2)))
    ;; |
    ;; ├--
    ;; |
    (let [l1 [[0 0] [0 10]]
          l2 [[0 5] [10 5]]]
      (is (sut/lines-intersect? l1 l2))))

  (testing "non-intersection for segments when lines would intersect"
    ;; |
    ;; |  -----
    ;; |
    (let [l1 [[0 0] [0 10]]
          l2 [[5 5] [15 5]]]
      (is (not (sut/lines-intersect? l1 l2)))))

  (testing "Zero-length lines do not intersect."
    (testing "segment and 0-length line which would intersect"
      ;; ----  .
      (let [l1 [[0 0] [10 0]]
            l2 [[15 0] [15 0]]]
        (is (not (sut/lines-intersect? l1 l2)))))

    (testing "non-equal 0-length lines"
      ;; .  .
      (let [l1 [[0 0] [0 0]]
            l2 [[10 0] [10 0]]]
        (is (not (sut/lines-intersect? l1 l2)))))))
