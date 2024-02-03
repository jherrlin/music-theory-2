(ns se.jherrlin.music-theory.webapp.views.chord
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [re-frame.alpha :as re-frame]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.common :as common]))


(def events-
  [{:n ::chord-entity}
   {:n ::chord-pattern-entities}
   {:n ::chord-triad-pattern-entities}])

(doseq [{:keys [n s e]} events-]
  (re-frame/reg-sub n (or s (fn [db [n']] (get db n'))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))



(defn gather-view-data [{:keys [chord key-of instrument]} {:keys [nr-of-frets] :as query-params}]
  (let [{:keys [id] :as chord'}      (music-theory/get-chord chord)
        chord-entity                 (music-theory/entity key-of instrument id)
        chord-pattern-entities       (->> (music-theory/chord-patterns-belonging-to chord instrument)
                                          (music-theory/definitions-to-entities key-of instrument))
        chord-triad-pattern-entities (->> (music-theory/chord-pattern-triads-belonging-to chord instrument)
                                          (music-theory/definitions-to-entities key-of instrument))]

    (let [fretboard-matrix (common/prepair-instrument-data-for-entity chord-entity {} query-params)]
      (re-frame/dispatch [:add-entity-with-fretboard chord-entity fretboard-matrix]))

    (doseq [entity chord-pattern-entities]
      (let [fretboard-matrix (common/prepair-instrument-data-for-entity entity {} query-params)]
        (re-frame/dispatch [:add-entity-with-fretboard entity fretboard-matrix])))

    (doseq [entity chord-triad-pattern-entities]
      (let [fretboard-matrix (common/prepair-instrument-data-for-entity entity {} query-params)]
        (re-frame/dispatch [:add-entity-with-fretboard entity fretboard-matrix])))

    (re-frame/dispatch [::chord-entity chord-entity])
    (re-frame/dispatch [::chord-pattern-entities chord-pattern-entities])
    (re-frame/dispatch [::chord-triad-pattern-entities chord-triad-pattern-entities])))

(comment

  (gather-view-data
   {:instrument :guitar
    :key-of :c
    :chord :major}
   {:nr-of-frets 15})

  (get @re-frame.db/app-db ::fretboards)
  )


(defn chord-component [deps]
  (let [{:keys [chord instrument] :as path-params}
        @(re-frame/subscribe [:path-params])
        query-params                            @(re-frame/subscribe [:query-params])
        {chord-intervals :chord/intervals
         :as             chord-definition}      (music-theory/get-chord chord)
        {instrument-type :type :as instrument'} (music-theory/get-instrument instrument)
        chord-entity                              @(re-frame/subscribe [::chord-entity])
        chord-patterns                          @(re-frame/subscribe [::chord-pattern-entities])
        chord-triad-patterns                    @(re-frame/subscribe [::chord-triad-pattern-entities])]
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
                      (gather-view-data p q)
                      (events/do-on-url-change route-name p q))}]}]))
