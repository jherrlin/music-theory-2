(ns se.jherrlin.music-theory.music-theory
  (:require
   [malli.core :as m]
   [se.jherrlin.music-theory.instruments :as instruments]
   [se.jherrlin.music-theory.definitions :as definitions]
   [se.jherrlin.music-theory.harmonizations :as harmonizations]
   [se.jherrlin.music-theory.intervals :as intervals]
   [se.jherrlin.music-theory.utils :as utils]
   [se.jherrlin.utils :as basic-utils]
   [clojure.string :as str]
   [clojure.set :as set]
   [se.jherrlin.music-theory.models.tone :as models.tone]
   [se.jherrlin.music-theory.models.entity :as models.entity]))


(def instruments (instruments/instruments))

(comment
  instruments
  )

(def get-instrument instruments/instrument)
(defn get-instrument-tuning [instrument]
  (get (get-instrument instrument) :tuning))

(comment
  (get-instrument :five-string-banjo)
  (get-instrument-tuning :five-string-banjo)
  )

(def by-id definitions/by-id)

(def get-chord definitions/chord)
(def chords (definitions/chords))
(def get-scale definitions/scale)
(def scales (definitions/scales))

(def scales-for-harmonization
  (->> scales
       (filter (comp #{7} count :scale/intervals))))

(comment
  (get-chord :major)
  )

(def chord-patterns-belonging-to definitions/chord-patterns-belonging-to)
(def chord-pattern-triads-belonging-to definitions/chord-pattern-triads-belonging-to)

(comment
  (chord-patterns-belonging-to :major :guitar)
  (chord-pattern-triads-belonging-to :major :guitar)
  )

(def scale-patterns-for-scale-and-instrument definitions/scale-patterns-for-scale-and-instrument)

(comment
  (scale-patterns-for-scale-and-instrument :major :guitar)
  )

(comment
  (by-id #uuid "1cd72972-ca33-4962-871c-1551b7ea5244")
  )

(def fretboard-strings utils/fretboard-strings)

(comment
  (fretboard-strings
   [{:tone :g, :octave 4, :start-index 5}
    {:tone :d, :octave 3, :start-index 0}
    {:tone :g, :octave 3, :start-index 0}
    {:tone :b, :octave 3, :start-index 0}
    {:tone :d, :octave 4, :start-index 0}]
   10)
  )

(def tones-starting-at utils/tones-starting-at)

(comment
  (tones-starting-at :c)
  )

(def add-intervals-to-fretboard-matrix utils/add-basics-to-fretboard-matrix)
(def with-all-tones utils/with-all-tones)
(def interval-tones utils/interval-tones)
(defn intervals->tones [interval-tones intervals]
  (mapv vector interval-tones intervals))
(def trim-matrix basic-utils/trim-matrix)
(def update-matrix basic-utils/update-matrix)
(def pattern-with-intervals utils/pattern-with-intervals)
(def pattern-with-tones utils/pattern-with-tones)
(defn with-all-intervals [interval-tones intervals fretboard-matrix]
  (utils/with-all-intervals
    (intervals->tones interval-tones intervals)
    fretboard-matrix))



(interval-tones ["1" "b3" "5"] :c)

(defn create-fretboard-matrix
  ([nr-of-frets tuning]
   (fretboard-strings tuning nr-of-frets))
  ([key-of nr-of-frets tuning]
   (->> (fretboard-strings tuning nr-of-frets)
        (add-intervals-to-fretboard-matrix key-of))))

(defn scales-to-chord [scales chord-intervals]
  (->> scales
       (map (juxt :scale/scale-names identity))
       (into {})
       (vals)
       (filter
        (fn [{scale-intervals :scale/intervals}]
          (set/subset? (set chord-intervals) (set scale-intervals))))))

(defn chords-to-scale [chords scale-intervals]
  (->> chords
       (filter
        (fn [{chord-intervals :chord/intervals}]
          (set/subset? (set chord-intervals) (set scale-intervals))))))

(def all-tones (utils/all-tones))
(def get-harmonization harmonizations/harmonization)
(def harmonizations harmonizations/harmonizations)

(def tones-by-key-and-indexes utils/tones-by-key-and-indexes)
(def tones-by-key-and-intervals utils/tones-by-key-and-intervals)

(def find-chord utils/find-chord)
(def sharp-or-flat utils/sharp-or-flat)
(def rotate-until basic-utils/rotate-until)

(defn scale-interval-tones [key-of scale-intervals]
  (let [scale-indexes (intervals/functions-to-semitones scale-intervals)

        index-tones (tones-by-key-and-indexes key-of scale-indexes)]
    (map
     #(sharp-or-flat %1 %2)
     index-tones
     scale-intervals)))

(scale-interval-tones
 :c
 ["1" "2" "3" "4" "5" "6" "7"])
;; => (:c :d :e :f :g :a :b)


(def entity models.entity/entity)
(def definitions-to-entities models.entity/definitions-to-entities)
(def valid-entity? models.entity/valid-entity?)
(def valid-entities? models.entity/valid-entities?)
(def entity-to-str models.entity/entity-to-str)
(def str-to-entities models.entity/str-to-entities)
(def fretboard-entity? models.entity/fretboard-entity?)
(def select-entity-keys models.entity/select-entity-keys)


;; (def Unit
;;   [:map
;;    [:id                  uuid?]
;;    [:instrument          keyword?]
;;    [:key-of              keyword?]])

;; (def valid-unit?   (partial m/validate Unit))
;; (def explain-unit  (partial m/explain  Unit))

;; (defn unit [key-of instrument id]
;;   {:id         id
;;    :instrument instrument
;;    :key-of     key-of})

;; (defn to-units
;;   ([key-of instrument ms]
;;    (to-units key-of instrument :id ms))
;;   ([key-of instrument id-fn ms]
;;    (->> ms
;;         (map (fn [m]
;;                (let [id (id-fn m)]
;;                  (unit key-of instrument id)))))))

;; (defn unit-to-str [{:keys [instrument key-of id]}]
;;   (str (-> instrument name) "," (-> key-of name) "," id))

;; (defn str-to-unit [s]
;;   (let [[instrument key-of id] (str/split s ",")]
;;     {:instrument (keyword instrument)
;;      :key-of     (keyword key-of)
;;      :id         (uuid id)}))

;; (let [m {:instrument :guitar
;;          :key-of     :c
;;          :id         #uuid "c91cddfe-f776-4c0c-8125-4f4c5d074e77"}]
;;   (->> m
;;        (unit-to-str)
;;        #_(str-to-unit)
;;        #_(= m)))

;; (defn str-to-units [s]
;;   (->> (str/split s "_")
;;        (map str-to-unit)))

;; (str-to-units
;;  "guitar,c,94f5f7a4-d852-431f-90ca-9e99f89bbb9c")

(defn- generate [coll intervals-key index-key]
  (for [key-of        (apply concat all-tones)
        {intervals intervals-key
         indexes   index-key
         :as       m} coll]
    (let [interval-tones (tones-by-key-and-intervals key-of intervals)
          index-tones    (tones-by-key-and-indexes   key-of indexes)]
      (assoc m
             :interval-tones interval-tones
             :interval-tones-set (set interval-tones)
             :index-tones  index-tones
             :index-tones-set (set index-tones)
             :key-of key-of))))

(def generated-chords
  (generate chords :chord/intervals :chord/indexes))

(def generated-scales
  (generate scales :scale/intervals :scale/indexes))

(def index-or-interval-tones-set?
  (partial m/validate [:or
                       models.tone/IndexTonesSet
                       models.tone/IntervalTonesSet]))

(defn- match-tones-with-coll
  [coll tones-to-search]
  {:pre [(index-or-interval-tones-set? tones-to-search)]}
  (let [tones-to-search-count (count tones-to-search)]
    (->> coll
         (map (fn [{:keys [interval-tones-set index-tones-set] :as m}]
                (let [ts (if (models.tone/valid-index-tones-set? tones-to-search)
                           index-tones-set interval-tones-set)
                      ts-count (count ts)
                      intersections (set/intersection ts tones-to-search)]
                  (assoc m
                         :intersections intersections
                         :intersections-count (+ (count intersections)
                                                 (cond
                                                   (= index-tones-set tones-to-search) tones-to-search-count
                                                   (> tones-to-search-count ts-count) (- ts-count tones-to-search-count)
                                                   (< tones-to-search-count ts-count) (- tones-to-search-count ts-count)
                                                   :else 0))))))
         (sort-by :intersections-count #(compare %2 %1))
         (take 10))))

(def match-tones-with-chords
  (partial match-tones-with-coll generated-chords))

(def match-tones-with-scales
  (partial match-tones-with-coll generated-scales))

(comment
  (match-tones-with-coll generated-chords #{#{:c} #{:e} #{:g}})
  (match-tones-with-coll generated-chords #{:c :e :g})
  (match-tones-with-chords #{#{:c} #{:e} #{:g}})

  (match-tones-with-chords #{#{:c} #{:g} #{:e}})

  (match-tones-with-coll generated-scales #{#{:c} #{:e} #{:g}})
  (match-tones-with-coll generated-scales #{:e :g :c :b :d :f :a})
  (match-tones-with-scales #{#{:c} #{:e} #{:g} })
  )
