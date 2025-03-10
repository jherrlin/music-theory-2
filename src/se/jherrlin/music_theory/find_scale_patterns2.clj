(ns se.jherrlin.music-theory.find-scale-patterns2
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [se.jherrlin.utils :refer [trim-matrix map-matrix take-matrix drop-matrix
                                       fretboard-matrix->x+y-map]]
            [se.jherrlin.music-theory.definitions :as definitions]
            [se.jherrlin.music-theory.fretboard :as fretboard]
            [se.jherrlin.music-theory.instruments :as instruments]
            [se.jherrlin.music-theory.definitions.helpers :as definitions.helpers]))


(defn fretboard-matrix->map
  [fretboard-matrix]
  (->> fretboard-matrix
       (apply concat)
       (map (fn [{:keys [x y] :as m}]
              [[x y] m]))
       (into {})))

(defn ->found-matched-in-map
  [min-x max-x intervals-to-find fretboard-matrix-with-intervals]
  (let [fretboard-map   (fretboard-matrix->map fretboard-matrix-with-intervals)
        nr-of-strings   (-> fretboard-matrix-with-intervals count)
        intervals-count (count intervals-to-find)]
    (loop [matches-counter                              0
           [interval & rst-intervals :as all-intervals] intervals-to-find
           x                                            min-x
           y                                            (dec nr-of-strings)
           matches                                      {}]
      (let [last-interval? (and interval (not rst-intervals))
            x-end?         (= x max-x)
            end?           (and x-end?
                                (= y 0))]
        (if end?
          (->> matches
               (filter (fn [[k v]]
                         (= (-> v keys count) intervals-count)))
               (into {})
               (vals)
               (vec))
          (let [{looking-at-interval :interval
                 fret-x              :x
                 fret-y              :y
                 :as                 fret} (get fretboard-map [x y])
                intervals-matches?         (= looking-at-interval interval)
                last-interval-match?       (and intervals-matches? last-interval?)]
            (recur
             (cond-> matches-counter
               last-interval-match? inc)

             (cond
               last-interval-match?
               intervals-to-find

               intervals-matches?
               rst-intervals

               :else
               all-intervals)

             (cond
               last-interval-match? x
               (= x max-x)          min-x
               :else                (inc x))

             (cond-> y
               (= x max-x) dec)

             (cond-> matches
               intervals-matches? (assoc-in [matches-counter [fret-x fret-y]] fret)))))))))

(defn ->pattern-matrix
  [nr-of-strings nr-of-frets x-min x-max x+y-map]
  (->> (for [y     (range nr-of-strings)
             x     (range x-min (inc x-max))]
         (get-in x+y-map [[x y] :interval]))
       (partition-all nr-of-frets)
       (mapv (partial mapv identity))))

(defn find-scale-patterns
  [intervals fretboard-matrix-with-intervals]
  (let [intervals-to-find intervals #_(conj intervals (first intervals))
        x-min             (->> fretboard-matrix-with-intervals first first :x)
        x-max             (->> fretboard-matrix-with-intervals first last :x)
        nr-of-strings     (-> fretboard-matrix-with-intervals count)
        nr-of-frets       (-> fretboard-matrix-with-intervals first count)]
    (some->> fretboard-matrix-with-intervals
             (->found-matched-in-map x-min x-max intervals-to-find)
             (mapv (partial ->pattern-matrix nr-of-strings nr-of-frets x-min x-max))
             (mapv trim-matrix))))

(defn different-intervals
  [intervals]
  [(conj intervals (first intervals))
   (-> (concat intervals intervals [(first intervals)])
       vec)])

(different-intervals ["1" "2" "3" "5" "6"])



(comment
  (find-scale-patterns
   ["1" "2" "3" "5" "6" "1"]
   #_["1" "2" "3" "5" "6" "1" "2" "3" "5" "6" "1"]
   [[{:x 5, :y 0, :interval "5"}
          {:x 6, :y 0, :interval "b6"}
          {:x 7, :y 0, :interval "6"}
          {:x 8, :y 0, :interval "b7"}
          {:x 9, :y 0, :interval "7"}
          {:x 10, :y 0, :interval "1"}
          {:x 11, :y 0, :interval "b2"}]
         [{:x 5, :y 1, :interval "1"}
          {:x 6, :y 1, :interval "b2"}
          {:x 7, :y 1, :interval "2"}
          {:x 8, :y 1, :interval "b3"}
          {:x 9, :y 1, :interval "3"}
          {:x 10, :y 1, :interval "4"}
          {:x 11, :y 1, :interval "b5"}]
         [{:x 5, :y 2, :interval "4"}
          {:x 6, :y 2, :interval "b5"}
          {:x 7, :y 2, :interval "5"}
          {:x 8, :y 2, :interval "b6"}
          {:x 9, :y 2, :interval "6"}
          {:x 10, :y 2, :interval "b7"}
          {:x 11, :y 2, :interval "7"}]
         [{:x 5, :y 3, :interval "b7"}
          {:x 6, :y 3, :interval "7"}
          {:x 7, :y 3, :interval "1"}
          {:x 8, :y 3, :interval "b2"}
          {:x 9, :y 3, :interval "2"}
          {:x 10, :y 3, :interval "b3"}
          {:x 11, :y 3, :interval "3"}]])
  )

(defn rotate-intervals
  [intervals-in]
  (loop [intervals intervals-in
         acc       [intervals-in]
         counter   (count intervals-in)]
    (if (= counter 1)
      acc
      (let [[f & rst]     intervals
            new-intervals (into [] (concat rst [f]))]
        (recur
         new-intervals
         (conj acc new-intervals)
         (dec counter))))))

(comment
  (rotate-intervals ["1" "2" "3" "5" "6"])
  ;; =>
  [["1" "2" "3" "5" "6"]
   ["2" "3" "5" "6" "1"]
   ["3" "5" "6" "1" "2"]
   ["5" "6" "1" "2" "3"]
   ["6" "1" "2" "3" "5"]]

  (let [fretboard-matrix-with-intervals
        [[{:x 0, :tone #{:e}, :y 0, :interval "2"}
          {:x 1, :tone #{:f}, :y 0, :interval "b3"}
          {:y 0, :tone #{:gb :f#}, :x 2, :interval "3"}
          {:x 3, :tone #{:g}, :y 0, :interval "4"}
          {:y 0, :tone #{:g# :ab}, :x 4, :interval "b5"}
          {:y 0, :tone #{:a}, :x 5, :interval "5"}
          {:y 0, :tone #{:bb :a#}, :x 6, :interval "b6"}]
         [{:y 1, :tone #{:a}, :x 0, :interval "5"}
          {:y 1, :tone #{:bb :a#}, :x 1, :interval "b6"}
          {:x 2, :tone #{:b}, :y 1, :interval "6"}
          {:x 3, :tone #{:c}, :y 1, :interval "b7"}
          {:y 1, :tone #{:db :c#}, :x 4, :interval "7"}
          {:y 1, :tone #{:d}, :x 5, :interval "1"}
          {:y 1, :tone #{:d# :eb}, :x 6, :interval "b2"}]
         [{:y 2, :tone #{:d}, :x 0, :interval "1"}
          {:y 2, :tone #{:d# :eb}, :x 1, :interval "b2"}
          {:x 2, :tone #{:e}, :y 2, :interval "2"}
          {:x 3, :tone #{:f}, :y 2, :interval "b3"}
          {:y 2, :tone #{:gb :f#}, :x 4, :interval "3"}
          {:x 5, :tone #{:g}, :y 2, :interval "4"}
          {:y 2, :tone #{:g# :ab}, :x 6, :interval "b5"}]
         [{:x 0, :tone #{:g}, :y 3, :interval "4"}
          {:y 3, :tone #{:g# :ab}, :x 1, :interval "b5"}
          {:y 3, :tone #{:a}, :x 2, :interval "5"}
          {:y 3, :tone #{:bb :a#}, :x 3, :interval "b6"}
          {:x 4, :tone #{:b}, :y 3, :interval "6"}
          {:x 5, :tone #{:c}, :y 3, :interval "b7"}
          {:y 3, :tone #{:db :c#}, :x 6, :interval "7"}]]
        intervals         ["1" "2" "3" "5" "6"]]
    (->> (rotate-intervals intervals)
         (map (fn [intervals]
                (find-scale-patterns intervals fretboard-matrix-with-intervals)))
         (str/join "\n\n####\n\n")
         print))
  )


(defn fretboard-matrix-portions
  [n fretboard-matrix-in]
  (loop [fretboard-matrix fretboard-matrix-in
         acc []]
    (if (< (-> fretboard-matrix first count) n)
      acc
      (recur
       (drop-matrix 1 fretboard-matrix)
       (conj acc (take-matrix n fretboard-matrix))))))

(defn intervals-between-and-start-indexes
  [instrument-id]
  (let [intervals-between (->> (instruments/instrument instrument-id)
                               :tuning
                               (mapv :tone)
                               fretboard/intervals-between-interval-tones)
        start-indexes     (->> (instruments/instrument instrument-id)
                               :tuning
                               (mapv :start-index))]
    {:intervals-between intervals-between
     :start-indexes start-indexes}))

(comment
  (intervals-between-and-start-indexes :mandolin)
  )

(defn create-fretboard-matrix
  [instrument-tuning]
  (fretboard/create-fretboard-matrix
   :d
   24
   instrument-tuning))



(defn generate-mandolin-scale-patterns
  [instrument-id]
  (let [fretboard-matrix-with-intervals
        (create-fretboard-matrix
         (-> (instruments/instrument instrument-id)
             (get :tuning)))
        [r1 r2] (-> (instruments/instrument instrument-id)
                    :scale-pattern-range)]
    (->> (for [intervals-from-definitions (->> (definitions/scales)
                                               (mapv :scale/intervals)
                                               (set)
                                               (vec))
               r                          (range r1 (inc r2))
               intervals                  (rotate-intervals intervals-from-definitions)
               fretboard-matrix           (fretboard-matrix-portions r fretboard-matrix-with-intervals)
               intervals'                 (different-intervals intervals)]
           (->> (find-scale-patterns intervals' fretboard-matrix)
                (map (fn [pattern]
                       (let [m (-> (intervals-between-and-start-indexes instrument-id)
                                   (assoc :pattern pattern))
                             h (hash m)]
                         (assoc m :hash h))))))
         (apply concat)
         (mapv (juxt :hash identity))
         (into {}))))

(->> (generate-mandolin-scale-patterns :mandolin)
     keys
     count)
;; => 2065



(comment
  (let [s (->> (instruments/instruments)
               (filter :scale-pattern-range)
               (map :id)
               (map generate-mandolin-scale-patterns)
               (apply merge)
               (clojure.pprint/pprint)
               (with-out-str))]
    (->> (str
          "(ns se.jherrlin.music-theory.definitions.generated-patterns)
\n\n
(def generated-patterns\n\n"
          s
          "\n)\n")
         (spit "src/se/jherrlin/music_theory/definitions/generated_patterns.cljc")))
  )
