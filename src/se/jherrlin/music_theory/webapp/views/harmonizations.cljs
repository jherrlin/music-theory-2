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
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard :as instruments-fretboard]
   [re-frame.db :as db]))

(comment
  (get-in @re-frame.db/app-db [:se.jherrlin.music-theory.webapp.events/fretboards])

  (hash
   {:id         #uuid "39af7096-b5c6-45e9-b743-6791b217a3df",
    :instrument :mandolin,
    :key-of     :c})

  (let [entity {:id         #uuid "39af7096-b5c6-45e9-b743-6791b217a3df",
                :instrument :mandolin,
                :key-of     :c}]
    (get-in @re-frame.db/app-db [:se.jherrlin.music-theory.webapp.events/fretboards entity :fretboard]))

  (let [entity {:id         #uuid "eebf1ac1-b3c5-46f1-87ac-f8d24823b730",
                :instrument :mandolin,
                :key-of     :c}]
    (get-in @re-frame.db/app-db [:se.jherrlin.music-theory.webapp.events/fretboards entity :fretboard]))
  )


(def events-
  [{:n ::harmonization-scale}
   {:n ::harmonization-chords}
   {:n ::derp}])

(doseq [{:keys [n s e]} events-]
  (re-frame/reg-sub n (or s (fn [db [n']] (get db n'))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))

(comment
  (get @re-frame.db/app-db ::derp)
  )


(def gather-data-for-view-
  (fn [{:keys [db]} [_ {:keys [instrument key-of harmonization-id harmonization-scale] :as m}]]
    (let [query-params          (events/query-params db)
          {:keys [id]}          (music-theory/get-scale harmonization-scale)
          scale-entity          (music-theory/entity key-of instrument id)
          harmonization-chords  (music-theory/calc-harmonization-chords m)
          harmonization-chords' (->> harmonization-chords
                                     (map music-theory/select-entity-keys)
                                     (mapv (fn [entity]
                                             (music-theory/instrument-data-structure entity query-params)))
                                     (apply music-theory/merge-fretboards-matrixes))]
      {:db (assoc
            db
            ::derp harmonization-chords'
            ::harmonization-scale scale-entity
            ::harmonization-chords harmonization-chords)
       :fx (->> (concat
                 [scale-entity]
                 harmonization-chords)
                (map music-theory/select-entity-keys)
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

(defn harmonizations-view [{:keys [play-tone] :as deps}]
  (let [path-params                            @(re-frame/subscribe [:path-params])
        {:keys
         [as-intervals trim-fretboard surrounding-intervals surrounding-tones
          show-octave debug]
         :as query-params}                     @(re-frame/subscribe [:query-params])
        harmonization-chords                   @(re-frame/subscribe [::harmonization-chords])
        harmonization-scale                    @(re-frame/subscribe [::harmonization-scale])
        {instrument-type :type :as instrument} @(re-frame/subscribe [:instrument])
        derp-matrix                    @(re-frame/subscribe [::derp])
        ]
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

     #_[instruments-fretboard/styled-view
      (cond-> {:id                "derp-it"
               :on-click          (fn [{:keys [tone-str octave]} fretboard-matrix]
                                    (play-tone (str tone-str octave)))
               :fretboard-matrix  derp-matrix
               :dark-orange-fn    (fn [{:keys [root?] :as m}]
                                    (and root? (get m :out)))
               :orange-fn         :out}
        show-octave           (assoc :show-octave? true)
        surrounding-intervals (assoc :grey-fn :interval)
        surrounding-tones     (assoc :grey-fn :tone-str))]

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
                      (events/do-on-url-change route-name p q)
                      (re-frame/dispatch [::gather-data-for-view p]))}]}]))
