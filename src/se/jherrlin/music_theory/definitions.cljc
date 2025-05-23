(ns se.jherrlin.music-theory.definitions
  (:require
   [se.jherrlin.music-theory.models.chord :as models-chord]
   [se.jherrlin.music-theory.models.scale :as models-scale]
   [se.jherrlin.music-theory.models.fretboard-pattern :as models-fretboard-pattern]
   [se.jherrlin.music-theory.definitions.helpers :as helpers]
   [se.jherrlin.music-theory.definitions.generated-scale-patterns-mandolin :as generated-scale-patterns-mandolin]
   [se.jherrlin.music-theory.definitions.generated-scale-patterns-mandolin-aeae :as generated-scale-patterns-mandolin-aeae]
   [se.jherrlin.music-theory.definitions.generated-scale-patterns-cittern-dgdad :as generated-scale-patterns-cittern-dgdad]
   [se.jherrlin.music-theory.definitions.generated-scale-patterns-mandolin-gdad :as generated-scale-patterns-mandolin-gdad]))


(def definitions
  (let [generated-scale-patterns (merge generated-scale-patterns-mandolin/generated-scale-patterns
                                        generated-scale-patterns-mandolin-aeae/generated-scale-patterns
                                        generated-scale-patterns-cittern-dgdad/generated-scale-patterns
                                        generated-scale-patterns-mandolin-gdad/generated-scale-patterns)]
    (atom {:chords         {}
           :chord-patterns {}
           :scales         {}
           :scale-patterns generated-scale-patterns
           :ids            generated-scale-patterns})))

(comment
  (get @definitions :chords)
  (get @definitions :chord-patterns)
  (get @definitions :scales)
  (get @definitions :scale-patterns)
  )

(defn chords []
  (->> (get @definitions :chords)
       (vals)
       (sort-by :chord/order)))

(defn chord [k]
  (get-in @definitions [:chords k]))

(comment
  (chord :diminished-seventh)
  )

(defn get-scale
  [k]
  (get-in @definitions [:scales k]))

(defn get-scale-intervals
  [k]
  (get-in @definitions [:scales k :scale/intervals]))

(defn scales
  []
  (->> (get @definitions :scales)
       (vals)
       (sort-by :scale/order)))

(defn scales-for-harmonization []
  (->> (scales)
       (filter (comp #{7} count :scale/intervals))))

(defn chord-patterns []
  (->> (get @definitions :chord-patterns)
       (sort-by :fretboard-pattern/order)))

(defn chord-pattern [id]
  (get-in @definitions [:chord-patterns id]))

(defn scale-patterns-for-scale-and-instrument
  [scale-names instrument]
  {:pre [(set? scale-names)]}
  (->> (get @definitions :scale-patterns)
       (vals)
       (filter (fn [{bt :fretboard-pattern/belongs-to
                     t  :fretboard-pattern/tuning}]
                 (and (scale-names bt)
                      (= t instrument))))
       (sort-by :fretboard-pattern/order)))

(comment
  (scale-patterns-for-scale-and-instrument
   #{:natural-minor :minor :aeolian}
   :guitar)
  )

(defn chord-patterns-belonging-to [belongs-to instrument-kw]
  {:pre [(keyword? belongs-to) (keyword? instrument-kw)]}
  (->> (get @definitions :chord-patterns)
       (vals)
       (filter (fn [{bt     :fretboard-pattern/belongs-to
                     t      :fretboard-pattern/tuning
                     triad? :fretboard-pattern/triad?}]
                 (and (= bt belongs-to)
                      (= t instrument-kw)
                      (false? triad?))))
       (sort-by :fretboard-pattern/order)))

(defn chord-pattern-triads-belonging-to [belongs-to instrument]
  (->> (get @definitions :chord-patterns)
       (vals)
       (filter (fn [{bt     :fretboard-pattern/belongs-to
                     t      :fretboard-pattern/tuning
                     triad? :fretboard-pattern/triad?}]
                 (and (= bt belongs-to)
                      (= t instrument)
                      triad?)))
       (sort-by :fretboard-pattern/order)))

(defn scale-patterns []
  (->> (get @definitions :scale-patterns)
       (vals)
       (sort-by :fretboard-pattern/order)))

(defn scale-pattern [id]
  (get-in @definitions [:scale-patterns id]))

(defn ids []
  (get @definitions :ids))

(defn get-definition
  ([id]
   (get-in @definitions [:ids id]))
  ([id k]
   (get-in @definitions [:ids id k])))

(defn definition-type [id]
  (get (get-definition id) :type))

(defn- define-chord
  "Interpret the chord and add it to the chord state."
  [id chord-name {:keys [suffix] :as meta-data} chord-str]
  (let [chord (helpers/define-chord id chord-name suffix chord-str meta-data)]
    (do
      (swap! definitions assoc-in [:chords chord-name] chord)
      (swap! definitions assoc-in [:ids id] chord))))

(defn- define-chord-pattern
  "Interpret the chord pattern and add it to the chord pattern state."
  [id {:keys [belongs-to tuning] :as meta-data} pattern]
  (let [pattern (helpers/define-chord-pattern id belongs-to tuning meta-data pattern)]
    (do
      (swap! definitions assoc-in [:chord-patterns id] pattern)
      (swap! definitions assoc-in [:ids id] pattern))))

(defn- define-scale
  "Interpret the scale and add it to the scale state."
  ([id scale-names intervals-str]
   (define-scale id scale-names {} intervals-str))
  ([id scale-names meta-data intervals-str]
   (let [scale (helpers/define-scale id scale-names meta-data intervals-str)]
     (do
       (doseq [s scale-names]
         (swap! definitions assoc-in [:scales s] (assoc scale :scale s)))
       (swap! definitions assoc-in [:ids id] scale)))))

(defn- define-scale-pattern
  "Interpret the scale pattern and add it to the scale pattern state."
  [id {:keys [belongs-to tuning] :as meta-data} pattern]
  ;; No-op
  (let [pattern (helpers/define-scale-pattern id belongs-to tuning meta-data pattern)]
    (if (= tuning :mandolin)
      nil ;; We have generated patterns for mandolin
      (do
        (swap! definitions assoc-in [:scale-patterns id] pattern)
        (swap! definitions assoc-in [:ids id] pattern)))))


;; ---------------
;; Chords
;; ---------------
(define-chord #uuid "1cd72972-ca33-4962-871c-1551b7ea5244"
  :major
  {:suffix        ""
   :display-text "major"
   :explanation  "major"
   :order        1}
  "1 3 5")

(define-chord #uuid "f9426eb8-5046-474a-b4c9-62383e5b0345"
  :minor
  {:suffix        "m"
   :display-text "minor"
   :explanation  "minor"
   :order        2}
  "1 b3 5")

(define-chord #uuid "7dab3a40-8aff-4a10-9367-52a05d737f45"
  :major-maj-seven
  {:suffix       "maj7"
   :explanation "major maj 7th"
   :order        3}
  "1 3 5 7")

(define-chord #uuid "eebf1ac1-b3c5-46f1-87ac-f8d24823b730"
  :dominant-seven
  {:suffix       "7"
   :explanation "dominant 7th"
   :order        4}
  "1 3 5 b7")

(define-chord #uuid "3e260b03-e6ce-485d-8b0c-9361a2566629"
  :minor-seven
  {:suffix       "m7"
   :explanation "minor 7th"
   :order        5}
  "1 b3 5 b7")

(define-chord #uuid "1845518b-ace4-4baa-8083-6922ce33176f"
  :sus4-add-7
  {:suffix       "7sus4"
   :explanation "suspended 4 with added b7"
   :order        6}
  "1 4 5 b7")

(define-chord #uuid "ae6c7f03-819d-446e-a99a-edcee7c9d849"
  :seven-sharp-five
  {:suffix       "7#5"
   :order        6}
  "1 3 #5 b7")

(define-chord #uuid "d7a0a50b-5424-443c-83cb-d2354ba622e2"
  :seven-flat-five
  {:suffix       "7b5"
   :order        6}
  "1 3 b5 b7")

(define-chord #uuid "ede6dada-2c5d-4c63-af12-6569e89219c1"
  :diminished-seventh
  {:suffix       "dim7"
   :explanation "diminished seven"
   :text        "diminished whole, often sufixed with °"
   :order        7}
  "1 b3 b5 bb7")

(define-chord #uuid "5c957f40-8961-46a5-8e30-95fdfb827628"
  :dominant-seven-sharp-nine
  {:suffix       "7#9"
   :explanation "dominant 7th with a sharp nine"
   :text        "Also knows as the Hendrix chord."
   :order        8}
  "1 3 5 b7 #9")

(define-chord #uuid "e2f07542-bd79-424c-8bfc-401d12cb36d9"
  :dominant-seven-flat-nine
  {:suffix       "7b9"
   :explanation "dominant 9th"
   :order        9}
  "1 3 5 b7 b9")

(define-chord #uuid "c21c6b74-d211-4b05-a521-c8c2fc646c4c"
  :sixth
  {:suffix       "6"
   :explanation "sixth"
   :text        "There are 4 tones in sixth chords."
   :order       10}
  "1 3 5 6")

(define-chord #uuid "8962c55d-9bb5-4f44-a810-a52f8251730a"
  :minor-sixth
  {:suffix       "m6"
   :explanation "minor sixth"
   :text        "There are 4 tones in sixth chords."
   :order       11}
  "1 b3 5 6")

(define-chord #uuid "c1dff8c0-3c64-4e1c-a6cb-f311580141f9"
  :major-plus-5-flat-7
  {:suffix        "+7"}
  "1 3 #5 b7")

(define-chord #uuid "5c217b88-f9d5-41bb-9b89-8589105d14dd"
  :minor-maj-seven
  {:suffix       "m(maj7)"
   :explanation "minor maj 7th"}
  "1 b3 5 7")

(define-chord #uuid "4f853d42-89bb-4bcb-9462-aeb4cd379fe4"
  :sus2
  {:suffix       "sus2"
   :explanation "suspended 2"}
  "1 2 5")

(define-chord #uuid "f2722ef1-f66f-468a-a428-fecd85a9200b"
  :augmented-triad
  {:suffix       "aug"
   :explanation "augmented triad"}
  "1 3 #5")

(define-chord #uuid "b490706e-a09d-485e-8d94-1f15c294eb5b"
  :sus4
  {:suffix       "sus4"
   :explanation "suspended 4"}
  "1 4 5")

(define-chord #uuid "3b20204a-185a-4460-b845-ef93aaee36e5"
  :aug7
  {:suffix "aug7"}
  "1 3 #5 b7")

(define-chord #uuid "74fe0ffb-2611-4658-a8bf-6e20497785e5"
  :major-maj-seven-augmented-7
  {:suffix "maj7(#11)"}
  "1 3 7 #11")

(define-chord #uuid "b6855fb7-bdcf-4e18-a60e-95746bf8e7e9"
  :minor-seven-flat-5
  {:suffix       "m7b5"
   :text        "diminished half, often sufixed with Ø"
   :explanation "minor seven flat 5"}
  "1 b3 b5 b7")

(define-chord #uuid "07db914e-3a8b-4eeb-9024-5bc5a5c21a9c"
  :major-seven-flat-5
  {:suffix       "maj7b5"
   :explanation "major major seven flat 5"}
  "1 3 b5 7")

(define-chord #uuid "143fe631-8ce0-4c84-bcd8-60f39a354e78"
  :major-seven-sharp-5
  {:suffix             "(maj7)#5"
   :explanation       "major major seven sharp 5"
   :suffix-alternative "maj7#5"}
  "1 3 #5 7")

(define-chord #uuid "7d42b8e7-0b47-4b3c-af23-84461f12e723"
  :fifth
  {:suffix       "5"
   :explanation "5th"}
  "1 5")

(define-chord #uuid "1559e2cf-5f29-4831-8a8f-dddd7ad89580"
  :diminished-fifth
  {:suffix       "dim"
   :explanation "diminished fifth"}
  "1 b3 b5")

(define-chord #uuid "6cb2b165-bd51-4c36-a5f1-e64732897bbf"
  :ninth
  {:suffix       "9"
   :explanation "ninth"
   :text        "This is a dominant chord. The most important tones are 1, 3, b7 and 9. The 5 can be ignored in the chord."}
  "1 3 5 b7 9")

(define-chord #uuid "1df9a391-b6a8-46b8-bb6e-5e2ed261022a"
  :maj-ninth
  {:suffix       "maj9"
   :explanation "major ninth"
   :text        "The most important tones are 1, 3, 7 and 9. The 5 can be ignored in the chord."}
  "1 3 5 7 9")

(define-chord #uuid "f8ddfbbf-50d8-46af-a7e5-fc542cc82355"
  :minor-ninth
  {:suffix       "m9"
   :explanation "minor ninth # fifth is the least important tone, it may be ignored"
   :text        "The most important tones are 1, b3, b7 and 9. The 5 can be ignored in the chord."}
  "1 b3 5 b7 9")

(define-chord #uuid "32b7ad4f-8b47-4e9d-8e99-52225db82ab6"
  :add9
  {:suffix       "add9"
   :explanation "major with an added 9"}
  "1 3 5 9")

(define-chord #uuid "065c90b0-a8dd-4137-9864-dc8b963c1f07"
  :minor-add9
  {:suffix       "m(add9)"
   :explanation "minor with an added 9"}
  "1 b3 5 9")

(define-chord #uuid "29942046-cae8-4647-bee4-969a090ee9b2"
  :minor-flat6
  {:suffix       "mb6"
   :explanation "minor with an added flat 6"}
  "1 b3 5 b6")

(define-chord #uuid "7c6a74ff-b643-419a-b14e-b4d666bf8115"
  :minor-sixth-added9
  {:suffix       "m6/9"
   :explanation "minor sixth with an added 9"}
  "1 b3 5 6 9")

(define-chord #uuid "aa156ab4-2ddb-49dc-bbdf-2f1f45cbd9e2"
  :maj-eleventh
  {:suffix       "maj11"
   :explanation "major eleventh"}
  "1 3 5 7 9 11")

(define-chord #uuid "8816ae91-e81f-4fec-930b-9597fd1c8efb"
  :eleventh
  {:suffix       "11"
   :explanation "dominant 11"}
  "1 3 5 b7 9 11")

(define-chord #uuid "2702633f-8919-4a41-84d5-84daf43c65db"
  :minor-eleventh
  {:suffix       "m11"
   :explanation "minor eleventh"}
  "1 b3 5 b7 9 11")

(define-chord #uuid "ecd08e90-1b69-4c26-8719-d82c1c101b28"
  :thirteen
  {:suffix       "13"
   :explanation "thirteenth. Dominant"}
  "1 3 5 b7 9 11 13")
;; ---------------
;; Chords end
;; ---------------

;; ---------------
;; Scales
;; ---------------
(define-scale #uuid "39af7096-b5c6-45e9-b743-6791b217a3df"
  #{:major :ionian}
  {:order 1}
  "1, 2, 3, 4, 5, 6, 7")

(define-scale #uuid "d091b747-63b9-4db2-9daa-6e9974852080"
  #{:minor :aeolian :natural-minor}
  {:order 2}
  "1, 2, b3, 4, 5, b6, b7")

(define-scale #uuid "ddf306a1-b119-4eda-b3c3-1f5215cbe6d8"
  #{:harmonic-minor}
  {:order 3}
  "1, 2, b3, 4, 5, b6, 7")

(define-scale #uuid "6d8e0cba-658d-4072-838e-3d50d926ed0f"
  #{:melodic-minor}
  {:order 4}
  "1, 2, b3, 4, 5, 6, 7")

(define-scale #uuid "bac1ab62-34df-4232-b205-b197d25d8892"
  #{:pentatonic-blues}
  {:order 5}
  "1, b3, 4, b5, 5, b7")

(define-scale #uuid "82751272-7c3a-445e-a589-24c1ad87a30e"
  #{:pentatonic-minor}
  {:order 6}
  "1, b3, 4, 5, b7")

(define-scale #uuid "e7ad3188-1e4c-4d19-bd4b-99e97213c6f6"
  #{:pentatonic-major}
  {:order 7}
  "1, 2, 3, 5, 6")

(define-scale #uuid "b8af69c3-fe44-4f75-ab77-71ad5e132128"
  #{:pentatonic-major+b7}
  {:order 7}
  "1, 2, 3, 5, 6, b7")

(define-scale #uuid "0b675c5b-a6fe-44fe-b7cc-6596c6c570a4"
  #{:bluegrass-pentatonic-major}
  {:order 7}
  "1, 2, b3, 3, 5, 6")

(define-scale #uuid "8c0a7209-4ac4-4ec7-b8a5-e4fdaf449ad6"
  #{:lydian}
  "1, 2, 3, #4, 5, 6, 7")

(define-scale #uuid "5a5a5751-9213-4116-9ca7-894cc00be2dc"
  #{:mixolydian}
  "1, 2, 3, 4, 5, 6, b7")

(define-scale #uuid "825e9d0e-9b9d-461c-90b3-c2e71c9730aa"
  #{:dorian}
  "1, 2, b3, 4, 5, 6, b7")

(define-scale #uuid "dbf44d25-75b9-4608-8e11-ba733b6edfc0"
  #{:phrygian}
  "1, b2, b3, 4, 5, b6, b7")

(define-scale #uuid "862ef869-25e9-47f9-ab5e-8146f8a8b3e6"
  #{:locrian}
  "1, b2, b3, 4, b5, b6, b7")

(define-scale #uuid "2dd35839-9d00-45bd-b4e8-43868aa9836c"
  #{:pentatonic-neutral}
  "1, 2, 4, 5, b7")

(define-scale #uuid "0995cd25-a646-4347-918d-cfc9f06aa9a1"
  #{:diatonic}
  "1, 2, 3, 5, 6")

(define-scale #uuid "c5500473-a4e1-44c3-833b-95d0c840c9e8"
  #{:diminished}
  "1, 2, b3, 4, b5, b6, 6, 7")

(define-scale #uuid "3df70e72-dd4c-4e91-85b5-13de2bb062ce"
  #{:mixolydian-blues-hybrid}
  "1, 2, b3, 3, 4, b5, 5, 6, b7")

(define-scale #uuid "888bff34-1059-4f11-b5e0-79d6708dd3c7"
  #{:diminished-half}
  "1, b2, b3, 3, b5, 5, 6, b7")

(define-scale #uuid "4bd060d3-6a6a-48d2-910e-4c2962af1f30"
  #{:diminished-whole}
  "1, 2, b3, 4, b5, b6, 6, 7")

(define-scale #uuid "5867c2d8-e2cf-4221-872e-dc2e50507abd"
  #{:diminished-whole-tone}
  "1, b2, b3, 3, b5, b6, b7")

(define-scale #uuid "96b278ae-704e-46d7-9bd3-2b24bcd91d3b"
  #{:dominant-7th}
  "1, 2, 3, 4, 5, 6, b7")

(define-scale #uuid "1694ffaf-7fcd-4447-ada4-42f272e6ae5e"
  #{:lydian-augmented}
  "1, 2, 3, #4, #5, 6, 7")

(define-scale #uuid "6d32a072-cbbc-4b20-ae01-bda5a87071c6"
  #{:lydian-minor}
  "1, 2, 3, #4, 5, b6, b7")

(define-scale #uuid "656b338a-7938-447b-ad89-c478023a21d4"
  #{:lydian-diminished}
  "1, 2, b3, #4, 5, 6, 7")

(define-scale #uuid "a9141660-0d5b-4bb9-af22-773baff3a06c"
  #{:byzantine}
  "1 b2 3 4 5 b6 7")

(define-scale #uuid "eb6d06b0-f195-4346-8d4a-941011a57535"
  #{:enigmatic}
  {:order 9999}
  "1 b2 3 #4 #5 #6 7")

(define-scale #uuid "b40212ef-2c7a-435e-84f2-324321247e75"
  #{:persian}
  {:order 9999}
  "1 b2 3 4 b5 b6 7")

(define-scale #uuid "b27b1dc3-9184-4be2-b9a5-df4b006ad5f9"
  #{:javanese-pelog}
  {:order 9999}
  "1 b2 b3 4 5 6 b7")

(define-scale #uuid "506e26c7-fcd1-4c93-b92a-f91f95367b39"
  #{:neapolitan-minor}
  {:order 9999}
  "1 b2 b3 4 5 b6 7")

(define-scale #uuid "b2073fbb-0a86-4288-b3e5-c1d2231fbe0e"
  #{:neapolitan-major}
  {:order 9999}
  "1 b2 b3 4 5 6 7")

(define-scale #uuid "aafd9cd7-1c57-4cb4-863b-f1adf7c952ee"
  #{:hungarian-minor}
  {:order 9999}
  "1 2 b3 #4 5 b6 7")

(define-scale #uuid "534f4620-7a45-4915-8053-f711ca05a5aa"
  #{:overtone}
  {:order 9999}
  "1 2 3 #4 5 6 b7")

(define-scale #uuid "cf8348d5-fc32-4588-9ccd-9962005c25e7"
  #{:hindu}
  {:order 9999}
  "1 2 3 4 5 b6 b7")

(define-scale #uuid "ffaf5574-8dc7-4077-b14d-15764c6354ec"
  #{:romanian-major}
  {:order 9999}
  "1 b2 3 #4 5 6 b7")

(define-scale #uuid "1e0f218a-f0b6-4bea-8144-04981136fee5"
  #{:hungarian-major}
  {:order 9999}
  "1 #2 3 #4 5 6 b7")

(define-scale #uuid "702f3a3b-f8d2-470b-b1d4-ef62f916a38c"
  #{:spanish-gips}
  {:order 9999}
  "1 b2 3 4 5 b6 b7")

(define-scale #uuid "75ff9b45-8f64-4fc5-bc8a-1eb152338efb"
  #{:arabian}
  {:order 9999}
  "1 2 3 4 b5 b6 b7")

(define-scale #uuid "3ff2667c-29df-4855-afc1-cd7db100feb9"
  #{:asian}
  {:order 9999}
  "1 b2 3 4 b5 6 b7")


;; ---------------
;; Scales end
;; ---------------


;; --------------------
;; Chord patterns
;;
;; Matrixes specifies patterns on how chords looks like and where each interval
;; is located in the matrix. This corresponds to how the chord looks like on the
;; guitar fret board. `nil`s are tones on the fret board where no finger is
;; located.
;; --------------------
(define-chord-pattern #uuid "94f5f7a4-d852-431f-90ca-9e99f89bbb9c"
  {:belongs-to :major
   :tuning     :guitar}
  "3   -   -   -
   -   1   -   -
   5   -   -   -
   -   -   3   -
   -   -   -   1
   -   -   -   -")

(define-chord-pattern #uuid "3a4d8901-1742-4ccb-9fc8-62238fa2f1c0"
  {:belongs-to :major
   :tuning     :ukulele}
  "-   -   -   1
   3   -   -   -
   1   -   -   -
   5   -   -   -")

(define-chord-pattern #uuid "7a0a2ac8-5c26-40a5-b5f2-11fecc447f3e"
  {:belongs-to :major
   :tuning     :ukulele}
  "1   -   -
   5   -   -
   -   3   -
   -   -   1")

(define-chord-pattern #uuid "cb55c177-c7de-429e-9356-e6d9869805ef"
  {:belongs-to :major
   :tuning     :guitar}
  "5   -   -
   -   -   3
   -   -   1
   -   -   5
   1   -   -
   -   -   -")

(define-chord-pattern #uuid "72b1230b-2546-4380-a746-c6074c8a7278"
  {:belongs-to :major
   :tuning     :guitar}
  "1   -   -
   5   -   -
   -   3   -
   -   -   1
   -   -   5
   1   -   -")

(define-chord-pattern #uuid "03f12eab-1220-407f-9fd5-68be0d83f240"
  {:belongs-to :major
   :tuning     :guitar}
  "-   -   -   1
   3   -   -   -
   1   -   -   -
   5   -   -   -
   -   -   3   -
   -   -   -   1")

(define-chord-pattern #uuid "f115cc21-bbc0-48df-a0d5-110f52118c4e"
  {:belongs-to :major-plus-5
   :tuning     :guitar}
  "1   -   -   -
   -  #5   -   -
   -   3   -   -
   -   -   1   -
   -   -   -  #5
   1   -   -   -")

(define-chord-pattern #uuid "ddba5f7f-831e-4836-85f8-de2cb6f7127f"
  {:belongs-to :minor
   :tuning     :ukulele}
  "1   -   -
   5   -   -
  b3   -   -
   -   -   1")

(define-chord-pattern #uuid "750c8d5d-983a-4937-bbe3-0d0104f6ee39"
  {:belongs-to :minor
   :tuning     :ukulele}
  "5   -   -
   -  b3   -
   -   -   1
   -   -   5")

(define-chord-pattern #uuid "e5e74566-82e0-48e3-acba-be673c563006"
  {:belongs-to :minor
   :tuning     :guitar}
  "1   -   -
   5   -   -
  b3   -   -
   -   -   1
   -   -   5
   1   -   -")

(define-chord-pattern #uuid "9a23d114-5957-4d7c-a196-1f09b6f5c00c"
  {:belongs-to :minor
   :tuning     :guitar}
  "5   -   -
   -  b3   -
   -   -   1
   -   -   5
   1   -   -
   -   -   -")

(define-chord-pattern #uuid "a0bb5d7a-8f8b-4741-b9c8-478ff5a4cb91"
  {:belongs-to :minor
   :tuning     :guitar}
  "-  b3   -   -
   -   -   -   1
   -   -   5   -
   1   -   -   -
   -   -   -   -
   -   -   -   -")

(define-chord-pattern #uuid "32c7a71f-8992-4d1c-a5b8-5607f1e9f409"
  {:belongs-to :dominant-seven
   :tuning     :ukulele}
  "-  b7
   3   -
   1   -
   5   -")

(define-chord-pattern #uuid "74846a21-412d-4b7a-a01e-6f798fa42a5f"
  {:belongs-to :dominant-seven
   :tuning     :ukulele}
  "-   -   3
   -  b7   -
   -   -   5
   1   -   -")

(define-chord-pattern #uuid "4c264eba-ae0d-4510-a2fc-367a3d94b42a"
  {:belongs-to :dominant-seven
   :tuning     :guitar}
  "1   -   -
   5   -   -
   -   3   -
  b7   -   -
   -   -   5
   1   -   -")

(define-chord-pattern #uuid "15c84786-78e6-41e2-8492-1866f320059e"
  {:belongs-to :dominant-seven
   :tuning     :guitar}
  "-   -   3
   -  b7   -
   -   -   5
   1   -   -
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "37e97549-af79-40a5-850e-8e40e36c9ed7"
  {:belongs-to :dominant-seven
   :tuning     :guitar}
  "5   -   -
   -   -   3
  b7   -   -
   -   -   5
   1   -   -
   -   -   -")

(define-chord-pattern #uuid "a8249afc-b6aa-4db1-8aed-c2110f0ce1f6"
  {:belongs-to :dominant-seven
   :tuning     :guitar}
  "-   -   -   -
   -   -   -  b7
   -   3   -   -
   -   -   1   -
   -   -   5   -
   -   -   -   -")

(define-chord-pattern #uuid "7dca2ec3-ac71-4562-a8c8-3a7bd994374d"
  {:belongs-to :dominant-seven
   :tuning     :guitar}
  "-   -   -   -
   -   -   -   5
   1   -   -   -
   -   -   -  b7
   -   -   3   -
   -   -   -   -")

(define-chord-pattern #uuid "639a0d6d-a636-4761-9c43-3563cff792ff"
  {:belongs-to :dominant-seven
   :tuning     :guitar}
  "-   -   -  b7
   -   -   3   -
   -   -   1   -
   -   -   5   -
   1   -   -   -
   -   -   -   -")

(define-chord-pattern #uuid "e3548c9a-2522-4c01-b838-2932d5ca7eb1"
  {:belongs-to :dominant-seven
   :tuning     :guitar}
  "-   -   -
   1   -   -
   -   -  b7
   -   3   -
   -   -   1
   -   -   -")

(define-chord-pattern #uuid "a679b008-d36b-4d29-b660-71b48536be32"
  {:belongs-to :dominant-seven-sharp-nine
   :tuning     :guitar}
  "-   -
   -  #9
   -  b7
   3   -
   -   1
   -   -")

(define-chord-pattern #uuid "9498a64e-49d7-4fbd-af50-91d2dffa44c0"
  {:belongs-to :dominant-seven-flat-nine
   :tuning     :guitar}
  "-   -
  b9   -
   -  b7
   3   -
   -   1
   -   -")

(define-chord-pattern #uuid "45ccc319-651a-49ae-9cd4-6f2f2a995b36"
  {:belongs-to :minor-seven
   :tuning     :guitar
   :order      1}
  "-   -   -
   -  b3   -
  b7   -   -
   -   -   5
   1   -   -
   -   -   -")

(define-chord-pattern #uuid "8b9091f3-7452-4364-8fca-1b25d0038c60"
  {:belongs-to :minor-seven
   :tuning     :guitar
   :order      2}
  "-   -
   -   1
   5   -
   -  b3
   -  b7
   -   -")

(define-chord-pattern #uuid "899aa830-b2e7-4819-b708-27b59a87345f"
  {:belongs-to :minor-seven
   :tuning     :guitar}
  "
   1   -   -
   5   -   -
  b3   -   -
  b7   -   -
   -   -   5
   1   -   -")

(define-chord-pattern #uuid "b009d31f-3b1b-4f69-a4c8-4be4649d67a8"
  {:belongs-to :minor-seven
   :tuning     :guitar}
  "
   -  b3   -
   -  b7   -
   -   -   5
   1   -   -
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "2f43882c-298d-4e74-a6ad-b85edfb8453e"
  {:belongs-to :minor-seven
   :tuning     :guitar}
  "
   -
   5
  b3
  b7
   -
   1")

(define-chord-pattern #uuid "bd8b0fcd-03d9-46da-89ee-22b33ff8ef0b"
  {:belongs-to :fifth
   :tuning     :guitar}
  "-  -  -
   -  -  -
   -  -  -
   -  -  1
   -  -  5
   1  -  -")

(define-chord-pattern #uuid "2c99b2d8-564f-4267-8865-2447fc1e3170"
  {:belongs-to :fifth
   :tuning     :guitar}
  "
   -   -   -   -
   -   -   -   1
   -   -   5   -
   1   -   -   -
   -   -   -   -
   -   -   -   -")

(define-chord-pattern #uuid "1a40de53-085c-4d7d-ad07-e1df31ab5f96"
  {:belongs-to :diminished-fifth
   :tuning     :guitar}
  "-   -   -
   -  b3   -
   -   -   1
   -  b5   -
   1   -   -
   -   -   -")

(define-chord-pattern #uuid "109b77a4-f6fc-48bc-ac77-01876b9f4794"
  {:belongs-to :diminished-fifth
   :tuning     :guitar}
  "-   -   -
   -   -   -
   b3  -   -
   -   -   1
   -  b5   -
   1   -   -")

(define-chord-pattern #uuid "83e131cd-18ac-49ca-a050-7a750d53526e"
  {:belongs-to :major-maj-seven
   :tuning     :ukulele}
  "7   -   -   -
   -   5   -   -
   -   -   3   -
   -   -   -   1")

(define-chord-pattern #uuid "2601ea29-18be-46ce-8768-04d37f0ede59"
  {:belongs-to :major-maj-seven
   :tuning     :guitar}
  "1   -   -
   5   -   -
   -   3   -
   -   7   -
   -   -   5
   1   -   -")

(define-chord-pattern #uuid "14c1aabc-2bde-425c-86dc-eb604473d7bf"
  {:belongs-to :major-maj-seven
   :tuning     :guitar
   :order      1}
  "-   -   -
   -   -   3
   -   7   -
   -   -   5
   1   -   -
   -   -   -")

(define-chord-pattern #uuid "090c373d-87b7-4952-ad97-53c0d0e3263c"
  {:belongs-to :major-maj-seven
   :tuning     :guitar
   :order      2}
  "-   -   -   -
   -   -   -   7
   3   -   -   -
   -   1   -   -
   -   5   -   -
   -   -   -   -")

(define-chord-pattern #uuid "a2e3e5c3-da29-444e-bfa3-e34b74e0cbec"
  {:belongs-to :major-maj-seven
   :tuning     :guitar}
  "-   -   3
   -   -   7
   -   -   5
   1   -   -
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "81999502-29d7-4564-8549-71c3820a5959"
  {:belongs-to :major-maj-seven
   :tuning     :guitar}
  "-   -
   5   -
   -   3
   -   7
   -   -
   1   -")

(define-chord-pattern #uuid "efde58bf-56c3-486d-8c8a-11a071a4d943"
  {:belongs-to :sixth
   :tuning     :guitar}
  "1   -   -
   -   -   6
   -   3   -
   -   -   1
   -   -   5
   -   -   -")

(define-chord-pattern #uuid "a0c3b2f9-677f-4c6b-ba4a-e78fb1f3925a"
  {:belongs-to :sixth
   :tuning     :guitar}
  "-   -   6
   -   -   3
   -   -   1
   -   -   5
   1   -   -
   -   -   -")

(define-chord-pattern #uuid "e096b9e5-11c8-4dfe-9bfa-3b5eafe298ce"
  {:belongs-to :sixth
   :tuning     :guitar}
  "6   -   -
   3   -   -
   1   -   -
   5   -   -
   -   -   3
   6   -   -")

(define-chord-pattern #uuid "81d9db89-6031-413b-aaff-11a626251899"
  {:belongs-to :sixth
   :tuning     :guitar}
  "-   1   -
   -   5   -
   -   -   3
   6   -   -
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "f9f49ac2-0f40-46a0-ad75-4eb4b2a75d01"
  {:belongs-to :sixth
   :tuning     :guitar}
  "-   -   3
   6   -   -
   -   -   5
   1   -   -
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "a8e57c65-20a7-4f33-b6ae-6b2e639cd140"
  {:belongs-to :sixth
   :tuning     :guitar}
  "3   -   -
   -   1   -
   5   -   -
   -   -   3
   6   -   -
   3   -   -")

(define-chord-pattern #uuid "729fbe2b-e627-4535-9b36-099127f3db9c"
  {:belongs-to :sixth
   :tuning     :guitar}
  "-   -   5
   1   -   -
   -   6   -
   -   3   -
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "72a0e97c-0c7f-4318-8bf0-a6d5f1f19135"
  {:belongs-to :ninth
   :tuning     :guitar}
  "-   5
   -   9
   -  b7
   3   -
   -   1
   -   -")

(define-chord-pattern #uuid "4df3ba12-9d2d-4672-ba17-a05d0225ff4f"
  {:belongs-to :ninth
   :tuning     :guitar}
  "-   9   -
   -   -  b7
   3   -   -
   -   1   -
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "4d945cda-73e8-4dcc-b670-297eb91be3a1"
  {:belongs-to :ninth
   :tuning     :guitar}
  "-   -
   -   -
   9   -
   -  b7
   3   -
   -   1")

(define-chord-pattern #uuid "ca0174f2-6cef-4a15-a017-a8f764daab2d"
  {:belongs-to :minor-seven-flat-5
   :tuning     :guitar}
  "-   -
  b5   -
   -  b3
   -  b7
   -   -
   -   1")

(define-chord-pattern #uuid "9edf0b37-d066-4af4-bc0e-77c364df86d6"
  {:belongs-to :minor-seven-flat-5
   :tuning     :guitar}
  "-   -   -
   -   -   1
  b5   -   -
   -   -  b3
   -   -  b7
   -   -   -")

(define-chord-pattern #uuid "4e9d3630-d01d-4f0d-9bd1-d052af31f5b2"
  {:belongs-to :minor-seven-flat-5
   :tuning     :guitar}
  "-   -
   -  b3
  b7   -
   -  b5
   1   -
   -   -")

(define-chord-pattern #uuid "74773752-c21e-43ef-8bad-5faefe59b788"
  {:belongs-to :minor-seven-flat-5
   :tuning     :guitar}
  "-   1
  b5   -
   -  b3
   -  b7
   -   -
   -   -")

(define-chord-pattern #uuid "fb66c18d-c20a-4f84-b5b9-58e9e072a24c"
  {:belongs-to :minor-seven-flat-5
   :tuning     :guitar}
  "-  b3
   -  b7
   -  b5
   1   -
   -   -
   -   -")

(define-chord-pattern #uuid "dabfc0ee-6aa7-49a9-b228-51181cb93488"
  {:belongs-to :minor-seven-flat-5
   :tuning     :guitar}
  "-  b5   -
   1   -   -
   -   -  b7
  b3   -   -
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "17d5216c-9592-4c06-8c60-05aba7a5b985"
  {:belongs-to :minor-ninth
   :tuning     :guitar}
  "-   -   -
   -   -   9
   -   -  b7
  b3   -   -
   -   -   1
   -   -   -")

(define-chord-pattern #uuid "f6cc1ac8-263a-48d0-a31c-18ce131d03ce"
  {:belongs-to :eleventh
   :tuning     :guitar}
  "5
   9
  b7
  11
   1
   -")

(define-chord-pattern #uuid "59764f90-0e30-42a1-a2ea-79d2e1260b45"
  {:belongs-to :eleventh
   :tuning     :guitar}
  "-   -   -
  11   -   -
   -   9   -
   -   -  b7
   -   -   -
   -   -   1")

(define-chord-pattern #uuid "57fc33e7-ac9d-47e3-90cb-f0b2a2f8d796"
  {:belongs-to :major
   :tuning     :mandola
   :order      1}
  "-   -   -   5
   -   -   -   1
   -   -   3   -
   5   -   -   -")

(define-chord-pattern #uuid "24febab7-3332-4b0f-9018-0e5896bc5605"
  {:belongs-to :major
   :tuning     :mandola
   :order      2}
  "1   -   -
   -   -   5
   -   -   1
   -   3   -")

(define-chord-pattern #uuid "b589ffc5-b21a-4387-8440-3dd07f320cad"
  {:belongs-to :minor
   :tuning     :mandola
   :order      1}
  "-   -   -   1
   -  b3   -   -
   5   -   -   -
   1   -   -   -")

(define-chord-pattern #uuid "6808731a-3c0c-4d05-a272-297a9b90cdc1"
  {:belongs-to :minor
   :tuning     :mandola
   :order      2}
  "-  b3   -
   5   -   -
   1   -   -
   -   -   5")

(define-chord-pattern #uuid "f44d2f11-b9cd-4434-8811-73709b2b380e"
  {:belongs-to :dominant-seven
   :tuning     :mandola
   :order      1}
  "-  b7   -
   -   -   3
   5   -   -
   1   -   -")

(define-chord-pattern #uuid "1ca3225a-296d-4f82-93a3-dc5144f384bf"
  {:belongs-to :dominant-seven
   :tuning     :mandola
   :order      2}
  "5   -   -
   1   -   -
   -   -   5
  b7   -   -")

(define-chord-pattern #uuid "144e925c-52c8-4089-b855-efe5bf66d266"
  {:belongs-to :major
   :tuning     :mandolin
   :order      1}
  "-   -   -   1
   -   -   3   -
   5   -   -   -
   1   -   -   -")

(define-chord-pattern #uuid "86abde57-1178-425d-8db0-628cf39f0f83"
  {:belongs-to :major
   :tuning     :mandolin
   :order      2}
  "1   -   -
   -   -   5
   -   -   1
   -   3   -")

(define-chord-pattern #uuid "452c1047-d9c5-429f-8dd7-10b9100e86da"
  {:belongs-to :major
   :tuning     :mandolin
   :order      3}
  "-   5   -   -
   -   1   -   -
   3   -   -   -
   -   -   -   1")

(define-chord-pattern #uuid "e4db6b64-e5e1-46c1-a8ab-e1a99aa26a85"
  {:belongs-to :major
   :tuning     :mandolin}
  "-   -   3
   5   -   -
   1   -   -
   -   -   5")

(define-chord-pattern #uuid "2e35ce27-4afc-4d3e-a16d-bc01b6607d71"
  {:belongs-to :major
   :tuning     :mandolin}
  "-   -   -   5
   -   -   -   1
   -   -   3   -
   5   -   -   -")

(define-chord-pattern #uuid "8d4d421d-0996-4183-a09c-7b10d51be8e1"
  {:belongs-to :major
   :tuning     :mandolin}
  "1   -   -
   -   -   5
   -   -   1
   -   3   -")

(define-chord-pattern #uuid "44837702-2c92-4b90-adfb-7e5a82799e42"
  {:belongs-to :minor
   :tuning     :mandolin
   :order      1}
  "-   -   -   1
   -  b3   -   -
   5   -   -   -
   1   -   -   -")

(define-chord-pattern #uuid "d3eeb38f-2606-4196-8ff7-e925987e6c2c"
  {:belongs-to :minor
   :tuning     :mandolin
   :order      2}
  "-   -   -   5
   -   -   -   1
   -  b3   -   -
   5   -   -   -")

(define-chord-pattern #uuid "2467a28a-d01a-43e0-a3e4-47a776997b58"
  {:belongs-to :minor
   :tuning     :mandolin
   :order      3}
  "-   -   1   -   -   -   -
  b3   -   -   -   -   -   -
   -   -   -   -   1   -   -
   -   -   -   -   -   -   5")

(define-chord-pattern #uuid "e1ce4515-14fb-48fb-bd7d-c489b7267e55"
  {:belongs-to :major-maj-seven
   :tuning     :mandolin}
  "-   -   7
   -   -   3
   5   -   -
   1   -   -")

(define-chord-pattern #uuid "7408c360-a25a-402f-bf52-9c609f6740ee"
  {:belongs-to :major-maj-seven
   :tuning     :mandolin}
  "1   -   -
   -   -   5
   -   7   -
   -   3   -")

(define-chord-pattern #uuid "b5b5e1a8-eed8-4398-8f65-725d050aeb57"
  {:belongs-to :major-maj-seven
   :tuning     :mandolin}
  "-   5   -
   -   1   -
   3   -   -
   -   -   7")

(define-chord-pattern #uuid "0f4522ba-fefd-4af9-bb54-664f79f8b05a"
  {:belongs-to :major-maj-seven
   :tuning     :mandolin}
  "-   -   3   -   -
   -   -   -   -   7
   1   -   -   -   -
   -   -   5   -   -")

(define-chord-pattern #uuid "08c87a5f-bdb3-4d43-8a60-39a50d864ebf"
  {:belongs-to :minor-seven-flat-5
   :tuning     :mandolin}
  "-  b5   -
   -   -   1
  b3   -   -
   -   -  b7")

(define-chord-pattern #uuid "08726a63-271a-4424-a4f2-5b8866e24a5d"
  {:belongs-to :minor-seven-flat-5
   :tuning     :mandolin}
  "-   -   -   -   -   1
   -   -   -  b3   -   -
   -  b5   -   -   -   -
  b7   -   -   -   -   -")

(define-chord-pattern #uuid "bdb5b4c7-c5d2-4c68-b4d4-e980b4aad494"
  {:belongs-to :minor-seven-flat-5
   :tuning     :mandolin}
  "-   -  b7
   -   -  b3
  b5   -   -
   -   1   -")

(define-chord-pattern #uuid "c44cdfce-5408-42f1-94b9-7c88b4a37bf3"
  {:belongs-to :dominant-seven
   :tuning     :mandolin
   :order      1}
  "-   -   3   -
   -   -   -  b7
   1   -   -   -
   -   -   5   -")

(define-chord-pattern #uuid "f812e550-0fef-4dd4-86fd-026d324da5b0"
  {:belongs-to :dominant-seven
   :tuning     :mandolin
   :order      2}
  "-  b7   -
   -   -   3
   5   -   -
   1   -   -")

(define-chord-pattern #uuid "d87cdf9b-9d38-40d6-852e-ff6313cce4d8"
  {:belongs-to :dominant-seven
   :tuning     :mandolin
   :order      3}
  "-   -   -   -   -   1
   -   -   -   -   3   -
   -   -   5   -   -   -
  b7   -   -   -   -   -")

(define-chord-pattern #uuid "f4b0414a-1175-4a06-bef4-44839fddd293"
  {:belongs-to :dominant-seven
   :tuning     :mandolin
   :order      4}
  "5   -   -   -   -   -
   -   -   -   -   3   -
   -   -   -   -   -  b7
   -   -   1   -   -   -")

(define-chord-pattern #uuid "3974da8c-aad9-48f0-83a4-99b2700d7e56"
  {:belongs-to :minor-seven
   :tuning     :mandolin}
  "-  b7
   -  b3
   5   -
   1   -")

(define-chord-pattern #uuid "5613a49c-a44e-455f-83f3-7f8592de9f20"
  {:belongs-to :minor-seven
   :tuning     :mandolin}
  "1   -   -
   -   -   5
  b7   -   -
  b3   -   -")

(define-chord-pattern #uuid "1515d7b2-0016-41a2-95ab-25a862469494"
  {:belongs-to :minor-seven
   :tuning     :mandolin}
  "-   -   5
   -   -   1
  b3   -   -
   -   -  b7")

(define-chord-pattern #uuid "1a808ddd-db74-4076-b384-67e6b5059b58"
  {:belongs-to :major
   :tuning     :tres-cubano
   :order      1}
  "5  -  -
   -  3  -
   -  -  1")

(define-chord-pattern #uuid "c6936da6-8370-46d7-9f36-7e8b860d0a83"
  {:belongs-to :major
   :tuning     :tres-cubano
   :order      2}
  "3
   1
   5")

(define-chord-pattern #uuid "1143df4f-9a9e-43a2-9e89-99712784ca32"
  {:belongs-to :major
   :tuning     :tres-cubano
   :order      3}
  "-  1  -
   5  -  -
   -  -  3")

(define-chord-pattern #uuid "358351b7-3713-4667-908a-3145e90a8637"
  {:belongs-to :minor
   :tuning     :tres-cubano
   :order      1}
  "5  -  -
  b3  -  -
   -  -  1")

(define-chord-pattern #uuid "37e7536a-9e99-481f-8907-26bcf651e7a9"
  {:belongs-to :minor
   :tuning     :tres-cubano
   :order      2}
  "-  1
   5  -
   - b3")

(define-chord-pattern #uuid "53e19294-9978-41af-94b9-f327164a8788"
  {:belongs-to :minor
   :tuning     :tres-cubano
   :order      3}
  "b3 -
   -  1
   -  5")

(define-chord-pattern #uuid "a9acea3e-3069-4c22-9c71-076721597739"
  {:belongs-to :major
   :tuning     :guitar
   :order      1}
  "3   -
   -   1
   5   -
   -   -
   -   -
   -   -")

(define-chord-pattern #uuid "b35c4cda-7d75-4936-95c5-dbaad7c4e08c"
  {:belongs-to :major
   :tuning     :guitar
   :order      2}
  "5   -   -
   -   -   3
   -   -   1
   -   -   -
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "e267154d-3f5b-4124-bf2d-06b9e3e4a621"
  {:belongs-to :major
   :tuning     :guitar
   :order      3}
  "1   -   -
   5   -   -
   -   3   -
   -   -   -
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "6e1242cd-2b41-424e-b8b0-b52a818697a1"
  {:belongs-to :major
   :tuning     :guitar
   :order      4}
  "-
   3
   1
   5
   -
   -")

(define-chord-pattern #uuid "ccdd8b36-065c-4170-ad4a-5f4086c308e9"
  {:belongs-to :major
   :tuning     :guitar
   :order      5}
  "-   -   -
   5   -   -
   -   3   -
   -   -   1
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "a2479488-683d-4daf-8f9f-f2beb7fe3049"
  {:belongs-to :major
   :tuning     :guitar
   :order      6}
  "-   -   -
   -   1   -
   5   -   -
   -   -   3
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "2ec51c51-9d7f-498a-b487-3945d943a341"
  {:belongs-to :major
   :tuning     :guitar
   :order      7}
  "-   -   -
   -   -   -
   3   -   -
   -   1   -
   -   5   -
   -   -   -")

(define-chord-pattern #uuid "1b83bc79-54d7-4eaf-a900-9873701eb3f4"
  {:belongs-to :major
   :tuning     :guitar
   :order      8}
  "-   -   -   -
   -   -   -   -
   5   -   -   -
   -   -   3   -
   -   -   -   1
   -   -   -   -")

(define-chord-pattern #uuid "2c0e915c-5b08-4bb7-aefa-def3d29323e7"
  {:belongs-to :major
   :tuning     :guitar
   :order      9}
  "-   -   -
   -   -   -
   1   -   -
   5   -   -
   -   -   3
   -   -   -")

(define-chord-pattern #uuid "066acddd-a338-4032-a6c5-773dbf956238"
  {:belongs-to :major
   :tuning     :guitar
   :order      10}
  "-   -   -
   -   -   -
   -   -   -
   1   -   -
   5   -   -
   -   -   3")

(define-chord-pattern #uuid "ddcdee89-b132-496e-a81a-6c50c0307ca7"
  {:belongs-to :major
   :tuning     :guitar
   :order      11}
  "-   -
   -   -
   -   -
   3   -
   -   1
   -   5")

(define-chord-pattern #uuid "38a5c7cb-7908-4abe-b767-55b307030cfd"
  {:belongs-to :major
   :tuning     :guitar
   :order      12}
  "-   -   -   -
   -   -   -   -
   -   -   -   -
   5   -   -   -
   -   -   3   -
   -   -   -   1")

(define-chord-pattern #uuid "9e7ff5b5-8ed6-4d7f-8919-053f3f8787d1"
  {:belongs-to :minor
   :tuning     :guitar
   :order      1}
  "5   -   -
   -  b3   -
   -   -   1
   -   -   -
   -   -   -
   -   -   -")

(define-chord-pattern #uuid "4db09dd6-9a44-4a1b-8c0f-6ed82796c8b5"
  {:belongs-to :minor
   :tuning     :guitar
   :order      2}
  "1
   5
  b3
   -
   -
   -")

(define-chord-pattern #uuid "010c370f-cb51-4c26-9aa4-ba4f25be8118"
  {:belongs-to :minor
   :tuning     :guitar
   :order      3}
  "b3   -   -
    -   -   1
    -   5   -
    -   -   -
    -   -   -
    -   -   -")

(define-chord-pattern #uuid "6ebef587-b755-4c34-80e9-ed6781c5badb"
  {:belongs-to :minor
   :tuning     :guitar
   :order      4}
  " -   -
    -   1
    5   -
    -  b3
    -   -
    -   -")

(define-chord-pattern #uuid "cb43eb3e-31b5-4473-ab86-f34efe038297"
  {:belongs-to :minor
   :tuning     :guitar
   :order      5}
  " -   -
   b3   -
    -   1
    -   5
    -   -
    -   -")

(define-chord-pattern #uuid "dbb16753-9b14-4fa7-9192-8dd43e8f677e"
  {:belongs-to :minor
   :tuning     :guitar
   :order      6}
  " -   -   -
    5   -   -
   b3   -   -
    -   -   1
    -   -   -
    -   -   -")

(define-chord-pattern #uuid "1b671313-f497-41b2-8d58-ee72503d8e15"
  {:belongs-to :minor
   :tuning     :guitar
   :order      7}
  " -   -   -   -
    -   -   -   -
    5   -   -   -
    -  b3   -   -
    -   -   -   1
    -   -   -   -")

(define-chord-pattern #uuid "40aa93ae-2bdc-44a0-95a3-d98004cc46d6"
  {:belongs-to :minor
   :tuning     :guitar
   :order      8}
  " -   -
    -   -
    1   -
    5   -
    -  b3
    -   -")

(define-chord-pattern #uuid "076078fe-00ec-4471-9ec1-06caedf26a81"
  {:belongs-to :minor
   :tuning     :guitar
   :order      9}
  " -   -   -
    -   -   -
   b3   -   -
    -   -   1
    -   -   5
    -   -   -")

(define-chord-pattern #uuid "c92c3a4c-739c-440c-8cfa-70643213f1fd"
  {:belongs-to :minor
   :tuning     :guitar
   :order      10}
  " -   -   -
    -   -   -
    -   -   -
   b3   -   -
    -   -   1
    -   -   5")

(define-chord-pattern #uuid "7a046538-822a-47ac-ad1f-f0efdda910d1"
  {:belongs-to :minor
   :tuning     :guitar
   :order      11}
  " -   -   -   -
    -   -   -   -
    -   -   -   -
    5   -   -   -
    -  b3   -   -
    -   -   -   1")

(define-chord-pattern #uuid "f2625159-e4bd-45b1-863a-25e4caaebdcd"
  {:belongs-to :minor
   :tuning     :guitar
   :order      12}
  " -   -
    -   -
    -   -
    1   -
    5   -
    -  b3")

(define-chord-pattern #uuid "caa73690-3602-42f3-b11c-1ffe12ae04a6"
  {:belongs-to :major
   :tuning     :mandolin
   :order      1}
  "-   -   -   -
   -   1   -   -
   3   -   -   -
   -   -   -   1")

(define-chord-pattern #uuid "319567c3-4218-4baf-8eea-681a2f9df2e0"
  {:belongs-to :major
   :tuning     :mandolin
   :order      2}
  "-   -   -
   -   -   3
   5   -   -
   1   -   -")

(define-chord-pattern #uuid "3bed9889-984e-4f4d-9c57-aa3096e9fa7f"
  {:belongs-to :major
   :tuning     :mandolin
   :order      3}
  "-   -   3
   5   -   -
   1   -   -
   -   -   -")

(define-chord-pattern #uuid "87d66805-8b91-4d70-aa33-257ac33b01d1"
  {:belongs-to :major
   :tuning     :mandolin
   :order      4}
  "-   -
   -   5
   -   1
   3   -")

(define-chord-pattern #uuid "914ebc94-498a-4d0c-8963-01451c64ff6f"
  {:belongs-to :major
   :tuning     :mandolin
   :order      5}
  "-   -   -   -
   -   -   -   1
   -   -   3   -
   5   -   -   -")

(define-chord-pattern #uuid "685f1cf5-5d85-4606-a3c7-eab4da30e993"
  {:belongs-to :minor
   :tuning     :mandolin
   :order      1}
  "-   -   -   -
   -  b3   -   -
   5   -   -   -
   1   -   -   -")

(define-chord-pattern #uuid "472cdb95-b8cd-40cd-8575-d571331a0fdd"
  {:belongs-to :minor
   :tuning     :mandolin
   :order      2}
  "-  b3   -   -
   5   -   -   -
   1   -   -   -
   -   -   -   -")

(define-chord-pattern #uuid "213c6887-c7ed-4a0c-b70e-422fb31fed90"
  {:belongs-to :minor
   :tuning     :mandolin
   :order      3}
  "-   -   -   -
   -   -   -   1
   -  b3   -   -
   5   -   -   -")

(define-chord-pattern #uuid "4ce974f9-99a6-4e89-927a-58ae770c63b1"
  {:belongs-to :minor
   :tuning     :mandolin
   :order      4}
  "-   -   -
   -   -   5
   -   -   1
  b3   -   -")
;; --------------------
;; Chord patterns end
;; --------------------

;; --------------------
;; Modes
;; --------------------
(define-scale-pattern #uuid "55189945-37fa-4071-9170-b0b068a23174"
  {:belongs-to :ionian
   :tuning     :guitar
   :order      1}
  "7   1   -   2
   -   5   -   6
   2   -   3   4
   6   -   7   1
   3   4   -   5
   -   1   -   2")

(define-scale-pattern #uuid "1aaa72af-7c36-4b87-8e22-b1b4a719ed1b"
  {:belongs-to :ionian
   :tuning     :guitar
   :order      2}
  "-   -   -   -
   -   -   -   -
   -   -   -   -
   6   -   7   1
   3   4   -   5
   -   1   -   2")

(define-scale-pattern #uuid "8e9ee464-1a23-459a-82f7-9cd30728215a"
  {:belongs-to :ionian
   :tuning     :guitar
   :order      3}
  "-   -   -   -
   -   -   -   -
   6   -   7   1
   3   4   -   5
   -   1   -   2
   -   -   -   -")

(define-scale-pattern #uuid "cec2cd9d-ae7f-4d3e-8107-aaf22aaf4004"
  {:belongs-to :ionian
   :tuning     :guitar
   :order      4}
  "-   -   -   -   -
   -   6   -   7   1
   3   4   -   5   -
   -   1   -   2   -
   -   -   -   -   -
   -   -   -   -   -")

(define-scale-pattern #uuid "e0c2d152-729c-45ce-b59f-1088677d010b"
  {:belongs-to :ionian
   :tuning     :guitar
   :order      5}
  "-   -   -   -   -
   6   -   7   1   -
   4   -   5   -   -
   1   -   2   -   3
   -   -   -   -   -
   -   -   -   -   -")

(define-scale-pattern #uuid "dbc69a09-b3dc-4bfa-a4df-6dd767b65d25"
  {:belongs-to :ionian
   :tuning     :guitar
   :order      5}
  "6   -   7   1
   3   4   -   5
   1   -   2   -
   -   -   -   -
   -   -   -   -
   -   -   -   -")

(define-scale-pattern #uuid "fb4b0879-0664-49a2-a5f6-e8f46ccc4fa4"
  {:belongs-to :ionian
   :tuning     :guitar
   :order      6}
  "-   -   -   -
   7   1   -   -
   5   -   6   -
   2   -   3   4
   -   -   -   1
   -   -   -   -")

(define-scale-pattern #uuid "900094ba-9561-4bca-8750-f21f47d08c27"
  {:belongs-to :mixolydian
   :tuning     :guitar}
  "-   1   -   2   -
   -   5   -   6  b7
   2   -   3   4   -
   6  b7   -   1   -
   3   4   -   5   -
   -   1   -   2   -")

(define-scale-pattern #uuid "1a422a8c-b40c-4c91-9734-b41f437ddc51"
  {:belongs-to :mixolydian
   :string     6
   :tuning     :guitar}
  "-   -   -   -
   -   -   -   -
   -   -   -   -
   6  b7   -   1
   3   4   -   5
   -   1   -   2")

(define-scale-pattern #uuid "5c63f201-aee6-4ec8-a224-5ed856a0b8ab"
  {:belongs-to :mixolydian
   :string     5
   :tuning     :guitar}
  "-   -   -   -
   -   -   -   -
   6  b7   -   1
   3   4   -   5
   -   1   -   2
   -   -   -   -")

(define-scale-pattern #uuid "4422ee55-0d0d-4944-a0b7-f5ba18a6b8fe"
  {:belongs-to :mixolydian
   :string     4
   :tuning     :guitar}
  "-   -   -   -   -
   -   6  b7   -   1
   3   4   -   5   -
   -   1   -   2   -
   -   -   -   -   -
   -   -   -   -   -")

(define-scale-pattern #uuid "66e4daed-4d20-485a-b7fa-954c7c876a51"
  {:belongs-to :mixolydian
   :string     3
   :tuning     :guitar}
  "6  b7   -   1
   3   4   -   5
   1   -   2   -
   -   -   -   -
   -   -   -   -
   -   -   -   -")

(define-scale-pattern #uuid "86adb0c7-0f12-46c2-90f1-6634e3da8cf2"
  {:belongs-to :aeolian
   :tuning     :guitar}
  "-   1   -   2  b3
   -   5  b6   -  b7
   2  b3   -   4   -
   -  b7   -   1   -
   -   4   -   5  b6
   -   1   -   2  b3")

(define-scale-pattern #uuid "52340c31-5897-42fe-8920-b5ba0b6d9d61"
  {:belongs-to :aeolian
   :string     6
   :tuning     :guitar}
  "-   -   -   -
   -   -   -   -
   -   -   -   -
  b7   -   1   -
   4   -   5  b6
   1   -   2  b3")

(define-scale-pattern #uuid "45897120-9d79-4272-baf9-f90b4d3b4fb0"
  {:belongs-to :aeolian
   :string     5
   :tuning     :guitar}
  "-   -   -   -
   -   -   -   -
  b7   -   1   -
   4   -   5  b6
   1   -   2  b3
   -   -   -   -")

(define-scale-pattern #uuid "473802da-be04-4b35-99c4-4f8c2557169c"
  {:belongs-to :aeolian
   :string     4
   :tuning     :guitar}
  "-   -   -   -
   -  b7   -   1
   4   -   5  b6
   1   -   2  b3
   -   -   -   -
   -   -   -   -")

(define-scale-pattern #uuid "228ead56-2275-459d-819d-52ddd40c3370"
  {:belongs-to :aeolian
   :string     3
   :tuning     :guitar}
  "-  b7   -   1   -
   -   4   -   5  b6
   1   -   2  b3   -
   -   -   -   -   -
   -   -   -   -   -
   -   -   -   -   -")

(define-scale-pattern #uuid "9c1b21b5-028a-4a0c-a201-d592e3e319d3"
  {:belongs-to :dorian
   :tuning     :guitar
   :order      4}
  "-   1   -   2  b3
   -   5   -   6  b7
   2  b3   -   4   -
   6  b7   -   1   -
   -   4   -   5   -
   -   1   -   2  b3")

(define-scale-pattern #uuid "920adafa-32a3-4c21-b6e0-5186d2984348"
  {:belongs-to :dorian
   :tuning     :guitar
   :order      1}
  "-   -   -   6  b7   -   1
   -   -   -   -   4   -   5
   6  b7   -   1   -   2  b3
   -   4   -   5   -   -   -
   -   1   -   2  b3   -   -
   -   -   -   -   -   -   -")

(define-scale-pattern #uuid "8d921443-11e6-4bfc-b10e-f017ce748375"
  {:belongs-to :dorian
   :string     6
   :tuning     :guitar
   :order      5}
  "-   -   -   -   -
   -   -   -   -   -
   -   -   -   -   -
   6  b7   -   1   -
   -   4   -   5   -
   -   1   -   2  b3")

(define-scale-pattern #uuid "dd98e6d1-68c7-429a-b8a5-c5c18b898b59"
  {:belongs-to :dorian
   :string     5
   :tuning     :guitar
   :order      6}
  "-   -   -   -   -
   -   -   -   -   -
   6  b7   -   1   -
   -   4   -   5   -
   -   1   -   2  b3
   -   -   -   -   -")

(define-scale-pattern #uuid "8168c9d7-6201-4e0c-88b3-f5bcba5d68f1"
  {:belongs-to :dorian
   :string     4
   :tuning     :guitar
   :order      2}
  "-   -   -   -
   6  b7   -   1
   4   -   5   -
   1   -   2  b3
   -   -   -   -
   -   -   -   -")

(define-scale-pattern #uuid "5bae88ab-d90c-4f4e-8e79-6783aaa51788"
  {:belongs-to :dorian
   :string     3
   :tuning     :guitar
   :order      3}
  "6  b7   -   1
   -   4   -   5
   1   -   2  b3
   -   -   -   -
   -   -   -   -
   -   -   -   -")

(define-scale-pattern #uuid "5d84995d-72ba-4626-aadf-299f4f26152b"
  {:belongs-to :dorian
   :tuning     :guitar
   :order      10}
  "-   -   -   -
   -   1   -   -
   5   -   6  b7
   2  b3   -   4
   -   -   -   1
   -   -   -   -")

(define-scale-pattern #uuid "24aeb3b1-99dd-46bf-953a-fb21ac41c88e"
  {:belongs-to :phrygian
   :tuning     :guitar}
  "1  b2   -  b3
   5  b6   -  b7
  b3   -   4   -
  b7   -   1  b2
   4   -   5  b6
   1  b2   -  b3")

(define-scale-pattern #uuid "3b414277-b301-40cc-bb30-238b82c21098"
  {:belongs-to :phrygian
   :tuning     :guitar}
  "-   -   -   -
   -   -   -   -
   -   -   -   -
  b7   -   1   -
   4   -   5  b6
   1  b2   -  b3")

(define-scale-pattern #uuid "63a3ddd4-2430-451d-90fe-47522e819cb9"
  {:belongs-to :phrygian
   :tuning     :guitar}
  "-   -   -   -
   -   -   -   -
  b7   -   1   -
   4   -   5  b6
   1  b2   -  b3
   -   -   -   -")

(define-scale-pattern #uuid "1fd0e509-9f40-4c20-8070-03f96477873c"
  {:belongs-to :phrygian
   :tuning     :guitar}
  "-   -   -   -
   -  b7   -   1
   4   -   5  b6
   1  b2   -  b3
   -   -   -   -
   -   -   -   -")

(define-scale-pattern #uuid "a2f11495-c4f9-48b2-b18f-7de5a97763bb"
  {:belongs-to :phrygian
   :tuning     :guitar}
  "-  b7   -   1   -
   -   4   -   5  b6
   1  b2   -  b3   -
   -   -   -   -   -
   -   -   -   -   -
   -   -   -   -   -")

(define-scale-pattern #uuid "cffc762f-b2aa-43bc-9651-29668ab982db"
  {:belongs-to :lydian
   :tuning     :guitar}
  "7   1   -   2
  b5   5   -   6
   2   -   3   -
   6   -   7   1
   3   -  b5   5
   -   1   -   2")

(define-scale-pattern #uuid "c2ece510-024e-4b38-a39e-0f776f93665e"
  {:belongs-to :lydian
   :tuning     :guitar
   :order      1}
  "-   -   -   6   -   7   1
   -   -   -   3   -  #4   5
   6   -   7   1   -   2   -
   3   -  #4   5   -   -   -
   -   1   -   2   -   -   -
   -   -   -   -   -   -   -")

(define-scale-pattern #uuid "37e8a1b9-79b6-46bd-ae0b-37b6af92ebeb"
  {:belongs-to :lydian
   :tuning     :guitar}
  "-   -   -   -
   -   -   -   -
   -   -   -   -
   6   -   7   1
   3   -  #4   5
   -   1   -   2")

(define-scale-pattern #uuid "ff997c3d-26e9-4805-8812-e087f468238e"
  {:belongs-to :lydian
   :tuning     :guitar}
  "-   -   -   -
   -   -   -   -
   6   -   7   1
   3   -  #4   5
   -   1   -   2
   -   -   -   -")

(define-scale-pattern #uuid "b66957a2-c045-4a48-aa73-7b6014e6b451"
  {:belongs-to :lydian
   :tuning     :guitar}
  "-   -   -   -   -
   -   6   -   7   1
   3   -  #4   5   -
   -   1   -   2   -
   -   -   -   -   -
   -   -   -   -   -")

(define-scale-pattern #uuid "6c906182-ab7d-4ebe-845f-5f7381c84dc3"
  {:belongs-to :lydian
   :tuning     :guitar}
  "6   -   7   1
   3   -  #4   5
   1   -   2   -
   -   -   -   -
   -   -   -   -
   -   -   -   -")

(define-scale-pattern #uuid "b8594762-64ed-48f9-bcea-c1ddae24a610"
  {:belongs-to :locrian
   :tuning     :guitar}
  "1  b2   -  b3
   -  b6   -  b7
  b3   -   4  b5
  b7   -   1  b2
   4  b5   -  b6
   1  b2   -  b3")

(define-scale-pattern #uuid "5e44c225-8508-472c-8121-03d9b0408e3d"
  {:belongs-to :locrian
   :tuning     :guitar}
  "-   -   -   -
   -   -   -   -
   -   -   -   -
  b7   -   1   -
   4  b5   -  b6
   1  b2   -  b3")

(define-scale-pattern #uuid "30fdd92d-4ef5-42a2-80eb-daf2245ec637"
  {:belongs-to :locrian
   :tuning     :guitar}
  "-   -   -   -
   -   -   -   -
  b7   -   1   -
   4  b5   -  b6
   1  b2   -  b3
   -   -   -   -")

(define-scale-pattern #uuid "d92fed54-eb65-40df-ada3-b707228d782f"
  {:belongs-to :locrian
   :tuning     :guitar}
  "-   -   -   -
   -  b7   -   1
   4  b5   -  b6
   1  b2   -  b3
   -   -   -   -
   -   -   -   -")

(define-scale-pattern #uuid "f1e11c06-deda-4458-b020-119dd3713b9c"
  {:belongs-to :locrian
   :tuning     :guitar}
  "-  b7   -   1   -
   -   4  b5   -  b6
   1  b2   -  b3   -
   -   -   -   -   -
   -   -   -   -   -
   -   -   -   -   -")

(define-scale-pattern #uuid "81610fe6-98c3-441f-b361-5a268b8dd45a"
  {:belongs-to :mixolydian-blues-hybrid
   :tuning     :guitar}
  "-   1   -   2  b3
   -   5   -   6  b7
   2  b3   3   4  b5
   6  b7   -   1   -
   3   4  b5   5   -
   -   1   -   2  b3")
;; --------------------
;; Modes end
;; --------------------

;; ---------------
;; Scales patterns
;; ---------------
(define-scale-pattern #uuid "895f4f5d-947a-4645-8af6-2a02fa75f0e1"
  {:belongs-to :pentatonic-major
   :tuning     :mandolin}
  "5   -   6   -   -   1
   1   -   2   -   3   -
   -   -   -   -   -   -
   -   -   -   -   -   -")

(define-scale-pattern #uuid "4a5a1484-0fe6-4e7e-bd72-288cdb61e369"
  {:belongs-to :pentatonic-major
   :tuning     :mandolin}
  "-   -   -   -   -   -
   5   -   6   -   -   1
   1   -   2   -   3   -
   -   -   -   -   -   -")

(define-scale-pattern #uuid "19acc7ba-5145-4b8b-a936-e9c603347d8e"
  {:belongs-to :pentatonic-major
   :tuning     :mandolin}
  "-   -   -   -   -   -
   -   -   -   -   -   -
   5   -   6   -   -   1
   1   -   2   -   3   -")

(define-scale-pattern #uuid "ad111254-d2a1-46ff-a8ae-40f3f52e2ed5"
  {:belongs-to :pentatonic-major
   :tuning     :mandolin}
  "-   -   -   -   -   -
   6   -   -   1   -   -
   2   -   3   -   -   5
   -   -   -   -   -   1")

(define-scale-pattern #uuid "51068b44-d374-49a0-baa8-07d69a6a7597"
  {:belongs-to :pentatonic-major
   :tuning     :mandolin}
  "-   -   -   -   -   -
   -   1   -   -   -   -
   3   -   -   5   -   6
   -   -   -   1   -   2")

(define-scale-pattern #uuid "72c25a90-86bb-40cd-874c-71c153e157b5"
  {:belongs-to :pentatonic-major
   :tuning     :mandolin}
  "-   1   -   -   -   -
   3   -   -   5   -   6
   -   -   -   1   -   2
   -   -   -   -   -   -")

(define-scale-pattern #uuid "5c4d2258-c71b-4cee-8e33-86769891677f"
  {:belongs-to :pentatonic-major
   :tuning     :mandolin}
  "-   -   -   -   -   -
   -   -   -   -   -   -
   -   1   -   2   -   3
   3   -   -   5   -   6")

(define-scale-pattern #uuid "40763c46-3657-47da-be85-340d0ca354d6"
  {:belongs-to :pentatonic-major
   :tuning     :mandolin}
  "-   -   -   -   -   -
   -   1   -   2   -   3
   3   -   -   5   -   6
   -   -   -   -   -   -")

(define-scale-pattern #uuid "6df97b1a-2a94-4f1e-8b3d-cba461494ea3"
  {:belongs-to :pentatonic-major
   :tuning     :mandolin}
  "-   1   -   2   -   3
   3   -   -   5   -   6
   -   -   -   -   -   -
   -   -   -   -   -   -")

(define-scale-pattern #uuid "0d6c5af6-e606-4118-b16e-b9cb3f7f28e7"
  {:belongs-to :pentatonic-major
   :tuning     :mandolin}
  "-   -   -   -   -   -
   -   -   -   -   -   -
   2   -   3   -   -   5
   5   -   6   -   -   1")

(define-scale-pattern #uuid "0f1a1dbe-d5eb-48cd-9532-4968b5704c1d"
  {:belongs-to :pentatonic-major
   :tuning     :mandolin}
  "-   -   -   -   -   -
   2   -   3   -   -   5
   5   -   6   -   -   1
   -   -   -   -   -   -")

(define-scale-pattern #uuid "3f01ecb7-5a7e-4b53-813d-7c7339575b87"
  {:belongs-to :pentatonic-major
   :tuning     :mandolin}
  "2   -   3   -   -   5
   5   -   6   -   -   1
   -   -   -   -   -   -
   -   -   -   -   -   -")

(define-scale-pattern #uuid "4bdb1907-9f82-43cc-99a8-3ba4d7802ade"
  {:belongs-to :pentatonic-blues
   :tuning     :mandolin
   :order      1}
  "5   -   -  b7   -   1   -
   1   -   -  b3   -   4  b5
   4  b5   5   -   -  b7   -
   -   -   1   -   -  b3   -")

(define-scale-pattern #uuid "c2369bd8-e82f-4405-82b7-222b9c2614bd"
  {:belongs-to :pentatonic-blues
   :tuning     :guitar
   :order      1}
  "-   -   -   -
   -   -   -   -
   -   -   -   -
  b7   -   1   -
   4  b5   5   -
   1   -   -  b3")

(define-scale-pattern #uuid "442d3af1-1453-40ed-b5e3-8afc271cb168"
  {:belongs-to :pentatonic-blues
   :tuning     :guitar
   :order      2}
  "-   -   -   -
   -   -   -   -
  b7   -   1   -
   4  b5   5   -
   1   -   -  b3
   -   -   -   -")

(define-scale-pattern #uuid "3170e394-9e23-478f-a0f7-5b585dfdaa9c"
  {:belongs-to :pentatonic-blues
   :tuning     :guitar
   :order      3}
  "-   -   -   -
   -  b7   -   1
   4  b5   5   -
   1   -   -  b3
   -   -   -   -
   -   -   -   -")

(define-scale-pattern #uuid "fa1c1ba7-2bef-4b1a-90c4-3befa39613ab"
  {:belongs-to :pentatonic-blues
   :tuning     :guitar
   :order      4}
  "-  b7   -   1
   -   4  b5   5
   1   -   -  b3
   -   -   -   -
   -   -   -   -
   -   -   -   -")

(define-scale-pattern #uuid "23f304c1-9947-4654-9617-7311991b8fef"
  {:belongs-to :major
   :tuning     :ukulele
   :order      1}
  "-   2   -   3   4
   -   6   -   7   1
   3   4   -   5   -
   -   1   -   2   -")

(define-scale-pattern #uuid "bc68e54c-6f15-4147-9dea-691f334a043d"
  {:belongs-to :major
   :tuning     :ukulele
   :order      2}
  "6   -   7   1   -
   3   4   -   5   -
   1   -   2   -   -
   -   -   -   -   -")

;; mandolin major
(define-scale-pattern #uuid "dcad1b25-f92b-4d1e-93d2-7b46a7de874c"
  {:belongs-to :major
   :tuning     :mandolin
   :order      1
   :text       "Index finger position"}
  "-   -   -   -   -   -
   -   -   -   -   -   -
   5   -   6   -   7   1
   1   -   2   -   3   4")

(define-scale-pattern #uuid "10e574ba-dec9-4fbf-a731-1fc0265e3ec4"
  {:belongs-to :major
   :tuning     :mandolin
   :order      2
   :text       "Index finger position"}
  "-   -   -   -   -   -
   5   -   6   -   7   1
   1   -   2   -   3   4
   -   -   -   -   -   -")


(define-scale-pattern #uuid "dbe70a92-9ca1-42f9-b5b4-fc43ff08914e"
  {:belongs-to :major
   :tuning     :mandolin
   :order      3
   :text       "Index finger position"}
  "5   -   6   -   7   1
   1   -   2   -   3   4
   -   -   -   -   -   -
   -   -   -   -   -   -")

(define-scale-pattern #uuid "d65b26d8-2b8c-449c-ac6c-b66b0ae455d4"
  {:belongs-to :major
   :tuning     :mandolin
   :order      4
   :text       "Ring finger positon"}
  "-   -   -   -   -   -
   7   1   -   -   -   -
   3   4   -   5   -   6
   -   -   -   1   -   2")

(define-scale-pattern #uuid "54aa619e-b1b1-4495-91f8-fec18bdf118e"
  {:belongs-to :major
   :tuning     :mandolin
   :order      5
   :text       "Ring finger positon"}
  "7   1   -   -   -   -
   3   4   -   5   -   6
   -   -   -   1   -   2
   -   -   -   -   -   -")

(define-scale-pattern #uuid "11a94327-3df5-419b-8c0a-eba0c46ef780"
  {:belongs-to :major
   :tuning     :mandolin
   :text       "Pinky finger positon"
   :order      6}
  "-   -   -   -   -   -
   6   -   7   1   -   -
   2   -   3   4   -   5
   -   -   -   -   -   1")

(define-scale-pattern #uuid "982e6f6c-ad5a-4d11-969a-392c245565d7"
  {:belongs-to :major
   :tuning     :mandolin
   :text       "Pinky finger positon"
   :order      7}
  "6   -   7   1   -   -
   2   -   3   4   -   5
   -   -   -   -   -   1
   -   -   -   -   -   -")

(define-scale-pattern #uuid "9ba24b3e-f0fb-407c-aee0-1bdadd5e5548"
  {:belongs-to :major
   :tuning     :mandolin
   :order      10
   :text       "Whiskey part B"}
  "5   -   6   -   7   1   -   2   -   3   4   -
   1   -   -   -   3   4   -   5   -   6   -   7
   -   -   -   -   -   -   -   -   -   -   -   -
   -   -   -   -   -   -   -   -   -   -   -   -")

(define-scale-pattern #uuid "3926946b-8017-47e6-a3d9-5a94710aa0d0"
  {:belongs-to :minor
   :tuning     :mandolin
   :text       "Index finger positon"
   :order      1}
  "-   -   -   -   -   -
   -   -   -   -   -   -
   5  b6   -  b7   -   1
   1   -   2  b3   -   4")

(define-scale-pattern #uuid "35a245fb-3323-413f-b2f2-045642aaddca"
  {:belongs-to :minor
   :tuning     :mandolin
   :text       "Index finger positon"
   :order      2}
  "-   -   -   -   -   -
   5  b6   -  b7   -   1
   1   -   2  b3   -   4
   -   -   -   -   -   -")

(define-scale-pattern #uuid "6d7480a5-fe70-4ad4-b71f-619a71a3df07"
  {:belongs-to :minor
   :tuning     :mandolin
   :text       "Index finger positon"
   :order      3}
  "5  b6   -  b7   -   1
   1   -   2  b3   -   4
   -   -   -   -   -   -
   -   -   -   -   -   -")

(define-scale-pattern #uuid "a8c2971b-2103-4d6c-821c-98bc706beb8b"
  {:belongs-to :minor
   :tuning     :mandolin
   :text       "Middle finger positon"
   :order      4}
  "-   -   -   -   -   -
   1   -   -   -   -   -
   4   -   5  b6   -  b7
   -   -   1   -   2  b3")

(define-scale-pattern #uuid "f8b3e6cd-72e2-40ef-bea3-03f36da51033"
  {:belongs-to :minor
   :tuning     :mandolin
   :text       "Middle finger positon"
   :order      5}
  "1   -   -   -   -   -
   4   -   5  b6   -  b7
   -   -   1   -   2  b3
   -   -   -   -   -   -")

(define-scale-pattern #uuid "5cac16cb-ac24-4fb6-a456-b7434642bace"
  {:belongs-to :mixolydian
   :tuning     :mandolin
   :order      1}
  "-   -   -   -   -   -
   -   -   -   -   -   -
   5   -   6  b7   -   1
   1   -   2   -   3   4")

(define-scale-pattern #uuid "ad1ccf28-ea6f-4574-88a4-ba304a84f018"
  {:belongs-to :mixolydian
   :tuning     :mandolin
   :order      2}
  "-   -   -   -   -   -
   5   -   6  b7   -   1
   1   -   2   -   3   4
   -   -   -   -   -   -")

(define-scale-pattern #uuid "31c28cf0-808e-4c6d-be74-51f5e136c210"
  {:belongs-to :mixolydian
   :tuning     :mandolin
   :order      3}
  "5   -   6  b7   -   1
   1   -   2   -   3   4
   -   -   -   -   -   -
   -   -   -   -   -   -")


(define-scale-pattern #uuid "a4be6404-23eb-4c68-8a35-efb507469f68"
  {:belongs-to :mixolydian
   :tuning     :mandolin
   :order      4}
  "-   -   -   -   -   -
   6  b7   -   1   -   -
   2   -   3   4   -   5
   -   -   -   -   -   1")

(define-scale-pattern #uuid "c91cddfe-f776-4c0c-8125-4f4c5d074e77"
  {:belongs-to :mixolydian
   :tuning     :mandolin
   :order      5}
  "6  b7   -   1   -   -
   2   -   3   4   -   5
   -   -   -   -   -   1
   -   -   -   -   -   -")
;; ---------------
;; Scales patterns end
;; ---------------

;; Licks
(define-scale-pattern #uuid "aa663f6a-87ab-4e0a-9012-e17c50fe0844"
  {:belongs-to :major
   :tuning     :mandolin
   :url        "https://youtu.be/GmNGaFEkMms?t=248"
   :order      10
   :text       "Alan Bibey Lick #1. 20 Mandolin licks by David Benedict"}
  "b3   3   4  b5   5   -   6   -   -   1
    -   6   -   -   1   -   -   -   3   -
    -   -   -   -   -   -   -   -   -   -
    -   -   -   -   -   -   -   -   -   -")

(define-scale-pattern #uuid "ce10126d-513d-47e0-8152-42c8839eee61"
  {:belongs-to :major
   :tuning     :mandolin
   :url        "https://youtu.be/GmNGaFEkMms?t=446"
   :order      11
   :text       "John Reischman Lick #1. 20 Mandolin licks by David Benedict. I play it in D."}
  "-   -   -   -   -   -   -   -   -   -
   5   -   6   -   7   1   -   2   -   3
   -   -   -   -   3   4  b5   5   -   -
   -   -   -   -   -   -   -   -   -   -")

(define-scale-pattern #uuid "197e6ab5-5ecc-468d-85bb-56b5eaada9e3"
  {:belongs-to :major
   :tuning     :mandolin
   :order      12
   :text       "Tombstone junction but in D"}
  " -   -   -   -   -
   b7   -   1   -   -
   b3   3   4   -   5
    -   -   -   -   1")
