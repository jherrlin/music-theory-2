(ns se.jherrlin.music-theory.models.fretboard-matrix
    "Representation of a fretboard as a matrix.

  Example:
  [[{:x 0, :tone #{:e}, :y 0}
    {:x 1, :tone #{:f}, :y 0}
    {:x 2, :tone #{:gb :f#}, :y 0}]
   [{:x 0, :tone #{:b}, :y 1}
    {:x 1, :tone #{:c}, :y 1}
    {:x 2, :tone #{:db :c#}, :y 1}]
   [{:x 0, :tone #{:g}, :y 2}
    {:x 1, :tone #{:g# :ab}, :y 2}
    {:x 2, :tone #{:a}, :y 2}]
   [{:x 0, :tone #{:a}, :y 3}
    {:x 1, :tone #{:bb :a#}, :y 3}
    {:x 2, :tone #{:b}, :y 3}]
   [{:x 0, :tone #{:b}, :y 4}
    {:x 1, :tone #{:c}, :y 4}
    {:x 2, :tone #{:db :c#}, :y 4}]]"
  (:require
   [malli.core :as m]
   [se.jherrlin.music-theory.models.tone :as models.tone]))


(def FretboardMatrix
  [:vector
   [:+
    [:map
     [:x                                       int?]
     [:y                                       int?]
     [:xy                                      int?]
     [:octave                 {:optional true} int?]
     [:tone                   {:optional true} models.tone/IndexTone]
     [:root?                  {:optional true} boolean?]
     [:interval               {:optional true} string?]
     [:tone-str               {:optional true} string?]

     [:match?                 {:optional true} boolean?]
     [:pattern-found-tone     {:optional true} string?]
     [:pattern-found-interval {:optional true} string?]
     [:out                    {:optional true} string?]]]])

(def validate-fretboard-matrix?  (partial m/validate FretboardMatrix))
(def explain-fretboard-matrix    (partial m/explain  FretboardMatrix))

(comment

  (validate-fretboard-matrix?
   [[{:x 0, :tone #{:e}, :y 0}
     {:x 1, :tone #{:f}, :y 0}
     {:x 2, :tone #{:gb :f#}, :y 0}]
    [{:x 0, :tone #{:b}, :y 1}
     {:x 1, :tone #{:c}, :y 1}
     {:x 2, :tone #{:db :c#}, :y 1}]
    [{:x 0, :tone #{:g}, :y 2}
     {:x 1, :tone #{:g# :ab}, :y 2}
     {:x 2, :tone #{:a}, :y 2}]
    [{:x 0, :tone #{:a}, :y 3}
     {:x 1, :tone #{:bb :a#}, :y 3}
     {:x 2, :tone #{:b}, :y 3}]
    [{:x 0, :tone #{:b}, :y 4}
     {:x 1, :tone #{:c}, :y 4}
     {:x 2, :tone #{:db :c#}, :y 4}]]))
