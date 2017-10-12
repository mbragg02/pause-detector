(ns pause-detector.pause-detector
  (:require
   [pause-detector.domain :refer :all]
   [pause-detector.audio-decoder :refer :all]
   [clojure.spec.alpha :as s])
  (:import (javax.sound.sampled AudioInputStream)))

(defmulti detect-silence
  "Dispathcing function currently no-op, defaulting to spl implementation"
  (fn [samples] ()))

(def spl-audible-threshold -70)

;; Implementation of a silence detecting algorithm using sound pressure level.
(defmethod detect-silence :default
  [samples]
  (let [power (reduce #(+ %1 (* %2 %2)) (double 0.0) samples)
        value (/ (Math/pow power 0.5) (count samples))
        spl (* 20.0 (Math/log10 value))]
    (if (> spl spl-audible-threshold) true false)))

(defn extract-audio-chunk
  "Extract an AudioChunk from the raw audio bytes."
  [buffer buffer-length audio-file time]
  (let [samples (extract-samples buffer buffer-length audio-file)
        audible (detect-silence samples)]
    (map->AudioChunk {
                      :audible? audible
                      :samples samples
                      :start-time time})))

(defn capture-audible-transition-chunks
  "Detect and record if an AudioChunk is at a pause boundary. i.e a pause  start or a end pause."
  [previous-audio-chunk current-audio-chunk audio-chunks]
  (if (not (nil? previous-audio-chunk))
    (cond
      (and (true? (:audible? previous-audio-chunk))
           (false? (:audible? current-audio-chunk))) ;; start pause
      (conj audio-chunks current-audio-chunk)
      (and (not-empty audio-chunks) ;; so that we always start with a "start pause" event
           (false? (:audible? previous-audio-chunk))
           (true? (:audible? current-audio-chunk)))  ;; end pause
      (conj audio-chunks current-audio-chunk)
      :else audio-chunks)
    audio-chunks))

(defn process-audio-file
  "Process the supplied audio file, returning a vector of AudioChunks ordered in pairs of pause start/ends."
  [input-file]
  (let [bufsize 4192
        buffer (byte-array bufsize)
        ^javax.sound.sampled.AudioInputStream is (:is input-file)
        audio-file (:file input-file)]
    (loop [total-frames-read 0.0
           previous-audio-chunk nil
           audio-chunks []]
      (let [bytes-read (.read is buffer)
            frames-in-buffer (/ bytes-read (:frame-size audio-file))
            current-frames-read (+ frames-in-buffer total-frames-read)
            current-secs-read (/ current-frames-read (:frame-rate audio-file))]
        (cond
          (pos? bytes-read)
          (let [audio-chunk (extract-audio-chunk buffer bytes-read audio-file current-secs-read)
                updated-audio-chunks (capture-audible-transition-chunks
                                      previous-audio-chunk audio-chunk audio-chunks)]
            (recur current-frames-read audio-chunk updated-audio-chunks))
          :else audio-chunks)))))

(defn extract-audio-pauses
  "Transforms the AudioChunk pairs into AudioPauses."
  [audio-chunks file-name]
  (loop [i 0
         pause-id 1
         audio-pauses []]
    (if (< (+ 1 i) (count audio-chunks))
      (let [start-time (:start-time (nth audio-chunks i))
            end-time (:start-time (nth audio-chunks (+ 1 i)))
            audio-pause (map->AudioPause {
                                          :file file-name
                                          :id pause-id
                                          :start-time start-time
                                          :end-time end-time
                                          :duration (- end-time start-time)})]

        (recur (+ 2 i) (inc pause-id) (conj audio-pauses audio-pause)))
      audio-pauses)))
