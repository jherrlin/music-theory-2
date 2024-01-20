(ns se.jherrlin.music-theory.definitions
  (:require
   [se.jherrlin.music-theory.utils :as utils]
   [se.jherrlin.music-theory.models.chord :as models-chord]
   [se.jherrlin.music-theory.models.scale :as models-scale]
   [se.jherrlin.music-theory.models.fretboard-pattern :as models-fretboard-pattern]
   [se.jherrlin.music-theory.definitions.helpers :as helpers]))



;; [:pattern :scale]
;; [:pattern :chord]
;; [:chord]
;; [:scale]



(def definitions
  (atom {:chords          {}
         :chord-patterns  {}
         :scales          {}
         :scales-patterns {}
         :ids             {}}))

(defn- define-chord
  "Interpret the chord and add it to the chord state."
  [id chord-name {:keys [suffix] :as meta-data} chord-str]
  (let [chord (helpers/define-chord id chord-name suffix chord-str meta-data)]
    (do
      (swap! definitions assoc-in [:chords chord-name] chord)
      (swap! definitions assoc-in [:ids id] chord))))

(defn- define-chord-pattern
  "Interpret the chord pattern and add it to the chord pattern state."
  [id meta-data pattern]
  )

(defn- define-scale
  "Interpret the scale and add it to the scale state."
  ([id scale-names intervals-str]
   (define-scale id scale-names {} intervals-str))
  ([id scale-names meta-data intervals-str]
   (let [scale (helpers/define-scale id scale-names meta-data intervals-str)]
     (do
       (doseq [s scale-names]
           (swap! definitions assoc-in [:scales s] scale))
         (swap! definitions assoc-in [:ids id] scale)))))

(defn- define-scale-pattern
  "Interpret the scale pattern and add it to the scale pattern state."
  [id meta-data pattern]
  )



;;
;; Chords
;;
(define-chord #uuid "1cd72972-ca33-4962-871c-1551b7ea5244"
  :major
  {:suffix       ""
   :display-text "major"
   :explanation  "major"
   :order        1}
  "1 3 5")

;;
;; Scales
;;
(define-scale #uuid "39af7096-b5c6-45e9-b743-6791b217a3df"
  #{:major :ionian}
  "1, 2, 3, 4, 5, 6, 7")
