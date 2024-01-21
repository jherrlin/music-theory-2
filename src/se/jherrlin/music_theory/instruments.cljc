(ns se.jherrlin.music-theory.instruments)


(def definitions
  {:guitar   {:id              :guitar
              :text            "Guitar"
              :description     "Standard guitar tuning"
              :instrument-type :fretboard
              :order           1
              :tuning          [{:tone        :e
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
   :mandolin {:id              :mandolin
              :text            "Mandolin"
              :description     "Standard mandolin tuning"
              :instrument-type :fretboard
              :order           2
              :tuned-in        :fifth
              :tuning          [{:tone        :g
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
   :mandola  {:id              :mandola
              :text            "Mandola"
              :description     "Standard mandola tuning"
              :instrument-type :fretboard
              :order           3
              :tuned-in        :fifth
              :tuning          [{:tone        :c
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
