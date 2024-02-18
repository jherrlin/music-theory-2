(ns se.jherrlin.music-theory.music-theory
  (:require
   [malli.core :as m]
   [clojure.set :as set]
   [se.jherrlin.music-theory.instruments :as instruments]
   [se.jherrlin.music-theory.definitions :as definitions]
   [se.jherrlin.music-theory.harmonizations :as harmonizations]
   [se.jherrlin.utils :as basic-utils]
   [se.jherrlin.music-theory.general :as general]
   [se.jherrlin.music-theory.fretboard :as fretboard]
   [se.jherrlin.music-theory.models.tone :as models.tone]
   [se.jherrlin.music-theory.models.entity :as models.entity]))

;;
;; Basic utils
;;
(def trim-matrix basic-utils/trim-matrix)
(def update-matrix basic-utils/update-matrix)
(def rotate-until basic-utils/rotate-until)


;;
;; Models
;;
(def entity models.entity/entity)
(def definitions-to-entities models.entity/definitions-to-entities)
(def valid-entity? models.entity/valid-entity?)
(def valid-entities? models.entity/valid-entities?)
(def entity-to-str models.entity/entity-to-str)
(def str-to-entities models.entity/str-to-entities)
(def fretboard-entity? models.entity/fretboard-entity?)
(def select-entity-keys models.entity/select-entity-keys)

;;
;; Definitions
;;

(def get-definition definitions/by-id)
(def get-definition-type definitions/definition-type)
(def get-chord definitions/chord)
(def chords (definitions/chords))
(def get-scale definitions/get-scale)
(def scales (definitions/scales))
(def scales-for-harmonization (definitions/scales-for-harmonization))
(def chord-patterns-belonging-to definitions/chord-patterns-belonging-to)
(def chord-pattern-triads-belonging-to definitions/chord-pattern-triads-belonging-to)
(def scale-patterns-for-scale-and-instrument definitions/scale-patterns-for-scale-and-instrument)

(comment
  (get-definition #uuid "1cd72972-ca33-4962-871c-1551b7ea5244")
  (get-chord :major)
  (chord-patterns-belonging-to :major :guitar)
  (chord-pattern-triads-belonging-to :major :guitar)
  (scale-patterns-for-scale-and-instrument #{:major} :guitar)
  )


;;
;; General
;;
(def all-tones (general/all-tones))
(def tones-starting-at general/tones-starting-at)
(def interval-tones general/interval-tones)
(def scales-to-chord general/scales-to-chord)
(def chords-to-scale general/chords-to-scale)
(def tones-by-key-and-indexes general/tones-by-key-and-indexes)
(def tones-by-key-and-intervals general/tones-by-key-and-intervals)
(def find-chord general/find-chord)
(def sharp-or-flat general/sharp-or-flat)
(def generated-chords
  (general/generate chords :chord/intervals :chord/indexes))

(def generated-scales
  (general/generate scales :scale/intervals :scale/indexes))

(def match-tones-with-chords
  (partial general/match-tones-with-coll generated-chords))

(def match-tones-with-scales
  (partial general/match-tones-with-coll generated-scales))


(comment
  (tones-starting-at :c)
  (match-tones-with-chords #{#{:c} #{:e} #{:g}})
  (match-tones-with-chords #{#{:c} #{:g} #{:e}})
  (match-tones-with-scales #{#{:c} #{:e} #{:g}})
  )


;;
;; Instruments
;;
(def instruments (instruments/instruments))
(def get-instrument instruments/instrument)
(def get-instrument-type instruments/get-instrument-type)
(def get-instrument-tuning instruments/get-instrument-tuning)

(comment
  instruments
  (get-instrument :five-string-banjo)
  (get-instrument-tuning :five-string-banjo)
  )


;;
;; Fretboard
;;
(def fretboard-strings fretboard/fretboard-strings)
(def with-all-tones fretboard/with-all-tones)
(def pattern-with-intervals fretboard/pattern-with-intervals)
(def pattern-with-tones fretboard/pattern-with-tones)
(def create-fretboard-matrix fretboard/create-fretboard-matrix)
(defn with-all-intervals [interval-tones intervals fretboard-matrix]
  (let [intervals->tones (mapv vector interval-tones intervals)]
    (fretboard/with-all-intervals
      (intervals->tones interval-tones intervals)
      fretboard-matrix)))

(comment
  (fretboard-strings
   [{:tone :g, :octave 4, :start-index 5}
    {:tone :d, :octave 3, :start-index 0}
    {:tone :g, :octave 3, :start-index 0}
    {:tone :b, :octave 3, :start-index 0}
    {:tone :d, :octave 4, :start-index 0}]
   10)
  )


;;
;; Harmonizations
;;
(def get-harmonization harmonizations/get-harmonization)
(def harmonizations harmonizations/harmonizations)
