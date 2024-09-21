(ns se.jherrlin.music-theory.music-theory
  (:require
   [malli.core :as m]
   [clojure.set :as set]
   [se.jherrlin.music-theory.instruments :as instruments]
   [se.jherrlin.music-theory.definitions :as definitions]
   [se.jherrlin.music-theory.harmonizations :as harmonizations]
   [se.jherrlin.utils :as basic-utils]
   [se.jherrlin.music-theory.general :as general]
   [se.jherrlin.music-theory.fretboard :as fretboard]
   [se.jherrlin.music-theory.models.tone :as models.tone]
   [se.jherrlin.music-theory.models.entity :as models.entity]
   [se.jherrlin.music-theory.models.harmonization :as models.harmonization]
   [se.jherrlin.music-theory.models.fretboard-matrix :as models.fretboard-matrix]))

(comment
  (remove-ns 'se.jherrlin.music-theory.music-theory)
  )

;;
;; Basic utils
;;
(def trim-matrix basic-utils/trim-matrix)
(def update-matrix basic-utils/update-matrix)
(def rotate-until basic-utils/rotate-until)
(def map-matrix basic-utils/map-matrix)
(def map-xyz-matrix basic-utils/map-xyz-matrix)


;;
;; Models
;;
(def entity models.entity/entity)
(def definitions-to-entities models.entity/definitions-to-entities)
(def valid-entity? models.entity/valid-entity?)
(def valid-entities? models.entity/valid-entities?)
(def entity-to-str models.entity/entity-to-str)
(def entities-to-str models.entity/entities-to-str)
(def str-to-entities models.entity/str-to-entities)
(def fretboard-entity? models.entity/fretboard-entity?)
(def select-entity-keys models.entity/select-entity-keys)

;;
;; Definitions
;;

(def get-definition definitions/get-definition)
(def get-definition-type definitions/definition-type)
(def get-chord definitions/chord)
(def chords (definitions/chords))
(def get-scale definitions/get-scale)
(def get-scale-intervals definitions/get-scale-intervals)
(def scales (definitions/scales))
(def scales-for-harmonization (definitions/scales-for-harmonization))
(def chord-patterns-belonging-to definitions/chord-patterns-belonging-to)
(def chord-pattern-triads-belonging-to definitions/chord-pattern-triads-belonging-to)
(def scale-patterns-for-scale-and-instrument definitions/scale-patterns-for-scale-and-instrument)

(comment
  (get-definition #uuid "1cd72972-ca33-4962-871c-1551b7ea5244")
  (get-chord :major)
  (chord-patterns-belonging-to :major :guitar)
  (chord-pattern-triads-belonging-to :major :guitar)
  (scale-patterns-for-scale-and-instrument #{:major} :guitar)
  )


;;
;; General
;;
(def all-tones (general/all-tones))
(def tones-starting-at general/tones-starting-at)
(def interval-tones general/interval-tones)
(def scales-to-chord general/scales-to-chord)
(def chords-to-scale general/chords-to-scale)
(def tones-by-key-and-indexes general/tones-by-key-and-indexes)
(def tones-by-key-and-intervals general/tones-by-key-and-intervals)
(def find-chord general/find-chord)
(def sharp-or-flat general/sharp-or-flat)
(def generated-chords
  (general/generate chords :chord/intervals :chord/indexes))

(def generated-scales
  (general/generate scales :scale/intervals :scale/indexes))

(def match-tones-with-chords
  (partial general/match-tones-with-coll generated-chords))

(def match-tones-with-scales
  (partial general/match-tones-with-coll generated-scales))


(comment
  (tones-starting-at :c)
  (match-tones-with-chords #{#{:c} #{:e} #{:g}})
  (match-tones-with-chords #{#{:c} #{:g} #{:e}})
  (match-tones-with-scales #{#{:c} #{:e} #{:g}})
  )


;;
;; Instruments
;;
(def instruments (instruments/instruments))
(def get-instrument instruments/instrument)
(def get-instrument-type instruments/get-instrument-type)
(def get-instrument-tuning instruments/get-instrument-tuning)

(comment
  instruments
  (get-instrument :five-string-banjo)
  (get-instrument-tuning :five-string-banjo)
  )


;;
;; Fretboard
;;
(def fretboard-strings fretboard/fretboard-strings)
(def with-all-tones fretboard/with-all-tones)
(def pattern-with-intervals fretboard/pattern-with-intervals)
(def pattern-with-tones fretboard/pattern-with-tones)
(def create-fretboard-matrix fretboard/create-fretboard-matrix)
(def merge-fretboards-matrixes fretboard/merge-fretboards-matrixes)
(def filter-matches fretboard/filter-matches)
(def fretboard-matrix->tonejs-dispatches fretboard/fretboard-matrix->tonejs-dispatches)
(def fretboard-matrix->tonejs-dispatches-2 fretboard/fretboard-matrix->tonejs-dispatches-2)
(def fretboard-has-matches? fretboard/matches?)
(def left-is-blank? fretboard/left-is-blank?)
(def fretboard2-keys fretboard/fretboard2-keys)
(def fretboard-matrix->fretboard2 fretboard/fretboard-matrix->fretboard2)
(def first-fret? fretboard/first-fret?)
(def circle-dom-id fretboard/circle-dom-id)
(defn with-all-intervals [interval-tones intervals fretboard-matrix]
  (let [intervals->tones (mapv vector interval-tones intervals)]
    (fretboard/with-all-intervals intervals->tones fretboard-matrix))
  )

;; fretboard2
(def root-note-color fretboard/root-note-color)
(def note-color fretboard/note-color)
(def center-text fretboard/center-text)
(def circle-color fretboard/circle-color)
(def down-right-text fretboard/down-right-text)


(comment
  (fretboard-strings
   [{:tone :g, :octave 4, :start-index 5}
    {:tone :d, :octave 3, :start-index 0}
    {:tone :g, :octave 3, :start-index 0}
    {:tone :b, :octave 3, :start-index 0}
    {:tone :d, :octave 4, :start-index 0}]
   10)
  )


;;
;; Harmonizations
;;
(def get-harmonization harmonizations/get-harmonization)
(def get-harmonization-type harmonizations/get-harmonization-type)
(def get-harmonization-chords harmonizations/get-harmonization-chords)
(def harmonizations harmonizations/harmonizations)





(defmulti calc-harmonization-chords
  (fn [{:keys [harmonization-id]}]
    (get-harmonization-type harmonization-id)))

(defmethod calc-harmonization-chords :predefined
  [{:keys [harmonization-id harmonization-scale instrument key-of]}]
  (let [harmonization-chords (get-harmonization-chords harmonization-id)
        scale-intervals      (get-scale-intervals harmonization-scale)
        interval-tones'      (interval-tones key-of scale-intervals)
        harmonization-chords (->> harmonization-chords
                                  (map (fn [{:keys [idx-fn] :as m}]
                                         (assoc m :key-of (idx-fn interval-tones'))))
                                  (mapv
                                   (fn [{:keys [key-of] :as harmonization-chord}]
                                     (let [chord
                                           (get-chord (get harmonization-chord :chord))
                                           {chord-intervals :chord/intervals} chord]
                                       (-> (merge harmonization-chord chord)
                                           (assoc :key-of key-of
                                                  :instrument instrument
                                                  :interval-tones (interval-tones
                                                                   key-of
                                                                   chord-intervals))
                                           (dissoc :idx-fn))))))]
    (basic-utils/validate
     models.harmonization/HarmonizationChords
     harmonization-chords)))

(comment
  (calc-harmonization-chords
   {:instrument          :guitar,
    :key-of              :e,
    :harmonization-id    :I7-IV7-V7-Blues,
    :harmonization-scale :major})
  )

(defmethod calc-harmonization-chords :generated
  [{:keys [harmonization-id harmonization-scale instrument key-of]}]
  (let [scale                (get-scale harmonization-scale)
        harmonization        (get-harmonization harmonization-id)
        scale-indexes        (get scale :scale/indexes)
        scale-intervals      (get scale :scale/intervals)
        chord-fn             (get harmonization :function)
        scale-interval-tones (interval-tones key-of scale-intervals)
        scale-index-tones    (tones-by-key-and-indexes key-of scale-indexes)
        found-chords         (map (fn [tone]
                                    (let [index-tones-in-chord (rotate-until
                                                                #(% tone)
                                                                scale-index-tones)]
                                      (-> (find-chord
                                           chords
                                           (chord-fn index-tones-in-chord))
                                          (assoc :key-of tone
                                                 :instrument instrument))))
                                  scale-interval-tones)
        first-is-major?      ((-> found-chords first :chord/categories) :major)
        harmonization-chords (mapv
                              #(assoc %1
                                      :idx %2
                                      :symbol %3
                                      :mode  %4
                                      :family %5)
                              found-chords        ;; 1
                              (iterate inc 1)     ;; 2
                              (if first-is-major? ;; 3
                                ["I" "ii" "iii" "IV" "V" "vi" "vii"]
                                ["i" "ii" "III" "iv" "v" "VI" "VII"])
                              (if first-is-major? ;; 4
                                [:ionian  :dorian  :phrygian :lydian :mixolydian :aeolian :locrian]
                                [:aeolian :locrian :ionian   :dorian :phrygian   :lydian  :mixolydian])
                              (if first-is-major?
                                [:tonic :subdominant :tonic :subdominant :dominant :tonic :dominant]
                                [:tonic :subdominant :tonic :subdominant :dominant :subdominant :dominant]))]
    (basic-utils/validate
     models.harmonization/HarmonizationChords
     harmonization-chords)))

(comment

  (calc-harmonization-chords
   {:instrument          :mandolin,
    :key-of              :c,
    :harmonization-id    :triads,
    :harmonization-scale :major})

  )



;;
;; Instrument data structure
;;
(defmulti instrument-data-structure
  "Generate a datastructure for the instrument from a entity."
  (fn [{:keys [id instrument key-of] :as entity} opts]
    (let [instrument-type (get-instrument-type instrument)
          definition-type (get-definition-type id)]
      [instrument-type definition-type])))

(defmethod instrument-data-structure [:fretboard [:chord]]
  [{:keys [id instrument key-of] :as entity}
   {trim-fretboard? :trim-fretboard
    :keys           [nr-of-frets as-intervals]
    :as             opts}]
  (let [intervals         (get-definition id :chord/intervals)
        interval-tones    (interval-tones key-of intervals)
        instrument-tuning (get-instrument-tuning instrument)]
    (cond->> (create-fretboard-matrix key-of nr-of-frets instrument-tuning)
      as-intervals       (with-all-intervals interval-tones intervals)
      (not as-intervals) (with-all-tones interval-tones)
      ;; trim-fretboard?    (trim-matrix #(every? nil? (map :out %)))
      :always            (basic-utils/validate models.fretboard-matrix/FretboardMatrix))))
(comment
  (instrument-data-structure
   {:id         #uuid "1cd72972-ca33-4962-871c-1551b7ea5244",
    :instrument :guitar,
    :key-of     :g}
   {:nr-of-frets 15})
  )


(defmethod instrument-data-structure [:fretboard [:chord :pattern]]
  [{:keys [id instrument key-of] :as entity}
   {trim-fretboard? :trim-fretboard
    :keys           [nr-of-frets as-intervals]
    :as             opts}]
  (let [pattern           (get-definition id :fretboard-pattern/pattern)
        instrument-tuning (get-instrument-tuning instrument)]
    (cond->> (create-fretboard-matrix key-of nr-of-frets instrument-tuning)
      as-intervals       (pattern-with-intervals key-of pattern)
      (not as-intervals) (pattern-with-tones key-of pattern)
      ;; trim-fretboard?    (trim-matrix #(every? nil? (map :out %)))
      :always            (basic-utils/validate models.fretboard-matrix/FretboardMatrix))))
(comment
  (instrument-data-structure
   {:id         #uuid "94f5f7a4-d852-431f-90ca-9e99f89bbb9c",
    :instrument :guitar,
    :key-of     :c}
   {:nr-of-frets 15})
  )

(defmethod instrument-data-structure [:fretboard [:scale]]
  [{:keys [id instrument key-of] :as entity}
   {trim-fretboard? :trim-fretboard
    :keys           [nr-of-frets as-intervals]
    :as             opts}]
  (let [intervals         (get-definition id :scale/intervals)
        interval-tones    (interval-tones key-of intervals)
        instrument-tuning (get-instrument-tuning instrument)]
    (cond->> (create-fretboard-matrix key-of nr-of-frets instrument-tuning)
      as-intervals       (with-all-intervals interval-tones intervals)
      (not as-intervals) (with-all-tones interval-tones)
      ;; trim-fretboard?    (trim-matrix #(every? nil? (map :out %)))
      :always            (basic-utils/validate models.fretboard-matrix/FretboardMatrix))))
(comment
  (instrument-data-structure
   {:id         #uuid "39af7096-b5c6-45e9-b743-6791b217a3df",
    :instrument :guitar,
    :key-of     :c}
   {:nr-of-frets  15
    :as-intervals true})
  )


(defmethod instrument-data-structure [:fretboard [:scale :pattern]]
  [{:keys [id instrument key-of] :as entity}
   {trim-fretboard? :trim-fretboard
    :keys           [nr-of-frets as-intervals]
    :as             opts}]
  (let [pattern           (get-definition id :fretboard-pattern/pattern)
        instrument-tuning (get-instrument-tuning instrument)]
    (cond->> (create-fretboard-matrix key-of nr-of-frets instrument-tuning)
      as-intervals       (pattern-with-intervals key-of pattern)
      (not as-intervals) (pattern-with-tones key-of pattern)
      ;; trim-fretboard?    (trim-matrix #(every? nil? (map :out %)))
      :always            (basic-utils/validate models.fretboard-matrix/FretboardMatrix))))
(comment
  (instrument-data-structure
   {:id         #uuid "55189945-37fa-4071-9170-b0b068a23174",
    :instrument :guitar,
    :key-of     :c}
   {:nr-of-frets  15
    :as-intervals true})
  )
