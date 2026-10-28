(ns taskc2.core-test
  (:require [clojure.test :refer :all]
            [taskc2.core :refer :all]))

(deftest primes-test
  (testing "first 10 primes are correct"
    (is (= [2 3 5 7 11 13 17 19 23 29]
           (take 10 primes))))

  (testing "1000th prime is 7919"
    (is (= 7919 (nth primes 999))))

  (testing "all values in sequence are prime"
    (let [ps (set (take 50 primes))]
      (doseq [n (range 2 100)]
        (let [prime? (every? #(not (zero? (mod n %)))
                             (range 2 n))]
          (is (= prime? (contains? ps n)))))))

  (testing "sequence is infinite and lazy"
    (is (= 104729 (nth primes 9999)))))
