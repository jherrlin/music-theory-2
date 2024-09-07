(ns se.jherrlin.utils-test
  (:require [se.jherrlin.utils :as utils]
            [clojure.test :refer [are deftest is testing use-fixtures]]))


(deftest fretboard-matrix->x+y-map
  (is
   (=
    (utils/fretboard-matrix->x+y-map
     [[{:x 5, :y 0, :interval "5"}
       {:x 6, :y 0, :interval "b6"}]
      [{:x 5, :y 1, :interval "1"}
       {:x 6, :y 1, :interval "b2"}]
      [{:x 5, :y 2, :interval "4"}
       {:x 6, :y 2, :interval "b5"}]
      [{:x 5, :y 3, :interval "b7"}
       {:x 6, :y 3, :interval "7"}]])
    {[5 0] {:x 5, :y 0, :interval "5"},
     [6 0] {:x 6, :y 0, :interval "b6"},
     [5 1] {:x 5, :y 1, :interval "1"},
     [6 1] {:x 6, :y 1, :interval "b2"},
     [5 2] {:x 5, :y 2, :interval "4"},
     [6 2] {:x 6, :y 2, :interval "b5"},
     [5 3] {:x 5, :y 3, :interval "b7"},
     [6 3] {:x 6, :y 3, :interval "7"}})))

(deftest vec-remove
  (is
   (=
    (utils/vec-remove
     1
     [1 2 3])
    [1 3])))

(deftest list-insert
  (is
   (=
    (utils/list-insert
     0
     3
     [1 2 3 4])
    '(1 2 3 0 4))))

(deftest rotate-until
  (is
   (=
    (utils/rotate-until
     #(% :f#)
     [#{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f} #{:gb :f#} #{:g} #{:g# :ab} #{:a} #{:bb :a#} #{:b}])
    [#{:gb :f#} #{:g} #{:g# :ab} #{:a} #{:bb :a#} #{:b} #{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f}])))

(deftest take-indexes
  (is
   (=
    (utils/take-indexes
     [#{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f}]
     [0 3 5])
    [#{:c} #{:d# :eb} #{:f}])))

(deftest rotate-matrix
  (is
   (=
    (utils/rotate-matrix
     [["3" nil nil]
      [nil "1" nil]
      ["5" nil nil]
      [nil nil nil]
      [nil nil nil]
      [nil nil nil]])
    [["3" nil "5" nil nil nil]
     [nil "1" nil nil nil nil]
     [nil nil nil nil nil nil]])))

(deftest take-matrix
  (is
   (=
    (utils/take-matrix
     2
     [[1 2 3 4]
      [1 2 3 4]
      [1 2 3 4]])
    [[1 2]
     [1 2]
     [1 2]])))

(deftest drop-matrix
  (is
   (=
    (utils/drop-matrix
     1
     [[1 2 3 4]
      [1 2 3 4]
      [1 2 3 4]])

    [[2 3 4]
     [2 3 4]
     [2 3 4]])))

(deftest trim-matrix
  (is
   (=
    (utils/trim-matrix
     [["3" nil nil]
      [nil "1" nil]
      ["5" nil nil]
      [nil nil nil]
      [nil nil nil]
      [nil nil nil]])
    [["3" nil]
     [nil "1"]
     ["5" nil]
     [nil nil]
     [nil nil]
     [nil nil]]))

  (is
   (=
    (utils/trim-matrix
     [[nil "1" nil nil]
      [nil "5" nil nil]
      [nil "b3" nil nil]
      [nil nil nil "1"]
      [nil nil nil "5"]
      [nil "1" nil nil]])
    [["1" nil nil]
     ["5" nil nil]
     ["b3" nil nil]
     [nil nil "1"]
     [nil nil "5"]
     ["1" nil nil]])))

(deftest map-matrix
  (is
   (=
    (utils/map-matrix
     inc
     [[1]
      [2]
      [3]])
    [[2] [3] [4]])))

(deftest update-matrix
  (is
   (=
    (utils/update-matrix
     0 0
     inc
     [[1] [2] [3]])
    [[2] [2] [3]])))
