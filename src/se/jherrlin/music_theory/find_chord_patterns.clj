(ns se.jherrlin.music-theory.find-chord-patterns
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [se.jherrlin.utils :refer [trim-matrix map-matrix take-matrix drop-matrix
                                       fretboard-matrix->x+y-map]]
            [se.jherrlin.music-theory.definitions :as definitions]
            [se.jherrlin.music-theory.fretboard :as fretboard]
            [se.jherrlin.music-theory.instruments :as instruments]
            [se.jherrlin.music-theory.definitions.helpers :as definitions.helpers]))



(def fretboard-matrix
  (->> [[{:x 0, :y 0, :interval "6"}
         {:x 1, :y 0, :interval "b7"}
         {:x 2, :y 0, :interval "7"}
         {:x 3, :y 0, :interval "1"}
         {:x 4, :y 0, :interval "b2"}
         {:x 5, :y 0, :interval "2"}
         {:x 6, :y 0, :interval "b3"}
         {:x 7, :y 0, :interval "3"}]
        [{:x 0, :y 1, :interval "2"}
         {:x 1, :y 1, :interval "b3"}
         {:x 2, :y 1, :interval "3"}
         {:x 3, :y 1, :interval "4"}
         {:x 4, :y 1, :interval "b5"}
         {:x 5, :y 1, :interval "5"}
         {:x 6, :y 1, :interval "b6"}
         {:x 7, :y 1, :interval "6"}]
        [{:x 0, :y 2, :interval "5"}
         {:x 1, :y 2, :interval "b6"}
         {:x 2, :y 2, :interval "6"}
         {:x 3, :y 2, :interval "b7"}
         {:x 4, :y 2, :interval "7"}
         {:x 5, :y 2, :interval "1"}
         {:x 6, :y 2, :interval "b2"}
         {:x 7, :y 2, :interval "2"}]
        [{:x 0, :y 3, :interval "1"}
         {:x 1, :y 3, :interval "b2"}
         {:x 2, :y 3, :interval "2"}
         {:x 3, :y 3, :interval "b3"}
         {:x 4, :y 3, :interval "3"}
         {:x 5, :y 3, :interval "4"}
         {:x 6, :y 3, :interval "b5"}
         {:x 7, :y 3, :interval "5"}]]
       (map-matrix #(select-keys % [:x :y :interval]))))

(def chord-pattern-reach 7)

(let [ranges (->> chord-pattern-reach
                  (range)
                  (rest)
                  (vec))]
  (->> ranges
       (mapv (fn [range']
               (fretboard/partition-matrix range' fretboard-matrix)))
       (apply concat)
       (vec))
  )

;; g chop


(let [fretboard-matrix [[{:x 2, :y 0, :interval "7"}
                         {:x 3, :y 0, :interval "1"}
                         {:x 4, :y 0, :interval "b2"}
                         {:x 5, :y 0, :interval "2"}
                         {:x 6, :y 0, :interval "b3"}
                         {:x 7, :y 0, :interval "3"}]
                        [{:x 2, :y 1, :interval "3"}
                         {:x 3, :y 1, :interval "4"}
                         {:x 4, :y 1, :interval "b5"}
                         {:x 5, :y 1, :interval "5"}
                         {:x 6, :y 1, :interval "b6"}
                         {:x 7, :y 1, :interval "6"}]
                        [{:x 2, :y 2, :interval "6"}
                         {:x 3, :y 2, :interval "b7"}
                         {:x 4, :y 2, :interval "7"}
                         {:x 5, :y 2, :interval "1"}
                         {:x 6, :y 2, :interval "b2"}
                         {:x 7, :y 2, :interval "2"}]
                        [{:x 2, :y 3, :interval "2"}
                         {:x 3, :y 3, :interval "b3"}
                         {:x 4, :y 3, :interval "3"}
                         {:x 5, :y 3, :interval "4"}
                         {:x 6, :y 3, :interval "b5"}
                         {:x 7, :y 3, :interval "5"}]]
      min-x            (fretboard/min-x fretboard-matrix)
      min-y            (fretboard/min-y fretboard-matrix)
      max-x            (fretboard/max-x fretboard-matrix)
      max-y            (fretboard/max-y fretboard-matrix)
      intervals        #{"1" "3" "5"}
      id (random-uuid)]
  {:min-x min-x
   :max-x max-x
   :min-y min-y
   :max-y max-y}
  (map-matrix
   (fn [m]
     (assoc m :id id))
   fretboard-matrix)
  )
