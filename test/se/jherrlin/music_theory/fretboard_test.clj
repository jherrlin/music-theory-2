(ns se.jherrlin.music-theory.fretboard-test
  (:require [se.jherrlin.utils :as utils]
            [se.jherrlin.music-theory.general :as general]
            [se.jherrlin.music-theory.intervals :as intervals]
            [se.jherrlin.music-theory.models.tone :as models.tone]
            [se.jherrlin.music-theory.fretboard :as fretboard]
            [clojure.test :refer [are deftest is testing use-fixtures]]))


(deftest fretboard-string
  (is
   (=
    (fretboard/fretboard-string
     (general/all-tones)
     {:tone :e, :octave 2, :start-index 5}
     7)
    [{:blank? true, :x 0}
     {:blank? true, :x 1}
     {:blank? true, :x 2}
     {:blank? true, :x 3}
     {:blank? true, :x 4}
     {:x 5, :tone #{:e}, :octave 2}
     {:x 6, :tone #{:f}, :octave 2}]))

  (is
   (=
    (fretboard/fretboard-string
     (general/all-tones)
     {:tone        :g
      :octave      3
      :start-index 0}
     7)
    [{:x 0, :tone #{:g}, :octave 3}
     {:x 1, :tone #{:g# :ab}, :octave 3}
     {:x 2, :tone #{:a}, :octave 3}
     {:x 3, :tone #{:bb :a#}, :octave 3}
     {:x 4, :tone #{:b}, :octave 3}
     {:x 5, :tone #{:c}, :octave 4}
     {:x 6, :tone #{:db :c#}, :octave 4}])))

(deftest fretboard-strings
  (is
   (=
    (fretboard/fretboard-strings
     (general/all-tones)
     [{:tone        :g
       :octave      3
       :start-index 0}
      {:tone        :d
       :octave      4
       :start-index 0}
      {:tone        :a
       :octave      4
       :start-index 0}
      {:tone        :e
       :octave      5
       :start-index 0}]
     6)
    [[{:x 0, :tone #{:e}, :octave 5, :y 0, :yx 0}
      {:x 1, :tone #{:f}, :octave 5, :y 0, :yx 1}
      {:x 2, :tone #{:gb :f#}, :octave 5, :y 0, :yx 2}
      {:x 3, :tone #{:g}, :octave 5, :y 0, :yx 3}
      {:x 4, :tone #{:g# :ab}, :octave 5, :y 0, :yx 4}
      {:x 5, :tone #{:a}, :octave 5, :y 0, :yx 5}]
     [{:x 0, :tone #{:a}, :octave 4, :y 1, :yx 100}
      {:x 1, :tone #{:bb :a#}, :octave 4, :y 1, :yx 101}
      {:x 2, :tone #{:b}, :octave 4, :y 1, :yx 102}
      {:x 3, :tone #{:c}, :octave 5, :y 1, :yx 103}
      {:x 4, :tone #{:db :c#}, :octave 5, :y 1, :yx 104}
      {:x 5, :tone #{:d}, :octave 5, :y 1, :yx 105}]
     [{:x 0, :tone #{:d}, :octave 4, :y 2, :yx 200}
      {:x 1, :tone #{:d# :eb}, :octave 4, :y 2, :yx 201}
      {:x 2, :tone #{:e}, :octave 4, :y 2, :yx 202}
      {:x 3, :tone #{:f}, :octave 4, :y 2, :yx 203}
      {:x 4, :tone #{:gb :f#}, :octave 4, :y 2, :yx 204}
      {:x 5, :tone #{:g}, :octave 4, :y 2, :yx 205}]
     [{:x 0, :tone #{:g}, :octave 3, :y 3, :yx 300}
      {:x 1, :tone #{:g# :ab}, :octave 3, :y 3, :yx 301}
      {:x 2, :tone #{:a}, :octave 3, :y 3, :yx 302}
      {:x 3, :tone #{:bb :a#}, :octave 3, :y 3, :yx 303}
      {:x 4, :tone #{:b}, :octave 3, :y 3, :yx 304}
      {:x 5, :tone #{:c}, :octave 4, :y 3, :yx 305}]]))

  (is
   (=
    (fretboard/fretboard-strings
     (general/all-tones)
     [{:tone        :g
       :octave      4
       :start-index 5}
      {:tone        :d
       :octave      3
       :start-index 0}
      {:tone        :g
       :octave      3
       :start-index 0}
      {:tone        :b
       :octave      3
       :start-index 0}
      {:tone        :d
       :octave      4
       :start-index 0}]
     7)
    [[{:x 0, :tone #{:d}, :octave 4, :y 0, :yx 0}
      {:x 1, :tone #{:d# :eb}, :octave 4, :y 0, :yx 1}
      {:x 2, :tone #{:e}, :octave 4, :y 0, :yx 2}
      {:x 3, :tone #{:f}, :octave 4, :y 0, :yx 3}
      {:x 4, :tone #{:gb :f#}, :octave 4, :y 0, :yx 4}
      {:x 5, :tone #{:g}, :octave 4, :y 0, :yx 5}
      {:x 6, :tone #{:g# :ab}, :octave 4, :y 0, :yx 6}]
     [{:x 0, :tone #{:b}, :octave 3, :y 1, :yx 100}
      {:x 1, :tone #{:c}, :octave 4, :y 1, :yx 101}
      {:x 2, :tone #{:db :c#}, :octave 4, :y 1, :yx 102}
      {:x 3, :tone #{:d}, :octave 4, :y 1, :yx 103}
      {:x 4, :tone #{:d# :eb}, :octave 4, :y 1, :yx 104}
      {:x 5, :tone #{:e}, :octave 4, :y 1, :yx 105}
      {:x 6, :tone #{:f}, :octave 4, :y 1, :yx 106}]
     [{:x 0, :tone #{:g}, :octave 3, :y 2, :yx 200}
      {:x 1, :tone #{:g# :ab}, :octave 3, :y 2, :yx 201}
      {:x 2, :tone #{:a}, :octave 3, :y 2, :yx 202}
      {:x 3, :tone #{:bb :a#}, :octave 3, :y 2, :yx 203}
      {:x 4, :tone #{:b}, :octave 3, :y 2, :yx 204}
      {:x 5, :tone #{:c}, :octave 4, :y 2, :yx 205}
      {:x 6, :tone #{:db :c#}, :octave 4, :y 2, :yx 206}]
     [{:x 0, :tone #{:d}, :octave 3, :y 3, :yx 300}
      {:x 1, :tone #{:d# :eb}, :octave 3, :y 3, :yx 301}
      {:x 2, :tone #{:e}, :octave 3, :y 3, :yx 302}
      {:x 3, :tone #{:f}, :octave 3, :y 3, :yx 303}
      {:x 4, :tone #{:gb :f#}, :octave 3, :y 3, :yx 304}
      {:x 5, :tone #{:g}, :octave 3, :y 3, :yx 305}
      {:x 6, :tone #{:g# :ab}, :octave 3, :y 3, :yx 306}]
     [{:y 4, :yx 400, :blank? true, :x 0} ;; banjo
      {:y 4, :yx 401, :blank? true, :x 1}
      {:y 4, :yx 402, :blank? true, :x 2}
      {:y 4, :yx 403, :blank? true, :x 3}
      {:y 4, :yx 404, :blank? true, :x 4}
      {:x 5, :tone #{:g}, :octave 4, :y 4, :yx 405}
      {:x 6, :tone #{:g# :ab}, :octave 4, :y 4, :yx 406}]])))

(deftest intevals-string->intervals-matrix
  (is
   (=
    (fretboard/intevals-string->intervals-matrix
     "   3   -   -
   -   bb1   -
   5   -   -
   -   -   -
   -   -   -
   -   -   -")

    [["3" nil nil]
     [nil "bb1" nil]
     ["5" nil nil]
     [nil nil nil]
     [nil nil nil]
     [nil nil nil]])))
