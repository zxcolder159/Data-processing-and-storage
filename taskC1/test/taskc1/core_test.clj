(ns taskc1.core-test
  (:require [clojure.test :refer :all]
            [taskc1.core :refer :all]))

(deftest no-repeat-strings-test
  (testing "length 1 returns alphabet itself"
    (is (= (sort ["a" "b" "c"])
           (sort (no-repeat-strings ["a" "b" "c"] 1)))))

  (testing "length 2 no adjacent duplicates"
    (let [result (no-repeat-strings ["a" "b" "c"] 2)]
      (is (= 6 (count result)))
      (is (= (sort ["ab" "ac" "ba" "bc" "ca" "cb"])
             (sort result)))))

  (testing "no string has two equal adjacent chars"
    (doseq [s (no-repeat-strings ["a" "b" "c" "d"] 4)]
      (is (not-any? (fn [[a b]] (= a b))
                    (partition 2 1 s)))))

  (testing "count for alphabet size k and length n is k*(k-1)^(n-1)"
    (is (= 24
           (count (no-repeat-strings ["a" "b" "c"] 4))))))
