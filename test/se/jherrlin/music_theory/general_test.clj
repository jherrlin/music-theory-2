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


(deftest tones-on-indexes-with-intervals
  (is
   (=
    (general/tones-on-indexes-with-intervals
     [0 3 7]
     ["1" "b3" "5"])
    [:c :eb :g]))

  (is
   (=
    (general/tones-on-indexes-with-intervals
     [0 4 7]
     ["1" "3" "5"])
    [:c :e :g])))

(deftest tones-by-indexes
  (is
   (=
    (general/tones-by-indexes
     (general/all-tones)
     [0 4 7])
    [#{:c} #{:e} #{:g}]))

  (is
   (=
    (general/tones-by-indexes
     (general/all-tones)
     [0 3 7])
    [#{:c} #{:d# :eb} #{:g}])))

(deftest tones-by-key-and-indexes
  (is
   (=
    (general/tones-by-key-and-indexes
     (general/all-tones)
     :d
     [0 2 4 5 7 9 11])
    [#{:d} #{:e} #{:gb :f#} #{:g} #{:a} #{:b} #{:db :c#}]))

  (is
   (=
    (general/tones-by-key-and-indexes
     #{:c}
     [0 4 7])
    [#{:c} #{:e} #{:g}])))

(deftest tones-by-intervals
  (is
   (=
    (general/tones-by-intervals
     (general/all-tones)
     ["1" "3" "5"])
    [:c :e :g]))

  (is
   (=
    (general/tones-by-intervals
     ["1" "b3" "5"])
    [:c :eb :g])))

(deftest tones-by-key-and-intervals
  (is
   (=
    (general/tones-by-key-and-intervals
     (general/all-tones)
     :c
     ["1" "b3" "5"])
    [:c :eb :g]))

  (is
   (=
    (general/tones-by-key-and-intervals
     :c
     ["1" "2" "3" "4" "5" "6" "7"])
    [:c :d :e :f :g :a :b])))

(deftest intervals-to-indexes
  (is
   (=
    (general/intervals-to-indexes ["1" "b3" "5"])
    [0 3 7]))

  (is
   (=
    (general/intervals-to-indexes ["1" "3" "5"])
    [0 4 7])))

(deftest index-tones
  (is
   (=
    (general/index-tones :c [0 1 2])
    [#{:c} #{:db :c#} #{:d}]))

  (is
   (=
    (general/index-tones #{:c} [0 1 2])
    [#{:c} #{:db :c#} #{:d}])))

(deftest interval-tones
  (is
   (=
    (general/interval-tones (general/all-tones) :c ["1" "b3" "5"])
    [:c :eb :g]))

  (is
   (=
    (general/interval-tones #{:c} ["1" "b3" "5"])
    [:c :eb :g])))

(deftest tones-data-from-indexes-and-intervals
  (is
   (=
    (general/tones-data-from-indexes-and-intervals
     (general/all-tones)
     [0 3 7]
     ["1" "b3" "5"])
    [{:semitones 0, :function "1", :name/en "Root", :name/sv "Root", :index-tone #{:c}, :interval-tone :c, :interval "1", :index 0}
     {:index-tone #{:d# :eb}, :interval-tone :eb, :text/en "Blue note", :index 3, :name/en "Minor third", :function "b3", :name/sv "Moll-ters", :semitones 3, :interval "b3"}
     {:semitones 7, :function "5", :name/en "Perfect fifth", :name/sv "Kvint", :index-tone #{:g}, :interval-tone :g, :interval "5", :index 7}])))

(deftest tones-data-from-key-of-and-intervals
  (is
   (=
    (general/tones-data-from-key-of-and-intervals
     (general/all-tones)
     :c
     ["1" "b3" "5"])
    [{:semitones 0, :function "1", :name/en "Root", :name/sv "Root", :index-tone #{:c}, :interval-tone :c, :interval "1", :index 0}
     {:index-tone #{:d# :eb}, :interval-tone :eb, :text/en "Blue note", :index 3, :name/en "Minor third", :function "b3", :name/sv "Moll-ters", :semitones 3, :interval "b3"}
     {:semitones 7, :function "5", :name/en "Perfect fifth", :name/sv "Kvint", :index-tone #{:g}, :interval-tone :g, :interval "5", :index 7}]))

  (is
   (=
    (general/tones-data-from-key-of-and-intervals
     :c
     ["1" "3" "5"])
    [{:semitones 0, :function "1", :name/en "Root", :name/sv "Root", :index-tone #{:c}, :interval-tone :c, :interval "1", :index 0}
     {:semitones 4, :function "3", :name/en "Major third", :name/sv "Dur-ters", :index-tone #{:e}, :interval-tone :e, :interval "3", :index 4}
     {:semitones 7, :function "5", :name/en "Perfect fifth", :name/sv "Kvint", :index-tone #{:g}, :interval-tone :g, :interval "5", :index 7}])))


(deftest match-chord-with-scales
  (is
   (=
    (general/match-chord-with-scales
     [#:scale{:id        :ionian,
              :intervals ["1" "2" "3" "4" "5" "6" "7"],
              :indexes   [0 2 4 5 7 9 11],
              :title     "ionian"}]
     [0 4 7])
    [#:scale{:id :ionian, :intervals ["1" "2" "3" "4" "5" "6" "7"], :indexes [0 2 4 5 7 9 11], :title "ionian"}])))

(deftest find-chords
  (is
   (=
    (general/find-chords
     (general/all-tones)
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
     [#{:c} #{:e} #{:g}])
    [#:chord{:id :major, :intervals ["1" "3" "5"], :indexes [0 4 7], :title "major", :order 1, :sufix "", :explanation "major", :display-text "major"}]))

  (is
   (=
    (general/find-chords
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
     [:c :eb :g])
    [#:chord{:id :minor, :intervals ["1" "b3" "5"], :indexes [0 3 7], :title "minor", :order 2, :sufix "m", :explanation "minor", :display-text "minor"}])))

(deftest find-chords
  (is
   (=
    (general/find-chords
     (general/all-tones)
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

     [#{:c} #{:e} #{:g}])
    [#:chord{:id           :major,
             :intervals    ["1" "3" "5"],
             :indexes      [0 4 7],
             :title        "major",
             :order        1,
             :sufix        "",
             :explanation  "major",
             :display-text "major"}]))

  (is
   (=
    (general/find-chords
     (general/all-tones)
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
     [:c :eb :g])
    [#:chord{:id           :minor,
             :intervals    ["1" "b3" "5"],
             :indexes      [0 3 7],
             :title        "minor",
             :order        2,
             :sufix        "m",
             :explanation  "minor",
             :display-text "minor"}])))

(deftest chord-name
  (is
   (=
    (general/chord-name
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
     [:c :eb :g])
    "Cm"))

  (is
   (=
    (general/chord-name
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
     [:c :e :g])
    "C")))



(deftest scales-to-chord
  (is
   (=
    (general/scales-to-chord
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
    [{:id                #uuid "39af7096-b5c6-45e9-b743-6791b217a3df",
      :type              [:scale],
      :scale/scale-names #{:ionian :major},
      :scale/intervals   ["1" "2" "3" "4" "5" "6" "7"],
      :scale/indexes     [0 2 4 5 7 9 11],
      :scale/categories  #{:major},
      :scale/order       1,
      :scale             :ionian}]))

  (is
   (=
    (general/scales-to-chord
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
     ["1" "b3" "5"])
    [{:id                #uuid "d091b747-63b9-4db2-9daa-6e9974852080",
      :type              [:scale],
      :scale/scale-names #{:natural-minor :minor :aeolian},
      :scale/intervals   ["1" "2" "b3" "4" "5" "b6" "b7"],
      :scale/indexes     [0 2 3 5 7 8 10],
      :scale/categories  #{:minor},
      :scale/order       2,
      :scale             :minor}])))


(deftest chords-to-scale
  (is
   (=
    (general/chords-to-scale
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
      :chord/indexes        [0 3 7]}]))

  (is
   (=
    (general/chords-to-scale
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
     ["1" "2" "3" "4" "5" "6" "7"])
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
      :chord/indexes        [0 4 7]}])))
