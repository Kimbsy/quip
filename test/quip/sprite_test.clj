(ns quip.sprite-test
  (:require [quip.sprite :as sut]
            [clojure.test :refer :all]))

(deftest sprite-position-offset-test
  (let [s {:w 40 :h 60}]
    (testing "Default behaviour is to offset a sprite to it's center based on `:w` and `:h`"
      (is (= [-20 -30] (sut/pos-offsets s))))

    (testing "Can specify x offset"
      (is (= [0 -30] (sut/pos-offsets (assoc s :offsets [:left]))))
      (is (= [-20 -30] (sut/pos-offsets (assoc s :offsets [:center]))))
      (is (= [-40 -30] (sut/pos-offsets (assoc s :offsets [:right])))))

    (testing "Can specify both x and y offsets"
      (is (= [0 0] (sut/pos-offsets (assoc s :offsets [:left :top]))))
      (is (= [0 -30] (sut/pos-offsets (assoc s :offsets [:left :center]))))
      (is (= [0 -60] (sut/pos-offsets (assoc s :offsets [:left :bottom]))))
      (is (= [-20 0] (sut/pos-offsets (assoc s :offsets [:center :top]))))
      (is (= [-20 -30] (sut/pos-offsets (assoc s :offsets [:center :center]))))
      (is (= [-20 -60] (sut/pos-offsets (assoc s :offsets [:center :bottom]))))
      (is (= [-40 0] (sut/pos-offsets (assoc s :offsets [:right :top]))))
      (is (= [-40 -30] (sut/pos-offsets (assoc s :offsets [:right :center]))))
      (is (= [-40 -60] (sut/pos-offsets (assoc s :offsets [:right :bottom])))))

    (testing "Can use alternate spelling of center"
      (is (= (sut/pos-offsets (assoc s :offsets [:centre]))
             (sut/pos-offsets (assoc s :offsets [:center]))))
      (is (= (sut/pos-offsets (assoc s :offsets [:centre :centre]))
             (sut/pos-offsets (assoc s :offsets [:center :center])))))))
