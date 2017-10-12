(ns pause-detector.pause-detector-test
  (:require [clojure.test :refer :all]
            [pause-detector.spec :refer :all]
            [pause-detector.pause-detector :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen])
  (:import (javax.sound.sampled AudioFormat$Encoding)))

(defn- compare-floats
  ;; Compare two floats with consant precision.
  [f1 f2]
  (< (Math/abs(- f1 f2)) 0.00000001))

(def max-magnitude-for-16-bit 32768.0)

(def sample-audio-file {:bytes-per-sample 2 :bits-per-sample 16
                        :f-scale max-magnitude-for-16-bit :is-little-endian true
                        :encoding AudioFormat$Encoding/PCM_SIGNED})

(deftest test-sound-pressure-level
  (testing "db spl function"
    (is (= true (detect-silence [1.0 1.0 1.0 1.0])))
    (is (= true (detect-silence [-1.0 -1.0 -1.0 -1.0])))))

(deftest test-extract-audio-chunk
  (testing "Function to extract AudioChunk from the raw audio bytes."
    (is (= true (:audible? (extract-audio-chunk [-128 -128] 2 sample-audio-file 1.0))))
    (is (= true (:audible? (extract-audio-chunk [127 127] 2 sample-audio-file 1.0))))
    (is (= true (:audible? (extract-audio-chunk [-128 127] 2 sample-audio-file 1.0))))
    (is (= true (:audible? (extract-audio-chunk [127 -128] 2 sample-audio-file 1.0))))
    (is (= false (:audible? (extract-audio-chunk [0 0] 2 sample-audio-file 1.0))))
    (is (= false (:audible? (extract-audio-chunk [0 1 0 0 0 0 0 0 0 0
                                                  0 0 0 0 0 0 0 0 0 0
                                                  0 0 0 0 0 0 0 0 0 0
                                                  0 0 0 0 0 0 0 0 0 0
                                                  0 0 0 0 0 0 0 0 0 0] 50 sample-audio-file 1.0))))
    (is (= true (:audible? (extract-audio-chunk [5 -5] 2 sample-audio-file 1.0))))
    (is (= true (:audible? (extract-audio-chunk [30 -30] 2 sample-audio-file 1.0))))
    (is (= true (:audible? (extract-audio-chunk [100 -100] 2 sample-audio-file 1.0))))))


(deftest test-capture-audible-transition-chunks
  (testing "Testing pause capturing logic"
    (is (= [] (capture-audible-transition-chunks {:audible? false} {:audible? false} [])))
    (is (= [{:audible? false}] (capture-audible-transition-chunks {:audible? true} {:audible? false} [])))
    (is (= [] (capture-audible-transition-chunks {:audible? true} {:audible? true} [])))
    (is (= [] (capture-audible-transition-chunks {:audible? false} {:audible? true} [])))
    (is (= [{:audible? true} {:audible? false}] (capture-audible-transition-chunks {:audible? true} {:audible? false} [{:audible? true}])))))

(deftest test-extract-audio-pauses
  (testing "Testing the reducing of AudioChunks into AudioPauses"
    (is (.equals [{:file "test-file-name"
                   :id 1
                   :start-time 1
                   :end-time 5
                   :duration 4}] (extract-audio-pauses [{:start-time 1 :audible? false}
                                                        {:start-time 5 :audible? true}] "test-file-name")))))

;; TODO (deftest test-process-file)
