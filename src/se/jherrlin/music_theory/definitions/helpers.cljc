(ns se.jherrlin.music-theory.definitions.helpers
  (:require
   #?(:cljs [goog.string.format])
   #?(:cljs [goog.string :as gstring])
   [clojure.set :as set]
   [clojure.string :as str]
   [malli.core :as m]
   [se.jherrlin.music-theory.intervals :as intervals]
   [se.jherrlin.music-theory.models.chord :as models.chord]
   [se.jherrlin.music-theory.models.fretboard-pattern :as models.fretboard-pattern]
   [se.jherrlin.music-theory.models.scale :as models.scale]
   [se.jherrlin.music-theory.models.tone :as models.tone]))


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
                       (->> meta-data
                            (reduce-kv
                             (fn [m k v]
                               (assoc m (->> k name (str "chord/") keyword) v))
                             {})))]
     (if (models.chord/valid-chord? chord)
       chord
       (throw (ex-info "Chord is not valid" (models.chord/explain-chord chord))))))
