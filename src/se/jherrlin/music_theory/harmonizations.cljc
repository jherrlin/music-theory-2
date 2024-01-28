(ns se.jherrlin.music-theory.harmonizations)


(def harmonizations
  (let [triads-fn  (juxt #(nth % 0) #(nth % 2) #(nth % 4))
        seventh-fn (juxt #(nth % 0) #(nth % 2) #(nth % 4) #(nth % 6))]
    {:major-triads {:description "hejsan"
                    :id          :major-triads
                    :type        :predefined
                    :chords      [{:chord  :major
                                   :symbol "I"
                                   :mode   :ionian
                                   :family :tonic}
                                  {:chord  :minor
                                   :symbol "ii"
                                   :mode   :dorian
                                   :family :subdominant}
                                  {:chord  :minor
                                   :symbol "iii"
                                   :mode   :phrygian
                                   :family :tonic}
                                  {:chord  :major
                                   :symbol "IV"
                                   :mode   :lydian
                                   :family :subdominant}
                                  {:chord  :major
                                   :symbol "V"
                                   :mode   :mixolydian
                                   :family :dominant}
                                  {:chord  :minor
                                   :symbol "vi"
                                   :mode   :aeolian
                                   :family :tonic}
                                  {:chord  :diminished-fifth
                                   :symbol "vii"
                                   :mode   :locrian
                                   :family :dominant}]}
     ;; :minor-triads
     ;; :major-seventh
     ;; :minor-seventh
     }))

(defn harmonization [k] (get harmonizations k))
