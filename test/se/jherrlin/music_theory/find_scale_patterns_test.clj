(ns se.jherrlin.music-theory.find-scale-patterns-test
  (:require [se.jherrlin.music-theory.find-scale-patterns :as find-scale-patterns]
            [clojure.test :refer [are deftest is testing use-fixtures]]))



(deftest ->found-matched-in-map
  (is
   (=
    (find-scale-patterns/->found-matched-in-map
     5
     11
     ["1" "2" "3" "5" "6" "1"]
     [[{:x 5, :y 0, :interval "5"}
       {:x 6, :y 0, :interval "b6"}
       {:x 7, :y 0, :interval "6"}
       {:x 8, :y 0, :interval "b7"}
       {:x 9, :y 0, :interval "7"}
       {:x 10, :y 0, :interval "1"}
       {:x 11, :y 0, :interval "b2"}]
      [{:x 5, :y 1, :interval "1"}
       {:x 6, :y 1, :interval "b2"}
       {:x 7, :y 1, :interval "2"}
       {:x 8, :y 1, :interval "b3"}
       {:x 9, :y 1, :interval "3"}
       {:x 10, :y 1, :interval "4"}
       {:x 11, :y 1, :interval "b5"}]
      [{:x 5, :y 2, :interval "4"}
       {:x 6, :y 2, :interval "b5"}
       {:x 7, :y 2, :interval "5"}
       {:x 8, :y 2, :interval "b6"}
       {:x 9, :y 2, :interval "6"}
       {:x 10, :y 2, :interval "b7"}
       {:x 11, :y 2, :interval "7"}]
      [{:x 5, :y 3, :interval "b7"}
       {:x 6, :y 3, :interval "7"}
       {:x 7, :y 3, :interval "1"}
       {:x 8, :y 3, :interval "b2"}
       {:x 9, :y 3, :interval "2"}
       {:x 10, :y 3, :interval "b3"}
       {:x 11, :y 3, :interval "3"}]])

    [{[7 3]  {:x 7, :y 3, :interval "1"},
      [9 3]  {:x 9, :y 3, :interval "2"},
      [11 3] {:x 11, :y 3, :interval "3"},
      [7 2]  {:x 7, :y 2, :interval "5"},
      [9 2]  {:x 9, :y 2, :interval "6"},
      [5 1]  {:x 5, :y 1, :interval "1"}}
     {[5 1]  {:x 5, :y 1, :interval "1"},
      [7 1]  {:x 7, :y 1, :interval "2"},
      [9 1]  {:x 9, :y 1, :interval "3"},
      [5 0]  {:x 5, :y 0, :interval "5"},
      [7 0]  {:x 7, :y 0, :interval "6"},
      [10 0] {:x 10, :y 0, :interval "1"}}])))

(deftest find-scale-patterns
  (is
   (=
    (find-scale-patterns/find-scale-patterns
     ["1" "2" "3" "5" "6"]
     [[{:x 5, :y 0, :interval "5"}
       {:x 6, :y 0, :interval "b6"}
       {:x 7, :y 0, :interval "6"}
       {:x 8, :y 0, :interval "b7"}
       {:x 9, :y 0, :interval "7"}
       {:x 10, :y 0, :interval "1"}
       {:x 11, :y 0, :interval "b2"}]
      [{:x 5, :y 1, :interval "1"}
       {:x 6, :y 1, :interval "b2"}
       {:x 7, :y 1, :interval "2"}
       {:x 8, :y 1, :interval "b3"}
       {:x 9, :y 1, :interval "3"}
       {:x 10, :y 1, :interval "4"}
       {:x 11, :y 1, :interval "b5"}]
      [{:x 5, :y 2, :interval "4"}
       {:x 6, :y 2, :interval "b5"}
       {:x 7, :y 2, :interval "5"}
       {:x 8, :y 2, :interval "b6"}
       {:x 9, :y 2, :interval "6"}
       {:x 10, :y 2, :interval "b7"}
       {:x 11, :y 2, :interval "7"}]
      [{:x 5, :y 3, :interval "b7"}
       {:x 6, :y 3, :interval "7"}
       {:x 7, :y 3, :interval "1"}
       {:x 8, :y 3, :interval "b2"}
       {:x 9, :y 3, :interval "2"}
       {:x 10, :y 3, :interval "b3"}
       {:x 11, :y 3, :interval "3"}]])
    [[[nil nil nil nil nil nil nil]
      ["1" nil nil nil nil nil nil]
      [nil nil "5" nil "6" nil nil]
      [nil nil "1" nil "2" nil "3"]]
     [["5" nil "6" nil nil "1"]
      ["1" nil "2" nil "3" nil]
      [nil nil nil nil nil nil]
      [nil nil nil nil nil nil]]])))
