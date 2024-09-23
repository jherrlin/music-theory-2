(ns se.jherrlin.music-theory.find-scale-patterns
  "Find scale patterns

  Namespace only used for REPL dev."
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [se.jherrlin.utils :refer [trim-matrix map-matrix take-matrix drop-matrix
                                       fretboard-matrix->x+y-map]]
            [se.jherrlin.music-theory.definitions :as definitions]
            [se.jherrlin.music-theory.fretboard :as fretboard]
            [se.jherrlin.music-theory.instruments :as instruments]
            [se.jherrlin.music-theory.definitions.helpers :as definitions.helpers]))

(comment
  (remove-ns 'se.jherrlin.music-theory.find-scale-patterns)
  )


(defn fulfilled-matches
  [intervals-count matches]
  (->> matches
       (filter (fn [[k v]]
                 (= (-> v keys count) intervals-count)))
       (into {})
       (vals)
       (vec)))

(defn nr-of-matches-on-y
  [matches-counter y matches]
  (->> (get matches matches-counter)
       (vals)
       (filter (comp #{y} :y))
       count))

(defn throw-loop-exhaust!
  [m]
  (throw
   (ex-info "loop exhaust" m)))

(defn ->found-matched-in-map
  [{:keys [min-x max-x intervals-to-find fretboard-map nr-of-strings max-matches-per-y]
    :or   {max-matches-per-y 4}}]
  (loop [loop-counter                                 0
         matches-counter                              0
         [interval & rst-intervals :as all-intervals] intervals-to-find
         x                                            min-x
         y                                            (dec nr-of-strings)
         matches                                      {}]
    (let [last-interval? (and interval (not rst-intervals))
          {looking-at-interval :interval fret-x :x fret-y :y :as fret}
          (get fretboard-map [x y])

          end?          (or (nil? fret)
                            (nil? interval))
          loop-exhaust? (= loop-counter 300)]
      (cond
        loop-exhaust? (throw-loop-exhaust!
                       {:matches-counter matches-counter
                        :matches         matches
                        :x               x
                        :y               y})
        end?          matches
        :else
        (let [intervals-matches?   (= looking-at-interval interval)
              last-interval-match? (and intervals-matches? last-interval?)
              matches'             (cond-> matches
                                     intervals-matches? (assoc-in [matches-counter [fret-x fret-y]] fret))
              nr-of-matches-on-y'  (nr-of-matches-on-y matches-counter y matches')
              matches-counter'     (cond-> matches-counter
                                     last-interval-match? inc)
              reset-x?             (or (= x max-x)
                                       (= max-matches-per-y nr-of-matches-on-y'))
              dec-y?               (or reset-x? (= max-matches-per-y nr-of-matches-on-y'))]
          (recur
           (inc loop-counter)
           matches-counter'

           (cond
             last-interval-match?
             intervals-to-find

             intervals-matches?
             rst-intervals

             :else
             all-intervals)

           (cond
             last-interval-match? x
             reset-x?             min-x
             :else                (inc x))

           (cond-> y
             dec-y? dec)

           matches'))))))

(defn ->pattern-matrix
  [nr-of-strings nr-of-frets x-min x-max x+y-map]
  (->> (for [y     (range nr-of-strings)
             x     (range x-min (inc x-max))]
         (get-in x+y-map [[x y] :interval]))
       (partition-all nr-of-frets)
       (mapv (partial mapv identity))))

(defn ->pattern-str
  [pattern-matrix]
  (->> pattern-matrix
       (map #(map (fn [x] (if (nil? x) "-" x)) %))
       (map (partial str/join "   "))
       (str/join "\n")))

(defn find-scale-patterns
  [intervals-to-find fretboard-matrix-with-intervals]
  (let [min-x           (fretboard/min-x fretboard-matrix-with-intervals)
        max-x           (fretboard/max-x fretboard-matrix-with-intervals)
        nr-of-strings   (fretboard/nr-of-strings fretboard-matrix-with-intervals)
        nr-of-frets     (fretboard/nr-of-frets fretboard-matrix-with-intervals)
        intervals-count (count intervals-to-find)
        fretboard-map   (fretboard-matrix->x+y-map fretboard-matrix-with-intervals)]
    (some->> (->found-matched-in-map
              {:min-x             min-x
               :max-x             max-x
               :intervals-to-find intervals-to-find
               :fretboard-map     fretboard-map
               :nr-of-strings     nr-of-strings})
             (fulfilled-matches intervals-count)
             (mapv (partial ->pattern-matrix nr-of-strings nr-of-frets min-x max-x))
             (mapv trim-matrix))))

(comment
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
  )


;; (->>
;;      (map-matrix #(select-keys % [:x :y :interval])))

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

(defn different-intervals
  [intervals]
  [(conj intervals (first intervals))
   (-> (concat intervals intervals [(first intervals)])
       vec)
   (-> (concat intervals intervals)
       vec)
   (-> (concat intervals (drop-last 1 intervals))
       vec)])

(defn generate-mandolin-scale-patterns []
  (let [instrument :mandolin
        fretboard-matrix-with-intervals
        (fretboard/create-fretboard-matrix
         :d
         18
         (-> (instruments/instrument instrument)
             (get :tuning)))]

    (->> (for [{scale-name      :scale
                scale-intervals :scale/intervals
                :as             scale} (->> (definitions/scales)
                                            #_(filter (comp #{:pentatonic-major} :scale)))
               intervals               (rotate-intervals scale-intervals)
               fretboard-matrix        (fretboard-matrix-portions 7 fretboard-matrix-with-intervals)
               intervals' (different-intervals intervals)]
           (->> (find-scale-patterns intervals' fretboard-matrix)
                (map (fn [pattern]
                       (let [pattern-str (->pattern-str pattern)]
                         {:type                                 [:scale :pattern]
                          :fretboard-pattern/tuning             instrument
                          :fretboard-pattern/on-strings         (definitions.helpers/on-strings pattern)
                          :fretboard-pattern/belongs-to         scale-name
                          :fretboard-pattern/intervals          intervals
                          :fretboard-pattern/starts-on-interval (first intervals)
                          :fretboard-pattern/generated?         true
                          :fretboard-pattern/pattern-hash       (hash pattern)
                          :fretboard-pattern/pattern-str        pattern-str
                          :fretboard-pattern/inversion?         (definitions.helpers/inversion? pattern)
                          :fretboard-pattern/pattern            pattern})))))
         (apply concat)
         (set)
         (mapv #(assoc % :id (random-uuid)))
         (mapv (juxt :id identity))
         (into {}))))



(comment
  (generate-mandolin-scale-patterns)

  ;; Generate scale patterns and write them to file
  (let [s (->> (generate-mandolin-scale-patterns)
               (clojure.pprint/pprint)
               (with-out-str))]
    (->> (str
          "(ns se.jherrlin.music-theory.definitions.generated-scale-patterns-mandolin)
\n\n
(def generated-scale-patterns\n\n"
          s
          "\n)\n")
         (spit "src/se/jherrlin/music_theory/definitions/generated_scale_patterns_mandolin.cljc")))

  )

(and
 (=
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
  [[[nil nil nil nil nil]
    [nil nil nil nil nil]
    ["5" nil "6" nil nil]
    ["1" nil "2" nil "3"]]
   [["5" nil "6" nil nil]
    ["1" nil "2" nil "3"]
    [nil nil nil nil nil]
    [nil nil nil nil nil]]])
 (=
  (find-scale-patterns
   ["1" "2" "3" "4" "5" "6" "7" "1" "2" "3" "4" "5" "6" "7" "1"]
   [[{:x 0, :y 0, :interval "6"}
     {:x 1, :y 0, :interval "b7"}
     {:x 2, :y 0, :interval "7"}
     {:x 3, :y 0, :interval "1"}
     {:x 4, :y 0, :interval "b2"}
     {:x 5, :y 0, :interval "2"}
     {:x 6, :y 0, :interval "b3"}
     {:x 7, :y 0, :interval "3"}
     {:x 8, :y 0, :interval "4"}
     {:x 9, :y 0, :interval "b5"}
     {:x 10, :y 0, :interval "5"}
     {:x 11, :y 0, :interval "b6"}
     {:x 12, :y 0, :interval "6"}
     {:x 13, :y 0, :interval "b7"}
     {:x 14, :y 0, :interval "7"}]
    [{:x 0, :y 1, :interval "2"}
     {:x 1, :y 1, :interval "b3"}
     {:x 2, :y 1, :interval "3"}
     {:x 3, :y 1, :interval "4"}
     {:x 4, :y 1, :interval "b5"}
     {:x 5, :y 1, :interval "5"}
     {:x 6, :y 1, :interval "b6"}
     {:x 7, :y 1, :interval "6"}
     {:x 8, :y 1, :interval "b7"}
     {:x 9, :y 1, :interval "7"}
     {:x 10, :y 1, :interval "1"}
     {:x 11, :y 1, :interval "b2"}
     {:x 12, :y 1, :interval "2"}
     {:x 13, :y 1, :interval "b3"}
     {:x 14, :y 1, :interval "3"}]
    [{:x 0, :y 2, :interval "5"}
     {:x 1, :y 2, :interval "b6"}
     {:x 2, :y 2, :interval "6"}
     {:x 3, :y 2, :interval "b7"}
     {:x 4, :y 2, :interval "7"}
     {:x 5, :y 2, :interval "1"}
     {:x 6, :y 2, :interval "b2"}
     {:x 7, :y 2, :interval "2"}
     {:x 8, :y 2, :interval "b3"}
     {:x 9, :y 2, :interval "3"}
     {:x 10, :y 2, :interval "4"}
     {:x 11, :y 2, :interval "b5"}
     {:x 12, :y 2, :interval "5"}
     {:x 13, :y 2, :interval "b6"}
     {:x 14, :y 2, :interval "6"}]
    [{:x 0, :y 3, :interval "1"}
     {:x 1, :y 3, :interval "b2"}
     {:x 2, :y 3, :interval "2"}
     {:x 3, :y 3, :interval "b3"}
     {:x 4, :y 3, :interval "3"}
     {:x 5, :y 3, :interval "4"}
     {:x 6, :y 3, :interval "b5"}
     {:x 7, :y 3, :interval "5"}
     {:x 8, :y 3, :interval "b6"}
     {:x 9, :y 3, :interval "6"}
     {:x 10, :y 3, :interval "b7"}
     {:x 11, :y 3, :interval "7"}
     {:x 12, :y 3, :interval "1"}
     {:x 13, :y 3, :interval "b2"}
     {:x 14, :y 3, :interval "2"}]])
  [[["6" nil "7" "1" nil nil]
    ["2" nil "3" "4" nil "5"]
    ["5" nil "6" nil "7" "1"]
    ["1" nil "2" nil "3" "4"]]]))
