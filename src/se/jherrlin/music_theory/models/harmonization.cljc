(ns se.jherrlin.music-theory.models.harmonization
  (:require
   [malli.core :as m]
   [clojure.string :as str]
   [se.jherrlin.music-theory.models.tone :as tone]
   [se.jherrlin.music-theory.instruments :as instruments]))

(def instruments'
  (->> instruments/definitions
       vals
       (map :id)
       set
       vec))

(def HarmonizationChord
  [:map
   [:id                uuid?]
   [:key-of            tone/IntervalTone]
   [:instrument        (into [:enum] instruments')]
   [:idx               int?]
   [:symbol            string?]
   [:family            [:enum :tonic :dominant :subdominant]]
   [:mode              keyword?]
   [:chord/intervals   [:vector {:min 1} string?]]])

(def HarmonizationChords
  [:vector
   {:min 1}
   HarmonizationChord])

(def valid-harmonization-chord?    (partial m/validate HarmonizationChord))
(def valid-harmonization-chords?   (partial m/validate HarmonizationChords))
(def explain-harmonization-chord   (partial m/explain HarmonizationChord))
(def explain-harmonization-chords  (partial m/explain HarmonizationChords))

(valid-harmonization-chord?
 #_explain-harmonization-chord
 {:chord/intervals      ["1" "3" "5"],
  :family               :tonic,
  :instrument           :mandolin,
  :symbol               "I",
  :mode                 :ionian,
  :chord/chord-name-str "major",
  :chord/chord-name     :major,
  :chord/order          1,
  :type                 [:chord],
  :chord/categories     #{:major},
  :chord/display-text   "major",
  :id                   #uuid "1cd72972-ca33-4962-871c-1551b7ea5244",
  :chord/intervals-str  "1, 3, 5",
  :idx                  1,
  :chord/explanation    "major",
  :chord/suffix         "",
  :chord/indexes        [0 4 7],
  :key-of               :c})

(valid-harmonization-chord?
 {:chord/intervals      ["1" "3" "5" "b7"],
  :family               :tonic,
  :instrument           :guitar,
  :interval-tones       [:e :g# :b :d],
  :symbol               "I7",
  :chord                :dominant-seven,
  :mode                 :ionian,
  :chord/chord-name-str "dominant seven",
  :chord/chord-name     :dominant-seven,
  :chord/order          4,
  :type                 [:chord],
  :chord/categories     #{:dominant :major},
  :id                   #uuid "eebf1ac1-b3c5-46f1-87ac-f8d24823b730",
  :chord/intervals-str  "1, 3, 5, b7",
  :idx                  1,
  :chord/explanation    "dominant 7th",
  :chord/suffix         "7",
  :chord/indexes        [0 4 7 10],
  :key-of               :e})
