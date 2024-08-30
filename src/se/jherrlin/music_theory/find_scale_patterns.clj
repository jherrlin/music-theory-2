(ns se.jherrlin.music-theory.find-scale-patterns
  "Find scale patterns

  Namespace only used for REPL dev."
  (:require [clojure.string :as str]
            [se.jherrlin.utils :refer [trim-matrix]]
            [se.jherrlin.music-theory.definitions :as definitions]
            [se.jherrlin.music-theory.fretboard :as fretboard]
            [se.jherrlin.music-theory.instruments :as instruments]
            ))

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
  [intervals fretboard-matrix-with-intervals]
  (let [intervals-to-find intervals
        fretboard-map     (fretboard-matrix->map fretboard-matrix-with-intervals)
        nr-of-strings     (-> fretboard-matrix-with-intervals count)
        nr-of-frets       (-> fretboard-matrix-with-intervals first count)]
    (loop [[interval & rst-intervals :as all-intervals] intervals-to-find
           x                                            0
           y                                            (dec nr-of-strings)
           matches                                      {}]
      (if (or (nil? interval)
              (and (= x nr-of-frets)
                   (= y 0)))
        (when (= (-> matches keys count) (-> intervals-to-find count))
          matches)
        (let [{looking-at-interval :interval :as fret} (get fretboard-map [x y])]
          (recur
           (if (= looking-at-interval interval)
             rst-intervals all-intervals)
           (if (= x nr-of-frets) 0 (inc x))
           (if (= x nr-of-frets) (dec y) y)
           (if (not= looking-at-interval interval)
             matches
             (assoc matches [x y] fret))))))))

(defn ->pattern-matrix
  [nr-of-strings nr-of-frets matched-map]
  (->> (for [y (range nr-of-strings)
             x (range nr-of-frets)]
         (get-in matched-map [[x y] :interval]))
       (partition-all nr-of-frets)
       (mapv #(mapv identity %))))

(defn ->pattern-str
  [pattern-matrix]
  (->> pattern-matrix
       (map #(map (fn [x] (if (nil? x) "-" x)) %))
       (map (partial str/join "   "))
       (str/join "\n")))


(defn find-scale-patterns
  [intervals fretboard-matrix-with-intervals]
  (let [intervals-to-find (conj intervals (first intervals))
        nr-of-strings     (-> fretboard-matrix-with-intervals count)
        nr-of-frets       (-> fretboard-matrix-with-intervals first count)]
    (some->> fretboard-matrix-with-intervals
             (->found-matched-in-map intervals-to-find)
             (->pattern-matrix nr-of-strings nr-of-frets)
             (trim-matrix)
             (->pattern-str))))


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




(defn take-matrix [n matrix]
  (->> matrix
       (mapv (partial take n))
       (mapv #(mapv identity %))))

(defn drop-matrix [n matrix]
  (->> matrix
       (mapv (partial drop n))
       (mapv #(mapv identity %))))

(defn fretboard-matrix-portions
  [n fretboard-matrix-in]
  (loop [fretboard-matrix fretboard-matrix-in
         acc []]
    (if (< (-> fretboard-matrix first count) n)
      acc
      (recur
       (drop-matrix 1 fretboard-matrix)
       (conj acc (take-matrix n fretboard-matrix))))))



(let [instrument        :mandolin
      instrument-tuning (-> (instruments/instrument instrument)
                            (get :tuning))
      {intervals :scale/intervals :as scale}            (->> (definitions/scales)
                                                             (filter (comp #{:pentatonic-major} :scale))
                                                             first)
      fretboard-matrix-with-intervals
      (fretboard/create-fretboard-matrix
       :g
       20
       instrument-tuning)]

  (->> (fretboard-matrix-portions 7 fretboard-matrix-with-intervals)
       (map (fn [fretboard-matrix]
              (find-scale-patterns intervals fretboard-matrix)))
       (keep identity)

       ))
