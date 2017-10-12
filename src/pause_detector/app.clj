(ns pause-detector.app
  (:require
   [pause-detector.file :as f]
   [pause-detector.pause-detector :refer :all]
   [clojure.core.async :as async :refer [thread to-chan pipeline put! <! >! <!! >!! timeout chan]]
   [clojure.spec.alpha :as s]
   [pause-detector.em :as em]
   [pause-detector.natural-breaks :as nb])
  (:import (javax.sound.sampled AudioFormat AudioInputStream AudioSystem))
  (:gen-class))

(def logger
  "Pass though transducer to print from a channel."
  (map (fn [msg] (println  msg) msg)))

(def parallelism
  "Determines a value for the parallelism to use for conconrrent file processnig."
  (+ (.availableProcessors (Runtime/getRuntime)) 1))

(def process-file
  "Transducer that processes a given audio file"
  (map (fn
         [input]
         (let [audio-file (f/get-file input)
               audio-chunks (process-audio-file audio-file)
               audio-pauses (extract-audio-pauses audio-chunks (:name (:file audio-file)))]
           (f/format-audio-pause-for-csv audio-pauses)))))

(defn processing-pipeline
  "Takes a collection of input files and processes them concurrently, producing an output channel."
  [input-file-paths]
  (let [input-chan (async/to-chan input-file-paths)
        logged-input-chan (chan)
        output-chan (chan 1)]
    (pipeline 1 logged-input-chan logger input-chan)
    (pipeline parallelism output-chan process-file logged-input-chan)
    output-chan))


(defn pause-detector
  "Responsible for prosessing the supplied input file and generating the output file."
  [input output]
  (let [input-files (flatten (f/read-input-csv input))
        audio-pauses-chan (processing-pipeline input-files)
        processed-output (<!! (async/reduce conj [] audio-pauses-chan))]
    (f/write-output-csv! (concat f/out-header
                                 (reduce concat processed-output)) output)))

(defn -main
  [& args]
  (time (pause-detector f/input-file-path f/output-file-path)))


(comment
  ;; Some tempoary methods used for manual testing the classification
  (defn pause-detector-em
    "Responsible for prosessing the supplied input file and generating the output file."
    [input output]
    (let [input-files (flatten (f/read-input-csv input))
          audio-pauses-chan (processing-pipeline input-files)
          processed-output (<!! audio-pauses-chan)]
      (clojure.pprint/pprint (em/classify (sort (filter #(< 0.3 %) (map #(nth % 4) processed-output)))))))


  (defn pause-detector-nb
    "Responsible for prosessing the supplied input file and generating the output file."
    [input output]
    (let [input-files (flatten (f/read-input-csv input))
          audio-pauses-chan (processing-pipeline input-files)
          processed-output (<!! audio-pauses-chan)]
      (clojure.pprint/pprint (nb/classify (sort (filter #(< 0.3 %) (map #(nth % 4) processed-output))))))))
