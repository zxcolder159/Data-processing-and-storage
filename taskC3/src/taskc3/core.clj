(ns taskc3.core)

(def default-block-size 512)

(defn pfilter
  ([pred coll] (pfilter pred coll default-block-size))
  ([pred coll block-size]
   (lazy-seq
    (let [block (take block-size coll)
          remainder (drop block-size coll)]
      (when (seq block)
        (let [fut (future (filter pred block))]
          (concat @fut (pfilter pred remainder block-size))))))))

(defn -main [& _]
  (let [result (take 10 (pfilter even? (range)))]
    (println "First 10 even numbers:" result))
  (let [n 1000000
        data (range n)]
    (println "Sequential time:")
    (time (doall (filter even? data)))
    (println "Parallel time:")
    (time (doall (pfilter even? data)))))
