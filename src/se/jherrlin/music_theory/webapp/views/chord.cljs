(ns se.jherrlin.music-theory.webapp.views.chord
  (:require
   [re-frame.alpha :as re-frame]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.common :as common]))


(def events-
  [{:n ::entity}
   {:n ::pattern-entities}
   {:n ::triad-pattern-entities}])

(doseq [{:keys [n s e]} events-]
  (re-frame/reg-sub n (or s (fn [db [n']] (get db n'))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))

(def gather-data-for-view-
  (fn [{:keys [db]} [_ {:keys [chord key-of instrument]}]]
    (let [query-params           (events/query-params db)
          {:keys [id]}           (music-theory/get-chord chord)
          entity                 (music-theory/entity key-of instrument id)
          pattern-entities       (->> (music-theory/chord-patterns-belonging-to chord instrument)
                                      (music-theory/definitions-to-entities key-of instrument))
          triad-pattern-entities (->> (music-theory/chord-pattern-triads-belonging-to chord instrument)
                                      (music-theory/definitions-to-entities key-of instrument))]
      {:db (assoc
            db
            ::entity entity
            ::pattern-entities pattern-entities
            ::triad-pattern-entities triad-pattern-entities)
       :fx (->> (concat
                 [entity]
                 pattern-entities
                 triad-pattern-entities)
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
    {:chord      :major
     :key-of     :c
     :instrument :guitar}])

  (get @re-frame.db/app-db ::fretboards))


(defn chord-component [deps]
  (let [{:keys [chord instrument] :as path-params}
        @(re-frame/subscribe [:path-params])
        query-params                            @(re-frame/subscribe [:query-params])
        {chord-intervals :chord/intervals
         :as             chord-definition}      (music-theory/get-chord chord)
        {instrument-type :type :as instrument'} (music-theory/get-instrument instrument)
        chord-entity                            @(re-frame/subscribe [::entity])
        chord-patterns                          @(re-frame/subscribe [::pattern-entities])
        chord-triad-patterns                    @(re-frame/subscribe [::triad-pattern-entities])]
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
     [common/chord-selection]
     [:br]
     [:br]
     [common/settings
      {:as-text?        (= instrument-type :fretboard)
       :nr-of-frets?    (= instrument-type :fretboard)
       :trim-fretboard? (= instrument-type :fretboard)
       :nr-of-octavs?   (= instrument-type :keyboard)}]
     [:br]
     [common/definition-view-detailed
      chord-definition instrument' path-params query-params]
     [:br]
     [:br]
     [common/instrument-view
      chord-entity path-params query-params deps]

     (when (seq chord-patterns)
       [:h2 "Chord patterns"])

     (for [{:keys [id] :as entity} chord-patterns]
       ^{:key (str "chord-patterns-" id)}
       [:<>
        [common/instrument-view
         entity path-params query-params deps]
        [:br]
        [:br]])

     (when (seq chord-triad-patterns)
       [:h2 "Triads"])

     (for [{:keys [id] :as entity} chord-triad-patterns]
       ^{:key (str "chord-triad-patterns-" id)}
       [:<>
        [common/instrument-view
         entity path-params query-params deps]
        [:br]
        [:br]])
     [:br]
     [:br]
     [:h2 "Scales to this chord:"]
     [common/scales-to-chord path-params query-params chord-intervals]]))

(defn routes [deps]
  (let [route-name :chord]
    ["/chord/:instrument/:key-of/:chord"
     {:name       route-name
      :view       [chord-component deps]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:path  [:map
                           [:instrument      keyword?]
                           [:key-of          keyword?]
                           [:chord           keyword?]]
                   :query events/Query}
      :controllers
      [{:parameters {:path  [:instrument :key-of :chord]
                     :query events/query-keys}
        :start      (fn [{p :path q :query}]
                      (events/do-on-url-change route-name p q)
                      (re-frame/dispatch [::gather-data-for-view p]))}]}]))
