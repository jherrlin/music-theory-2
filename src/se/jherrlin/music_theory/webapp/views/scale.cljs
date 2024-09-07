(ns se.jherrlin.music-theory.webapp.views.scale
  (:require
   [re-frame.alpha :as re-frame]
   [reitit.coercion.malli]
   [clojure.string :as str]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.common :as common]
   [re-frame.db :as db]
   [se.jherrlin.music-theory.models.entity :as entity]
   [se.jherrlin.music-theory.instruments :as instruments]
   [se.jherrlin.music-theory.webapp.views.scale-calcs :as scale-calcs]))

(def events-
  [{:n ::entity}
   {:n ::pattern-entities}])

(def app-db-path ::scale)

(defn path [x]
  (-> [app-db-path x] flatten vec))

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

(re-frame/reg-flow
 (let [inputs {:scale-id      (re-frame/flow<- ::events/scale-id)
               :instrument-id (re-frame/flow<- ::events/instrument-id)
               :key-of        (re-frame/flow<- ::events/key-of)}]
   {:id          ::scale-entity
    :live-inputs inputs
    :inputs      inputs
    :live?       (fn [{:keys [key-of instrument-id scale-id]}]
                   (and key-of instrument-id scale-id))
    :output      (fn [{:keys [key-of instrument-id scale-id]}]
                   (music-theory/entity key-of instrument-id scale-id))
    :path        (path ::scale-entity)}))

(re-frame/reg-sub ::scale-entity :-> #(get-in % (path ::scale-entity)))

(comment
  @(re-frame/subscribe [:flow {:id ::scale-entity}])
  (get @re-frame.db/app-db app-db-path)
  )

(re-frame/reg-flow
 (let [inputs {:scale-names   (re-frame/flow<- ::events/scale-names)
               :instrument-id (re-frame/flow<- ::events/instrument-id)
               :key-of        (re-frame/flow<- ::events/key-of)}]
   {:id          ::pattern-entities
    :doc         "A seq of unsorted scale pattern entities"
    :live-inputs inputs
    :inputs      inputs
    :live?       (fn [{:keys [scale-names instrument-id key-of]}]
                   (and scale-names instrument-id key-of))
    :output      (fn [{:keys [key-of instrument-id scale-names]}]
                   (scale-calcs/scale-pattern-entities key-of instrument-id scale-names))
    :path        (path ::pattern-entities)}))

(re-frame/reg-sub ::pattern-entities :-> #(get-in % (path ::pattern-entities)))

(comment
  @(re-frame/subscribe [:flow {:id ::pattern-entities}])
  (get @re-frame.db/app-db app-db-path)
  )


#_(re-frame/reg-flow
 ;; TODO: extract the sorting part into a new flow.
 (let [inputs {:unsorted-pattern-entities (re-frame/flow<- ::pattern-entities)
               :query-params              [:query-params]}]
   {:id          ::resolve-and-sort-entities
    :doc         "A seq of sorted and resolved scale pattern entities.

Returns a map: entity+[:entity, :definition, :instrument-data-structure]"
    :live-inputs inputs
    :inputs      inputs
    :live?       (fn [{:keys [unsorted-pattern-entities query-params]}]
                   (and unsorted-pattern-entities query-params))
    :output      (fn [{:keys [unsorted-pattern-entities query-params]}]
                   (->> unsorted-pattern-entities
                        (map (fn [{:keys [id] :as entity}]
                               (let [{intervals :fretboard-pattern/intervals :as definition}
                                     (music-theory/get-definition id)
                                     instrument-data-structure
                                     (music-theory/instrument-data-structure entity query-params)
                                     nr-of-intervals (count intervals)
                                     sort-score
                                     (common/sort-fretboard-nr nr-of-intervals instrument-data-structure)]
                                 {:entity                    entity
                                  :definition                definition
                                  :instrument-data-structure instrument-data-structure
                                  :sort-score                sort-score})))
                        (filter :sort-score)
                        (sort-by :sort-score)
                        (map (juxt :entity identity))
                        (into {})))
    :path        (path ::resolve-and-sort-entities)}))

#_(re-frame/reg-sub ::resolve-and-sort-entities :-> #(get-in % (path ::resolve-and-sort-entities)))

(comment
  (<sub [::resolve-and-sort-entities])
  )

(defn scale-component [deps]
  (let [{:keys [id scale instrument] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                                  @(re-frame/subscribe [:query-params])
        scale-patterns-starts-on                      (<sub [:scale-patterns-starts-on])
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
                                                                  (merge (music-theory/get-definition id) entity)))
                                                           (filter (fn [{intervals :fretboard-pattern/intervals}]
                                                                     ((set scale-patterns-starts-on)
                                                                      (first intervals))))
                                                           (map (fn [{intervals :fretboard-pattern/intervals :as entity}]
                                                                  (let [{:keys [instrument-data-structure] :as m} (get fretboards (select-keys entity [:id :instrument :key-of]))]
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

     #_[:p (str/join "," scale-intervals)]



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
