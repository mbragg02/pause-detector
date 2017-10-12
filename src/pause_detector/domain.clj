(ns pause-detector.domain)

;; A Audio File.
(defrecord AudioFile [name encoding is-little-endian f-scale frame-rate frame-size bits-per-sample bytes-per-sample])

;; A "pause" in a audio file.
(defrecord AudioPause [file id start-time end-time duration])

;; A portion of a larger Audio file, that may or may not be a pause.
(defrecord AudioChunk [audible? start-time samples])
