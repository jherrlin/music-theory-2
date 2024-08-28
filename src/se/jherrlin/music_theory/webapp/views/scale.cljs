(ns se.jherrlin.music-theory.webapp.views.scale
  (:require
   [re-frame.alpha :as re-frame]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.common :as common]))

(def events-
  [{:n ::entity}
   {:n ::pattern-entities}])

(doseq [{:keys [n s e]} events-]
  (re-frame/reg-sub n (or s (fn [db [n']] (get db n'))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))

(def gather-data-for-view-
  (fn [{:keys [db]} [_ {:keys [scale key-of instrument]}]]
    (let [query-params       (events/query-params db)
          {scale-names :scale/scale-names
           :keys       [id]} (music-theory/get-scale scale)
          entity             (music-theory/entity key-of instrument id)
          pattern-entities   (->> (music-theory/scale-patterns-for-scale-and-instrument scale-names instrument)
                                  (music-theory/definitions-to-entities key-of instrument))]
      {:db (assoc
            db
            ::entity entity
            ::pattern-entities pattern-entities)
       :fx (->> (concat
                 [entity]
                 pattern-entities)
                (mapv (fn [entity]
                        (let [instrument-ds (music-theory/instrument-data-structure
                                             entity query-params)]
                          [:dispatch [:add-entity-instrument-data-structure entity instrument-ds]]))))})))

(re-frame/reg-event-fx ::gather-data-for-view gather-data-for-view-)

(comment

  (gather-data-for-view-
   {:db
    {:query-params
     {:nr-of-frets 15}}}
   [nil
    {:scale      :major
     :key-of     :c
     :instrument :guitar}])

  (get @re-frame.db/app-db ::fretboards))

(defn scale-component [deps]
  (let [{:keys [id scale instrument] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                                  @(re-frame/subscribe [:query-params])
        {instrument-type :type :as instrument'}       (music-theory/get-instrument instrument)
        {scale-intervals :scale/intervals
         scale-names     :scale/scale-names
         :as             scale'}                      (music-theory/get-scale scale)
        scale-patterns                                (music-theory/scale-patterns-for-scale-and-instrument scale-names instrument)
        scale-entity                                  @(re-frame/subscribe [::entity])
        scale-pattern-entities                        @(re-frame/subscribe [::pattern-entities])
        fretboards                                    @(re-frame/subscribe [:fretboards])

        ;; Sort patterns so that the ones closest to the intrument head will be
        ;; shown first. Remove fretboards where pattern is not shown.
        entities                                      (->> scale-pattern-entities
                                                           (map (fn [{:keys [id] :as entity}]
                                                                  (let [{intervals :fretboard-pattern/intervals}  (music-theory/get-definition id)
                                                                        {:keys [instrument-data-structure] :as m} (get fretboards entity)]
                                                                    (assoc m :sort-score (common/sort-fretboard-nr
                                                                                          (count intervals)
                                                                                          instrument-data-structure)))))
                                                           (filter :sort-score)
                                                           (sort-by :sort-score)
                                                           (map :id))]

    [:<>
     [common/menu]
     [:br]
     [common/instrument-selection]
     [:br]
     [common/instrument-details instrument']
     [:br]
     [:br]
     [common/key-selection]
     [:br]
     [:br]
     [common/scale-selection]
     [:br]
     [:br]
     [common/settings
      {:as-text?        (= instrument-type :fretboard)
       :nr-of-frets?    (= instrument-type :fretboard)
       :trim-fretboard? (= instrument-type :fretboard)
       :nr-of-octavs?   (= instrument-type :keyboard)}]
     [:br]
     [common/definition-view-detailed
      scale' instrument' path-params query-params]
     [:br]
     [:br]
     [common/instrument-view
      scale-entity path-params query-params deps]

     (when (seq scale-patterns)
       [:<>
        [:h2 "Scale patterns"]
        (for [{:keys [id] :as entity} entities]
          ^{:key (str "scale-patterns-" id)}
          [:<>
           [common/instrument-view entity path-params query-params deps]
           [:br]
           [:br]])])

     [:br]
     [:br]

     [:h2 "Chords to this scale:"]
     [common/chords-to-scale path-params query-params scale-intervals]]))

(defn routes [deps]
  (let [route-name :scale]
    ["/scale/:instrument/:key-of/:scale"
     {:name       route-name
      :view       [scale-component deps]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:path  [:map
                           [:instrument      keyword?]
                           [:key-of          keyword?]
                           [:scale           keyword?]]
                   :query events/Query}
      :controllers
      [{:parameters {:path  [:instrument :key-of :scale]
                     :query events/query-keys}
        :start      (fn [{p :path q :query}]
                      (events/do-on-url-change route-name p q)
                      (re-frame/dispatch [::gather-data-for-view p]))}]}]))
