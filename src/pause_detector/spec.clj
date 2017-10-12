(ns pause-detector.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]
            [pause-detector.file :as file]))

;; TODO define specs for Domain records as well as key functions

;; Example function spec for bytes-per-sample-function
(s/fdef ::file/bytes-per-sample
        :args (s/cat :bits-per-sample pos-int?)
        :ret double?)
