(ns taskc3.core-test
  (:require [clojure.test :refer :all]
            [taskc3.core :refer :all]))

(deftest pfilter-test
  (testing "filters finite sequence same as filter"
    (is (= (filter even? (range 100))
           (pfilter even? (range 100)))))

  (testing "filters with custom block size"
    (is (= (filter odd? (range 200))
           (pfilter odd? (range 200) 10))))

  (testing "works on infinite sequences (take first N)"
    (is (= (take 20 (filter even? (range)))
           (take 20 (pfilter even? (range))))))

  (testing "empty sequence returns empty"
    (is (= [] (pfilter even? []))))

  (testing "all-match predicate returns all"
    (is (= (range 50)
           (pfilter number? (range 50)))))

  (testing "no-match predicate returns empty"
    (is (= []
           (pfilter nil? (range 50)))))

  (testing "block size 1 still works"
    (is (= (filter even? (range 30))
           (pfilter even? (range 30) 1))))

  (testing "is lazy - does not realize infinite seq"
    (is (= [0 2 4 6 8]
           (take 5 (pfilter even? (range)))))))

(deftest pfilter-perf-test
  (testing "parallel faster than sequential on cpu-heavy predicate"
    (let [heavy-pred (fn [n]
                       (loop [i 0 x 1.0]
                         (if (< i 5000)
                           (recur (inc i) (Math/sin x))
                           (even? n))))
          data (vec (range 1000))
          t-seq (let [start (System/nanoTime)]
                  (doall (filter heavy-pred data))
                  (- (System/nanoTime) start))
          t-par (let [start (System/nanoTime)]
                  (doall (pfilter heavy-pred data 100))
                  (- (System/nanoTime) start))]
      (println (str "seq: " (/ t-seq 1e6) "ms, par: " (/ t-par 1e6) "ms"))
      (is (< t-par t-seq)))))
