(ns se.jherrlin.music-theory.webapp.views.scale
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [re-frame.core :as re-frame]
   [re-frame.alpha :as re-frame-alpha]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.utils :as utils]
   [se.jherrlin.music-theory.webapp.views.common :as common]))

(def events-
  [{:n ::scale-entity}
   {:n ::scale-pattern-entities}])

(doseq [{:keys [n s e]} events-]
  (re-frame/reg-sub n (or s (fn [db [n']] (get db n'))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))

(defn gather-view-data [{:keys [scale key-of instrument]} {:keys [nr-of-frets]}]
  (let [{scale-names :scale/scale-names
         :keys       [id] :as scale'} (music-theory/get-scale scale)
        {:keys [tuning]}              (music-theory/get-instrument instrument)
        scale-entity                  (music-theory/entity key-of instrument id)
        fretboard-matrix              (music-theory/create-fretboard-matrix key-of nr-of-frets tuning)
        scale-patterns                (music-theory/scale-patterns-for-scale-and-instrument
                                       scale-names
                                       instrument)
        scale-pattern-entities        (music-theory/definitions-to-entities key-of instrument scale-patterns)]
    (re-frame/dispatch [:add-entity-with-fretboard scale-entity fretboard-matrix])
    (re-frame/dispatch [:add-entities-with-fretboard scale-pattern-entities fretboard-matrix])
    (re-frame/dispatch [::scale-entity scale-entity])
    (re-frame/dispatch [::scale-pattern-entities scale-pattern-entities])))

(defn scale-component [deps]
  (let [{:keys [id scale instrument] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                                  @(re-frame/subscribe [:query-params])
        {instrument-type :type :as instrument'}       (music-theory/get-instrument instrument)
        {scale-intervals :scale/intervals
         scale-names     :scale/scale-names
         :as             scale'}                      (music-theory/get-scale scale)
        scale-patterns                                (music-theory/scale-patterns-for-scale-and-instrument scale-names instrument)
        scale-entity                                  @(re-frame/subscribe [::scale-entity])
        scale-pattern-entities                        @(re-frame/subscribe [::scale-pattern-entities])]
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
        (for [{:keys [id] :as entity} scale-pattern-entities]
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
                      (gather-view-data p q)
                      (events/do-on-url-change route-name p q))}]}]))
