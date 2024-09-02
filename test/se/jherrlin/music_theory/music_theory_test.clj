(ns se.jherrlin.music-theory.music-theory-test
  (:require [se.jherrlin.music-theory.music-theory :as music-theory]
            [clojure.test :refer [are deftest is testing use-fixtures]]))


(deftest get-chord
  (is
   (=
    (music-theory/get-chord :major)
    {:chord/intervals      ["1" "3" "5"],
     :chord/chord-name-str "major",
     :chord/chord-name     :major,
     :chord/order          1,
     :type                 [:chord],
     :chord/categories     #{:major},
     :chord/display-text   "major",
     :id                   #uuid "1cd72972-ca33-4962-871c-1551b7ea5244",
     :chord/intervals-str  "1, 3, 5",
     :chord/explanation    "major",
     :chord/suffix         "",
     :chord/indexes        [0 4 7]})))

(deftest chord-patterns-belonging-to
  (is
   (-> (music-theory/chord-patterns-belonging-to :major :guitar)
       seq boolean)))
