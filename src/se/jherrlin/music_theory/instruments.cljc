(ns se.jherrlin.music-theory.instruments)


(def definitions
  {:piano             {:instrument/id          :piano
                       :instrument/text        "Piano"
                       :instrument/description "Standard guitar tuning"
                       :instrument/type        :keyboard
                       :instrument/order       1}
   :guitar            {:instrument/id          :guitar
                       :instrument/text        "Guitar"
                       :instrument/description "Standard guitar tuning"
                       :instrument/type        :fretboard
                       :instrument/order       2
                       :instrument/tuning      [{:tone        :e
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
                                                 :octave      2
                                                 :start-index 0}]}
   :mandolin          {:instrument/id          :mandolin
                       :instrument/text        "Mandolin"
                       :instrument/description "Standard mandolin tuning"
                       :instrument/type        :fretboard
                       :instrument/order       3
                       :instrument/tuned-in    :fifth
                       :instrument/tuning      [{:tone        :g
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
   :mandola           {:instrument/id          :mandola
                       :instrument/text        "Mandola"
                       :instrument/description "Standard mandola tuning"
                       :instrument/type        :fretboard
                       :instrument/order       4
                       :instrument/tuned-in    :fifth
                       :instrument/tuning      [{:tone        :c
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
   :five-string-banjo {:instrument/id          :five-string-banjo
                       :instrument/text        "Banjo, 5-string"
                       :instrument/description "Standard modern five-string banjo."
                       :instrument/type        :fretboard
                       :instrument/order       5
                       :instrument/tuning      [{:tone        :g
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
                                                 :start-index 0}]}})

(defn instrument
  "Get `instrument'` map."
  [instrument']
  (get definitions instrument'))

(defn instruments
  "Get `instruments'` map."
  []
  (->> definitions
       (vals)
       (sort-by :order)))

(defn tuning
  "Get tuning and text from `instrument'`."
  [instrument']
  (-> (instrument instrument')
      :tuning))

(defn description
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
