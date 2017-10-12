(ns pause-detector.audio-decoder
  (:import (javax.sound.sampled AudioFormat$Encoding)))

(defmulti decode-codec (fn [temp audio-file] (:encoding audio-file)))

(defmethod decode-codec AudioFormat$Encoding/PCM_SIGNED
  [temp audio-file]
  (let [extension-bits (- 64 (:bits-per-sample audio-file))]
    (bit-shift-right
     (bit-shift-left temp extension-bits)
     extension-bits)))

(defmulti decode-bytes (fn [byte-buffer i audio-file] (:bits-per-sample audio-file)))

(defmethod decode-bytes 16
  [byte-buffer i audio-file]
  (if (:is-little-endian audio-file)
    (bit-or
     (bit-and (nth byte-buffer i)
              (long 0xff))
     (bit-shift-left
      (bit-and (nth byte-buffer (+ i 1))
               (long 0xff))
      (long 8)))))

(defn extract-samples
  "Extract samples (as floats) from the raw audio bytes."
  [byte-buffer buffer-length audio-file]
  (loop [i 0.0
         samples []]
    (cond (< i buffer-length)
          (let [temp (decode-bytes byte-buffer i audio-file)
                sample (float (/ (decode-codec temp audio-file) (:f-scale audio-file)))
                current-samples (conj samples sample)]
            (recur (+ i (:bytes-per-sample audio-file)) current-samples))
          :else samples)))
