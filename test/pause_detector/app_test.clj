(ns pause-detector.app-test
  (:require [clojure.test :refer :all]
            [pause-detector.spec :refer :all]
            [pause-detector.app :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.test.check.generators :as tgen]
            [clojure.spec.gen.alpha :as gen]))

;; TODO End to end integration test
