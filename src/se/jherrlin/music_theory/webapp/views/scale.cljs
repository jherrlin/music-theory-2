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
   [se.jherrlin.music-theory.models.entity :as models.entity]
   [se.jherrlin.music-theory.instruments :as instruments]
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard2 :as fretboard2]
   [se.jherrlin.music-theory.webapp.views.scale-calcs :as scale-calcs]
   [se.jherrlin.music-theory.webapp.components.tonejs :as tonejs]
   [se.jherrlin.music-theory.fretboard :as fretboard]))

(def events-
  [{:n ::entity}
   {:n ::pattern-entities}])

(doseq [{:keys [n s e]} events-]
  (re-frame/reg-sub n (or s (fn [db [n']] (get db n'))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))

(def app-db-path ::scale)

(defn path [x]
  (-> [app-db-path x] flatten vec))



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


(re-frame/reg-flow
 (let [inputs {:pattern-entities (re-frame/flow<- ::pattern-entities)
               :query-params     [:query-params]}]
   {:id  ::entity+definition+instrument-ds+qp
    :doc "A map of resolved scale patterns.
Returns a map: entity+[:entity, :definition, :instrument-data-structure]"
    :live-inputs inputs
    :inputs      inputs
    :live?       (fn [{:keys [pattern-entities query-params]}]
                   (and pattern-entities query-params))
    :output      (fn [{:keys [pattern-entities query-params]}]
                   (->> pattern-entities
                        (map (fn [{:keys [id] :as entity}]
                               (let [definition
                                     (music-theory/get-definition id)
                                     instrument-data-structure
                                     (music-theory/instrument-data-structure entity query-params)]
                                 (when (music-theory/fretboard-has-matches? instrument-data-structure)
                                   {:entity                    entity
                                    :definition                definition
                                    :instrument-data-structure instrument-data-structure
                                    :query-params              query-params}))))
                        (keep identity)
                        ;; Remove duplicates
                        (map (fn [{:keys [definition] :as m}]
                               [(or (:fretboard-pattern/pattern-hash definition)
                                    (:id definition))
                                m]))
                        (into {})
                        (vals)
                        ;; ;; Remove duplicates end
                        (map (juxt :entity identity))
                        (into {})))
    :path        (path ::entity+definition+instrument-ds+qp)}))

(re-frame/reg-sub ::entity+definition+instrument-ds+qp :-> #(get-in % (path ::entity+definition+instrument-ds+qp)))

(comment
  @(re-frame/subscribe [:flow {:id ::entity+definition+instrument-ds+qp}])
  (get-in @re-frame.db/app-db (path ::entity+definition+instrument-ds+qp))
  )

(re-frame/reg-event-db
 ::highlight
 (fn [db [_event-id {:keys [entity x y highlight?]
                     :or   {highlight? true}}]]
   (update-in db
              (conj
               (path ::entity+definition+instrument-ds+qp)
               entity
               :instrument-data-structure
               y x)
              assoc :highlight? highlight?)))

(comment
  (>evt [::highlight {:entity {:id         #uuid "5fb96ff2-e549-46ea-9346-6c5249bfcce4",
                               :instrument :mandolin,
                               :key-of     :d}
                      :x 0
                      :y 1}])
  )




(re-frame/reg-flow
 (let [inputs {:patterns (re-frame/flow<- ::entity+definition+instrument-ds+qp)}]
   {:id          ::sorted-patterns
    :doc         "Sort patterns into a seq."
    :live-inputs inputs
    :inputs      inputs
    :live?       :patterns
    :output      (fn [{:keys [patterns]}]
                   (->> patterns
                        vals
                        (map (fn [{:keys [entity definition instrument-data-structure] :as m}]
                               (let [{intervals :fretboard-pattern/intervals} definition
                                     nr-of-intervals                          (count intervals)
                                     sort-score
                                     (common/sort-fretboard-nr nr-of-intervals instrument-data-structure)]
                                 {:entity     entity
                                  :sort-score sort-score})))
                        (filter :sort-score)
                        (sort-by :sort-score)))
    :path        (path ::sorted-patterns)}))

(re-frame/reg-sub ::sorted-patterns :-> #(get-in % (path ::sorted-patterns)))

(comment
  @(re-frame/subscribe [:flow {:id ::sorted-patterns}])
  (get @re-frame.db/app-db app-db-path)
  )

(re-frame/reg-flow
 (let [inputs {:patterns        (re-frame/flow<- ::entity+definition+instrument-ds+qp)
               :sorted-patterns (re-frame/flow<- ::sorted-patterns)}]
   {:id          ::patterns->view
    :doc         "Patterns ready for view.
Returns a seq or maps:
[:entity, :definition, :instrument-data-structure, :query-params]"
    :inputs      inputs
    :live-inputs inputs
    :live?       (fn [{:keys [patterns sorted-patterns]}]
                   (and patterns sorted-patterns))
    :output      (fn [{:keys [patterns sorted-patterns]}]
                   (->> sorted-patterns
                        (mapv (fn [{:keys [entity _sort-score]}]
                                (get patterns entity)))))
    :path        (path ::patterns->view)}))

(re-frame/reg-sub  ::patterns->view :-> #(get-in % (path ::patterns->view)))

(comment
  @(re-frame/subscribe [:flow {:id ::patterns->view}])
  (get @re-frame.db/app-db app-db-path)
  )

(defn play-tone [{:keys [interval-tone octave] :as m}]
  (fn [_]
    (>evt [:tonejs/play-tone {:tone interval-tone :octave octave}])))

(defn add-play-tone
  [m]
  (assoc m :on-click (play-tone m)))

(re-frame/reg-flow
 (let [inputs {:e+d+i-ds+qp (re-frame/flow<- ::entity+definition+instrument-ds+qp)}]
   {:id          ::fretboard2-map
    :doc         "Fretboard2 matrix"
    :inputs      inputs
    :live-inputs inputs
    :live?       :e+d+i-ds+qp
    :output      (fn [{:keys [e+d+i-ds+qp]}]
                   (->> e+d+i-ds+qp
                        vals
                        (map (fn [m]
                               (let [instrument-data-structure (:instrument-data-structure m)
                                     entity                    (:entity m)
                                     query-params              (:query-params m)]
                                 [entity
                                  (->> (music-theory/fretboard-matrix->fretboard2
                                        query-params
                                        instrument-data-structure)
                                       (music-theory/map-matrix add-play-tone))])))

                        (into {})))
    :path (path ::fretboard2-map)}))

(re-frame/reg-sub  ::fretboard2-map :-> #(get-in % (path ::fretboard2-map)))


(comment
  @(re-frame/subscribe [:flow {:id ::fretboard2-map}])
  (get @re-frame.db/app-db app-db-path)
  )



(re-frame/reg-flow
 (let [inputs {:fretboard2-map  (re-frame/flow<- ::fretboard2-map)
               :sorted-patterns (re-frame/flow<- ::sorted-patterns)}]
   {:id          ::fretboard2->view
    :doc         "fretboard2 patterns ready for view."
    :inputs      inputs
    :live-inputs inputs
    :live?       (fn [{:keys [fretboard2-map sorted-patterns]}]
                   (and fretboard2-map sorted-patterns))
    :output      (fn [{:keys [fretboard2-map sorted-patterns]}]
                   (->> sorted-patterns
                        (mapv (fn [{:keys [entity _sort-score]}]
                                [entity
                                 {:entity            entity
                                  :fretboard2-matrix (get fretboard2-map entity)}]))
                        (into {})))
    :path        (path ::fretboard2->view)}))

(re-frame/reg-sub ::fretboard2->view :-> #(get-in % (path ::fretboard2->view)))

(comment
  @(re-frame/subscribe [:flow {:id ::fretboard2->view}])
  (get-in @re-frame.db/app-db (path ::fretboard2->view))
  )

(re-frame/reg-event-fx
 ::play-tones
 (fn [_ [_event-id entity fretboard-matrix]]
   {:fx (music-theory/fretboard-matrix->tonejs-dispatches-2 entity fretboard-matrix)}))

(re-frame/reg-event-fx
 ::play-tones-2
 (fn [_ [_event-id fxs]]
   {:fx fxs}))





(re-frame/reg-sub
 ::fretboard2-matrix
 (fn [db [_event-id entity]]
   (let [p (conj (path ::fretboard2->view) entity :fretboard2-matrix)
         fretboard2 (get-in db p)]
     fretboard2)))

(re-frame/reg-sub
 ::fretboard-matrix
 (fn [db [_event-id entity]]
   (let [p (conj (path ::entity+definition+instrument-ds+qp) entity :instrument-data-structure)
         fretboard-matrix (get-in db p)]
     fretboard-matrix)))


(defn up [entity fretboard-matrix]
  (->> fretboard-matrix
       (music-theory/filter-matches)
       (map (fn [{:keys [x y tone-str] :as m}]
              (assoc m
                     :tone tone-str
                     :circle-dom-id
                     (music-theory/circle-dom-id
                      (music-theory/entity-to-str entity)
                      x y))))
       (mapv #(select-keys % [:x :y :tone :octave :circle-dom-id]))))

(defn up-and-down [entity fretboard-matrix]
  (let [derps (up entity fretboard-matrix)]
    (concat derps (rest (reverse derps)))))

(defn up-and-down-repeat [entity fretboard-matrix]
  (let [ud           (up-and-down entity fretboard-matrix)
        last-removed (drop-last 1 ud)
        f            (first ud)]
    (concat
     (take (* (count last-removed) 100)
           (cycle last-removed))
     [f])))


(def min-in-milli 60000)

(defonce to-play
  (atom []))

(defn play-and-highlight [elm bpm {:keys [circle-dom-id tone octave x y]}]
  (let [current-background-color (-> elm
                                     (.-style)
                                     (.-backgroundColor))]
    (tonejs/play-tone {:tone tone :octave octave})
    (-> elm
        (.-style)
        (.-backgroundColor)
        (set! "green"))
    (js/setTimeout (fn []
                     (-> js/document
                         (.getElementById circle-dom-id)
                         (.-style)
                         (.-backgroundColor)
                         (set! (str current-background-color))))
                   bpm)))

(def current-player-ids (atom {}))

(defn play-tone-handler [{:keys [action times bpm fretboard-matrix entity] :as args}]
  (condp = action
    :play  (do
             (play-tone-handler (assoc args :action :stop))
             (let [ts (condp = times
                        :up-and-down-repeat (up-and-down-repeat entity fretboard-matrix)
                        (up-and-down entity fretboard-matrix))
                   interval-id
                   (js/setInterval
                    (fn []
                      (if-let [{:keys [circle-dom-id] :as f} (first @to-play)]
                        (when-let [elm (-> js/document
                                           (.getElementById circle-dom-id))]
                          (play-and-highlight
                           elm
                           (/ min-in-milli bpm)
                           f)
                          (swap! to-play rest))
                        (play-tone-handler (assoc args :action :stop))))
                    (/ min-in-milli bpm))]
               (reset! to-play ts)
               (swap! current-player-ids assoc entity interval-id)))
    :stop (when-let [id (get @current-player-ids entity)]
            (js/clearInterval id)
            (swap! current-player-ids dissoc entity))))

(re-frame/reg-fx
 ::play-tone-handler
 play-tone-handler)



(re-frame/reg-event-fx
 ::play-tone-new-derp
 (fn [{:keys [db]} [_event-id {:keys [action times bpm fretboard-matrix entity] :as m}]]
   (let [bpm-from-qp (get-in db [:query-params :bpm] 80)]
     #_(cond-> {::play-tone-handler m}
         (nil? bpm) (assoc :bpm bpm-from-qp))
     {::play-tone-handler (assoc m :bpm bpm-from-qp)})))

(defn scale-pattern-view
  [entity]
  (let [fretboard2-matrix (<sub [::fretboard2-matrix entity])
        fretboard-matrix  (<sub [::fretboard-matrix entity])]
    [:<>
     [fretboard2/styled-view {:id               (:id entity)
                              :fretboard-matrix fretboard2-matrix
                              :entity-str       (models.entity/entity-to-str entity)
                              :fretboard-size   1}]
     [:br]
     [:div {:style {:display "flex"}}
      [:div {:style {:margin-right "0.5em"}}
       [:button
        {:on-click #(>evt [::play-tone-new-derp {:action :play
                                                 :times :up-and-down
                                                 :fretboard-matrix fretboard-matrix
                                                 :entity entity}])}
        "Play - up and down"]]
      [:div {:style {:margin-right "0.5em"}}
       [:button
        {:on-click #(>evt [::play-tone-new-derp {:action :play
                                                 :times :up-and-down-repeat
                                                 :fretboard-matrix fretboard-matrix
                                                 :entity entity}])}
        "Play - up and down, repeat"]]
      [:button
       {:on-click #(>evt [::play-tone-new-derp {:action :stop
                                                :entity entity}])}
       "Stop"]]]))

(defn scale-component [deps]
  (let [{:keys [id scale instrument] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                                  @(re-frame/subscribe [:query-params])
        debug?                                        (:debug query-params)
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
        entities            (->> scale-pattern-entities
                                 (map (fn [{:keys [id] :as entity}]
                                        (merge (music-theory/get-definition id) entity)))
                                 (map (fn [{id :id pattern-hash :fretboard-pattern/pattern-hash :as definition}]
                                        [(or pattern-hash id) definition]))
                                 (into {})
                                 (vals)
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
                                 (map :id))
        fretboard2-matrixes @(re-frame/subscribe [:flow {:id ::fretboard2->view}])
        entities++          @(re-frame/subscribe [:flow {:id ::entity+definition+instrument-ds+qp}])
        sorted-patterns     (<sub [::sorted-patterns])]
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

     (when (seq sorted-patterns)
         [:<>
          [:h2 "Scale patterns"]
          (for [{:keys [entity]} sorted-patterns]
            ^{:key (hash entity)}
            [:<>
             [scale-pattern-view entity]
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
