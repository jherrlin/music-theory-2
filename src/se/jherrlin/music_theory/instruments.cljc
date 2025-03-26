(ns se.jherrlin.music-theory.instruments)


(def definitions
  {;; :piano             {:id          :piano
   ;;                     :text        "Piano"
   ;;                     :description "Standard guitar tuning"
   ;;                     :type        :keyboard
   ;;                     :order       1}
   :guitar {:id                  :guitar
            :text                "Guitar"
            :description         "Standard guitar tuning"
            :type                :fretboard
            :order               10
            :scale-pattern-range [4 5]
            :tuning              [{:tone        :e
                                   :octave      3
                                   :start-index 0}
                                  {:tone        :a
                                   :octave      3
                                   :start-index 0}
                                  {:tone        :d
                                   :octave      4
                                   :start-index 0}
                                  {:tone        :g
                                   :octave      4
                                   :start-index 0}
                                  {:tone        :b
                                   :octave      4
                                   :start-index 0}
                                  {:tone        :e
                                   :octave      5
                                   :start-index 0}]
            :abc                 {:instrument "guitar"
                                  :tuning     ["E,", "A,", "D", "G", "B", "e"]
                                  :capo       0}}

   :mandolin       {:id                  :mandolin
                    :text                "Mandolin"
                    :description         "Standard mandolin tuning"
                    :type                :fretboard
                    :scale-pattern-range [6 7]
                    :order               20
                    :tuning              [{:tone        :g
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
                                           :start-index 0}]
                    :abc                 {:instrument "mandolin"
                                          :tuning     ["G,", "D", "A", "e"]
                                          :capo       0}}
   :mandolin-adae  {:id                  :mandolin-adae
                    :text                "Mandolin (ADAE)"
                    :description         "Mandolin (ADAE)"
                    :type                :fretboard
                    :scale-pattern-range [6 7]
                    :order               21
                    :tuning              [{:tone        :a
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
   :mandolin-gdad  {:id                  :mandolin-gdad
                    :text                "Mandolin (GDAD)"
                    :description         "Mandolin in GDAD tuning"
                    :type                :fretboard
                    :scale-pattern-range [6 7]
                    :order               22
                    :tuning              [{:tone        :g
                                           :octave      3
                                           :start-index 0}
                                          {:tone        :d
                                           :octave      4
                                           :start-index 0}
                                          {:tone        :a
                                           :octave      4
                                           :start-index 0}
                                          {:tone        :d
                                           :octave      5
                                           :start-index 0}]}
   :mandolin-aeae  {:id                  :mandolin-aeae
                    :text                "Mandolin (AEAE)"
                    :description         "Mandolin AEAE tuning"
                    :type                :fretboard
                    :scale-pattern-range [6 7]
                    :order               23
                    :tuning              [{:tone        :a
                                           :octave      3
                                           :start-index 0}
                                          {:tone        :e
                                           :octave      4
                                           :start-index 0}
                                          {:tone        :a
                                           :octave      4
                                           :start-index 0}
                                          {:tone        :e
                                           :octave      5
                                           :start-index 0}]}
   :mandolin-gdgd  {:id                  :mandolin-gdgd
                    :text                "Mandolin (GDGD)"
                    :description         "Mandolin GDGD tuning"
                    :type                :fretboard
                    :scale-pattern-range [6 7]
                    :order               24
                    :tuning              [{:tone        :g
                                           :octave      3
                                           :start-index 0}
                                          {:tone        :d
                                           :octave      4
                                           :start-index 0}
                                          {:tone        :g
                                           :octave      4
                                           :start-index 0}
                                          {:tone        :d
                                           :octave      5
                                           :start-index 0}]}
   :mandolin-gdaeb {:id                  :mandolin-gdaeb
                    :text                "Mandolin (GDAEB)"
                    :description         "Mandolin (GDAEB) tuning"
                    :type                :fretboard
                    :scale-pattern-range [6 7]
                    :order               25
                    :tuning              [{:tone        :g
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
                                           :start-index 0}
                                          {:tone        :b
                                           :octave      5
                                           :start-index 0}]}

   :mandola {:id                  :mandola
             :text                "Mandola"
             :description         "Standard mandola tuning"
             :type                :fretboard
             :order               30
             :scale-pattern-range [4 6]
             :tuning              [{:tone        :c
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

   :mandocello {:id                  :mandocello
                :text                "Mandocello"
                :description         "Standard mandocello tuning"
                :type                :fretboard
                :scale-pattern-range [4 5]
                :order               31
                :tuning              [{:tone        :c
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

   :cittern-dgdad {:id                  :cittern-dgdad
                   :text                "Cittern (DGDAD)"
                   :description         ""
                   :type                :fretboard
                   :order               40
                   :scale-pattern-range [4 5]
                   :tuning              [{:tone        :d
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
                                          :start-index 0}
                                         {:tone        :d
                                          :octave      4
                                          :start-index 0}]}

   :five-string-banjo      {:id          :five-string-banjo
                            :text        "Banjo, 5-string"
                            :description "Standard modern five-string banjo."
                            :type        :fretboard
                            ;; :scale-pattern-range [4 5]
                            :order       50
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
   :banjo-5-string-oldtime {:id          :banjo-5-string-oldtime
                            :text        "Banjo, oldtime tuning"
                            :description "Five-string banjo in oldtime tuning."
                            :type        :fretboard
                            ;; :scale-pattern-range [4 5]
                            :order       51
                            :tuning      [{:tone        :a
                                           :octave      4
                                           :start-index 5}
                                          {:tone        :e
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
                                           :start-index 0}]}

   :banjo-5-string-open-a    {:id          :banjo-5-string-open-a
                              :text        "Five-string banjo in open A tuning"
                              :description "Five-string banjo in open A tuning"
                              :type        :fretboard
                              ;; :scale-pattern-range [4 5]
                              :order       52
                              :tuning      [{:tone        :a
                                             :octave      2
                                             :start-index 3}
                                            {:tone        :e
                                             :octave      3
                                             :start-index 0}
                                            {:tone        :a
                                             :octave      3
                                             :start-index 0}
                                            {:tone        :c#
                                             :octave      3
                                             :start-index 0}
                                            {:tone        :e
                                             :octave      4
                                             :start-index 0}]}
   :banjo-5-string-oldtime-a {:id          :banjo-5-string-oldtime-a
                              :text        "Five-string banjo in oldtime A tuning"
                              :description "Five-string banjo in oldtime A tuning"
                              :type        :fretboard
                              ;; :scale-pattern-range [4 5]
                              :order       53
                              :tuning      [{:tone        :a
                                             :octave      2
                                             :start-index 3}
                                            {:tone        :e
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

(defn fretboard-instruments []
  (->> (instruments)
       (filter (comp #{:fretboard} :type))))

(defn get-instrument-tuning
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
