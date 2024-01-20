(ns se.jherrlin.music-theory.instruments)


(def definitions
  {:mandolin {:id          :mandolin
              :description "Standard mandolin tuning"
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
   :guitar   {:id          :guitar
              :description "Standard guitar tuning"
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
                             :octave      2
                             :start-index 0}]}})

(defn instrument
  "Get `instrument'` map."
  [instrument']
  (get definitions instrument'))

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
