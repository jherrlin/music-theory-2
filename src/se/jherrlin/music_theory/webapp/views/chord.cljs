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
  [{:n ::chord-unit}
   {:n ::chord-pattern-units}
   {:n ::chord-triad-pattern-units}])

(doseq [{:keys [n s e]} events-]
  (re-frame/reg-sub n (or s (fn [db [n']] (get db n'))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))

(defn to-units
  ([key-of instrument ms]
   (to-units key-of instrument :id ms))
  ([key-of instrument id-fn ms]
   (->> ms
        (map (fn [m]
               (let [id (id-fn m)]
                 (music-theory/unit key-of instrument id)))))))

(re-frame/reg-event-db
 :add-units-with-fretboard
 (fn [db [_ units fretboard]]
   (update db ::fretboards
           #(reduce
             (fn [m unit]
               (-> m
                   (assoc-in [::fretboards unit :id] unit)
                   (assoc-in [::fretboards unit :fretboard] fretboard)))
             %
             units))))

(re-frame/reg-event-db
 :add-unit-with-fretboard
 (fn [db [_ unit fretboard]]
   (-> db
       (update ::fretboards assoc-in [::fretboards unit :id] unit)
       (update ::fretboards assoc-in [::fretboards unit :fretboard] fretboard))))

(re-frame/reg-sub
 :fretboard-by-unit
 (fn [db [_ unit]]
   (get-in db [::fretboards unit :fretboard])))

(defn calc-chord-view [{:keys [chord key-of instrument]} {:keys [nr-of-frets]}]
  (let [{:keys [id] :as chord'}   (music-theory/get-chord chord)
        {:keys [tuning]}          (music-theory/get-instrument instrument)
        chord-unit                (music-theory/unit key-of instrument id)
        fretboard-matrix          (music-theory/create-fretboard-matrix key-of nr-of-frets tuning)
        chord-patterns            (music-theory/chord-patterns-belonging-to chord instrument)
        chord-pattern-units       (to-units key-of instrument chord-patterns)
        chord-triad-patterns      (music-theory/chord-pattern-triads-belonging-to chord instrument)
        chord-triad-pattern-units (to-units key-of instrument chord-triad-patterns)]
    (re-frame/dispatch [:add-unit-with-fretboard chord-unit fretboard-matrix])
    (re-frame/dispatch [:add-units-with-fretboard chord-pattern-units fretboard-matrix])
    (re-frame/dispatch [:add-units-with-fretboard chord-triad-pattern-units fretboard-matrix])
    (re-frame/dispatch [::chord-unit chord-unit])
    (re-frame/dispatch [::chord-pattern-units chord-pattern-units])
    (re-frame/dispatch [::chord-triad-pattern-units chord-triad-pattern-units])))

(comment

  (calc-chord-view
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
        chord-patterns                          (music-theory/chord-patterns-belonging-to chord instrument)
        chord-triad-patterns                    (music-theory/chord-pattern-triads-belonging-to chord instrument)]
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
      chord-definition instrument' path-params query-params deps]

     (when (seq chord-patterns)
       [:h2 "Chord patterns"])

     (for [{:keys [id] :as pattern-definitions} chord-patterns]
       ^{:key (str "chord-patterns-" id)}
       [:<>
        [common/instrument-view
         pattern-definitions instrument' path-params query-params deps]
        [:br]
        [:br]])

     (when (seq chord-triad-patterns)
       [:h2 "Triads"])

     (for [{:keys [id] :as pattern-definitions} chord-triad-patterns]
       ^{:key (str "chord-triad-patterns-" id)}
       [:<>
        [common/instrument-view
         pattern-definitions instrument' path-params query-params deps]
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
                      (calc-chord-view p q)
                      (events/do-on-url-change route-name p q))}]}]))
