(ns se.jherrlin.music-theory.definitions.helpers
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [se.jherrlin.music-theory.intervals :as intervals]
   [se.jherrlin.music-theory.fretboard :as fretboard]
   [se.jherrlin.utils :as basic-utils]
   [se.jherrlin.music-theory.models.chord :as models.chord]
   [se.jherrlin.music-theory.models.fretboard-pattern :as models.fretboard-pattern]
   [se.jherrlin.music-theory.models.scale :as models.scale]))


(defn qualify-keywords [m n]
  (reduce-kv
   (fn [m' k v]
     (assoc m' (->> k name (str n "/") keyword) v))
   {}
   m))

(defn define-chord
  [id chord-name suffix intervals-str meta-data]
  {:pre [(uuid? id) (string? suffix) (map? meta-data) (string? intervals-str)]}
  (let [intervals'    (->> (re-seq (re-pattern models.chord/regex) intervals-str)
                           (vec))
        indexes       (intervals/functions-to-semitones intervals')
        name'         (-> chord-name
                          name
                          (str/replace "-" " "))
        categories    (let [intervals (set intervals')]
                        (cond-> #{}
                          (intervals "3")                      (conj :major)
                          (intervals "b3")                     (conj :minor)
                          (intervals "b7")                     (conj :dominant)
                          (set/subset? #{"b3" "b5"} intervals) (conj :diminished)))
        intervals-str (str/join ", " intervals')
        chord         (merge
                       {:id                   id
                        :type                 [:chord]
                        :chord/chord-name     chord-name
                        :chord/chord-name-str name'
                        :chord/intervals      intervals'
                        :chord/intervals-str  intervals-str
                        :chord/indexes        indexes
                        :chord/suffix         suffix
                        :chord/categories     categories
                        :chord/order          1000}
                       (qualify-keywords meta-data "chord"))]
    (if (models.chord/valid-chord? chord)
      chord
      (throw (ex-info "Chord is not valid" (models.chord/explain-chord chord))))))

(defn define-scale
  [id scale-names meta-data intervals-str]
  (let [intervals' (->> (re-seq (re-pattern models.chord/regex) intervals-str)
                        (vec))
        indexes    (intervals/functions-to-semitones intervals')
        categories (let [intervals (set intervals')]
                     (cond-> #{}
                       (intervals "3")  (conj :major)
                       (intervals "b3") (conj :minor)))
        scale      (merge
                    {:id                id
                     :type              [:scale]
                     :scale/scale-names scale-names
                     :scale/intervals   intervals'
                     :scale/indexes     indexes
                     :scale/categories  categories
                     :scale/order       1000}
                    (qualify-keywords meta-data "scale"))]
     (if (models.scale/valid-scale? scale)
       scale
       (throw (ex-info "Scale is not valid" (models.scale/explain-scale scale))))))

(defn on-strings [pattern-matrix]
  (->> pattern-matrix
       (map-indexed vector)
       (vec)
       (filter (fn [[string-idx intervals-on-string]]
                 (some seq intervals-on-string)))
       (map (fn [[string-idx _]] string-idx))
       (vec)))

(defn inversion?
  [pattern-matrix]
  (->> pattern-matrix
       (reverse)
       (apply concat)
       (remove nil?)
       (first)
       (= "1")
       (not)))

(defn define-scale-pattern
  [id belongs-to tuning meta-data pattern-str]
  (let [pattern-matrix (->> pattern-str
                            (fretboard/intevals-string->intervals-matrix)
                            (basic-utils/trim-matrix))
        intervals      (->> pattern-matrix
                            reverse
                            (apply concat)
                            (keep identity)
                            (into []))
        meta-data      (qualify-keywords meta-data "fretboard-pattern")
        indexes        (mapv intervals/->index intervals)
        ;; On what strings are the pattern defined. Mainly used for triads.
        on-strings'    (on-strings pattern-matrix)
        inversion?'    (inversion? pattern-matrix)
        pattern*       (merge
                        {:id                           id
                         :type                         [:scale :pattern]
                         :fretboard-pattern/indexes    indexes
                         :fretboard-pattern/belongs-to belongs-to
                         :fretboard-pattern/intervals  intervals
                         :fretboard-pattern/tuning     tuning
                         :fretboard-pattern/pattern    pattern-matrix
                         :fretboard-pattern/str        pattern-str
                         :fretboard-pattern/inversion? inversion?'
                         :fretboard-pattern/on-strings on-strings'
                         :fretboard-pattern/order      1000}
                        meta-data)]
    (if (models.fretboard-pattern/validate-fretboard-pattern? pattern*)
      pattern*
      (throw (ex-info "Scale pattern is not valid" (models.fretboard-pattern/explain-fretboard-pattern pattern*))))))

(defn define-chord-pattern
  [id belongs-to tuning meta-data pattern]
  (let [pattern'    (->> pattern
                         (fretboard/intevals-string->intervals-matrix)
                         (basic-utils/trim-matrix))
        meta-data   (qualify-keywords meta-data "fretboard-pattern")
        nr-of-tones (->> pattern'
                         (apply concat)
                         (remove nil?)
                         (count))
        ;; On what strings are the pattern defined. Mainly used for triads.
        on-strings  (->> pattern'
                         (map-indexed vector)
                         (vec)
                         (filter (fn [[string-idx intervals-on-string]]
                                   (some seq intervals-on-string)))
                         (map (fn [[string-idx _]] string-idx))
                         (vec))
        inversion?  (->> pattern'
                         (reverse)
                         (apply concat)
                         (remove nil?)
                         (first)
                         (= "1")
                         (not))
        triad?      (= nr-of-tones 3)
        pattern*    (merge
                     {:id                            id
                      :type                          [:chord :pattern]
                      :fretboard-pattern/nr-of-tones nr-of-tones
                      :fretboard-pattern/triad?      triad?
                      :fretboard-pattern/belongs-to  belongs-to
                      :fretboard-pattern/tuning      tuning
                      :fretboard-pattern/pattern     pattern'
                      :fretboard-pattern/str         pattern
                      :fretboard-pattern/inversion?  inversion?
                      :fretboard-pattern/on-strings  on-strings
                      :fretboard-pattern/order       1000}
                     meta-data)]
    (if (models.fretboard-pattern/validate-fretboard-pattern? pattern*)
      pattern*
      (throw (ex-info "Chord pattern is not valid" (models.fretboard-pattern/explain-fretboard-pattern pattern*))))))
