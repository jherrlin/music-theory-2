(ns se.jherrlin.music-theory.fretboard
  "Ns with functions related to fretboard logic.

  Important terminology:
  - Index tones are sets:        #{:db :c#}
  - Interval tones are keywords: :c#"
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [se.jherrlin.music-theory.general :as general]
   [se.jherrlin.music-theory.intervals :as intervals]
   [se.jherrlin.music-theory.models.tone :as models.tone]
   [se.jherrlin.utils :as utils]
   [se.jherrlin.music-theory.definitions.tone-values :as tone-values]
   [se.jherrlin.music-theory.models.fretboard-matrix :as models.fretboard-matrix]))


(defn fretboard-string
  "Generate a freatboard string.

  `all-tones`        - A collection of all the tones
  `tuning`           - Tuning on string.
  `octave`           - What octave the string starts on.
  `number-of-frets`  - Width of the freatboard."
  ([m number-of-frets]
   (fretboard-string (general/all-tones) m number-of-frets))
  ([all-tones {:keys [tone octave start-index] :or {start-index 0}} number-of-frets]
   {:pre [(models.tone/valid-index-tones? all-tones)
          (models.tone/valid-interval-tone? tone)
          (number? octave)
          (number? number-of-frets)]}
   (let [string-without-octave-data (mapv
                                     (fn [x t]
                                       {:x    x
                                        :tone t})
                                     (iterate inc (if (= start-index 0) 0 start-index))
                                     (->> (general/tones-starting-at all-tones tone)
                                          (cycle)
                                          (take (inc number-of-frets))))]
     (loop [[{tone :tone :as fret} & rest] string-without-octave-data
            octave'                        octave
            new-vec                        []]
       (if (nil? rest)
         (if (= start-index 0)
           new-vec
           ;; For example banjo that starts with a couple of blanks
           (->> (concat
                 (take start-index (map
                                    #(hash-map :x %1 :blank? true)
                                    (iterate inc 0)))
                 new-vec)
                (take number-of-frets)
                (vec)))
         (let [o (if (and (tone :c) (seq new-vec)) (inc octave') octave')]
           (recur
            rest
            o
            (conj new-vec (assoc fret :octave o)))))))))

(defn fretboard-strings
  "Generate fretboard matrix.

  `all-tones`        - A collection of all the tones
  `tunings`          - Tuning on strings
  `number-of-frets`  - Width of the freatboard"
  ([string-tunes number-of-frets]
   (fretboard-strings (general/all-tones) string-tunes number-of-frets))
  ([all-tones string-tunes number-of-frets]
   {:pre [(models.tone/valid-index-tones? all-tones)
          (models.tone/valid-interval-tones-with-octave? string-tunes)]}
   (->> string-tunes
        (reverse)
        (mapv
         (fn [y string-tuning]
           (mapv
            (fn [{:keys [x] :as m}]
              (assoc m :y y :yx (+ (* 100 y) x)))
            (fretboard-string all-tones string-tuning number-of-frets)))
         (iterate inc 0)))))

(defn intevals-string->intervals-matrix
  "Read a string into interval matrix.

  (intevals-string->intervals-matrix
   \"   3   -   -
   -   bb1   -
   5   -   -
   -   -   -
   -   -   -
   -   -   -\")
  =>
  [[\"3\" nil nil]
   [nil \"bb1\" nil]
   [\"5\" nil nil]
   [nil nil nil]
   [nil nil nil]
   [nil nil nil]]"
  [interval]
  (->> interval
       (str/trim)
       (str/split-lines)
       (mapv (fn [line]
               (->> line
                    str/trim
                    (re-seq #"(b{0,2}#{0,2}\d{1,2})|-")
                    (mapv second))))))

(defn frets-to-matrix
  [frets]
  {:pre [(utils/validate models.fretboard-matrix/Frets frets)]}
  (->> frets
       (sort-by :y)
       (partition-by :y)
       (mapv (fn [coll]
               (vec (sort-by :x coll))))))

(defn fretboard-map-to-matrix
  "A fretboard `fretboard-map` can be represented as a hashmap,
  transform that into a matrix."
  [fretboard-map]
  (->> fretboard-map
       vals
       frets-to-matrix))

(defn fretboard-matrix-to-map
  "A fretboard `fretboard-matrix` can be represented as a matrix,
  transform that into a hashmap."
  [fretboard-matrix]
  (->> fretboard-matrix
       (apply concat)
       (reduce (fn [acc {:keys [x y] :as m}]
                 (assoc acc [x y] m))
               {})))

(defn filter-matches
  "Filter out `match?` into seq."
  [fretboard-matrix]
  (->> fretboard-matrix
       (mapv reverse)
       (apply concat)
       (reverse)
       (filter :match?)
       (vec)))

(defn fretboard-matrix->tonejs-dispatches
  "Returns a seq of dispatches the can be feed into re-frames `:fx`"
  [fretboard-matrix]
  (let [tones-to-play (->> fretboard-matrix
                           filter-matches
                           (mapv (fn [{:keys [x y octave tone-str]}]
                                   {:x x :y y :octave octave :tone tone-str})))
        inc-with      500]
    (loop [[t & tones] (rest tones-to-play)
           acc         [[:dispatch [:tonejs/play-tone (first tones-to-play)]]]
           counter     inc-with]
      (if (nil? t)
        acc
        (recur
         tones
         (conj acc [:dispatch-later {:ms counter :dispatch [:tonejs/play-tone t]}])
         (+ counter inc-with))))))

(defn fretboard-matrix->tonejs-dispatches-2
  "Returns a seq of dispatches the can be feed into re-frames `:fx`"
  [entity fretboard-matrix]
  (let [tones-to-play (->> fretboard-matrix
                           filter-matches
                           (mapv (fn [{:keys [x y octave tone-str]}]
                                   {:x x :y y :octave octave :tone tone-str})))
        inc-with      500]
    (loop [[{:keys [x y] :as t} & tones] (concat (rest tones-to-play) (rest (reverse tones-to-play)))
           acc                           [[:dispatch [:tonejs/play-tone (first tones-to-play)]]
                                          [:dispatch [:se.jherrlin.music-theory.webapp.views.scale/highlight
                                                      {:entity     entity
                                                       :x          (-> tones-to-play first :x)
                                                       :y          (-> tones-to-play first :y)
                                                       :highlight? true}]]
                                          [:dispatch-later {:ms inc-with
                                                            :dispatch
                                                            [:se.jherrlin.music-theory.webapp.views.scale/highlight
                                                             {:entity     entity
                                                              :x          (-> tones-to-play first :x)
                                                              :y          (-> tones-to-play first :y)
                                                              :highlight? false}]}]]
           counter                       inc-with]
      (if (nil? t)
        acc
        (recur
         tones
         (-> acc
             (conj [:dispatch-later {:ms counter :dispatch [:tonejs/play-tone t]}])
             (conj [:dispatch-later {:ms counter
                                     :dispatch
                                     [:se.jherrlin.music-theory.webapp.views.scale/highlight
                                      {:entity     entity
                                       :x          x
                                       :y          y
                                       :highlight? true}]}])
             (conj [:dispatch-later {:ms (+ counter inc-with)
                                     :dispatch
                                     [:se.jherrlin.music-theory.webapp.views.scale/highlight
                                      {:entity     entity
                                       :x          x
                                       :y          y
                                       :highlight? false}]}]))
         (+ counter inc-with))))))

(fretboard-matrix->tonejs-dispatches-2
 {:id         #uuid "9b716148-3c19-42a4-9583-07c8c1671b66",
  :instrument :mandolin,
  :key-of     :d}
 [[{:x 0, :tone #{:e}, :octave 5, :y 0, :yx 0, :interval "2", :tone-str "E"}
   {:x 1, :tone #{:f}, :octave 5, :y 0, :yx 1, :interval "b3", :tone-str "F"}
   {:y        0,
    :octave   5,
    :tone-str "Gb",
    :yx       2,
    :tone     #{:gb :f#},
    :flat     "Gb",
    :sharp    "F#",
    :x        2,
    :interval "3"}
   {:x 3, :tone #{:g}, :octave 5, :y 0, :yx 3, :interval "4", :tone-str "G"}
   {:y 0,
    :octave 5,
    :tone-str "Ab",
    :yx 4,
    :tone #{:g# :ab},
    :flat "Ab",
    :sharp "G#",
    :x 4,
    :interval "b5"}
   {:x 5, :tone #{:a}, :octave 5, :y 0, :yx 5, :interval "5", :tone-str "A"}
   {:y 0,
    :octave 5,
    :tone-str "Bb",
    :yx 6,
    :tone #{:bb :a#},
    :flat "Bb",
    :sharp "A#",
    :x 6,
    :interval "b6"}]
  [{:y 1,
    :octave 4,
    :pattern-found-tone "A",
    :tone-str "A",
    :yx 100,
    :pattern-found-interval "5",
    :tone #{:a},
    :out "5",
    :x 0,
    :interval "5",
    :match? true}
   {:y 1,
    :octave 4,
    :tone-str "Bb",
    :yx 101,
    :tone #{:bb :a#},
    :flat "Bb",
    :sharp "A#",
    :x 1,
    :interval "b6"}
   {:y 1,
    :octave 4,
    :pattern-found-tone "B",
    :tone-str "B",
    :yx 102,
    :pattern-found-interval "6",
    :tone #{:b},
    :out "6",
    :x 2,
    :interval "6",
    :match? true}
   {:x 3,
    :tone #{:c},
    :octave 5,
    :y 1,
    :yx 103,
    :interval "b7",
    :tone-str "C"}
   {:y 1,
    :octave 5,
    :tone-str "Db",
    :yx 104,
    :tone #{:db :c#},
    :flat "Db",
    :sharp "C#",
    :x 4,
    :interval "7"}
   {:y 1,
    :octave 5,
    :root? true,
    :pattern-found-tone "D",
    :tone-str "D",
    :yx 105,
    :pattern-found-interval "1",
    :tone #{:d},
    :out "1",
    :x 5,
    :interval "1",
    :match? true}
   {:y 1,
    :octave 5,
    :tone-str "Eb",
    :yx 106,
    :tone #{:d# :eb},
    :flat "Eb",
    :sharp "D#",
    :x 6,
    :interval "b2"}]
  [{:y 2,
    :octave 4,
    :root? true,
    :pattern-found-tone "D",
    :tone-str "D",
    :yx 200,
    :pattern-found-interval "1",
    :tone #{:d},
    :out "1",
    :x 0,
    :interval "1",
    :match? true}
   {:y 2,
    :octave 4,
    :tone-str "Eb",
    :yx 201,
    :tone #{:d# :eb},
    :flat "Eb",
    :sharp "D#",
    :x 1,
    :interval "b2"}
   {:y 2,
    :octave 4,
    :pattern-found-tone "E",
    :tone-str "E",
    :yx 202,
    :pattern-found-interval "2",
    :tone #{:e},
    :out "2",
    :x 2,
    :interval "2",
    :match? true}
   {:x 3,
    :tone #{:f},
    :octave 4,
    :y 2,
    :yx 203,
    :interval "b3",
    :tone-str "F"}
   {:y 2,
    :octave 4,
    :pattern-found-tone "F#",
    :tone-str "Gb",
    :yx 204,
    :pattern-found-interval "3",
    :tone #{:gb :f#},
    :out "3",
    :flat "Gb",
    :sharp "F#",
    :x 4,
    :interval "3",
    :match? true}
   {:x 5, :tone #{:g}, :octave 4, :y 2, :yx 205, :interval "4", :tone-str "G"}
   {:y 2,
    :octave 4,
    :tone-str "Ab",
    :yx 206,
    :tone #{:g# :ab},
    :flat "Ab",
    :sharp "G#",
    :x 6,
    :interval "b5"}]
  [{:x 0, :tone #{:g}, :octave 3, :y 3, :yx 300, :interval "4", :tone-str "G"}
   {:y 3,
    :octave 3,
    :tone-str "Ab",
    :yx 301,
    :tone #{:g# :ab},
    :flat "Ab",
    :sharp "G#",
    :x 1,
    :interval "b5"}
   {:x 2, :tone #{:a}, :octave 3, :y 3, :yx 302, :interval "5", :tone-str "A"}
   {:y 3,
    :octave 3,
    :tone-str "Bb",
    :yx 303,
    :tone #{:bb :a#},
    :flat "Bb",
    :sharp "A#",
    :x 3,
    :interval "b6"}
   {:x 4, :tone #{:b}, :octave 3, :y 3, :yx 304, :interval "6", :tone-str "B"}
   {:x 5,
    :tone #{:c},
    :octave 4,
    :y 3,
    :yx 305,
    :interval "b7",
    :tone-str "C"}
   {:y 3,
    :octave 4,
    :tone-str "Db",
    :yx 306,
    :tone #{:db :c#},
    :flat "Db",
    :sharp "C#",
    :x 6,
    :interval "7"}]])

;; TODO: test
(defn merge-fretboards-matrixes [& fretboards-matrixes]
  (->> (apply
        map
        (fn [& fretboard-matrixes]
          (apply merge fretboard-matrixes))
        (mapv (partial apply concat) fretboards-matrixes))
       frets-to-matrix))

(comment
  (let [fretboard [[{:x 0, :tone #{:e}, :octave 2, :y 0}
                    {:x 1, :tone #{:f}, :octave 2, :y 0}
                    {:x 2, :tone #{:gb :f#}, :octave 2, :y 0}
                    {:x 3, :tone #{:g}, :octave 2, :y 0}
                    {:x 4, :tone #{:g# :ab}, :octave 2, :y 0}]
                   [{:x 0, :tone #{:b}, :octave 3, :y 1}
                    {:x 1, :tone #{:c}, :octave 4, :y 1}
                    {:x 2, :tone #{:db :c#}, :octave 4, :y 1}
                    {:x 3, :tone #{:d}, :octave 4, :y 1}
                    {:x 4, :tone #{:d# :eb}, :octave 4, :y 1}]]]
    (=
     fretboard
     (->> (fretboard-matrix-to-map fretboard)
          (fretboard-map-to-matrix))))
  )

(defn find-fretboard-pattern
  "Find patterns on the fretboard"
  [all-tones key-of interval-matrix fretboard-matrix]
  (let [;; interval-matrix       (trim-matrix interval-matrix)
        interval-matrix-width (-> interval-matrix first count)
        fretboard-count       (-> fretboard-matrix first count)]
    (loop [counter           0
           found-pattern-xys #{}]
      (let [combinations-p
            (->>  fretboard-matrix
                  (mapv (comp vec (partial take interval-matrix-width) (partial drop counter))))
            combinations
            (->> combinations-p
                 (apply concat)
                 (mapv vector (apply concat interval-matrix)))
            box-match? (->> combinations
                            (remove (comp nil? first))
                            (every? (fn [[interval' {:keys [tone] :as tone'}]]
                                      (let [interval-semitones (get-in intervals/intervals-map-by-function [interval' :semitones])
                                            fretboard-tone     (nth
                                                                (general/tones-starting-at all-tones key-of)
                                                                interval-semitones)]
                                        (and
                                         (= (-> combinations-p first count)
                                            interval-matrix-width)
                                         (= tone fretboard-tone))))))
            pattern-check
            (->> combinations
                 (remove (comp nil? first))
                 (map (fn [[interval' {:keys [tone] :as tone'}]]
                        (let [interval-semitones (get-in intervals/intervals-map-by-function [interval' :semitones])
                              fretboard-tone     (nth
                                                  (general/tones-starting-at all-tones key-of)
                                                  interval-semitones)]
                          (assoc tone'
                                 :match? (and box-match? (= tone fretboard-tone))
                                 :pattern-found-tone (-> (general/sharp-or-flat tone interval')
                                                         (name)
                                                         (str/capitalize))
                                 :pattern-found-interval interval')))))
            found-pattern-xys'
            (if-not box-match?
              found-pattern-xys
              (->> pattern-check
                   (filter :match?)
                   (map #(select-keys % [:x :y :match? :pattern-found-tone :pattern-found-interval]))
                   (set)
                   (into found-pattern-xys)))]
        (if (< counter fretboard-count)
          (recur
           (inc counter)
           found-pattern-xys')
          (reduce
           (fn [fm {:keys [x y match? pattern-found-tone pattern-found-interval]}]
             (update-in fm [y x] assoc
                        :match? match?
                        :pattern-found-tone pattern-found-tone
                        :pattern-found-interval pattern-found-interval))
           fretboard-matrix
           found-pattern-xys'))))))

(find-fretboard-pattern
 (general/all-tones)
 :e
 [["1" nil nil]
  ["5" nil nil]
  ["b3" nil nil]
  [nil nil "1"]
  [nil nil "5"]
  ["1" nil nil]]
 (fretboard-strings
  (general/all-tones)
  [{:tone   :e
    :octave 2}
   {:tone   :a
    :octave 2}
   {:tone   :d
    :octave 3}
   {:tone   :g
    :octave 3}
   {:tone   :b
    :octave 3}
   {:tone   :e
    :octave 2}]
  3))

(defn add-layer [f fretboard-matrix]
  (utils/map-matrix
   (fn [{:keys [blank?] :as fret}]
     ;; skip blank frets, for example on the banjo
     (if blank? fret (f fret)))
   fretboard-matrix))

(defn add-pattern
  [{:keys [x y tone match? interval out] :as m}]
  (if match?
    (assoc m :out
           (-> (general/sharp-or-flat tone interval) name str/capitalize)
           :match? true)
    m))

(defn add-chord-tones
  [chord-tones {:keys [x y tone pattern-match? interval out] :as m}]
  (if-let [tone' (first (set/intersection (set chord-tones) tone))]
    (assoc m
           :out (-> tone' name str/capitalize)
           :match? true)
    m))

;; :circle-text
;; :circle-color
;; :show-octave?
;; :string-thickness
;; :fret-color
;; :background-color

(defn add-styling
  [{:keys [blank? x y tone match? interval out root?] :as m}
   {:keys [show-octave surrounding-intervals surrounding-tones]
    :as query-params}]
  (cond-> m
    (and root? match?) (assoc :style/circle-color "#ff7600")))

(defn add-root
  [root-tone {:keys [tone out] :as m}]
  {:pre [(set? tone)]}
  (if (or (= out "1")
          (tone root-tone))
    (assoc m :root? true)
    m))

(defn add-intervals
  "Show as intervals.

  Example:
  chord-tones-and-intervals: `[[:c \"1\"] [:d \"2\"] [:e \"3\"] [:f \"4\"] [:g \"5\"] [:a \"6\"] [:b \"7\"]]`"
  [chord-tones-and-intervals {:keys [x y tone pattern-match? interval out] :as m}]
  {:pre [(set? tone)]}
  (if-let [i (->> chord-tones-and-intervals
                  (filter (fn [[tone' interval']]
                            (tone tone')))
                  (first)
                  (second))]
    (assoc m :out i)
    m))

(defn add-basics
  "Show as intervals.

  Example:
  tones->intervals: `[[:c \"1\"] [:d \"2\"] [:e \"3\"] [:f \"4\"] [:g \"5\"] [:a \"6\"] [:b \"7\"]]`"
  [tones->intervals {:keys [x y tone pattern-match? interval] :as m}]
  {:pre [(set? tone)]}
  (if-let [i (->> tones->intervals
                  (filter (fn [[tone' interval']]
                            (tone tone')))
                  (first)
                  (second))]
    (cond-> m
      :always            (assoc :interval i)
      :always            (assoc :tone-str (-> tone (general/sharp-or-flat "b") name str/capitalize))
      (= 2 (count tone)) (assoc :sharp (-> tone (general/sharp-or-flat "#") name str/capitalize))
      (= 2 (count tone)) (assoc :flat (-> tone (general/sharp-or-flat "b") name str/capitalize))
      (= "1" i)          (assoc :root? true))
    m))

(->> (find-fretboard-pattern
      (general/all-tones)
      :e
      [["1" nil nil]
       ["5" nil nil]
       ["b3" nil nil]
       [nil nil "1"]
       [nil nil "5"]
       ["1" nil nil]]
      (fretboard-strings
       (general/all-tones)
       [{:tone   :e
         :octave 2}
        {:tone   :a
         :octave 2}
        {:tone   :d
         :octave 3}
        {:tone   :g
         :octave 3}
        {:tone   :b
         :octave 3}
        {:tone   :e
         :octave 2}]
       5))
     (add-layer
      #_add-flats
      #_add-sharps
      add-pattern)
     (add-layer
      (partial add-root :e)))

(->> (fretboard-strings
      (general/all-tones)
      [{:tone   :e
        :octave 2}
       {:tone   :a
        :octave 2}
       {:tone   :d
        :octave 3}
       {:tone   :g
        :octave 3}
       {:tone   :b
        :octave 3}
       {:tone   :e
        :octave 2}]
      10)
     (add-layer
      #_(partial add-chord-tones [:e :b :g])
      (partial add-intervals [[:e "1"] [:b "b3"] [:g "5"]]))
     (add-layer
      (partial add-root nil)))

(defn add-basics-to-fretboard-matrix' [tones-and-intervals matrix]
  (add-layer (partial add-basics tones-and-intervals) matrix))

(def add-styling-to-fretboard-matrix
  (partial add-layer add-styling))

(defn add-basics-to-fretboard-matrix [key-of fretboard-matrix]
  (let [tones-matched-with-intervals
        (mapv
         vector
         (->> (general/tones-starting-at key-of)
              (map first))
         ["1" "b2" "2" "b3" "3" "4" "b5" "5" "b6" "6" "b7" "7"])]
    (add-basics-to-fretboard-matrix'
     tones-matched-with-intervals
     fretboard-matrix)))

(defn fretboard-str
  [tone-f matrix]
  (let [add-table-stuff
        (fn [row]
          (str "|" (apply str (interpose "|" (map #(utils/fformat " %-3s" %) row))) "|"))
        rows
        (->> matrix
             (map
              (fn [row]
                (->> row
                     (map tone-f))))
             (map add-table-stuff))
        row-length (-> rows first count)]
    (->> rows
         (utils/list-insert (add-table-stuff (->> matrix first (map (comp str :x)))) 0)
         (utils/list-insert (str "|" (apply str (take (- row-length 2) (repeat "-"))) "|") 1)
         (str/join "\n"))))

(->> (fretboard-strings
      (general/all-tones)
      [{:tone   :e
        :octave 2}
       {:tone   :a
        :octave 2}
       {:tone   :d
        :octave 3}
       {:tone   :g
        :octave 3}
       {:tone   :b
        :octave 3}
       {:tone   :e
        :octave 2}]
      12)
     (add-layer
      #_(partial add-chord-tones [:e :b :g])
      (partial add-intervals [[:e "1"] [:b "b3"] [:g "5"]]))
     (fretboard-str (fn [{:keys [out]}] (if (nil? out) "" out)))
     (println))

(str
 "| 0  | 1  | 2  | 3  | 4  | 5  | 6  | 7  | 8  | 9  | 10 | 11 |\n"
 "|-----------------------------------------------------------|\n"
 "| 1  |    |    | 5  |    |    |    | b3 |    |    |    |    |\n"
 "| b3 |    |    |    |    | 1  |    |    | 5  |    |    |    |\n"
 "| 5  |    |    |    | b3 |    |    |    |    | 1  |    |    |\n"
 "|    |    | 1  |    |    | 5  |    |    |    | b3 |    |    |\n"
 "|    |    | b3 |    |    |    |    | 1  |    |    | 5  |    |\n"
 "| 1  |    |    | 5  |    |    |    | b3 |    |    |    |    |")

(->> (find-fretboard-pattern
      (general/all-tones)
      :e
      [["1" nil nil]
       ["5" nil nil]
       ["b3" nil nil]
       [nil nil "1"]
       [nil nil "5"]
       ["1" nil nil]]
      (fretboard-strings
       (general/all-tones)
       [{:tone   :e
         :octave 2}
        {:tone   :a
         :octave 2}
        {:tone   :d
         :octave 3}
        {:tone   :g
         :octave 3}
        {:tone   :b
         :octave 3}
        {:tone   :e
         :octave 2}]
       10))
     (add-layer
      #_add-flats
      #_add-sharps
      #_add-pattern
      (partial add-intervals [[:e "1"] [:b "b3"] [:g "5"]]))
     ;; (trim-matrix #(every? nil? (map :out %))) ;; Trim fretboard
     (fretboard-str (fn [{:keys [out]}] (if (nil? out) "" out)))
     (println))

(->> (find-fretboard-pattern
      (general/all-tones)
      :c
      [["5" nil nil]
       [nil nil "3"]
       [nil nil "1"]
       [nil nil "5"]
       ["1" nil nil]
       ["5" nil nil]]
      (fretboard-strings
       (general/all-tones)
       [{:tone   :e
         :octave 2}
        {:tone   :a
         :octave 2}
        {:tone   :d
         :octave 3}
        {:tone   :g
         :octave 3}
        {:tone   :b
         :octave 3}
        {:tone   :e
         :octave 2}]
       10))
     (add-layer
      #_add-flats
      #_add-sharps
      add-pattern)
     ;; (trim-matrix #(every? nil? (map :out %))) ;; Trim fretboard
     (fretboard-str (fn [{:keys [out]}] (if (nil? out) "" out)))
     (println))

;; Public functions

(defn add-pattern-with-intervals
  [{:keys [x y tone match? interval out] :as m}]
  (if match?
    (assoc m :out interval)
    m))

(defn pattern-with-intervals
  [key-of pattern fretboard-matrix]
  (->> (find-fretboard-pattern
        (general/all-tones)
        key-of
        pattern
        fretboard-matrix)
       (add-layer add-pattern-with-intervals)))

(->> (pattern-with-intervals
      :a
      [["5" nil nil]
       [nil nil "3"]
       [nil nil "1"]
       [nil nil "5"]
       ["1" nil nil]
       ["5" nil nil]]
      (fretboard-strings
       (general/all-tones)
       [{:tone   :e
         :octave 2}
        {:tone   :a
         :octave 2}
        {:tone   :d
         :octave 3}
        {:tone   :g
         :octave 3}
        {:tone   :b
         :octave 3}
        {:tone   :e
         :octave 2}]
       10))
     (utils/trim-matrix #(every? nil? (map :out %)))
     (fretboard-str (fn [{:keys [out]}] (if (nil? out) "" out)))
     (println))

(defn pattern-with-tones
  [key-of pattern fretboard-matrix]
  (->> (find-fretboard-pattern
        (general/all-tones)
        key-of
        pattern
        fretboard-matrix)
       (add-layer add-pattern)))

(->> (fretboard-strings
      (general/all-tones)
      [{:tone   :e
        :octave 2}
       {:tone   :a
        :octave 2}
       {:tone   :d
        :octave 3}
       {:tone   :g
        :octave 3}
       {:tone   :b
        :octave 3}
       {:tone   :e
        :octave 2}]
      10)
     (pattern-with-tones
      :c
      [["5" nil nil]
       [nil nil "3"]
       [nil nil "1"]
       [nil nil "5"]
       ["1" nil nil]
       ["5" nil nil]])
     (utils/trim-matrix #(every? nil? (map :out %)))
     ;; (fretboard-str (fn [{:keys [out]}] (if (nil? out) "" out)))
     ;; (println)
     )
(defn with-all-tones
  "
  `tones` - `[:e :b :g]`"
  [tones fretboard-matrix]
  (->> fretboard-matrix
       (add-layer (partial add-chord-tones tones))
       (add-layer (partial add-root (first tones)))))

(->> (fretboard-strings
      ;; (general/all-tones)
      [{:tone   :e
        :octave 2}
       {:tone   :a
        :octave 2}
       {:tone   :d
        :octave 3}
       {:tone   :g
        :octave 3}
       {:tone   :b
        :octave 3}
       {:tone   :e
        :octave 2}]
      16)
     (with-all-tones [:e :b :g])
     #_(utils/trim-matrix #(every? nil? (map :out %)))
     ;; (fretboard-str (fn [{:keys [out]}] (if (nil? out) "" out)))
     ;; (println)
     )
(defn with-all-intervals
  "
  chord-tones-and-intervals: `[[:c \"1\"] [:d \"2\"] [:e \"3\"] [:f \"4\"] [:g \"5\"] [:a \"6\"] [:b \"7\"]]`"
  [chord-tones-and-intervals fretboard-matrix]
  (->> fretboard-matrix
       (add-layer (partial add-intervals chord-tones-and-intervals))))

(->> (with-all-intervals
       [[:e "1"] [:b "b3"] [:g "5"]]
       (fretboard-strings
        (general/all-tones)
        [{:tone   :e
          :octave 2}
         {:tone   :a
          :octave 2}
         {:tone   :d
          :octave 3}
         {:tone   :g
          :octave 3}
         {:tone   :b
          :octave 3}
         {:tone   :e
          :octave 2}]
        12))
     (utils/trim-matrix #(every? nil? (map :out %)))
     (fretboard-str (fn [{:keys [out]}] (if (nil? out) "" out)))
     (println))


(defn create-fretboard-matrix
  ([nr-of-frets tuning]
   (fretboard-strings tuning nr-of-frets))
  ([key-of nr-of-frets tuning]
   (->> (fretboard-strings tuning nr-of-frets)
        (add-basics-to-fretboard-matrix key-of))))

(create-fretboard-matrix
 :c
 15
 [{:tone :e, :octave 2, :start-index 0}
  {:tone :a, :octave 2, :start-index 0}
  {:tone :d, :octave 3, :start-index 0}
  {:tone :g, :octave 3, :start-index 0}
  {:tone :b, :octave 3, :start-index 0}
  {:tone :e, :octave 4, :start-index 0}])

(create-fretboard-matrix
 15
 [{:tone :e, :octave 2, :start-index 0}
  {:tone :a, :octave 2, :start-index 0}
  {:tone :d, :octave 3, :start-index 0}
  {:tone :g, :octave 3, :start-index 0}
  {:tone :b, :octave 3, :start-index 0}
  {:tone :e, :octave 4, :start-index 0}])

(defn compare-two-frets-
  "Used mainly for sorting frets from low to high pitch."
  ([x]
   (tone-values/compare-tones x))
  ([x1 x2]
   (tone-values/compare-tones x1 x2)))

(compare-two-frets-
 {:x 5, :tone #{:e}, :octave 2}
 {:x 6, :tone #{:f}, :octave 2})


(defn sort-frets
  "Sort frets by pitch."
  [coll]
  (sort-by compare-two-frets- coll))

(sort-frets
 [{:tone #{:b}, :octave 0}
  {:tone #{:e}, :octave 4}
  {:tone #{:g# :ab}, :octave 4}
  {:tone #{:c}, :octave 0}])

;;
;; fretboard2 helpers
;;

(def root-note-color "#ff7600")
(def note-color "#ffa500") ;; orange

(defn center-text
  [pred f]
  (fn [m]
    (cond-> m
      (pred m) (assoc :center-text (f m)))))

(defn circle-color
  [pred color]
  (fn [m]
    (cond-> m
      (pred m) (assoc :circle-color color))))

(defn down-right-text
  [pred f]
  (fn [m]
    (cond-> m
      (pred m) (assoc :down-right-text (f m)))))

(defn matches?
  "True if `fretboard-matrix` has matches"
  [fretboard-matrix]
  (->> fretboard-matrix
       (apply concat)
       (filter :match?)
       (seq)
       (boolean)))

(defn first-fret?
  "First fret?"
  [fretboard-matrix {:keys [x y]}]
  (let [xy-map (->> fretboard-matrix
                    (apply concat)
                    (map (fn [{:keys [x y] :as m}]
                           [[x y] m]))
                    (into {}))
        fret (get-in xy-map [[(dec x) y]])]
    (or (get fret :blank?)
        (nil? fret))))

(defn left-is-blank?
  "Is fret to the left blank?"
  [fretboard-matrix {:keys [x y] :as m}]
  (let [f? (first-fret? fretboard-matrix m)]
    (cond-> m
      f? (assoc :left-is-blank? true))))

(def fretboard2-keys
  [:background-color
   :circle-color
   :center-text
   :down-right-text
   :blank?
   :y
   :x
   :on-click
   :x-max?
   :x-min?
   :left-is-blank?
   :fretboard-size])

(defn fretboard-matrix->fretboard2
  [{:keys [as-intervals
           show-octave
           surrounding-intervals
           surrounding-tones]}
   frets-to-matrix]
  (->> frets-to-matrix
       (utils/map-matrix
        (comp
         #(select-keys % fretboard2-keys)
         (circle-color :highlight? :color/highlight)
         (circle-color
          (fn [{:keys [root? match?]}]
            (and root? match?))
          :color/root-note)
         (circle-color :match? :color/note)
         (circle-color (comp not :match?) :color/grey)
         (if show-octave
           (down-right-text (comp not :match?) :octave)
           identity)
         (if surrounding-intervals
           (comp
            (center-text (comp not :match?) :interval)
            (circle-color (comp not :match?) :color/grey))
           identity)
         (if surrounding-tones
           (comp
            (circle-color (comp not :match?) :color/grey)
            (center-text (comp not :match?) :tone-str))
           identity)
         (if as-intervals
           (center-text :match? :interval)
           (center-text :match? :tone-str))
         (partial left-is-blank? frets-to-matrix)))))
