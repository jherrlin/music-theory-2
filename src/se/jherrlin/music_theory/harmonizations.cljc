(ns se.jherrlin.music-theory.harmonizations)


(def harmonizations'
  (let [triads-fn  (juxt #(nth % 0) #(nth % 2) #(nth % 4))
        seventh-fn (juxt #(nth % 0) #(nth % 2) #(nth % 4) #(nth % 6))]
    {:triads              {:id          :triads
                           :description "Triads"
                           :type        :generated
                           :function    triads-fn}
     :seventh             {:id          :seventh
                           :description "Seventh"
                           :type        :generated
                           :function    seventh-fn}
     :145-dominant-sevens {:id          :145-dominant-sevens
                           :description "I-IV-V Blues"
                           :order       1
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
