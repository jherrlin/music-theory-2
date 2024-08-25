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
  )

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
   (->> all-tones
        (utils/rotate-until
         #(if (models.tone/valid-index-tone? x)
            (= % x)
            (% x))))))

(comment
  (tones-starting-at (all-tones) :c)
  (tones-starting-at :c)
  (tones-starting-at (all-tones) #{:c})
  (tones-starting-at (all-tones) :d#)
  )

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

(comment
  (sharp #{:db :c#}) ;; => :db
  (sharp #{:db})     ;; => :db
  (sharp #{:c#})     ;; => :c#
  )

(defn flat [index-tone]
  {:pre [(models.tone/valid-index-tone? index-tone)]}
  (sharp-or-flat index-tone "#"))

(comment
  (flat #{:db :c#}) ;; => :c#
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
  )

;; => [:c :eb :g]

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
        (mapv (fn [interval]
                (let [index (intervals/interval->index interval)]
                  (sharp-or-flat (nth all-tones index) interval)))))))

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
       (mapv intervals/interval->index)))

(comment
  (intervals-to-indexes ["1" "b3" "5"])
  ;; => [0 3 7]

  (intervals-to-indexes ["1" "3" "5"])
  ;; => [0 4 7]
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
  [{:semitones 0, :function "1", :name/en "Root", :name/sv "Root", :index-tone #{:c}, :interval-tone :c, :interval "1", :index 0}
   {:index-tone #{:d# :eb}, :interval-tone :eb, :text/en "Blue note", :index 3, :name/en "Minor third", :function "b3", :name/sv "Moll-ters", :semitones 3, :interval "b3"}
   {:semitones 7, :function "5", :name/en "Perfect fifth", :name/sv "Kvint", :index-tone #{:g}, :interval-tone :g, :interval "5", :index 7}] nil)


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
  ;; =>
  [{:semitones 0, :function "1", :name/en "Root", :name/sv "Root", :index-tone #{:c}, :interval-tone :c, :interval "1", :index 0}
   {:index-tone #{:d# :eb}, :interval-tone :eb, :text/en "Blue note", :index 3, :name/en "Minor third", :function "b3", :name/sv "Moll-ters", :semitones 3, :interval "b3"}
   {:semitones 7, :function "5", :name/en "Perfect fifth", :name/sv "Kvint", :index-tone #{:g}, :interval-tone :g, :interval "5", :index 7}]
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
                   (set/subset? (set chord-indexes) (set scale-indexes)))))
       (into [])))

(comment
  (match-chord-with-scales
   [#:scale{:id        :ionian,
            :intervals ["1" "2" "3" "4" "5" "6" "7"],
            :indexes   [0 2 4 5 7 9 11],
            :title     "ionian"}]
   [0 4 7])
  ;; =>
  [#:scale{:id :ionian, :intervals ["1" "2" "3" "4" "5" "6" "7"], :indexes [0 2 4 5 7 9 11], :title "ionian"}]
  )


(defn find-chords
  "Find chords from interval or index tones."
  ([chord-maps chord-tones]
   (find-chords (all-tones) chord-maps chord-tones))
  ([all-tones chord-maps chord-tones]
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
                                (every? true?))))))
          (into [])))))

(comment
  (find-chords
   (all-tones)
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

   [#{:c} #{:e} #{:g}]
   #_[#{:c} #{:d# :eb} #{:g}]
   #_[:c :e :g])
  ;; =>
  [#:chord{:id           :major,
           :intervals    ["1" "3" "5"],
           :indexes      [0 4 7],
           :title        "major",
           :order        1,
           :sufix        "",
           :explanation  "major",
           :display-text "major"}]
  )



(defn find-chord
  ([chord-maps chord-tones]
   (find-chord (all-tones) chord-maps chord-tones))
  ([all-tones chord-maps chord-tones]
   {:pre [(coll? chord-maps)]}
   (->> (find-chords all-tones chord-maps chord-tones)
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
        {:chord/keys [sufix]} (find-chord (all-tones) chord-maps chord-tones)]
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
   [:c :eb :g]))

(defn scales-to-chord [scales chord-intervals]
  (->> scales
       (map (juxt :scale/scale-names identity))
       (into {})
       (vals)
       (filter
        (fn [{scale-intervals :scale/intervals}]
          (set/subset? (set chord-intervals) (set scale-intervals))))
       (into [])))

(comment
  (scales-to-chord
   [{:id                #uuid "39af7096-b5c6-45e9-b743-6791b217a3df",
     :type              [:scale],
     :scale/scale-names #{:ionian :major},
     :scale/intervals   ["1" "2" "3" "4" "5" "6" "7"],
     :scale/indexes     [0 2 4 5 7 9 11],
     :scale/categories  #{:major},
     :scale/order       1,
     :scale             :ionian}
    {:id                #uuid "d091b747-63b9-4db2-9daa-6e9974852080",
     :type              [:scale],
     :scale/scale-names #{:natural-minor :minor :aeolian},
     :scale/intervals   ["1" "2" "b3" "4" "5" "b6" "b7"],
     :scale/indexes     [0 2 3 5 7 8 10],
     :scale/categories  #{:minor},
     :scale/order       2,
     :scale             :minor}]
   ["1" "3" "5"])
  ;; =>
  [{:id                #uuid "39af7096-b5c6-45e9-b743-6791b217a3df",
    :type              [:scale],
    :scale/scale-names #{:ionian :major},
    :scale/intervals   ["1" "2" "3" "4" "5" "6" "7"],
    :scale/indexes     [0 2 4 5 7 9 11],
    :scale/categories  #{:major},
    :scale/order       1,
    :scale             :ionian}]
  )


(defn chords-to-scale [chords scale-intervals]
  (->> chords
       (filter
        (fn [{chord-intervals :chord/intervals}]
          (set/subset? (set chord-intervals) (set scale-intervals))))
       (into [])))

(comment
  (chords-to-scale
   [{:chord/intervals      ["1" "3" "5"],
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
     :chord/indexes        [0 4 7]}
    {:chord/intervals      ["1" "b3" "5"],
     :chord/chord-name-str "minor",
     :chord/chord-name     :minor,
     :chord/order          2,
     :type                 [:chord],
     :chord/categories     #{:minor},
     :chord/display-text   "minor",
     :id                   #uuid "f9426eb8-5046-474a-b4c9-62383e5b0345",
     :chord/intervals-str  "1, b3, 5",
     :chord/explanation    "minor",
     :chord/suffix         "m",
     :chord/indexes        [0 3 7]}]
   ["1" "2" "b3" "4" "5" "b6" "b7"])
  ;; =>
  [{:chord/intervals      ["1" "b3" "5"],
    :chord/chord-name-str "minor",
    :chord/chord-name     :minor,
    :chord/order          2,
    :type                 [:chord],
    :chord/categories     #{:minor},
    :chord/display-text   "minor",
    :id                   #uuid "f9426eb8-5046-474a-b4c9-62383e5b0345",
    :chord/intervals-str  "1, b3, 5",
    :chord/explanation    "minor",
    :chord/suffix         "m",
    :chord/indexes        [0 3 7]}]
  )

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
