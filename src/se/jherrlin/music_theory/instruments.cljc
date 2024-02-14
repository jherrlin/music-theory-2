(ns se.jherrlin.music-theory.instruments)


(def definitions
  {;; :piano             {:id          :piano
   ;;                     :text        "Piano"
   ;;                     :description "Standard guitar tuning"
   ;;                     :type        :keyboard
   ;;                     :order       1}
   :guitar            {:id          :guitar
                       :text        "Guitar"
                       :description "Standard guitar tuning"
                       :type        :fretboard
                       :order       2
                       :tuning      [{:tone        :e
                                      :octave      2
                                      :start-index 0}
                                     {:tone        :a
                                      :octave      2
                                      :start-index 0}
                                     {:tone        :d
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :g
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :b
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :e
                                      :octave      4
                                      :start-index 0}]}
   :mandolin          {:id          :mandolin
                       :text        "Mandolin"
                       :description "Standard mandolin tuning"
                       :type        :fretboard
                       :order       3
                       :tuned-in    :fifth
                       :tuning      [{:tone        :g
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :d
                                      :octave      4
                                      :start-index 0}
                                     {:tone        :a
                                      :octave      4
                                      :start-index 0}
                                     {:tone        :e
                                      :octave      5
                                      :start-index 0}]}
   :mandola           {:id          :mandola
                       :text        "Mandola"
                       :description "Standard mandola tuning"
                       :type        :fretboard
                       :order       4
                       :tuned-in    :fifth
                       :tuning      [{:tone        :c
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :g
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :d
                                      :octave      4
                                      :start-index 0}
                                     {:tone        :a
                                      :octave      4
                                      :start-index 0}]}
   :five-string-banjo {:id          :five-string-banjo
                       :text        "Banjo, 5-string"
                       :description "Standard modern five-string banjo."
                       :type        :fretboard
                       :order       5
                       :tuning      [{:tone        :g
                                      :octave      4
                                      :start-index 5}
                                     {:tone        :d
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :g
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :b
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :d
                                      :octave      4
                                      :start-index 0}]}
   :mandocello        {:id          :mandocello
                       :text        "Mandocello"
                       :description "Standard mandocello tuning"
                       :type        :fretboard
                       :order       6
                       :tuning      [{:tone        :c
                                      :octave      2
                                      :start-index 0}
                                     {:tone        :g
                                      :octave      2
                                      :start-index 0}
                                     {:tone        :d
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :a
                                      :octave      3
                                      :start-index 0}]}
   :pers-banjo-tuning {:id          :pers-banjo-tuning
                       :text        "Pers banjo tuning"
                       :description "Pers banjo tuning"
                       :type        :fretboard
                       :order       7
                       :tuning      [{:tone        :a
                                      :octave      4
                                      :start-index 3}
                                     {:tone        :d
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :a
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :d
                                      :octave      3
                                      :start-index 0}
                                     {:tone        :e
                                      :octave      4
                                      :start-index 0}]}})

(defn instrument
  "Get `instrument'` map."
  [instrument']
  (get definitions instrument'))

(defn get-instrument-type
  "Get instrument `k` type."
  [k]
  (get-in definitions [k :type]))

(defn instruments
  "Get `instruments'` map."
  []
  (->> definitions
       (vals)
       (sort-by :order)))

(defn get-tuning
  "Get tuning and text from `instrument'`."
  [instrument']
  (-> (instrument instrument')
      :tuning))

(defn get-description
  "Get description and text from `instrument'`."
  [instrument']
  (-> (instrument instrument')
      :description))

(defn fifth?
  "Is `instrument'` tuned in fifth?"
  [instrument']
  (-> (instrument instrument')
      :fifth
      boolean))
