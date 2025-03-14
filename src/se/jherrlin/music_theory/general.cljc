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
  {121 {:index-tone #{:db :c#}, :octave 9},
   65  {:index-tone #{:f}, :octave 4},
   70  {:index-tone #{:bb :a#}, :octave 4},
   62  {:index-tone #{:d}, :octave 4},
   74  {:index-tone #{:d}, :octave 5},
   110 {:index-tone #{:d}, :octave 8},
   130 {:index-tone #{:bb :a#}, :octave 9},
   128 {:index-tone #{:g# :ab}, :octave 9},
   59  {:index-tone #{:b}, :octave 3},
   86  {:index-tone #{:d}, :octave 6},
   20  {:index-tone #{:g# :ab}, :octave 0},
   72  {:index-tone #{:c}, :octave 5},
   58  {:index-tone #{:bb :a#}, :octave 3},
   60  {:index-tone #{:c}, :octave 4},
   27  {:index-tone #{:d# :eb}, :octave 1},
   69  {:index-tone #{:a}, :octave 4},
   101 {:index-tone #{:f}, :octave 7},
   24  {:index-tone #{:c}, :octave 1},
   102 {:index-tone #{:gb :f#}, :octave 7},
   55  {:index-tone #{:g}, :octave 3},
   85  {:index-tone #{:db :c#}, :octave 6},
   39  {:index-tone #{:d# :eb}, :octave 2},
   88  {:index-tone #{:e}, :octave 6},
   46  {:index-tone #{:bb :a#}, :octave 2},
   77  {:index-tone #{:f}, :octave 5},
   106 {:index-tone #{:bb :a#}, :octave 7},
   119 {:index-tone #{:b}, :octave 8},
   95  {:index-tone #{:b}, :octave 6},
   54  {:index-tone #{:gb :f#}, :octave 3},
   92  {:index-tone #{:g# :ab}, :octave 6},
   104 {:index-tone #{:g# :ab}, :octave 7},
   15  {:index-tone #{:d# :eb}, :octave 0},
   48  {:index-tone #{:c}, :octave 3},
   50  {:index-tone #{:d}, :octave 3},
   116 {:index-tone #{:g# :ab}, :octave 8},
   75  {:index-tone #{:d# :eb}, :octave 5},
   99  {:index-tone #{:d# :eb}, :octave 7},
   21  {:index-tone #{:a}, :octave 0},
   31  {:index-tone #{:g}, :octave 1},
   113 {:index-tone #{:f}, :octave 8},
   32  {:index-tone #{:g# :ab}, :octave 1},
   40  {:index-tone #{:e}, :octave 2},
   129 {:index-tone #{:a}, :octave 9},
   91  {:index-tone #{:g}, :octave 6},
   117 {:index-tone #{:a}, :octave 8},
   108 {:index-tone #{:c}, :octave 8},
   56  {:index-tone #{:g# :ab}, :octave 3},
   33  {:index-tone #{:a}, :octave 1},
   13  {:index-tone #{:db :c#}, :octave 0},
   22  {:index-tone #{:bb :a#}, :octave 0},
   90  {:index-tone #{:gb :f#}, :octave 6},
   109 {:index-tone #{:db :c#}, :octave 8},
   36  {:index-tone #{:c}, :octave 2},
   41  {:index-tone #{:f}, :octave 2},
   118 {:index-tone #{:bb :a#}, :octave 8},
   89  {:index-tone #{:f}, :octave 6},
   100 {:index-tone #{:e}, :octave 7},
   131 {:index-tone #{:b}, :octave 9},
   122 {:index-tone #{:d}, :octave 9},
   43  {:index-tone #{:g}, :octave 2},
   61  {:index-tone #{:db :c#}, :octave 4},
   29  {:index-tone #{:f}, :octave 1},
   44  {:index-tone #{:g# :ab}, :octave 2},
   93  {:index-tone #{:a}, :octave 6},
   111 {:index-tone #{:d# :eb}, :octave 8},
   28  {:index-tone #{:e}, :octave 1},
   64  {:index-tone #{:e}, :octave 4},
   103 {:index-tone #{:g}, :octave 7},
   51  {:index-tone #{:d# :eb}, :octave 3},
   25  {:index-tone #{:db :c#}, :octave 1},
   34  {:index-tone #{:bb :a#}, :octave 1},
   125 {:index-tone #{:f}, :octave 9},
   17  {:index-tone #{:f}, :octave 0},
   12  {:index-tone #{:c}, :octave 0},
   66  {:index-tone #{:gb :f#}, :octave 4},
   107 {:index-tone #{:b}, :octave 7},
   23  {:index-tone #{:b}, :octave 0},
   47  {:index-tone #{:b}, :octave 2},
   35  {:index-tone #{:b}, :octave 1},
   127 {:index-tone #{:g}, :octave 9},
   82  {:index-tone #{:bb :a#}, :octave 5},
   76  {:index-tone #{:e}, :octave 5},
   97  {:index-tone #{:db :c#}, :octave 7},
   19  {:index-tone #{:g}, :octave 0},
   57  {:index-tone #{:a}, :octave 3},
   68  {:index-tone #{:g# :ab}, :octave 4},
   115 {:index-tone #{:g}, :octave 8},
   112 {:index-tone #{:e}, :octave 8},
   83  {:index-tone #{:b}, :octave 5},
   14  {:index-tone #{:d}, :octave 0},
   45  {:index-tone #{:a}, :octave 2},
   53  {:index-tone #{:f}, :octave 3},
   78  {:index-tone #{:gb :f#}, :octave 5},
   26  {:index-tone #{:d}, :octave 1},
   123 {:index-tone #{:d# :eb}, :octave 9},
   16  {:index-tone #{:e}, :octave 0},
   81  {:index-tone #{:a}, :octave 5},
   120 {:index-tone #{:c}, :octave 9},
   79  {:index-tone #{:g}, :octave 5},
   38  {:index-tone #{:d}, :octave 2},
   126 {:index-tone #{:gb :f#}, :octave 9},
   98  {:index-tone #{:d}, :octave 7},
   124 {:index-tone #{:e}, :octave 9},
   87  {:index-tone #{:d# :eb}, :octave 6},
   30  {:index-tone #{:gb :f#}, :octave 1},
   73  {:index-tone #{:db :c#}, :octave 5},
   96  {:index-tone #{:c}, :octave 7},
   18  {:index-tone #{:gb :f#}, :octave 0},
   105 {:index-tone #{:a}, :octave 7},
   52  {:index-tone #{:e}, :octave 3},
   114 {:index-tone #{:gb :f#}, :octave 8},
   67  {:index-tone #{:g}, :octave 4},
   71  {:index-tone #{:b}, :octave 4},
   42  {:index-tone #{:gb :f#}, :octave 2},
   80  {:index-tone #{:g# :ab}, :octave 5},
   37  {:index-tone #{:db :c#}, :octave 2},
   63  {:index-tone #{:d# :eb}, :octave 4},
   94  {:index-tone #{:bb :a#}, :octave 6},
   49  {:index-tone #{:db :c#}, :octave 3},
   84  {:index-tone #{:c}, :octave 6}})

(defn ->midi-pitch->index-tone-with-octave
  "Map midi pitch to index tone and octave"
  [midi-pitch]
  (midi-pitch->index-tone-with-octave midi-pitch))

(def index-tone-with-octave->midi-pitch
  {{:index-tone #{:d# :eb}, :octave 7} 99,
   {:index-tone #{:d}, :octave 3}      50,
   {:index-tone #{:g}, :octave 0}      19,
   {:index-tone #{:g# :ab}, :octave 0} 20,
   {:index-tone #{:db :c#}, :octave 5} 73,
   {:index-tone #{:g# :ab}, :octave 4} 68,
   {:index-tone #{:f}, :octave 7}      101,
   {:index-tone #{:g}, :octave 5}      79,
   {:index-tone #{:d# :eb}, :octave 9} 123,
   {:index-tone #{:gb :f#}, :octave 1} 30,
   {:index-tone #{:e}, :octave 1}      28,
   {:index-tone #{:b}, :octave 1}      35,
   {:index-tone #{:g}, :octave 3}      55,
   {:index-tone #{:g}, :octave 7}      103,
   {:index-tone #{:c}, :octave 8}      108,
   {:index-tone #{:f}, :octave 9}      125,
   {:index-tone #{:gb :f#}, :octave 5} 78,
   {:index-tone #{:b}, :octave 9}      131,
   {:index-tone #{:d}, :octave 0}      14,
   {:index-tone #{:db :c#}, :octave 9} 121,
   {:index-tone #{:b}, :octave 6}      95,
   {:index-tone #{:d# :eb}, :octave 3} 51,
   {:index-tone #{:g}, :octave 4}      67,
   {:index-tone #{:gb :f#}, :octave 2} 42,
   {:index-tone #{:d# :eb}, :octave 2} 39,
   {:index-tone #{:f}, :octave 8}      113,
   {:index-tone #{:gb :f#}, :octave 4} 66,
   {:index-tone #{:bb :a#}, :octave 9} 130,
   {:index-tone #{:f}, :octave 6}      89,
   {:index-tone #{:g# :ab}, :octave 9} 128,
   {:index-tone #{:db :c#}, :octave 6} 85,
   {:index-tone #{:bb :a#}, :octave 8} 118,
   {:index-tone #{:e}, :octave 4}      64,
   {:index-tone #{:a}, :octave 5}      81,
   {:index-tone #{:d}, :octave 5}      74,
   {:index-tone #{:db :c#}, :octave 8} 109,
   {:index-tone #{:bb :a#}, :octave 0} 22,
   {:index-tone #{:d# :eb}, :octave 8} 111,
   {:index-tone #{:db :c#}, :octave 4} 61,
   {:index-tone #{:b}, :octave 8}      119,
   {:index-tone #{:b}, :octave 0}      23,
   {:index-tone #{:c}, :octave 9}      120,
   {:index-tone #{:b}, :octave 2}      47,
   {:index-tone #{:g# :ab}, :octave 2} 44,
   {:index-tone #{:b}, :octave 7}      107,
   {:index-tone #{:d}, :octave 6}      86,
   {:index-tone #{:gb :f#}, :octave 3} 54,
   {:index-tone #{:bb :a#}, :octave 7} 106,
   {:index-tone #{:gb :f#}, :octave 6} 90,
   {:index-tone #{:bb :a#}, :octave 2} 46,
   {:index-tone #{:db :c#}, :octave 7} 97,
   {:index-tone #{:bb :a#}, :octave 4} 70,
   {:index-tone #{:g# :ab}, :octave 1} 32,
   {:index-tone #{:c}, :octave 4}      60,
   {:index-tone #{:a}, :octave 6}      93,
   {:index-tone #{:g# :ab}, :octave 6} 92,
   {:index-tone #{:d# :eb}, :octave 4} 63,
   {:index-tone #{:f}, :octave 5}      77,
   {:index-tone #{:e}, :octave 9}      124,
   {:index-tone #{:bb :a#}, :octave 3} 58,
   {:index-tone #{:f}, :octave 3}      53,
   {:index-tone #{:d}, :octave 9}      122,
   {:index-tone #{:g# :ab}, :octave 8} 116,
   {:index-tone #{:a}, :octave 9}      129,
   {:index-tone #{:d# :eb}, :octave 6} 87,
   {:index-tone #{:bb :a#}, :octave 1} 34,
   {:index-tone #{:b}, :octave 3}      59,
   {:index-tone #{:a}, :octave 2}      45,
   {:index-tone #{:bb :a#}, :octave 5} 82,
   {:index-tone #{:a}, :octave 8}      117,
   {:index-tone #{:a}, :octave 4}      69,
   {:index-tone #{:c}, :octave 6}      84,
   {:index-tone #{:b}, :octave 4}      71,
   {:index-tone #{:c}, :octave 2}      36,
   {:index-tone #{:gb :f#}, :octave 7} 102,
   {:index-tone #{:e}, :octave 6}      88,
   {:index-tone #{:gb :f#}, :octave 9} 126,
   {:index-tone #{:a}, :octave 1}      33,
   {:index-tone #{:c}, :octave 0}      12,
   {:index-tone #{:c}, :octave 3}      48,
   {:index-tone #{:a}, :octave 0}      21,
   {:index-tone #{:g}, :octave 1}      31,
   {:index-tone #{:g}, :octave 2}      43,
   {:index-tone #{:d}, :octave 1}      26,
   {:index-tone #{:d}, :octave 4}      62,
   {:index-tone #{:e}, :octave 8}      112,
   {:index-tone #{:f}, :octave 2}      41,
   {:index-tone #{:db :c#}, :octave 3} 49,
   {:index-tone #{:e}, :octave 2}      40,
   {:index-tone #{:g}, :octave 6}      91,
   {:index-tone #{:b}, :octave 5}      83,
   {:index-tone #{:c}, :octave 1}      24,
   {:index-tone #{:d}, :octave 2}      38,
   {:index-tone #{:g# :ab}, :octave 5} 80,
   {:index-tone #{:d# :eb}, :octave 1} 27,
   {:index-tone #{:e}, :octave 5}      76,
   {:index-tone #{:a}, :octave 3}      57,
   {:index-tone #{:g# :ab}, :octave 7} 104,
   {:index-tone #{:g}, :octave 8}      115,
   {:index-tone #{:d# :eb}, :octave 5} 75,
   {:index-tone #{:db :c#}, :octave 0} 13,
   {:index-tone #{:a}, :octave 7}      105,
   {:index-tone #{:d# :eb}, :octave 0} 15,
   {:index-tone #{:e}, :octave 0}      16,
   {:index-tone #{:gb :f#}, :octave 0} 18,
   {:index-tone #{:e}, :octave 7}      100,
   {:index-tone #{:g}, :octave 9}      127,
   {:index-tone #{:c}, :octave 5}      72,
   {:index-tone #{:db :c#}, :octave 1} 25,
   {:index-tone #{:gb :f#}, :octave 8} 114,
   {:index-tone #{:db :c#}, :octave 2} 37,
   {:index-tone #{:d}, :octave 7}      98,
   {:index-tone #{:f}, :octave 1}      29,
   {:index-tone #{:g# :ab}, :octave 3} 56,
   {:index-tone #{:e}, :octave 3}      52,
   {:index-tone #{:bb :a#}, :octave 6} 94,
   {:index-tone #{:c}, :octave 7}      96,
   {:index-tone #{:d}, :octave 8}      110,
   {:index-tone #{:f}, :octave 0}      17,
   {:index-tone #{:f}, :octave 4}      65})

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
