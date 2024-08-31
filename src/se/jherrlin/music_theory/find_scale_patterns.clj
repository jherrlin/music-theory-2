(ns se.jherrlin.music-theory.find-scale-patterns
  "Find scale patterns

  Namespace only used for REPL dev."
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [se.jherrlin.utils :refer [trim-matrix map-matrix]]
            [se.jherrlin.music-theory.definitions :as definitions]
            [se.jherrlin.music-theory.fretboard :as fretboard]
            [se.jherrlin.music-theory.instruments :as instruments]
            [se.jherrlin.music-theory.definitions.helpers :as definitions.helpers]))

(comment
  (remove-ns 'se.jherrlin.music-theory.find-scale-patterns)
  )

(defn fretboard-matrix->map
  [fretboard-matrix]
  (->> fretboard-matrix
       (apply concat)
       (map (fn [{:keys [x y] :as m}]
              [[x y] m]))
       (into {})))

(defn ->found-matched-in-map
  [min-x max-x intervals fretboard-matrix-with-intervals]
  (let [intervals-to-find intervals
        fretboard-map     (fretboard-matrix->map fretboard-matrix-with-intervals)
        nr-of-strings     (-> fretboard-matrix-with-intervals count)]
    (loop [[interval & rst-intervals :as all-intervals] intervals-to-find
           x                                            min-x
           y                                            (dec nr-of-strings)
           matches                                      {}]
      (if (or (nil? interval)
              (and (= x max-x)
                   (= y 0)))
        (when (= (-> matches keys count) (-> intervals-to-find count))
          matches)
        (let [{looking-at-interval :interval :as fret} (get fretboard-map [x y])]
          (recur
           (if (= looking-at-interval interval)
             rst-intervals all-intervals)
           (if (= x max-x) min-x (inc x))
           (if (= x max-x) (dec y) y)
           (if (not= looking-at-interval interval)
             matches
             (assoc matches [x y] fret))))))))

(defn ->pattern-matrix
  [nr-of-strings nr-of-frets x-min x-max matched-map]
  (->> (for [y     (range nr-of-strings)
             x     (range x-min (inc x-max))]
         (get-in matched-map [[x y] :interval]))
       (partition-all nr-of-frets)
       (mapv (partial mapv identity))))

(defn ->pattern-str
  [pattern-matrix]
  (->> pattern-matrix
       (map #(map (fn [x] (if (nil? x) "-" x)) %))
       (map (partial str/join "   "))
       (str/join "\n")))

(defn find-scale-patterns
  [intervals fretboard-matrix-with-intervals]
  (let [intervals-to-find (conj intervals (first intervals))
        x-min             (->> fretboard-matrix-with-intervals first first :x)
        x-max             (->> fretboard-matrix-with-intervals first last :x)
        nr-of-strings     (-> fretboard-matrix-with-intervals count)
        nr-of-frets       (-> fretboard-matrix-with-intervals first count)]
    (some->> fretboard-matrix-with-intervals
             (->found-matched-in-map x-min x-max intervals-to-find)
             (->pattern-matrix nr-of-strings nr-of-frets x-min x-max)
             (trim-matrix)
             #_(->pattern-str))))

(find-scale-patterns
 ["1" "2" "3" "5" "6"]
 (->> [[{:x 5, :y 0, :interval "5"}
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
        {:x 11, :y 3, :interval "3"}]]
      (map-matrix #(select-keys % [:x :y :interval]))))


(comment
  (let [fretboard-matrix
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
        intervals ["1" "2" "3" "5" "6"]]
    (print
     (find-scale-patterns
      intervals
      fretboard-matrix)))
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




(defn take-matrix
  "Take `n` number of rows in matrix.


  (take-matrix
   2
   [[1 2 3 4]
    [1 2 3 4]
    [1 2 3 4]])
  ;; =>
  [[1 2]
   [1 2]
   [1 2]]"
  [n matrix]
  (->> matrix
       (mapv (partial take n))
       (mapv (partial mapv identity))))

(comment
  (take-matrix
   2
   [[1 2 3 4]
    [1 2 3 4]
    [1 2 3 4]])
  ;; =>
  [[1 2]
   [1 2]
   [1 2]]
  )

(defn drop-matrix
  "Drop `n` number of rows in matrix.

  (drop-matrix
   1
   [[1 2 3 4]
    [1 2 3 4]
    [1 2 3 4]])
  ;; =>
  [[2 3 4]
   [2 3 4]
   [2 3 4]]"
  [n matrix]
  (->> matrix
       (mapv (partial drop n))
       (mapv (partial mapv identity))))

(comment
  (drop-matrix
   1
   [[1 2 3 4]
    [1 2 3 4]
    [1 2 3 4]])
  ;; =>
  [[2 3 4]
   [2 3 4]
   [2 3 4]])


(defn fretboard-matrix-portions
  [n fretboard-matrix-in]
  (loop [fretboard-matrix fretboard-matrix-in
         acc []]
    (if (< (-> fretboard-matrix first count) n)
      acc
      (recur
       (drop-matrix 1 fretboard-matrix)
       (conj acc (take-matrix n fretboard-matrix))))))

(let [instrument :mandolin
      fretboard-matrix-with-intervals
      (fretboard/create-fretboard-matrix
       :d
       18
       (-> (instruments/instrument instrument)
           (get :tuning)))]

  (->> (for [{scale-name      :scale
              scale-intervals :scale/intervals
              :as             scale} (definitions/scales)
             intervals               (rotate-intervals scale-intervals)
             fretboard-matrix        (fretboard-matrix-portions 7 fretboard-matrix-with-intervals)]
         (let [pattern     (find-scale-patterns intervals fretboard-matrix)
               pattern-str (->pattern-str pattern)]
           {:type                           [:scale :pattern]
            :fretboard-pattern/tuning       instrument
            :fretboard-pattern/on-strings   (definitions.helpers/on-strings pattern)
            :fretboard-pattern/belongs-to   scale-name
            :fretboard-pattern/intervals    intervals
            :fretboard-pattern/pattern-hash (hash pattern)
            :fretboard-pattern/str          pattern-str
            :fretboard-pattern/pattern-str  pattern-str
            :fretboard-pattern/inversion?   (definitions.helpers/inversion? pattern)
            :fretboard-pattern/pattern      pattern}))
       (set)
       (mapv #(assoc % :id (random-uuid)))
       (mapv (juxt :id identity))
       (into {})
       (clojure.pprint/pprint)
       (with-out-str)
       (spit "src/se/jherrlin/music_theory/definitions/generated_scale_patterns.edn")))
