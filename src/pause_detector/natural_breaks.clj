(ns pause-detector.natural-breaks)

(def simple-sample [4 5 9 10])

(defn- mean
  [arr]
  (/ (reduce + arr) (count arr)))

(defn- sdam
  [arr]
  (reduce + (map #(Math/pow (- % (mean arr)) 2) arr)))

(defn- sdcm
  [split-arr]
  (reduce + (map sdam split-arr)))

(defn- sdcm-all
  [arr]
  (loop [n       1
         results []]
    (if (< n (count arr))
      (let [s   (split-at n arr)
            sum (sdcm s)]
        (do
          ;;(println  results)
          (recur (inc n) (conj results {:n n :sdcm sum}))))
      results)))

(defn- gvf
  [sdam scdm]
  (/ (- sdam scdm) sdam))

(defn- gvf-all
  [arr]
  (let [sdam     (sdam arr)
        sdcm-all (sdcm-all arr)]
    (map (fn
           [sdcm]
           (merge sdcm {:gvf (gvf sdam (:sdcm sdcm))}))
         sdcm-all)))

(defn classify
  [arr]
  (let [gvf-all (gvf-all arr)
        best    (reduce (fn
                          [x y]
                          (if (> (:gvf x) (:gvf y)) x y)) gvf-all)]
    (split-at (:n best) arr)))
