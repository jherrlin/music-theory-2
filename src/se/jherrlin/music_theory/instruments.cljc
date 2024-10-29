(ns se.jherrlin.music-theory.instruments)


(def definitions
  {;; :piano             {:id          :piano
   ;;                     :text        "Piano"
   ;;                     :description "Standard guitar tuning"
   ;;                     :type        :keyboard
   ;;                     :order       1}
   :guitar            {:id                  :guitar
                       :text                "Guitar"
                       :description         "Standard guitar tuning"
                       :type                :fretboard
                       :order               2
                       :scale-pattern-range [4 5]
                       :tuning              [{:tone        :e
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
   :cittern-dgdad     {:id                  :cittern-dgdad
                       :text                "Cittern (DGDAD)"
                       :description         ""
                       :type                :fretboard
                       :order               9
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
   :mandolin          {:id                  :mandolin
                       :text                "Mandolin"
                       :description         "Standard mandolin tuning"
                       :type                :fretboard
                       :scale-pattern-range [6 7]
                       :order               3
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
                                              :start-index 0}]}
   :mandolin-gdad          {:id                  :mandolin-gdad
                            :text                "Mandolin (GDAD)"
                            :description         "Mandolin in GDAD tuning"
                            :type                :fretboard
                            :scale-pattern-range [6 7]
                            :order               5
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
   :mandolin-aeae     {:id                  :mandolin-aeae
                       :text                "Mandolin (AEAE)"
                       :description         "Mandolin AEAE tuning"
                       :type                :fretboard
                       :scale-pattern-range [6 7]
                       :order               4
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
   :mandola           {:id                  :mandola
                       :text                "Mandola"
                       :description         "Standard mandola tuning"
                       :type                :fretboard
                       :order               5
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
   :five-string-banjo {:id          :five-string-banjo
                       :text        "Banjo, 5-string"
                       :description "Standard modern five-string banjo."
                       :type        :fretboard
                       ;; :scale-pattern-range [4 5]
                       :order       6
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
                            :order       6
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
   :mandocello        {:id                  :mandocello
                       :text                "Mandocello"
                       :description         "Standard mandocello tuning"
                       :type                :fretboard
                       :scale-pattern-range [4 5]
                       :order               7
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
   :pers-banjo-tuning {:id          :pers-banjo-tuning
                       :text        "Pers banjo tuning"
                       :description "Pers banjo tuning"
                       :type        :fretboard
                       ;; :scale-pattern-range [4 5]
                       :order       8
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
