(ns se.jherrlin.music-theory.harmonizations)


(def harmonizations'
  (let [triads-fn  (juxt #(nth % 0) #(nth % 2) #(nth % 4))
        seventh-fn (juxt #(nth % 0) #(nth % 2) #(nth % 4) #(nth % 6))]
    {:major-triads        {:description "Major triads"
                           :id          :major-triads
                           :order       1
                           :type        :predefined
                           :chords      [{:chord  :major
                                          :symbol "I"
                                          :mode   :ionian
                                          :family :tonic
                                          :idx-fn #(nth % 0)
                                          :idx    1}
                                         {:chord  :minor
                                          :symbol "ii"
                                          :mode   :dorian
                                          :family :subdominant
                                          :idx-fn #(nth % 1)
                                          :idx    2}
                                         {:chord  :minor
                                          :symbol "iii"
                                          :mode   :phrygian
                                          :family :tonic
                                          :idx-fn #(nth % 2)
                                          :idx    3}
                                         {:chord  :major
                                          :symbol "IV"
                                          :mode   :lydian
                                          :family :subdominant
                                          :idx-fn #(nth % 3)
                                          :idx    4}
                                         {:chord  :major
                                          :symbol "V"
                                          :mode   :mixolydian
                                          :family :dominant
                                          :idx-fn #(nth % 4)
                                          :idx    5}
                                         {:chord  :minor
                                          :symbol "vi"
                                          :mode   :aeolian
                                          :family :tonic
                                          :idx-fn #(nth % 5)
                                          :idx    6}
                                         {:chord  :diminished-fifth
                                          :symbol "vii"
                                          :mode   :locrian
                                          :family :dominant
                                          :idx-fn #(nth % 6)
                                          :idx    7}]}
     :145-dominant-sevens {:id          :145-dominant-sevens
                           :description "I-IV-V Blues"
                           :order       2
                           :type        :predefined
                           :chords      [{:chord  :dominant-seven
                                          :symbol "I"
                                          :mode   :ionian
                                          :family :tonic
                                          :idx-fn #(nth % 0)
                                          :idx    1}
                                         {:chord  :dominant-seven
                                          :symbol "IV"
                                          :mode   :lydian
                                          :family :subdominant
                                          :idx-fn #(nth % 3)
                                          :idx    4}
                                         {:chord  :dominant-seven
                                          :symbol "V"
                                          :mode   :mixolydian
                                          :family :dominant
                                          :idx-fn #(nth % 4)
                                          :idx    5}]}
     ;; :minor-triads
     ;; :major-seventh
     ;; :minor-seventh
     }))

(defn harmonization [k] (get harmonizations' k))

(def harmonizations
  (->> harmonizations'
       (vals)
       (sort-by :order)))
