(ns pause-detector.file
  (:require
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [environ.core :refer [env]]
   [pause-detector.domain :refer :all])
  (:import (java.io File FileInputStream)
           (javax.sound.sampled AudioFormat AudioInputStream AudioSystem)))

(def input-file-path
  "Environment variable value for the input file path."
  (env :input-path))

(def output-file-path
  "Environment variable value for the output file path."
  (env :output-path))

(defn read-input-csv
  "Opens and parses the given csv file, returning a lazy sequence
  of vectors or strings, where each vector corresponds to a new line."
  [filename]
  (with-open [reader (io/reader filename)]
    (doall
     (csv/read-csv reader :separator \|))))

(def out-header
  [["file" "pause" "start" "end" "duration"]])

(defn format-audio-pause-for-csv
  [audio-pauses]
  (into [] (map (fn [audio-pause]
                  [(:file audio-pause)
                   (:id audio-pause)
                   (:start-time audio-pause)
                   (:end-time audio-pause)
                   (:duration audio-pause)]) audio-pauses)))

(defn write-output-csv!
  "Writes data to the outout file in the csv format."
  [data output]
  (with-open [writer (io/writer output)]
    (doall
     (csv/write-csv writer data :separator \|))))

(defn audio-stream
  "For a given input File and audio format, returns a Audio Input Stream."
  [input audio-format]
  (AudioInputStream. (FileInputStream. input) audio-format (.length input)))

(defn full-scale
  "Determines the largest magnitue possible of the audio format."
  [bits-per-sample]
  (Math/pow 2.0 (- bits-per-sample 1)))

(defn bytes-per-sample
  "Calculates the number of bytes for a sample."
  [bits-per-sample]
  (Math/ceil (/ bits-per-sample 8.0)))

(defn get-file
  "Reads a File and extracts an audio input stream and AudioFile."
  [input]
  (let [input-file (File. input)
        file-name (.getName input-file)
        audio-format (.getFormat (AudioSystem/getAudioFileFormat input-file))
        bits-per-sample (.getSampleSizeInBits audio-format)
        bytes-per-sample (bytes-per-sample bits-per-sample)
        is-little-endian (not (.isBigEndian audio-format))
        audio-is (audio-stream input-file audio-format)
        f-scale (full-scale bits-per-sample)
        frame-rate (.getFrameRate audio-format)
        frame-size (.getFrameSize audio-format)
        encoding (.getEncoding audio-format)]
    {
     :is audio-is
     :file (map->AudioFile {
                            :name file-name
                            :encoding encoding
                            :is-little-endian is-little-endian
                            :f-scale f-scale
                            :frame-rate frame-rate
                            :frame-size frame-size
                            :bits-per-sample bits-per-sample
                            :bytes-per-sample bytes-per-sample})}))
