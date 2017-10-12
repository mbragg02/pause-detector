(ns pause-detector.audio-decoder-test
  (:require
   [pause-detector.audio-decoder :refer :all]
   [clojure.test :refer :all]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.alpha :as s])
  (:import (javax.sound.sampled AudioFormat$Encoding)))

;; Byte range generator to use in generative tests
(s/def ::byte-range (s/and int? #(s/int-in-range? -128 127 %)))
(gen/sample (s/gen ::byte-range))
(def y (gen/sample (s/gen ::byte-range) 2))

(deftest test-decode-bytes
  (testing "Decode audio byte buffer multi method"
    (is (= 0 (decode-bytes [0 0] 0.0 {:bits-per-sample 16 :is-little-endian true})))
    (is (= 32896 (decode-bytes [-128 -128] 0.0 {:bits-per-sample 16 :is-little-endian true})))
    (is (= 32639 (decode-bytes [127 127] 0.0 {:bits-per-sample 16 :is-little-endian true})))
    (is (= 32640 (decode-bytes [-128 127] 0.0 {:bits-per-sample 16 :is-little-endian true})))
    (is (= 32895 (decode-bytes [127 -128] 0.0 {:bits-per-sample 16 :is-little-endian true})))
    (is (= 32383 (decode-bytes [127 126] 0.0 {:bits-per-sample 16 :is-little-endian true})))
    (nil? (decode-bytes [0 0] 0.0 {:bits-per-sample 16 :is-little-endian false}))))

(deftest test-decode-codec
  (testing "Decode codex multi method"
    (is (= 0 (decode-codec 0 {:encoding AudioFormat$Encoding/PCM_SIGNED :bits-per-sample 16})))
    (is (= -32640  (decode-codec 32896 {:encoding AudioFormat$Encoding/PCM_SIGNED :bits-per-sample 16})))
    (is (= 32639 (decode-codec 32639 {:encoding AudioFormat$Encoding/PCM_SIGNED :bits-per-sample 16})))
    (is (= 32640 (decode-codec 32640 {:encoding AudioFormat$Encoding/PCM_SIGNED :bits-per-sample 16})))
    (is (= -32641 (decode-codec 32895 {:encoding AudioFormat$Encoding/PCM_SIGNED :bits-per-sample 16})))
    (is (= 32383 (decode-codec 32383 {:encoding AudioFormat$Encoding/PCM_SIGNED :bits-per-sample 16})))))

(def max-magnitude-for-16-bit 32768.0)

(def sample-audio-file {:bytes-per-sample 2 :bits-per-sample 16
                        :f-scale max-magnitude-for-16-bit :is-little-endian true
                        :encoding AudioFormat$Encoding/PCM_SIGNED})

(defn- compare-floats
  ;; Compare two floats with consant precision.
  [f1 f2]
  (< (Math/abs(- f1 f2)) 0.00000001))

(deftest test-extract-samples
  (testing "Full sample extraction from raw bytes, 16 bit, 2 bytes/sample"
    (is (compare-floats 0.0 (first (extract-samples [0 0] 2 sample-audio-file))))
    (is (compare-floats -0.99609375 (first (extract-samples [-128 -128] 2 sample-audio-file))))
    (is (compare-floats 0.99606323 (first (extract-samples [127 127] 2 sample-audio-file))))
    (is (compare-floats 0.99609375 (first (extract-samples [-128 127] 2 sample-audio-file))))
    (is (compare-floats -0.99612427 (first (extract-samples [127 -128] 2 sample-audio-file))))
    (is (compare-floats 0.98825073 (first (extract-samples [127 126] 2 sample-audio-file))))))
