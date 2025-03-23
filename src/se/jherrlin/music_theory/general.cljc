(ns se.jherrlin.music-theory.general
  "Ns with general music theory functions.

  Important terminology:
  - Index tones are sets:        #{:db :c#}
  - Interval tones are keywords: :c#"
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [malli.core :as m]
   [se.jherrlin.music-theory.intervals :as intervals]
   [se.jherrlin.music-theory.models.tone :as models.tone]
   [se.jherrlin.utils :as utils]))


(defn all-tones
  "All tones as index.

  =>
  [#{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f} #{:gb :f#} #{:g} #{:g# :ab} #{:a} #{:bb :a#} #{:b}]"
  []
  {:post [(models.tone/valid-index-tones? %)]}
  [#{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f} #{:gb :f#} #{:g} #{:g# :ab} #{:a} #{:bb :a#} #{:b}])

(comment
  (all-tones)
  :-)

(defn interval-tone->index-tone
  [interval-tone]
  (->> (all-tones)
       (filter #(% interval-tone))
       first))

(comment
  (interval-tone->index-tone :c#) ;; => #{:db :c#}
  (interval-tone->index-tone :c)  ;; => #{:c}
  :-)

(defn tones-starting-at
  "`x` - can be both a index tone and a interval tone.

  Example:
  `x` = `:d#`
  =>
  [#{:d# :eb} #{:e} #{:f} #{:gb :f#} #{:g} #{:g# :ab} #{:a} #{:bb :a#} #{:b} #{:c} #{:db :c#} #{:d}]"
  ([x]
   (tones-starting-at (all-tones) x))
  ([all-tones x]
   {:pre [(models.tone/valid-index-tones? all-tones)
          (models.tone/valid-interval-or-index-tone? x)]}
   (utils/rotate-until
    #(if (models.tone/valid-index-tone? x)
       (= % x)
       (% x))
    all-tones)))

(comment
  (tones-starting-at (all-tones) :c)
  (tones-starting-at :c)
  (tones-starting-at (all-tones) #{:c})
  (tones-starting-at (all-tones) :d#)
  :-)


(defn sharp-or-flat
  "Select tone from interval.
  Tone is a set: #{:db :c#}
  Interval is a string: \"3#\"

  =>
  :c#"
  [index-tone interval]
  {:pre [(models.tone/valid-index-tone? index-tone)]}
  (-> (cond
        (= 1 (count index-tone))     index-tone
        (str/includes? interval "b") (filter (comp #(str/includes? % "b") name) index-tone)
        :else                        (filter (comp #(str/includes? % "#") name) index-tone))
      (first)))

(comment
  (sharp-or-flat
   #{:db :c#}
   "#3")

  (sharp-or-flat
   #{:db :c#}
   "b3")

  (sharp-or-flat
   #{:db :c#}
   "3")

  (sharp-or-flat
   #{:db}
   "3")
  )

(defn sharp [index-tone]
  {:pre [(models.tone/valid-index-tone? index-tone)]}
  (sharp-or-flat index-tone "b"))

(defn flat [index-tone]
  {:pre [(models.tone/valid-index-tone? index-tone)]}
  (sharp-or-flat index-tone "#"))

(comment
  (sharp #{:db :c#})    ;; => :db
  (flat #{:db :c#})     ;; => :c#
  )


(defn tones-on-indexes-with-intervals
  ([indexes intervals]
   (tones-on-indexes-with-intervals (all-tones) indexes intervals))
  ([tones indexes intervals]
   (mapv
    (fn [index interval]
      (sharp-or-flat (nth tones index) interval))
    indexes
    intervals)))

(comment
  (tones-on-indexes-with-intervals
   [0 3 7]
   ["1" "b3" "5"])
  ;; => [:c :eb :g]

  )

(defn tones-by-indexes
  "Tones by indexes"
  ([indexes]
   (tones-by-indexes (all-tones) indexes))
  ([all-tones indexes]
   {:pre  [(models.tone/valid-index-tones? all-tones)
           (m/validate models.tone/Indexes indexes)]
    :post [(models.tone/valid-index-tones? %)]}
   (utils/take-indexes all-tones indexes)))

(comment
  (tones-by-indexes
   (all-tones)
   [0 4 7])
  ;; => [#{:c} #{:e} #{:g}]
  )


(defn tones-by-key-and-indexes
  "Index tones by `key-of` and `indexes`

  =>
  [#{:c} #{:d} #{:e}]"
  ([key-of indexes]
   (tones-by-key-and-indexes (all-tones) key-of indexes))
  ([all-tones key-of indexes]
   {:pre  [(models.tone/valid-index-tones? all-tones)
           (m/validate models.tone/Indexes indexes)]
    :post [(models.tone/valid-index-tones? %)]}
   (let [all-tones' (tones-starting-at all-tones key-of)]
     (tones-by-indexes all-tones' indexes))))

(comment
  (tones-by-key-and-indexes
   :d
   [0 2 4 5 7 9 11])
  ;; => [#{:d} #{:e} #{:gb :f#} #{:g} #{:a} #{:b} #{:db :c#}]

  (tones-by-key-and-indexes
   #{:c}
   [0 4 7])
  ;; => [#{:c} #{:e} #{:g}]
  )


(defn tones-by-intervals
  "Get tones from intervals

  `intervals`   -   [\"1\" \"3\" \"5\"]
  =>
  [:c :e :g]"
  ([intervals]
   (tones-by-intervals (all-tones) intervals))
  ([all-tones intervals]
   {:pre  [(m/validate models.tone/Intervals intervals)]
    :post [(m/validate models.tone/IntervalTones %)]}
   (->> intervals
        (mapv (fn [interval-function]
                (let [interval-index (get-in intervals/intervals-map-by-function [interval-function :semitones])]
                  (sharp-or-flat (nth all-tones interval-index) interval-function)))))))

(comment
  (tones-by-intervals
   (all-tones)
   ["1" "3" "5"])
  ;; => [:c :e :g]

  (tones-by-intervals
   ["1" "b3" "5"])
  ;; => [:c :eb :g]
  )

(defn tones-by-key-and-intervals
  "

  `key-of`    -  :c
  `intervals` -  [\"1\" \"3\" \"5\"]
  =>
  [:c :e :g]"
  ([key-of intervals]
   (tones-by-key-and-intervals (all-tones) key-of intervals))
  ([all-tones key-of intervals]
   {:pre  [(m/validate models.tone/IntervalTone key-of)
           (m/validate models.tone/Intervals intervals)]
    :post [(m/validate models.tone/IntervalTones %)]}
   (tones-by-intervals
    (tones-starting-at all-tones key-of)
    intervals)))

(comment
  (tones-by-key-and-intervals
   :c
   ["1" "b3" "5"])
  ;; => [:c :eb :g]

  (tones-by-key-and-intervals
   :c
   ["1" "2" "3" "4" "5" "6" "7"])
  ;; => [:c :d :e :f :g :a :b]
  )


(defn intervals-to-indexes
  "Indexes from intervals

  `intervals` -  [\"1\" \"3\" \"5\"]
  =>
  `[0 3 7]`"
  [intervals]
  {:pre  [(m/validate models.tone/Intervals intervals)]
   :post [(m/validate models.tone/Indexes %)]}
  (->> intervals
       (mapv (fn [interval]
               (get-in intervals/intervals-map-by-function [interval :semitones])))))

(comment
  (intervals-to-indexes ["1" "b3" "5"])
  ;; => [0 3 7]
  )

(defn index-tones
  "Index tones from indexes and key-of"
  ([key-of indexes]
   (index-tones (all-tones) key-of indexes))
  ([all-tones key-of indexes]
   {:pre  [(models.tone/valid-indexes? indexes)
           (models.tone/valid-interval-or-index-tone? key-of)]
    :post [models.tone/valid-index-tones?]}
   (let [tones (tones-starting-at all-tones key-of)]
     (tones-by-indexes tones indexes))))

(comment
  (index-tones :c [0 1 2])
  ;; => [#{:c} #{:db :c#} #{:d}]

  (index-tones #{:c} [0 1 2])
  ;; => [#{:c} #{:db :c#} #{:d}]

  )

(defn interval-tones
  "Interval tones from intervals and key-of"
  ([key-of intervals]
   (interval-tones (all-tones) key-of intervals))
  ([all-tones key-of intervals]
   {:pre  [(m/validate models.tone/Intervals intervals)
           (models.tone/valid-interval-or-index-tone? key-of)]
    :post [(m/validate models.tone/IntervalTones %)]}
   (tones-by-intervals
    (tones-starting-at all-tones key-of)
    intervals)))

(comment
  (interval-tones :c ["1" "b3" "5"])
  ;; => [:c :eb :g]

  (interval-tones #{:c} ["1" "b3" "5"])
  ;; => [:c :eb :g]

  )

(defn tones-data-from-indexes-and-intervals
  ([indexes intervals]
   (tones-data-from-indexes-and-intervals (all-tones) indexes intervals))
  ([all-tones indexes intervals]
   (mapv
    (fn [index interval]
      (merge
       (get intervals/intervals-map-by-function interval)
       (let [index-tone (nth all-tones index)]
         {:index-tone    index-tone
          :interval-tone (sharp-or-flat (nth all-tones index) interval)
          :interval      interval
          :index         index})))
    indexes
    intervals)))

(comment
  (tones-data-from-indexes-and-intervals
   (all-tones)
   [0 3 7]
   ["1" "b3" "5"])
  ;; => [{:semitones 0, :function "1", :name/en "Root", :name/sv "Root", :index-tone #{:c}, :interval-tone :c, :interval "1", :index 0} {:index-tone #{:d# :eb}, :interval-tone :eb, :text/en "Blue note", :index 3, :name/en "Minor third", :function "b3", :name/sv "Moll-ters", :semitones 3, :interval "b3"} {:semitones 7, :function "5", :name/en "Perfect fifth", :name/sv "Kvint", :index-tone #{:g}, :interval-tone :g, :interval "5", :index 7}]

  )

(defn tones-data-from-key-of-and-intervals
  "

  `key-of`    -  :c
  `intervals` -  [\"1\" \"3\" \"5\"]
  =>
  [:c :e :g]"
  ([key-of intervals]
   (tones-data-from-key-of-and-intervals (all-tones) key-of intervals))
  ([all-tones key-of intervals]
   {:pre  [(m/validate models.tone/IndexTones all-tones)
           (m/validate models.tone/IntervalTone key-of)]
    :post [(models.tone/valid-tones-data? %)]}
   (let [indexes (intervals-to-indexes intervals)]
     (tones-data-from-indexes-and-intervals
      (tones-starting-at all-tones key-of)
      indexes
      intervals))))

(comment
  (tones-data-from-key-of-and-intervals
   (all-tones)
   :c
   ["1" "b3" "5"])
  ;; => [{:semitones 0, :function "1", :name/en "Root", :name/sv "Root", :index-tone #{:c}, :interval-tone :c, :interval "1", :index 0} {:index-tone #{:d# :eb}, :interval-tone :eb, :text/en "Blue note", :index 3, :name/en "Minor third", :function "b3", :name/sv "Moll-ters", :semitones 3, :interval "b3"} {:semitones 7, :function "5", :name/en "Perfect fifth", :name/sv "Kvint", :index-tone #{:g}, :interval-tone :g, :interval "5", :index 7}]
  )


(defn match-chord-with-scales
  "Find what scales that works with a chord, by the chord indexes.

  `scales-map`     - Map with scales
  `chord-indexes`  - Seq with chord indexes, example: `[0 4 7]`"
  [scales-maps chord-indexes]
  {:pre [(seq scales-maps)]}
  (->> scales-maps
       (filter (fn [scale]
                 (let [scale-indexes (get scale :scale/indexes)]
                   (set/subset? (set chord-indexes) (set scale-indexes)))))))

(comment
  (match-chord-with-scales
   [#:scale{:id        :ionian,
            :intervals ["1" "2" "3" "4" "5" "6" "7"],
            :indexes   [0 2 4 5 7 9 11],
            :title     "ionian",
            :order     4}]
   [0 4 7])
  ;; => (#:scale{:id :ionian, :intervals ["1" "2" "3" "4" "5" "6" "7"], :indexes [0 2 4 5 7 9 11], :title "ionian", :order 4})

  )

(defn find-chords
  [chord-maps all-tones chord-tones]
  {:pre [(seq chord-maps)]}
  (let [[root-tone & _] chord-tones
        tones           (tones-starting-at all-tones root-tone)]
    (->> chord-maps
         (filter (fn [{:chord/keys [indexes]}]
                   (let [chord-to-serch (tones-by-indexes tones indexes)
                         chord-to-match chord-tones]
                     (and (= (count chord-to-serch) (count chord-to-match))
                          (->> (map (fn [a b]
                                      (if (set? b)
                                        (= a b)
                                        (boolean (a b))))
                                    chord-to-serch
                                    chord-to-match)
                               (every? true?)))))))))

(comment
  (find-chords
   [#:chord{:id           :major,
            :intervals    ["1" "3" "5"],
            :indexes      [0 4 7],
            :title        "major",
            :order        1,
            :sufix        "",
            :explanation  "major",
            :display-text "major"}
    #:chord{:id           :minor,
            :intervals    ["1" "b3" "5"],
            :indexes      [0 3 7],
            :title        "minor",
            :order        2,
            :sufix        "m",
            :explanation  "minor",
            :display-text "minor"}]
   (all-tones)
   [#{:c} #{:e} #{:g}]
   #_[#{:c} #{:d# :eb} #{:g}]
   #_[:c :e :g])
  )


(defn find-chord
  ([chord-maps chord-tones]
   (find-chord chord-maps (all-tones) chord-tones))
  ([chord-maps all-tones chord-tones]
   {:pre [(coll? chord-maps)]}
   (->> (find-chords chord-maps all-tones chord-tones)
        (first))))

(comment
  (find-chord
   [#:chord{:id           :major,
            :intervals    ["1" "3" "5"],
            :indexes      [0 4 7],
            :title        "major",
            :order        1,
            :sufix        "",
            :explanation  "major",
            :display-text "major"}]
   [#{:c} #{:e} #{:g}])
  )



(defn chord-name
  [chord-maps chord-tones]
  {:pre [(coll? chord-maps)]}
  (let [root-tone             (first chord-tones)
        {:chord/keys [sufix]} (find-chord chord-maps (all-tones) chord-tones)]
    (str (-> root-tone name str/lower-case str/capitalize) sufix)))

(comment
  (chord-name
   [#:chord{:id           :major,
            :intervals    ["1" "3" "5"],
            :indexes      [0 4 7],
            :title        "major",
            :order        1,
            :sufix        "",
            :explanation  "major",
            :display-text "major"}
    #:chord{:id           :minor,
            :intervals    ["1" "b3" "5"],
            :indexes      [0 3 7],
            :title        "minor",
            :order        2,
            :sufix        "m",
            :explanation  "minor",
            :display-text "minor"}]
   #_[:c :e :g]
   [:c :eb :g])
  )



(defn scales-to-chord [scales chord-intervals]
  (->> scales
       (map (juxt :scale/scale-names identity))
       (into {})
       (vals)
       (filter
        (fn [{scale-intervals :scale/intervals}]
          (set/subset? (set chord-intervals) (set scale-intervals))))))

(defn chords-to-scale [chords scale-intervals]
  (->> chords
       (filter
        (fn [{chord-intervals :chord/intervals}]
          (set/subset? (set chord-intervals) (set scale-intervals))))))


(defn generate [coll intervals-key index-key]
  (for [key-of        (apply concat (all-tones))
        {intervals intervals-key
         indexes   index-key
         :as       m} coll]
    (let [interval-tones (tones-by-key-and-intervals key-of intervals)
          index-tones    (tones-by-key-and-indexes   key-of indexes)]
      (assoc m
             :interval-tones interval-tones
             :interval-tones-set (set interval-tones)
             :index-tones  index-tones
             :index-tones-set (set index-tones)
             :key-of key-of))))

(def index-or-interval-tones-set?
  (partial m/validate [:or
                       models.tone/IndexTonesSet
                       models.tone/IntervalTonesSet]))

(defn match-tones-with-coll
  [coll tones-to-search]
  {:pre [(index-or-interval-tones-set? tones-to-search)]}
  (let [tones-to-search-count (count tones-to-search)]
    (->> coll
         (map (fn [{:keys [interval-tones-set index-tones-set] :as m}]
                (let [ts (if (models.tone/valid-index-tones-set? tones-to-search)
                           index-tones-set interval-tones-set)
                      ts-count (count ts)
                      intersections (set/intersection ts tones-to-search)]
                  (assoc m
                         :intersections intersections
                         :intersections-count (+ (count intersections)
                                                 (cond
                                                   (= index-tones-set tones-to-search) tones-to-search-count
                                                   (> tones-to-search-count ts-count) (- ts-count tones-to-search-count)
                                                   (< tones-to-search-count ts-count) (- tones-to-search-count ts-count)
                                                   :else 0))))))
         (sort-by :intersections-count #(compare %2 %1))
         (take 10))))

(def midi-pitch->index-tone-with-octave
  (let [nr-of-tones (count (all-tones))
        octaves     (concat
                     (take nr-of-tones (repeat 1))
                     (take nr-of-tones (repeat 2))
                     (take nr-of-tones (repeat 3))
                     (take nr-of-tones (repeat 4))
                     (take nr-of-tones (repeat 5))
                     (take nr-of-tones (repeat 6))
                     (take nr-of-tones (repeat 7))
                     (take nr-of-tones (repeat 8))
                     (take nr-of-tones (repeat 9)))]
    (->> (map
          (fn [index-tone octave midi-pitch-nr]
            [midi-pitch-nr {:index-tone index-tone :octave octave}])
          (take (count octaves) (cycle (all-tones)))
          octaves
          (range 24 (+ (count octaves) 24)))
         (into {}))))

(defn ->midi-pitch->index-tone-with-octave
  "Map midi pitch to index tone and octave"
  [midi-pitch]
  (midi-pitch->index-tone-with-octave midi-pitch))

(def index-tone-with-octave->midi-pitch
  (let [nr-of-tones (count (all-tones))
        octaves     (concat
                     (take nr-of-tones (repeat 1))
                     (take nr-of-tones (repeat 2))
                     (take nr-of-tones (repeat 3))
                     (take nr-of-tones (repeat 4))
                     (take nr-of-tones (repeat 5))
                     (take nr-of-tones (repeat 6))
                     (take nr-of-tones (repeat 7))
                     (take nr-of-tones (repeat 8))
                     (take nr-of-tones (repeat 9)))]
    (->> (map
          (fn [index-tone octave midi-pitch-nr]
            [{:index-tone index-tone :octave octave} midi-pitch-nr])
          (take (count octaves) (cycle (all-tones)))
          octaves
          (range 24 (+ (count octaves) 24)))
         (into {}))))

(defn ->index-tone-with-octave->midi-pitch
  "Map index tone and octave to midi pitch"
  [index-tone octave]
  (get index-tone-with-octave->midi-pitch {:index-tone index-tone :octave octave}))

(comment
  (->midi-pitch->index-tone-with-octave 71)  ;; => {:index-tone #{:b}, :octave 4}
  (->midi-pitch->index-tone-with-octave 21)  ;; => {:index-tone #{:a}, :octave 0}
  (->midi-pitch->index-tone-with-octave 111) ;; => {:index-tone #{:d# :eb}, :octave 8}

  (->index-tone-with-octave->midi-pitch #{:g# :ab} 4) ;; => 68
  (->index-tone-with-octave->midi-pitch #{:c} 1)      ;; => 24
  (->index-tone-with-octave->midi-pitch #{:d# :eb} 7) ;; => 99






  :-)
