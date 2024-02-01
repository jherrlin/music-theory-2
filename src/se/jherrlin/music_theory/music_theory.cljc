(ns se.jherrlin.music-theory.music-theory
  (:require
   [malli.core :as m]
   [se.jherrlin.music-theory.instruments :as instruments]
   [se.jherrlin.music-theory.definitions :as definitions]
   [se.jherrlin.music-theory.harmonizations :as harmonizations]
   [se.jherrlin.music-theory.intervals :as intervals]
   [se.jherrlin.music-theory.utils :as utils]
   [clojure.string :as str]
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

(def scales-for-harmonization
  (->> scales
       (filter (comp #{7} count :scale/intervals))))

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

(def add-intervals-to-fretboard-matrix utils/add-intervals-to-fretboard-matrix)
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

(defn create-fretboard-matrix [key-of nr-of-frets tuning]
  (->> (fretboard-strings tuning nr-of-frets)
       (add-intervals-to-fretboard-matrix key-of)))

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


(def get-harmonization harmonizations/harmonization)
(def harmonizations harmonizations/harmonizations)

(def tones-by-key-and-indexes utils/tones-by-key-and-indexes)
(def tones-by-key-and-intervals utils/tones-by-key-and-intervals)

(def find-chord utils/find-chord)
(def sharp-or-flat utils/sharp-or-flat)
(def rotate-until utils/rotate-until)

(defn scale-interval-tones [key-of scale-intervals]
  (let [scale-indexes (intervals/functions-to-semitones scale-intervals)

        index-tones (tones-by-key-and-indexes key-of scale-indexes)]
    (map
     #(sharp-or-flat %1 %2)
     index-tones
     scale-intervals)))

(scale-interval-tones
 :c
 ["1" "2" "3" "4" "5" "6" "7"])
;; => (:c :d :e :f :g :a :b)


(def Unit
  [:map
   [:id                  uuid?]
   [:instrument          keyword?]
   [:key-of              keyword?]])

(def valid-unit?   (partial m/validate Unit))
(def explain-unit  (partial m/explain  Unit))

(defn unit [key-of instrument id]
  {:id         id
   :instrument instrument
   :key-of     key-of})

(defn unit-to-str [{:keys [instrument key-of id]}]
  (str (-> instrument name) "," (-> key-of name) "," id))

(defn str-to-unit [s]
  (let [[instrument key-of id] (str/split s ",")]
    {:instrument (keyword instrument)
     :key-of     (keyword key-of)
     :id         (uuid id)}))

(let [m {:instrument :guitar
         :key-of     :c
         :id         #uuid "c91cddfe-f776-4c0c-8125-4f4c5d074e77"}]
  (->> m
       (unit-to-str)
       #_(str-to-unit)
       #_(= m)))

(defn str-to-units [s]
  (->> (str/split s "_")
       (map str-to-unit)))

(str-to-units
 "guitar,c,94f5f7a4-d852-431f-90ca-9e99f89bbb9c")
