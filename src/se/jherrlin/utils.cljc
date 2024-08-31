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
  [1 2 3 4])"
  [element index lst]
  (let [[l r] (split-at index lst)]
    (concat l [element] r)))

(list-insert
 0
 3
 [1 2 3 4])

#?(:cljs
   (defn fformat
     "Formats a string using goog.string.format.
   e.g: (format \"Cost: %.2f\" 10.0234)"
     [fmt & args]
     (apply gstring/format fmt args))
   :clj (def fformat format))

(fformat "%5d" 3)
(fformat "Hello there, %s" "bob")

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

(rotate-until
 #(% :f#)
 [#{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f} #{:gb :f#} #{:g} #{:g# :ab} #{:a} #{:bb :a#} #{:b}])

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

(take-indexes
 [#{:c} #{:db :c#} #{:d} #{:d# :eb} #{:e} #{:f}]
 [0 3 5])

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

(rotate-matrix
 [["3" nil nil]
  [nil "1" nil]
  ["5" nil nil]
  [nil nil nil]
  [nil nil nil]
  [nil nil nil]])

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

(defn trim-matrix
  "Trim a matrix left and right by `pred`.

  Will remove the columns and rows that have `nil` values.

 (trim-matrix
  [[\"3\" nil nil]
   [nil \"1\" nil]
   [\"5\" nil nil]
   [nil nil nil]
   [nil nil nil]
   [nil nil nil]])"
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

(trim-matrix
 [["3" nil nil]
  [nil "1" nil]
  ["5" nil nil]
  [nil nil nil]
  [nil nil nil]
  [nil nil nil]])

(trim-matrix
 [[nil "1" nil nil]
  [nil "5" nil nil]
  [nil "b3" nil nil]
  [nil nil nil "1"]
  [nil nil nil "5"]
  [nil "1" nil nil]])

(defn map-matrix
  "Flatten matrix into one dim list and run `f` on each item. The convert it back
  into a matrix.

  (map-matrix
   inc
   [[1]
    [2]
    [3]])"
  [f matrix]
  (let [matrix-width (-> matrix first count)]
    (->> (apply concat matrix)
         (map f)
         (partition matrix-width)
         (mapv #(mapv identity %)))))

(map-matrix
 inc
 [[1]
  [2]
  [3]])

(defn update-matrix [x y f matrix]
  (update-in matrix [y x] f))

(update-matrix
 0 0
 inc
 [[1]
  [2]
  [3]])

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
