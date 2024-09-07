(ns se.jherrlin.utils
  (:require
   #?(:cljs [goog.string.format])
   #?(:cljs [goog.string :as gstring])
   [malli.core :as m]))

(defn validate [model data]
  (if (m/validate model data)
    data
    (let [out {:model       model
               :data        data
               :explanation (m/explain model data)}]
      (println "Data doesn't validate against model!")
      (println out)
      (throw
       (ex-info "Data doesn't validate against model!" out)))))

(defn vec-remove
  "Remove `idx` in `coll`."
  [idx coll]
  (into (subvec coll 0 idx) (subvec coll (inc idx))))

(defn list-insert
  "Insert `element` on `index` on `lst`.

  (list-insert
   0
   3
   [1 2 3 4])
  =>
  [1 2 3 0 4]"
  [element index lst]
  (let [[l r] (split-at index lst)]
    (vec (concat l [element] r))))

#?(:cljs
   (defn fformat
     "Formats a string using goog.string.format.
   e.g: (format \"Cost: %.2f\" 10.0234)"
     [fmt & args]
     (apply gstring/format fmt args))
   :clj (def fformat format))

(fformat "%5d" 3)
(fformat "Hello there, %s" "bob")


(defn fretboard-matrix->x+y-map
  [fretboard-matrix]
  (->> fretboard-matrix
       (apply concat)
       (map (fn [{:keys [x y] :as m}]
              [[x y] m]))
       (into {})))

(defn rotate-until
  "Rotate collection `xs` util `pred`.

  (rotate-until
   #(% :f#)
   [#{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f} #{:gb :f#} #{:g}])"
  [pred xs]
  {:pre [(fn? pred) (coll? xs)]}
  (let [xs-count (count xs)]
    (->> (cycle xs)
         (drop-while #(not (pred %)))
         (take xs-count)
         (vec))))

(defn take-indexes
  "Take indexes from a collection.

  (take-indexes
   [#{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f}]
   [0 3 5])"
  [coll indexes]
  (mapv
   (fn [index]
     (nth coll index))
   indexes))

(defn rotate-matrix
  "Rotate matrix.
  In:  `[[1 2 3] [3 4 5] [6 7 8]]`
  Out: `[[1 3 6] [2 4 7] [3 5 8]]`

  (rotate-matrix
   [[\"3\" nil nil]
    [nil \"1\" nil]
    [\"5\" nil nil]
    [nil nil nil]
    [nil nil nil]
    [nil nil nil]])"
  [matrix]
  (if-not (seq matrix)
    matrix
    (->> matrix
         (apply mapv vector)
         (mapv identity))))

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

(defn trim-matrix
  "Trim a matrix left and right by `pred`.

  Will remove the columns and rows that have `nil` values.

 (trim-matrix
  [[\"3\" nil nil]
   [nil \"1\" nil]
   [\"5\" nil nil]
   [nil nil nil]
   [nil nil nil]
   [nil nil nil]])
  =>
  [[\"3\" nil]
   [nil \"1\"]
   [\"5\" nil]
   [nil nil]
   [nil nil]
   [nil nil]]"
  ([fretboard-matrix]
   (trim-matrix (partial every? nil?) fretboard-matrix))
  ([pred fretboard-matrix]
   (if (empty? fretboard-matrix)
     fretboard-matrix
     (let [rotated (rotate-matrix fretboard-matrix)
           first?  (->> rotated first pred)
           last?   (->> rotated last pred)]
       (cond
         (and first? last?)
         (->> rotated
              (drop 1)
              (drop-last 1)
              (rotate-matrix)
              (trim-matrix pred))
         (and first? (not last?))
         (->> rotated
              (drop 1)
              (rotate-matrix)
              (trim-matrix pred))
         (and (not first?) last?)
         (->> rotated
              (drop-last 1)
              (rotate-matrix)
              (trim-matrix pred))
         :else fretboard-matrix)))))

(defn map-matrix
  "Flatten matrix into one dim list and run `f` on each item. The convert it back
  into a matrix.

  (map-matrix
   inc
   [[1]
    [2]
    [3]])
  =>
  [[2] [3] [4]]"
  [f matrix]
  (let [matrix-width (-> matrix first count)]
    (->> (apply concat matrix)
         (map f)
         (partition matrix-width)
         (mapv #(mapv identity %)))))

(defn update-matrix
  "Run `f` on `x`, `y` in `matrix`.

  (update-matrix
   0 0
   inc
   [[1] [2] [3]])
  =>
  [[2] [2] [3]]"
  [x y f matrix]
  (update-in matrix [y x] f))


(defn map-xyz
  "Loop over `coll` and take the first three elements and provide them to `f`.

  (map-xyz
   (fn [x y z]
     (cond-> x
       (nil? y) (assoc :last? true)))
  [{:x 0, :y 0}
   {:x 1, :y 0}
   {:x 2, :y 0}
   {:x 3, :y 0}])
  =>
  [{:x 0, :y 0}
   {:x 1, :y 0}
   {:x 2, :y 0}
   {:x 3, :y 0, :last? true}]"
  [f coll]
  (loop [[x y z :as row] coll
         acc             []]
    (if (nil? x)
      (vec acc)
      (recur
       (rest row)
       (conj acc (f x y z))))))

(defn map-xyz-matrix
  "Map over each row in matrix and run `map-xyz` with `f`.

  (map-xyz-matrix
   (fn [x y z]
     (cond-> x
       (nil? y) (assoc :last? true)))
   [[{:x 0, :y 0}
     {:x 1, :y 0}
     {:x 2, :y 0}
     {:x 3, :y 0}]
    [{:x 0, :y 1}
     {:x 1, :y 1}
     {:x 2, :y 1}
     {:x 3, :y 1}]
    [{:x 0, :y 2}
     {:x 1, :y 2}
     {:x 2, :y 2}
     {:x 3, :y 2}]])
  =>
  [[{:x 0, :y 0} {:x 1, :y 0} {:x 2, :y 0} {:x 3, :y 0, :last? true}]
   [{:x 0, :y 1} {:x 1, :y 1} {:x 2, :y 1} {:x 3, :y 1, :last? true}]
   [{:x 0, :y 2} {:x 1, :y 2} {:x 2, :y 2} {:x 3, :y 2, :last? true}]]"
  [f matrix]
  (mapv (partial map-xyz f) matrix))

(defn add-x-min
  "Add `x-min?` `true` to min x.

  Each row needs to have the same lenght!

  (add-x-min
   [[{:x 0, :y 0}
     {:x 1, :y 0}]
    [{:x 0, :y 1}
     {:x 1, :y 1}]
    [{:x 0, :y 3}
     {:x 1, :y 3}]])
  =>
  [[{:x 0, :y 0, :x-min? true} {:x 1, :y 0}]
   [{:x 0, :y 1, :x-min? true} {:x 1, :y 1}]
   [{:x 0, :y 3, :x-min? true} {:x 1, :y 3}]]"
  [matrix]
  (let [x-min (->> matrix first (map :x) (apply min))]
    (->> matrix
         (map-matrix (fn [{:keys [x] :as m}]
                       (cond-> m
                         (#{x} x-min) (assoc :x-min? true)))))))

(defn add-x-max
  "Add `x-max?` `true` to max x.

  Each row needs to have the same lenght!

  (add-x-max
   [[{:x 0, :y 0}
     {:x 1, :y 0}]
    [{:x 0, :y 1}
     {:x 1, :y 1}]
    [{:x 0, :y 3}
     {:x 1, :y 3}]])
  =>
  [[{:x 0, :y 0} {:x 1, :y 0, :x-max? true}]
   [{:x 0, :y 1} {:x 1, :y 1, :x-max? true}]
   [{:x 0, :y 3} {:x 1, :y 3, :x-max? true}]]"
  [matrix]
  (let [x-max (->> matrix first (map :x) (apply max))]
    (->> matrix
         (map-matrix (fn [{:keys [x] :as m}]
                       (cond-> m
                         (#{x} x-max) (assoc :x-max? true)))))))

(defn add-y-min
  "Add `y-min?` `true`.

  (add-y-min
   [[{:x 0, :y 0}
     {:x 1, :y 0}
     {:x 2, :y 0}]
    [{:x 0, :y 1}
     {:x 1, :y 1}
     {:x 2, :y 1}]
    [{:x 0, :y 2}
     {:x 1, :y 2}
     {:x 2, :y 2}]])
  =>
  [[{:x 0, :y 0, :y-min? true}
    {:x 1, :y 0, :y-min? true}
    {:x 2, :y 0, :y-min? true}]
   [{:x 0, :y 1} {:x 1, :y 1} {:x 2, :y 1}]
   [{:x 0, :y 2} {:x 1, :y 2} {:x 2, :y 2}]]"
  [matrix]
  (let [y-min (->> matrix (map (comp :y first)) (apply min))]
    (->> matrix
         (map-matrix (fn [{:keys [y] :as m}]
                       (cond-> m
                         (#{y} y-min) (assoc :y-min? true)))))))

(defn add-y-max
  "Add `y-max?` `true`.

  (add-y-max
   [[{:x 0, :y 0}
     {:x 1, :y 0}
     {:x 2, :y 0}]
    [{:x 0, :y 1}
     {:x 1, :y 1}
     {:x 2, :y 1}]
    [{:x 0, :y 2}
     {:x 1, :y 2}
     {:x 2, :y 2}]])
  =>
  [[{:x 0, :y 0} {:x 1, :y 0} {:x 2, :y 0}]
   [{:x 0, :y 1} {:x 1, :y 1} {:x 2, :y 1}]
   [{:x 0, :y 2, :y-max? true}
    {:x 1, :y 2, :y-max? true}
    {:x 2, :y 2, :y-max? true}]]"
  [matrix]
  (let [y-max (->> matrix (map (comp :y first)) (apply max))]
    (->> matrix
         (map-matrix (fn [{:keys [y] :as m}]
                       (cond-> m
                         (#{y} y-max) (assoc :y-max? true)))))))

(defn add-min-and-max
  "Add `min-x?`, `max-x?`, `min-y?` and `max-y?`

  (add-min-and-max
   [[{:x 0, :y 0}
     {:x 1, :y 0}
     {:x 2, :y 0}]
    [{:x 0, :y 1}
     {:x 1, :y 1}
     {:x 2, :y 1}]
    [{:x 0, :y 2}
     {:x 1, :y 2}
     {:x 2, :y 2}]])
  =>
  [[{:x 0, :y 0, :x-min? true, :y-min? true}
    {:x 1, :y 0, :y-min? true}
    {:x 2, :y 0, :x-max? true, :y-min? true}]
   [{:x 0, :y 1, :x-min? true}
    {:x 1, :y 1}
    {:x 2, :y 1, :x-max? true}]
   [{:x 0, :y 2, :x-min? true, :y-max? true}
    {:x 1, :y 2, :y-max? true}
    {:x 2, :y 2, :x-max? true, :y-max? true}]]"
  [matrix]
  (->> matrix
       add-x-min
       add-x-max
       add-y-min
       add-y-max))

(defn add-qualified-ns
  "Add ns to keys in map `m`."
  [m ns']
  (reduce-kv
   (fn [m k v]
     (let [new-k (keyword (str (name ns') "/" (name k)))]
       (assoc m new-k v)))
   {}
   m))

(add-qualified-ns
 {:instrument :guitar,
  :key-of     :c,
  :id         #uuid "1cd72972-ca33-4962-871c-1551b7ea5244"}
 :instrument)
