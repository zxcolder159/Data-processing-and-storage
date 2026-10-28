(ns taskc2.core)

(defn sieve-primes []
  (let [composites (java.util.HashMap.)]
    (letfn [(next-prime [n]
              (if-let [factors (.get composites n)]
                (do
                  (.remove composites n)
                  (doseq [p factors]
                    (let [next (+ n p)]
                      (.merge composites next (list p)
                              (reify java.util.function.BiFunction
                                (apply [_ a b] (concat a b))))))
                  (next-prime (inc n)))
                (do
                  (.put composites (* n n) (list n))
                  n)))]
      (map (fn [_] (next-prime _))
           (reify clojure.lang.IReduceInit
             (reduce [_ f start]
               (loop [i 2 acc start]
                 (let [p (next-prime i)
                       result (f acc p)]
                   (if (reduced? result)
                     @result
                     (recur (inc i) result))))))))))

(def primes
  (let [composites (java.util.HashMap.)]
    (letfn [(step [n]
              (lazy-seq
               (if-let [factors (.get composites n)]
                 (do
                   (.remove composites n)
                   (doseq [p factors]
                     (let [nxt (+ n p)]
                       (.merge composites nxt (list p)
                               (reify java.util.function.BiFunction
                                 (apply [_ a b] (concat a b))))))
                   (step (inc n)))
                 (do
                   (.put composites (* n n) (list n))
                   (cons n (step (inc n)))))))]
      (step 2))))

(defn -main [& _]
  (println (take 20 primes)))
