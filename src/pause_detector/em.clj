(ns pause-detector.em
  (:require
   [clojure.core.async :as async :refer [thread to-chan pipeline put! <! >! <!! >!! timeout chan]]
   [incanter.distributions :as dis]
   [incanter.core :as encore]
   [incanter.charts :as encharts]
   [incanter.stats :as enstats])
  (:import (org.apache.commons.math3.distribution NormalDistribution)))

(def wav1
  "Extracted pauses for wav 1"
  [0.047528344671201816 1.0931519274376416 0.6653968253968259 1.1882086167800452 0.14258503401360656 0.7604535147392291 1.2832653061224484 0.6653968253968259 1.188208616780047 0.04752834467119982 0.14258503401360656 0.04752834467120337 0.7129251700680292 1.5209070294784581 0.19011337868480638 0.7604535147392291 0.8555102040816323 0.6178684807256225 1.3307936507936518 0.6653968253968259 0.7129251700680257 0.04752834467119982 0.14258503401360656 0.04752834467119982 0.7129251700680257 0.9980952380952388 0.2851700680272131 0.19011337868481348 0.9505668934240319 0.6653968253968259 1.0931519274376456 0.6653968253968259 0.6653968253968259 0.7129251700680257 1.2832653061224448 0.2851700680272131 0.14258503401360656 8.174875283446717 0.6653968253968259 1.2832653061224448 0.047528344671206924 0.14258503401360656 0.047528344671206924 0.6653968253968259 0.9980952380952317 0.04752834467119271 0.14258503401360656 0.047528344671206924 0.7129251700680186 1.283265306122459 0.04752834467119271 0.14258503401360656 0.047528344671206924 0.7129251700680186 0.950566893424039 0.2376417233560062 0.2851700680272131 0.9505668934240248 0.047528344671206924 0.14258503401360656 0.047528344671206924 0.6653968253968259 0.5703401360544262 0.04752834467119271 0.14258503401360656 0.04752834467119271 0.6653968253968259 0.8555102040816251 0.7129251700680186 3.2319274376417297 0.6653968253968259 1.235736961451238 0.14258503401360656 0.8079818594104324 1.0456235827664386 0.7129251700680328 1.4258503401360656 0.19011337868481348 0.7604535147392255 0.8079818594104324 0.047528344671206924 0.14258503401360656 0.7604535147392255 0.9980952380952317 0.6653968253968259 1.1406802721088525 0.7604535147392255 1.283265306122459 0.712925170068047 1.3783219954648587 0.6653968253968117 1.0931519274376456 0.2851700680272131 0.1901133786848277 0.9505668934240248 0.7129251700680186 8.317460317460302 0.2851700680272131 0.19011337868479927 0.9505668934240248 0.6653968253968401 0.6653968253968401 0.6653968253968117 1.235736961451238 0.7129251700680186 0.9980952380952317 0.7604535147392255 1.568435374149658 0.2376417233560062 0.2851700680272131 1.1406802721088525 0.7604535147392255 1.3307936507936518 0.14258503401362077 0.712925170068047 1.1406802721088525 0.19011337868479927 0.7604535147392255 0.6178684807256047 0.6653968253968401 1.140680272108824 0.7604535147392255 1.2832653061224448 0.2376417233560062 0.19011337868479927 0.7604535147392255 0.6653968253968117 0.7604535147392255 0.6178684807256332 0.9505668934240532 0.14258503401359235 0.7604535147392255 0.7129251700680186 0.6653968253968401 0.9030385487528463 0.6178684807256047 1.140680272108824 0.6653968253968401 1.0456235827664386 0.7129251700680186 1.0456235827664386 0.23764172335603462 0.19011337868479927 1.2357369614512663 0.6653968253968117 1.188208616780031 0.7604535147392255 1.3307936507936518 0.7604535147392255 0.7604535147392255 0.6653968253968401 0.8555102040816109 0.6653968253967832 1.2357369614512663 0.6653968253968401 1.1406802721088525 0.712925170068047 1.0931519274376456 0.7604535147391971 1.473378684807244 0.712925170068047 0.9505668934240248 0.6653968253967832 0.8079818594104609 0.712925170068047 0.9980952380952886 0.04752834467115008 0.14258503401362077 0.047528344671206924 0.6653968253968401 0.9505668934240248 0.6178684807256332 1.3307936507936233 0.23764172335603462 0.2851700680271847 1.1406802721088525 0.047528344671206924 0.14258503401356393 0.7604535147392539 1.1882086167800594 0.23764172335603462 0.1901133786848277 0.9505668934240248 0.6653968253967832 0.8079818594104609 0.6653968253968401 0.6653968253968401 0.7604535147391971 1.3783219954648303 0.23764172335597777 0.19011337868477085 0.6653968253968401 0.7129251700679902 1.9486621315193133 0.7604535147392539 1.6159637188208649 0.1901133786848277 0.807981859410404 0.8555102040816678 0.712925170068047 1.0456235827664386 0.19011337868477085 0.7604535147392539 1.1882086167800594 0.6653968253967832 0.7604535147392539 0.6653968253968401 1.3307936507936233 0.7129251700679902 0.9980952380952317 0.23764172335603462 0.1901133786848277 0.9980952380952317 0.14258503401362077 0.7129251700679902 0.9505668934240816 0.04752834467115008 0.14258503401362077 0.047528344671206924 0.6653968253968401 1.1882086167800594 0.23764172335597777 0.28517006802724154 1.3783219954648303 0.7129251700679902 1.0456235827664955 0.7604535147392539 1.1406802721088525 0.14258503401362077 0.807981859410404 1.473378684807244 0.23764172335597777 0.1901133786848277 0.4752834467120124 0.14258503401356393 0.8079818594104609 1.2832653061224164 0.712925170068047 1.3307936507936233 0.28517006802724154 0.047528344671206924 0.19011337868477085 0.6178684807256332 0.6653968253968401 1.1882086167800594 0.6653968253968401 1.4258503401360372 0.23764172335597777 0.19011337868477085 1.2357369614512663 0.2851700680271847 0.23764172335603462 1.473378684807244 0.19011337868477085 0.8079818594104609 1.0931519274375887 0.04752834467115008 0.14258503401362077 0.7604535147392539 0.8555102040816678 0.6653968253968401 0.7604535147392539 0.19011337868477085 0.8079818594104609 1.2357369614512095 0.23764172335597777 0.19011337868477085 1.1406802721088525 0.2851700680271847 0.19011337868477085 0.9505668934240248 0.6653968253968401 1.0931519274376456 0.2851700680271847 0.14258503401362077 0.9505668934240248 0.6653968253968401 1.1882086167800026 0.6653968253967832 0.8079818594104609 0.7604535147392539 1.425850340136094 0.14258503401362077 0.7604535147392539 0.8555102040816109 0.2851700680271847 0.047528344671206924 0.19011337868477085 0.8079818594104609 0.712925170068047 0.6178684807256332 0.6653968253968401 13.688163265306116 0.14258503401356393 0.712925170068047 0.7604535147391971 0.14258503401362077 0.7604535147391971 1.1882086167800594 0.047528344671206924 0.14258503401356393 0.047528344671206924 0.712925170068047 1.1882086167800594 0.6653968253967832 1.1882086167800026 0.23764172335603462 0.19011337868477085 1.0456235827664386 0.04752834467126377 0.14258503401367761 0.7604535147391971 1.3783219954648303 0.1901133786848277 0.7604535147391971 0.903038548752761 0.6653968253967832 1.2357369614512663 0.23764172335597777 0.1901133786848277 0.7129251700679333 0.23764172335609146 0.190113378684714 1.3307936507935665 0.712925170068047 1.0456235827664386 0.6653968253968969 1.3783219954648303 0.712925170068047 1.1882086167801162 0.6653968253968969 1.473378684807244 0.7604535147391971 1.9486621315193133 0.7604535147391971 0.9505668934240248 0.28517006802724154 0.1901133786848277 1.1406802721088525 0.04752834467126377 0.14258503401356393 0.7604535147391971 0.9030385487528747 0.712925170068047 1.1406802721088525 0.14258503401367761 0.8079818594104609 0.9505668934241385 0.28517006802724154 0.1901133786848277 0.7604535147391971 0.7604535147391971 1.663492063491958 0.7604535147391971 1.3307936507936802 0.712925170068047 1.473378684807244 0.23764172335609146 0.04752834467115008])

(def wav2
  "Extracted pauses for wav 2"
  [0.6653968253968241 0.6653968253968259 0.6653968253968259 0.6653968253968259 0.7129251700680257 0.7129251700680266 0.712925170068027 0.7129251700680292 0.7604535147392326 0.8079818594104324 0.9505668934240354 1.0931519274376384 1.093151927437642 1.1882086167800452 1.1882086167800452 1.1882086167800452 1.1882086167800452 1.1882086167800452 1.1882086167800454 1.235736961451245 1.3307936507936509 1.4733786848072583 5.1330612244897935])

(def sample-pauses
  "Example small pauses"[0.4 0.5 0.6 0.7 1.1 1.2 1.5 1.9 3.2 8.1 8.3 13.68])

(defn sample-input-range
  "Generates a sample input data set using a range"
  []
  (sort (concat [3.2 8.1 8.3 13.68] (range 0.4 1.9 0.01))))

;; Step 1
;; First, we just guess at the values for the parameters of each group.
(def initial-parameters {:normal {:mean 1.0 :std 1} :abnormal {:mean 7.0 :std 1}})

;; Step 2
;; To improve these guesses, we compute the likelihood of each data point appearing under these guesses for the mean and standard deviation
(defn normal-distribution
  [parameters]
  (dis/normal-distribution (:mean parameters) (:std parameters)))

(defn likelyhood
  [samples parameters]
  (let [normal-dist (normal-distribution (:normal parameters))
        abnormal-dist (normal-distribution (:abnormal parameters))]
    (map (fn [sample] {:sample sample
                       :normal-likelyhood (dis/pdf normal-dist sample)
                       :abnormal-likelyhood (dis/pdf abnormal-dist sample)}) samples)))

;; Step 3
;; Turn the likelyhood values into weights
(defn likelyhood-weights
  [likelyhoods]
  (map (fn [sample]
         (let [likelyhood-total (+ (:normal-likelyhood sample)
                                   (:abnormal-likelyhood sample))
               s (:sample sample)
               nw (/ (:normal-likelyhood sample) likelyhood-total)
               anw (/ (:abnormal-likelyhood sample) likelyhood-total)]
           {:sample s
            :normal-weight nw
            :abnormal-weight anw}))
       likelyhoods))

;; Step 4
(defn estimate-mean
  [data weight-type]
  (/ (reduce #(+ %1 (* (:sample %2) (weight-type %2))) 0.0 data)
     (reduce #(+ %1 (weight-type %2)) 0.0 data)))

(defn- variance
  [data weight-type mean]
  (/ (reduce #(+ %1 (* (weight-type %2)
                       (Math/pow (- (:sample %2) mean) 2))) 0.0 data)
     (reduce #(+ %1 (weight-type %2)) 0.0 data)))

(defn estimate-std
  [data weight-type mean]
  (Math/sqrt (variance data weight-type mean)))


(defn estimate-new-parameters
  [existing-paramters weighted-samples]
  (let [new-normal-mean (estimate-mean weighted-samples :normal-weight)
        new-abnormal-mean (estimate-mean weighted-samples :abnormal-weight)
        new-normal-std (estimate-std weighted-samples :normal-weight new-normal-mean)
        new-abnormal-std (estimate-std weighted-samples :abnormal-weight new-abnormal-mean)]
    {:normal {:mean new-normal-mean :std new-normal-std}
     :abnormal {:mean new-abnormal-mean :std new-abnormal-std}}))

(def convegered-diff 0.0001)

(def myrange (range 0 20 0.01))

(defn has-converged?
  [params previous-params]
  (if (nil? previous-params) false
      (if (and (< convegered-diff
                  (Math/abs (- (:mean (:normal params))
                               (:mean (:normal previous-params)))))
               (< convegered-diff
                  (Math/abs (- (:mean (:abnormal params))
                               (:mean (:abnormal previous-params)))))) false true)))

(defn em
  [initial-parms samples]
  (let [hist (encharts/histogram samples :density true)]
    (encore/view hist)
    (loop [x 0
           params initial-parms
           previous-params nil
           output '()]
      (if (has-converged? params previous-params)
        output
        (let [weights (likelyhood-weights (likelyhood samples params))
              new-params (estimate-new-parameters params weights)]
          (do
            (encharts/add-lines hist myrange (enstats/pdf-normal myrange :mean (:mean (:normal params)) :sd (:std (:normal params))))
            ;;(encharts/add-lines hist myrange (enstats/pdf-normal myrange :mean (:mean (:abnormal params)) :sd (:std (:abnormal params))))
            (clojure.pprint/pprint params)
            ;;(encore/view params)
            ;;(println (:std  (:normal new-params)))
            ;;(println (nth (map #(:normal-weight %) weights) 0))
            ;;(clojure.pprint/pprint (conj weights x))
            (Thread/sleep 1000) ;; Sleep so that we can see the graph update in real time.
            (recur (inc x) new-params params weights)))))))

(defn classify
  [data]
  (map (fn [sample]
         (if (> (:normal-weight sample) (:abnormal-weight sample))
           {:sample (:sample sample) :type :normal}
           {:sample (:sample sample) :type :abnormal}))
       (em initial-parameters data)))

(comment
  ;; Temporary functions for manual testing
  (defn classify-wav1
    []
    (filter #(= :abnormal (:type %)) (classify (sort wav1))))

  (defn classify-wav2
    []
    (filter #(= :abnormal (:type %)) (classify (sort wav2))))

  (defn classify-sample-data
    []
    (map (fn [sample]
           (if (> (:normal-weight sample) (:abnormal-weight sample))
             {:sample (:sample sample) :type :normal}
             {:sample (:sample sample) :type :abnormal}))
         (em initial-parameters (sample-input-range)))))