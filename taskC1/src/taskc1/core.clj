(ns taskc1.core)

(defn no-repeat-strings [alphabet n]
  (reduce
   (fn [acc _]
     (mapcat
      (fn [s]
        (map #(str s %) (remove #(= % (str (last s))) alphabet)))
      acc))
   (map str alphabet)
   (range (dec n))))

(defn -main [& _]
  (let [alphabet ["a" "b" "c"]
        n 2]
    (println (no-repeat-strings alphabet n))))
