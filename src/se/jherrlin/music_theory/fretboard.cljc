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
   [se.jherrlin.utils :as utils]))


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

(fretboard-string
 (general/all-tones)
 {:tone :e, :octave 2, :start-index 5}
 12)

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

(fretboard-strings
 (general/all-tones)
 [{:tone        :e
   :octave      2}
  {:tone        :a
   :octave      2}
  {:tone        :d
   :octave      3}
  {:tone        :g
   :octave      3}
  {:tone        :b
   :octave      3}
  {:tone        :e
   :octave      2
   :start-index 5}]
 13)



(defn intevals-string->intervals-matrix
  [interval]
  (->> interval
       (str/trim)
       (str/split-lines)
       (mapv (fn [line]
               (->> line
                    str/trim
                    (re-seq #"(b{0,2}#{0,2}\d{1,2})|-")
                    (mapv second))))))

(intevals-string->intervals-matrix
 "   3   -   -
   -   bb1   -
   5   -   -
   -   -   -
   -   -   -
   -   -   -")

(defn fretboard-map-to-matrix
  "A fretboard `fretboard-map` can be represented as a hashmap,
  transform that into a matrix."
  [fretboard-map]
  (->> fretboard-map
       vals
       (sort-by :y)
       (partition-by :y)
       (mapv (fn [coll]
               (vec (sort-by :x coll))))))

(defn fretboard-matrix-to-map
  "A fretboard `fretboard-matrix` can be represented as a matrix,
  transform that into a hashmap."
  [fretboard-matrix]
  (->> fretboard-matrix
       (apply concat)
       (reduce (fn [acc {:keys [x y] :as m}]
                 (assoc acc [x y] m))
               {})))

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
                    {:x 4, :tone #{:d# :eb}, :octave 4, :y 1}]
                   [{:x 0, :tone #{:g}, :octave 3, :y 2}
                    {:x 1, :tone #{:g# :ab}, :octave 3, :y 2}
                    {:x 2, :tone #{:a}, :octave 3, :y 2}
                    {:x 3, :tone #{:bb :a#}, :octave 3, :y 2}
                    {:x 4, :tone #{:b}, :octave 3, :y 2}]
                   [{:x 0, :tone #{:d}, :octave 3, :y 3}
                    {:x 1, :tone #{:d# :eb}, :octave 3, :y 3}
                    {:x 2, :tone #{:e}, :octave 3, :y 3}
                    {:x 3, :tone #{:f}, :octave 3, :y 3}
                    {:x 4, :tone #{:gb :f#}, :octave 3, :y 3}]
                   [{:x 0, :tone #{:a}, :octave 2, :y 4}
                    {:x 1, :tone #{:bb :a#}, :octave 2, :y 4}
                    {:x 2, :tone #{:b}, :octave 2, :y 4}
                    {:x 3, :tone #{:c}, :octave 3, :y 4}
                    {:x 4, :tone #{:db :c#}, :octave 3, :y 4}]
                   [{:x 0, :tone #{:e}, :octave 2, :y 5}
                    {:x 1, :tone #{:f}, :octave 2, :y 5}
                    {:x 2, :tone #{:gb :f#}, :octave 2, :y 5}
                    {:x 3, :tone #{:g}, :octave 2, :y 5}
                    {:x 4, :tone #{:g# :ab}, :octave 2, :y 5}]]]
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
  chord-tones-and-intervals: `[[:c \"1\"] [:d \"2\"] [:e \"3\"] [:f \"4\"] [:g \"5\"] [:a \"6\"] [:b \"7\"]]`"
  [chord-tones-and-intervals {:keys [x y tone pattern-match? interval out] :as m}]
  {:pre [(set? tone)]}
  (if-let [i (->> chord-tones-and-intervals
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
