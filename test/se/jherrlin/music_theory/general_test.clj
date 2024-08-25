(ns se.jherrlin.music-theory.general-test
  (:require [se.jherrlin.music-theory.general :as general]
            [clojure.test :refer [are deftest is testing use-fixtures]]))


(deftest all-tones
  (is
   (=
    (general/all-tones)
    [#{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f} #{:gb :f#} #{:g} #{:g# :ab} #{:a} #{:bb :a#} #{:b}])))

(deftest tones-starting-at
  (is
   (=
    (general/tones-starting-at (general/all-tones) :c)
    [#{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f} #{:gb :f#} #{:g} #{:g# :ab} #{:a} #{:bb :a#} #{:b}]))

  (is
   (=
    (general/tones-starting-at :c)
    [#{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f} #{:gb :f#} #{:g} #{:g# :ab} #{:a} #{:bb :a#} #{:b}]))

  (is
   (=
    (general/tones-starting-at #{:c})
    [#{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f} #{:gb :f#} #{:g} #{:g# :ab} #{:a} #{:bb :a#} #{:b}]))

  (is
   (=
    (general/tones-starting-at #{:db :c#})
    [#{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f} #{:gb :f#} #{:g} #{:g# :ab} #{:a} #{:bb :a#} #{:b} #{:c}]))

  (is
   (=
    (general/tones-starting-at :d#)
    [#{:d# :eb} #{:e} #{:f} #{:gb :f#} #{:g} #{:g# :ab} #{:a} #{:bb :a#} #{:b} #{:c} #{:db :c#} #{:d}])))


(deftest sharp-or-flat
  (is
   (=
    (general/sharp-or-flat
     #{:db :c#}
     "#3")
    :c#))

  (is
   (=
    (general/sharp-or-flat
     #{:db :c#}
     "b3")
    :db))

  (is
   (=
    (general/sharp-or-flat
     #{:db :c#}
     "3")
    :c#))

  (is
   (=
    (general/sharp-or-flat
     #{:db}
     "3")
    :db)))

(deftest sharp
  (is
   (=
    (general/sharp #{:db :c#})
    :db))

  (is
   (=
    (general/sharp #{:db})
    :db))

  (is
   (=
    (general/sharp #{:c#})
    :c#)))

(deftest flat
  (is
   (=
    (general/flat #{:db :c#})
    :c#)))


(deftest tones-on-indexes-with-intervals
  (is
   (=
    (general/tones-on-indexes-with-intervals
     [0 3 7]
     ["1" "b3" "5"])
    [:c :eb :g]))

  (is
   (=
    (general/tones-on-indexes-with-intervals
     [0 4 7]
     ["1" "3" "5"])
    [:c :e :g])))

(deftest tones-by-indexes
  (is
   (=
    (general/tones-by-indexes
     (general/all-tones)
     [0 4 7])
    [#{:c} #{:e} #{:g}]))

  (is
   (=
    (general/tones-by-indexes
     (general/all-tones)
     [0 3 7])
    [#{:c} #{:d# :eb} #{:g}])))

(deftest tones-by-key-and-indexes
  (is
   (=
    (general/tones-by-key-and-indexes
     (general/all-tones)
     :d
     [0 2 4 5 7 9 11])
    [#{:d} #{:e} #{:gb :f#} #{:g} #{:a} #{:b} #{:db :c#}]))

  (is
   (=
    (general/tones-by-key-and-indexes
     #{:c}
     [0 4 7])
    [#{:c} #{:e} #{:g}])))

(deftest tones-by-intervals
  (is
   (=
    (general/tones-by-intervals
     (general/all-tones)
     ["1" "3" "5"])
    [:c :e :g]))

  (is
   (=
    (general/tones-by-intervals
     ["1" "b3" "5"])
    [:c :eb :g])))

(deftest tones-by-key-and-intervals
  (is
   (=
    (general/tones-by-key-and-intervals
     (general/all-tones)
     :c
     ["1" "b3" "5"])
    [:c :eb :g]))

  (is
   (=
    (general/tones-by-key-and-intervals
     :c
     ["1" "2" "3" "4" "5" "6" "7"])
    [:c :d :e :f :g :a :b])))
