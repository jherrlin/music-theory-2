(ns se.jherrlin.music-theory.music-theory
  (:require
   [se.jherrlin.music-theory.instruments :as instruments]
   [se.jherrlin.music-theory.definitions :as definitions]
   [se.jherrlin.music-theory.harmonizations :as harmonizations]
   [se.jherrlin.music-theory.utils :as utils]
   [clojure.set :as set]))


(def instruments (instruments/instruments))

(comment
  instruments
  )

(def get-instrument instruments/instrument)

(comment
  (get-instrument :five-string-banjo)
  )

(def by-id definitions/by-id)

(def get-chord definitions/chord)
(def chords (definitions/chords))
(def get-scale definitions/scale)
(def scales (definitions/scales))

(comment
  (get-chord :major)
  )

(def chord-patterns-belonging-to definitions/chord-patterns-belonging-to)
(def chord-pattern-triads-belonging-to definitions/chord-pattern-triads-belonging-to)

(comment
  (chord-patterns-belonging-to :major :guitar)
  (chord-pattern-triads-belonging-to :major :guitar)
  )

(def scale-patterns-for-scale-and-instrument definitions/scale-patterns-for-scale-and-instrument)

(comment
  (scale-patterns-for-scale-and-instrument :major :guitar)
  )

(comment
  (by-id #uuid "1cd72972-ca33-4962-871c-1551b7ea5244")
  )

(def fretboard-strings utils/fretboard-strings)

(comment
  (fretboard-strings
   [{:tone :g, :octave 4, :start-index 5}
    {:tone :d, :octave 3, :start-index 0}
    {:tone :g, :octave 3, :start-index 0}
    {:tone :b, :octave 3, :start-index 0}
    {:tone :d, :octave 4, :start-index 0}]
   10)
  )

(def tones-starting-at utils/tones-starting-at)

(comment
  (tones-starting-at :c)
  )

(def add-basics-to-fretboard-matrix utils/add-basics-to-fretboard-matrix)
(def with-all-intervals utils/with-all-intervals)
(def with-all-tones utils/with-all-tones)
(def interval-tones utils/interval-tones)
(defn intervals->tones [interval-tones intervals]
  (mapv vector interval-tones intervals))
(def trim-matrix utils/trim-matrix)
(def pattern-with-intervals utils/pattern-with-intervals)
(def pattern-with-tones utils/pattern-with-tones)

(interval-tones ["1" "b3" "5"] :c)

(defn scales-to-chord [scales chord-intervals]
  (->> scales
       (map (juxt :scale/scale-names identity))
       (into {})
       (vals)
       (filter
        (fn [{scale-intervals :scale/intervals}]
          (set/subset? (set chord-intervals) (set scale-intervals))))))

(defn chords-to-scale [chords scale-intervals]
  (->> chords
       (filter
        (fn [{chord-intervals :chord/intervals}]
          (set/subset? (set chord-intervals) (set scale-intervals))))))

(def tones-by-key-and-intervals utils/tones-by-key-and-intervals)


(def get-harmonization harmonizations/harmonization)
(def harmonizations harmonizations/harmonizations)
