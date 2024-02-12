(ns se.jherrlin.music-theory.harmonizations
  (:require
   [clojure.string :as str]
   [se.jherrlin.music-theory.general :as general]
   [se.jherrlin.utils :as basic-utils]))


(def harmonizations'
  (let [triads-fn  (juxt #(nth % 0) #(nth % 2) #(nth % 4))
        seventh-fn (juxt #(nth % 0) #(nth % 2) #(nth % 4) #(nth % 6))]
    {:triads              {:id          :triads
                           :description "Triads"
                           :type        :generated
                           :function    triads-fn}
     :seventh             {:id          :seventh
                           :description "Seventh"
                           :type        :generated
                           :function    seventh-fn}
     :I7-IV7-V7-Blues {:id          :I7-IV7-V7-Blues
                       :description "I7-IV7-V7 Blues"
                       :order       1
                       :type        :predefined
                       :chords      [{:chord  :dominant-seven
                                      :symbol "I7"
                                      :mode   :ionian
                                      :family :tonic
                                      :idx-fn #(nth % 0)
                                      :idx    1}
                                     {:chord  :dominant-seven
                                      :symbol "IV7"
                                      :mode   :lydian
                                      :family :subdominant
                                      :idx-fn #(nth % 3)
                                      :idx    4}
                                     {:chord  :dominant-seven
                                      :symbol "V7"
                                      :mode   :mixolydian
                                      :family :dominant
                                      :idx-fn #(nth % 4)
                                      :idx    5}]}

     :i-ii-III-iv-v7-VI-VII {:id          :i-ii-III-iv-v7-VI-VII
                             :description "i-ii°-III-iv-v7-VI-VII"
                             :order       2
                             :type        :predefined
                             :chords      [{:chord  :minor
                                            :symbol "i"
                                            :mode   :aeolian
                                            :family :tonic
                                            :idx-fn #(nth % 0)
                                            :idx    1}
                                           {:chord  :diminished-fifth
                                            :symbol "ii°"
                                            :mode   :locrian
                                            :family :subdominant
                                            :idx-fn #(nth % 1)
                                            :idx    2}
                                           {:chord  :major
                                            :symbol "III"
                                            :mode   :ionian
                                            :family :tonic
                                            :idx-fn #(nth % 2)
                                            :idx    3}
                                           {:chord  :minor
                                            :symbol "iv"
                                            :mode   :dorian
                                            :family :subdominant
                                            :idx-fn #(nth % 3)
                                            :idx    4}
                                           {:chord  :dominant-seven
                                            :symbol "v7"
                                            :mode   :phrygian
                                            :family :dominant
                                            :idx-fn #(nth % 4)
                                            :idx    5}
                                           {:chord  :major
                                            :symbol "VI"
                                            :mode   :lydian
                                            :family :subdominant
                                            :idx-fn #(nth % 5)
                                            :idx    6}
                                           {:chord  :major
                                            :symbol "VII"
                                            :mode   :mixolydian
                                            :family :dominant
                                            :idx-fn #(nth % 6)
                                            :idx    7}]}}))

(defn harmonization [k] (get harmonizations' k))

(def harmonizations
  (->> harmonizations'
       (vals)
       (sort-by :order)))


(def triad   (juxt #(nth % 0) #(nth % 2) #(nth % 4)))
(def seventh (juxt #(nth % 0) #(nth % 2) #(nth % 4) #(nth % 6)))

(defn gen-harmonization [scales chords key-of scale' steps-fn]
  (let [scale         (->> scales
                           (filter (comp #(% scale') :scale/scale-names))
                           first)

        scale-indexes (get scale :scale/indexes)
        scale-tones   (general/tones-by-indexes
                       (general/tones-starting-at key-of)
                       scale-indexes)]
    (->> scale-tones
         (mapv
          (fn [t]
            (let [index-chord-tones (steps-fn
                                     (general/tones-starting-at scale-tones t))
                  found-chord       (general/find-chord
                                     chords
                                     (general/all-tones)
                                     index-chord-tones)
                  interval-tones    (mapv (fn [interval' index']
                                            (general/sharp-or-flat index' interval'))
                                          (:chord/intervals found-chord)
                                          index-chord-tones)]
              (assoc
               found-chord
               :key-of key-of
               :chord/index-tones index-chord-tones
               :chord/interval-tones interval-tones
               :chord/root-tone (first interval-tones)
               :chord-name (general/chord-name chords interval-tones)))))
         (mapv
          #(assoc %7
                  :harmonization/index      %1
                  :harmonization/position   %2
                  :harmonization/mode       %3
                  :harmonization/mode-str   %4
                  :harmonization/family     %5
                  :harmonization/family-str %6)
          (range 1 100)
          (if (= scale' :major)
            ["I" "ii" "iii" "IV" "V" "vi" "vii"]
            ["i" "ii" "III" "iv" "v" "VI" "VII"])
          (if (= scale' :major)
            [:ionian  :dorian  :phrygian :lydian :mixolydian :aeolian :locrian]
            [:aeolian :locrian :ionian   :dorian :phrygian   :lydian  :mixolydian])
          (if (= scale' :major)
            ["Ionian"  "Dorian"  "Phrygian" "Lydian" "Mixolydian" "Aeolian" "Locrian"]
            ["Aeolian" "Locrian" "Ionian"   "Dorian" "Phrygian"   "Lydian"  "Mixolydian"])
          (if (= scale' :major)
            [:tonic :subdominant :tonic :subdominant :dominant :tonic :dominant]
            [:tonic :subdominant :tonic :subdominant :dominant :subdominant :dominant])
          (if (= scale' :major)
            ["T" "S" "T" "S" "D" "T" "D"]
            ["T" "S" "T" "S" "D" "S" "D"])))))

(comment
  (gen-harmonization
   (vals @v5.se.jherrlin.music-theory.definitions/scales)
   (vals @v5.se.jherrlin.music-theory.definitions/chords)
   :c
   :major
   triad #_seventh)
  (gen-harmonization
   (vals (se.jherrlin.music-theory.definitions/scales))
   (vals (se.jherrlin.music-theory.definitions/chords))
   :c
   :major
   triad #_seventh)
  )

{:minor
 #:scale{:id        :minor,
         :intervals ["1" "2" "b3" "4" "5" "b6" "b7"],
         :indexes   [0 2 3 5 7 8 10],
         :title     "minor",
         :order     2}
 :major
 #:scale{:id        :major,
         :intervals ["1" "2" "3" "4" "5" "6" "7"],
         :indexes   [0    2   4   5   7   9  11],
         :title     "major",
         :order     1}}

(defn harmonization-str [xs]
  (str
   "  T = Tonic (stable), S = Subdominant (leaving), D = Dominant (back home)"
   "\n\n"
   (->> xs (map (comp #(basic-utils/fformat "  %-10s" %) str :harmonization/index)) (str/join) (str/trim))
   "\n"
   (->> xs (map (comp #(basic-utils/fformat "  %-10s" %) str :harmonization/position)) (str/join) (str/trim))
   "\n"
   (->> xs (map (comp #(basic-utils/fformat "  %-10s" %) str :harmonization/mode-str)) (str/join) (str/trim))
   "\n"
   (->> xs (map (comp #(basic-utils/fformat "  %-10s" %) str :harmonization/family-str)) (str/join) (str/trim))
   "\n"
   (->> xs (map (comp #(basic-utils/fformat "  %-10s" %) str :chord-name)) (str/join) (str/trim))))

(comment
  (->> (gen-harmonization
        @v4.se.jherrlin.music-theory.definitions/scales
        @v4.se.jherrlin.music-theory.definitions/chords
        :c
        :major
        triad)
       (harmonization-str)
       (println)))
