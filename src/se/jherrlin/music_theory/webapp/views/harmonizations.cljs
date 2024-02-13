(ns se.jherrlin.music-theory.webapp.views.harmonizations
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [re-frame.alpha :as re-frame]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.common :as common]
   [re-frame.db :as db]))

(comment
  @re-frame.db/app-db
  )


(def events-
  [{:n ::harmonization-scale}
   {:n ::harmonization-chords}])

(doseq [{:keys [n s e]} events-]
  (re-frame/reg-sub n (or s (fn [db [n']] (get db n'))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))

(defn calc-harmonization-scale
  [{:keys [harmonization-scale key-of instrument]}
   {:keys [nr-of-frets] :as query-params}]
  (let [{scale-names :scale/scale-names
         :keys       [id] :as scale'} (music-theory/get-scale harmonization-scale)
        instrument-tuning             (music-theory/get-instrument-tuning instrument)
        scale-entity                  (music-theory/entity key-of instrument id)]
    (let [entity scale-entity
          fretboard-matrix (common/prepair-instrument-data-for-entity entity {} query-params)]
      (re-frame/dispatch [:add-entity-with-fretboard entity fretboard-matrix]))

    (re-frame/dispatch [::harmonization-scale scale-entity])))

(defmulti calc-harmonization-chords
  (fn [{:keys [harmonization scale key-of]} query-params]
    (get harmonization :type)))

(defmethod calc-harmonization-chords :predefined
  [{{harmonization-chords :chords}     :harmonization
    {scale-intervals :scale/intervals} :scale
    :keys                              [key-of instrument nr-of-frets]}
   query-params]
  (let [instrument-tuning    (music-theory/get-instrument-tuning instrument)
        interval-tones       (music-theory/interval-tones key-of scale-intervals)
        harmonization-chords (->> harmonization-chords
                                  (map (fn [{:keys [idx-fn] :as m}]
                                         (assoc m :key-of (idx-fn interval-tones))))
                                  (map
                                   (fn [{:keys [key-of] :as harmonization-chord}]
                                     (let [chord
                                           (music-theory/get-chord (get harmonization-chord :chord))
                                           {chord-intervals :chord/intervals} chord]
                                       (-> (merge harmonization-chord chord)
                                           (assoc :key-of key-of
                                                  :instrument instrument
                                                  :interval-tones (music-theory/interval-tones
                                                                   key-of
                                                                   chord-intervals)))))))]
    ;; Create fretboard matrixes
    (doseq [chord harmonization-chords]
      (let [entity           (music-theory/select-entity-keys chord)
            fretboard-matrix (common/prepair-instrument-data-for-entity entity {} query-params)]
        (re-frame/dispatch [:add-entity-with-fretboard entity fretboard-matrix])))

    (re-frame/dispatch [::harmonization-chords harmonization-chords])))

(defmethod calc-harmonization-chords :generated
  [{:keys [key-of harmonization scale instrument nr-of-frets]} query-params]
  (let [scale-indexes        (get scale :scale/indexes)
        scale-intervals      (get scale :scale/intervals)
        chord-fn             (get harmonization :function)
        instrument-tuning    (music-theory/get-instrument-tuning instrument)
        scale-interval-tones (music-theory/interval-tones key-of scale-intervals)
        scale-index-tones    (music-theory/tones-by-key-and-indexes key-of scale-indexes)
        found-chords         (map (fn [tone]
                                    (let [index-tones-in-chord (music-theory/rotate-until
                                                                #(% tone)
                                                                scale-index-tones)]
                                      (-> (music-theory/find-chord
                                           music-theory/chords
                                           (chord-fn index-tones-in-chord))
                                          (assoc :key-of tone
                                                 :instrument instrument))))
                                  scale-interval-tones)
        first-is-major?      ((-> found-chords first :chord/categories) :major)
        harmonization-chords (mapv
                              #(assoc %1
                                      :idx %2
                                      :symbol %3
                                      :mode  %4
                                      :family %5)
                              found-chords        ;; 1
                              (iterate inc 1)     ;; 2
                              (if first-is-major? ;; 3
                                ["I" "ii" "iii" "IV" "V" "vi" "vii"]
                                ["i" "ii" "III" "iv" "v" "VI" "VII"])
                              (if first-is-major? ;; 4
                                [:ionian  :dorian  :phrygian :lydian :mixolydian :aeolian :locrian]
                                [:aeolian :locrian :ionian   :dorian :phrygian   :lydian  :mixolydian])
                              (if first-is-major?
                                [:tonic :subdominant :tonic :subdominant :dominant :tonic :dominant]
                                [:tonic :subdominant :tonic :subdominant :dominant :subdominant :dominant]))]

    (doseq [chord harmonization-chords]
      (let [entity           (music-theory/select-entity-keys chord)
            fretboard-matrix (common/prepair-instrument-data-for-entity entity {} query-params)]
        (re-frame/dispatch [:add-entity-with-fretboard entity fretboard-matrix])))

    (re-frame/dispatch [::harmonization-chords harmonization-chords])))

(defn table []
  (let [ms           @(re-frame/subscribe [::harmonization-chords])
        path-params  @(re-frame/subscribe [:path-params])
        query-params @(re-frame/subscribe [:query-params])]
    [:<>
     [:p "T = Tonic (stable), S = Subdominant (leaving), D = Dominant (back home)"]
     [:table
      [:thead
       [:tr
        (for [{:keys [idx] :as m} ms]
          ^{:key (str "harmonization-table-" idx)}
          [:th idx])]]
      [:tbody
       [:tr
        (for [{:keys [idx symbol] :as m} ms]
          ^{:key (str "harmonization-table-symbol" idx symbol)}
          [:th symbol])]

       [:tr
        (for [{:keys [idx symbol mode key-of] :as m} ms]
          ^{:key (str "harmonization-table-link" idx symbol)}
          [:th
           [:a {:href (rfe/href :scale
                                (assoc path-params :scale mode :key-of key-of)
                                query-params)}
            (-> mode name str/capitalize)]])]
       [:tr
        (for [{:keys [idx family] :as m} ms]
          ^{:key (str "harmonization-table-name" idx symbol)}
          [:th
           (-> family name str/capitalize)])]
       [:tr
        (for [{suffix :chord/suffix
               chord :chord/chord-name
               :keys  [idx key-of] :as m} ms]
          ^{:key (str "harmonization-table-chord" idx suffix)}
          [:th
           [:a {:href (rfe/href
                       :chord
                       (assoc path-params :chord chord :key-of key-of)
                       query-params)}
            (str (-> key-of name str/capitalize)
                 suffix)]])]]]]))

(defn harmonizations-view [deps]
  (let [path-params                            @(re-frame/subscribe [:path-params])
        query-params                           @(re-frame/subscribe [:query-params])
        harmonization-chords                   @(re-frame/subscribe [::harmonization-chords])
        harmonization-scale                    @(re-frame/subscribe [::harmonization-scale])
        {instrument-type :type :as instrument} @(re-frame/subscribe [:instrument])]
    [:<>
     [common/menu]
     [:br]
     [common/instrument-selection]
     [:br]
     [:br]
     [common/instrument-details instrument]
     [:br]
     [:br]
     [common/key-selection]
     [:br]
     [:br]
     [common/harmonization-scale-selection]
     [:br]

     [common/select-harmonization]
     [:br]

     [common/settings
      {:as-text?        (= instrument-type :fretboard)
       :nr-of-frets?    (= instrument-type :fretboard)
       :trim-fretboard? (= instrument-type :fretboard)
       :nr-of-octavs?   (= instrument-type :keyboard)}]

     [table]
     [:br]
     [:br]

     [common/instrument-view
      harmonization-scale
      path-params
      query-params
      deps]

     [:<>
      (for [{:keys [id idx key-of] :as harmonization-chord} harmonization-chords]
        ^{:key (str "harmonization-chord-" id idx)}
        [:<>
         [common/harmonizations-chord-details
          harmonization-chord
          instrument
          (assoc path-params :key-of key-of)
          query-params]
         [common/instrument-view
          (music-theory/select-entity-keys harmonization-chord)
          (assoc path-params :key-of key-of)
          query-params
          deps]])]]))

(defn gather-data-for-route
  [{:keys [instrument key-of harmonization-id harmonization-scale] :as path-params}
   {:keys [nr-of-frets] :as query-params}]
  (let [scale         (music-theory/get-scale harmonization-scale)
        harmonization (music-theory/get-harmonization harmonization-id)]
    (def instrument instrument)
    (def key-of key-of)
    (def harmonization-id harmonization-id)
    (def harmonization-scale harmonization-scale)
    (def nr-of-frets nr-of-frets)
    (def scale scale)
    (def harmonization harmonization)

    (calc-harmonization-scale path-params query-params)

    (calc-harmonization-chords
     {:harmonization harmonization
      :scale         scale
      :key-of        key-of
      :instrument    instrument
      :nr-of-frets   nr-of-frets}
     query-params)))

(defn routes [deps]
  (let [route-name :harmonizations]
    ["/harmonizations/:instrument/:key-of/:harmonization-scale/:harmonization-id"
     {:name       route-name
      :view       [harmonizations-view deps]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:path  [:map
                           [:instrument          keyword?]
                           [:key-of              keyword?]
                           [:harmonization-id    keyword?]
                           [:harmonization-scale keyword?]]
                   :query events/Query}
      :controllers
      [{:parameters {:path  [:instrument :key-of :harmonization-id :harmonization-scale]
                     :query events/query-keys}
        :start      (fn [{p :path q :query}]
                      (gather-data-for-route p q)
                      (events/do-on-url-change route-name p q))}]}]))
