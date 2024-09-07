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

(deftest fretboard-matrix-to-map_and_fretboard-map-to-matrix
  (is
   (let [fretboard-matrix
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
          6)]
     (->> fretboard-matrix
          fretboard/fretboard-matrix-to-map
          fretboard/fretboard-map-to-matrix
          (= fretboard-matrix)))))


(deftest filter-matches
  (is
   (=
    (fretboard/filter-matches
     [[{:x 0, :y 0, :tone #{:e}}
       {:x 1, :y 0, :tone #{:f}}
       {:x 2, :y 0, :tone #{:gb :f#}}
       {:x 3, :y 0, :tone #{:g}}
       {:x 4, :y 0, :tone #{:g# :ab}}
       {:x 5, :y 0, :tone #{:a}}]
      [{:x 0, :y 1, :match? true, :tone #{:a}}
       {:x 1, :y 1, :tone #{:bb :a#}}
       {:x 2, :y 1, :match? true, :tone #{:b}}
       {:x 3, :y 1, :tone #{:c}}
       {:x 4, :y 1, :tone #{:db :c#}}
       {:x 5, :y 1, :match? true, :tone #{:d}}]
      [{:x 0, :y 2, :match? true, :tone #{:d}}
       {:x 1, :y 2, :tone #{:d# :eb}}
       {:x 2, :y 2, :match? true, :tone #{:e}}
       {:x 3, :y 2, :tone #{:f}}
       {:x 4, :y 2, :match? true, :tone #{:gb :f#}}
       {:x 5, :y 2, :tone #{:g}}]
      [{:x 0, :y 3, :tone #{:g}}
       {:x 1, :y 3, :tone #{:g# :ab}}
       {:x 2, :y 3, :tone #{:a}}
       {:x 3, :y 3, :tone #{:bb :a#}}
       {:x 4, :y 3, :tone #{:b}}
       {:x 5, :y 3, :tone #{:c}}]])
    [{:x 0, :y 2, :match? true, :tone #{:d}}
     {:x 2, :y 2, :match? true, :tone #{:e}}
     {:x 4, :y 2, :match? true, :tone #{:gb :f#}}
     {:x 0, :y 1, :match? true, :tone #{:a}}
     {:x 2, :y 1, :match? true, :tone #{:b}}
     {:x 5, :y 1, :match? true, :tone #{:d}}])))



(deftest fretboard-matrix->tonejs-dispatches
  (is
   (=
    (fretboard/fretboard-matrix->tonejs-dispatches
     [[{:x 0, :tone #{:e}, :octave 5, :y 0, :yx 0, :interval "2", :tone-str "E"}
       {:x 1, :tone #{:f}, :octave 5, :y 0, :yx 1, :interval "b3", :tone-str "F"}
       {:y        0,
        :octave   5,
        :tone-str "Gb",
        :yx       2,
        :tone     #{:gb :f#},
        :flat     "Gb",
        :sharp    "F#",
        :x        2,
        :interval "3"}
       {:x 3, :tone #{:g}, :octave 5, :y 0, :yx 3, :interval "4", :tone-str "G"}
       {:y        0,
        :octave   5,
        :tone-str "Ab",
        :yx       4,
        :tone     #{:g# :ab},
        :flat     "Ab",
        :sharp    "G#",
        :x        4,
        :interval "b5"}
       {:x 5, :tone #{:a}, :octave 5, :y 0, :yx 5, :interval "5", :tone-str "A"}
       {:y        0,
        :octave   5,
        :tone-str "Bb",
        :yx       6,
        :tone     #{:bb :a#},
        :flat     "Bb",
        :sharp    "A#",
        :x        6,
        :interval "b6"}]
      [{:y                      1,
        :octave                 4,
        :pattern-found-tone     "A",
        :tone-str               "A",
        :yx                     100,
        :pattern-found-interval "5",
        :tone                   #{:a},
        :out                    "5",
        :x                      0,
        :interval               "5",
        :match?                 true}
       {:y        1,
        :octave   4,
        :tone-str "Bb",
        :yx       101,
        :tone     #{:bb :a#},
        :flat     "Bb",
        :sharp    "A#",
        :x        1,
        :interval "b6"}
       {:y                      1,
        :octave                 4,
        :pattern-found-tone     "B",
        :tone-str               "B",
        :yx                     102,
        :pattern-found-interval "6",
        :tone                   #{:b},
        :out                    "6",
        :x                      2,
        :interval               "6",
        :match?                 true}
       {:x        3,
        :tone     #{:c},
        :octave   5,
        :y        1,
        :yx       103,
        :interval "b7",
        :tone-str "C"}
       {:y        1,
        :octave   5,
        :tone-str "Db",
        :yx       104,
        :tone     #{:db :c#},
        :flat     "Db",
        :sharp    "C#",
        :x        4,
        :interval "7"}
       {:y                      1,
        :octave                 5,
        :root?                  true,
        :pattern-found-tone     "D",
        :tone-str               "D",
        :yx                     105,
        :pattern-found-interval "1",
        :tone                   #{:d},
        :out                    "1",
        :x                      5,
        :interval               "1",
        :match?                 true}
       {:y        1,
        :octave   5,
        :tone-str "Eb",
        :yx       106,
        :tone     #{:d# :eb},
        :flat     "Eb",
        :sharp    "D#",
        :x        6,
        :interval "b2"}]
      [{:y                      2,
        :octave                 4,
        :root?                  true,
        :pattern-found-tone     "D",
        :tone-str               "D",
        :yx                     200,
        :pattern-found-interval "1",
        :tone                   #{:d},
        :out                    "1",
        :x                      0,
        :interval               "1",
        :match?                 true}
       {:y        2,
        :octave   4,
        :tone-str "Eb",
        :yx       201,
        :tone     #{:d# :eb},
        :flat     "Eb",
        :sharp    "D#",
        :x        1,
        :interval "b2"}
       {:y                      2,
        :octave                 4,
        :pattern-found-tone     "E",
        :tone-str               "E",
        :yx                     202,
        :pattern-found-interval "2",
        :tone                   #{:e},
        :out                    "2",
        :x                      2,
        :interval               "2",
        :match?                 true}
       {:x        3,
        :tone     #{:f},
        :octave   4,
        :y        2,
        :yx       203,
        :interval "b3",
        :tone-str "F"}
       {:y                      2,
        :octave                 4,
        :pattern-found-tone     "F#",
        :tone-str               "Gb",
        :yx                     204,
        :pattern-found-interval "3",
        :tone                   #{:gb :f#},
        :out                    "3",
        :flat                   "Gb",
        :sharp                  "F#",
        :x                      4,
        :interval               "3",
        :match?                 true}
       {:x 5, :tone #{:g}, :octave 4, :y 2, :yx 205, :interval "4", :tone-str "G"}
       {:y        2,
        :octave   4,
        :tone-str "Ab",
        :yx       206,
        :tone     #{:g# :ab},
        :flat     "Ab",
        :sharp    "G#",
        :x        6,
        :interval "b5"}]
      [{:x 0, :tone #{:g}, :octave 3, :y 3, :yx 300, :interval "4", :tone-str "G"}
       {:y        3,
        :octave   3,
        :tone-str "Ab",
        :yx       301,
        :tone     #{:g# :ab},
        :flat     "Ab",
        :sharp    "G#",
        :x        1,
        :interval "b5"}
       {:x 2, :tone #{:a}, :octave 3, :y 3, :yx 302, :interval "5", :tone-str "A"}
       {:y        3,
        :octave   3,
        :tone-str "Bb",
        :yx       303,
        :tone     #{:bb :a#},
        :flat     "Bb",
        :sharp    "A#",
        :x        3,
        :interval "b6"}
       {:x 4, :tone #{:b}, :octave 3, :y 3, :yx 304, :interval "6", :tone-str "B"}
       {:x        5,
        :tone     #{:c},
        :octave   4,
        :y        3,
        :yx       305,
        :interval "b7",
        :tone-str "C"}
       {:y        3,
        :octave   4,
        :tone-str "Db",
        :yx       306,
        :tone     #{:db :c#},
        :flat     "Db",
        :sharp    "C#",
        :x        6,
        :interval "7"}]])
    [[:dispatch [:tonejs/play-tone {:x 0, :y 2, :octave 4, :tone "D"}]]
     [:dispatch-later
      {:ms 500, :dispatch [:tonejs/play-tone {:x 2, :y 2, :octave 4, :tone "E"}]}]
     [:dispatch-later
      {:ms       1000,
       :dispatch [:tonejs/play-tone {:x 4, :y 2, :octave 4, :tone "Gb"}]}]
     [:dispatch-later
      {:ms 1500, :dispatch [:tonejs/play-tone {:x 0, :y 1, :octave 4, :tone "A"}]}]
     [:dispatch-later
      {:ms 2000, :dispatch [:tonejs/play-tone {:x 2, :y 1, :octave 4, :tone "B"}]}]
     [:dispatch-later
      {:ms 2500, :dispatch [:tonejs/play-tone {:x 5, :y 1, :octave 5, :tone "D"}]}]])))
