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

;; TODO: rename
(def add-intervals-to-fretboard-matrix utils/add-intervals-to-fretboard-matrix)
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


(def harmonization harmonizations/harmonization)

(defmulti harmonization-by-type #(get-in % [:harmonization :type]))

(defmethod harmonization-by-type :predefined
  [{:keys [instrument harmonization scale key-of] :as m}]
  (let [scale-intervals (:scale/intervals scale)
        interval-tones  (interval-tones scale-intervals key-of)]
    {:m               m
     :scale-intervals scale-intervals
     :interval-tones  interval-tones
     :intervals->tones (intervals->tones
                        scale-intervals
                        interval-tones)
     :chords          (map
                       (fn [key-of harmonization-chord]

                         (assoc harmonization-chord
                                :chord (get-chord (get harmonization-chord :chord))
                                :key-of key-of))
                       interval-tones
                       (get harmonization :chords))}))


(let [instrument'          :guitar
      key-of'              :c
      harmonization-id'    :major-triads
      harmonization-scale' :major

      instrument''     (get-instrument instrument')
      harmonization''  (harmonization harmonization-id')
      scale''          (get-scale harmonization-scale')]
  (harmonization-by-type
   {:instrument    instrument''
    :harmonization harmonization''
    :scale         scale''
    :key-of        key-of'}))

#_(harmonization :major-triads)
