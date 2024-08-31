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
