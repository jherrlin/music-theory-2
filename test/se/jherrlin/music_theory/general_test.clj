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
