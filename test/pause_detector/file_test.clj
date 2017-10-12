(ns pause-detector.file-test
  (:require [clojure.test :refer :all]
            [pause-detector.spec :refer :all]
            [pause-detector.file :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]))

(deftest test-bytes-per-sample
  (is (= 2.0 (bytes-per-sample 16)))
  (is (= 4.0 (bytes-per-sample 32))))

(stest/check `bytes-per-sample)

(deftest test-full-scale
  (is (= 32768.0 (full-scale 16))))

;; TODO and some intergration IO tests
