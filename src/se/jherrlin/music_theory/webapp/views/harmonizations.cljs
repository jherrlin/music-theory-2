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



(defmulti calc-harmonization
  (fn [{:keys [harmonization scale key-of]}]
    (get harmonization :type)))

(defmethod calc-harmonization :predefined
  [{{harmonization-chords :chords}     :harmonization
    {scale-intervals :scale/intervals} :scale
    :keys [key-of]}]
  (let [interval-tones (music-theory/interval-tones scale-intervals key-of)]
    (->> harmonization-chords
         (map (fn [{:keys [idx-fn] :as m}]
                (assoc m :key-of (idx-fn interval-tones))))
         (map
          (fn [{:keys [key-of] :as harmonization-chord}]
            (let [chord
                  (music-theory/get-chord (get harmonization-chord :chord))
                  {chord-intervals :chord/intervals} chord]
              (-> (merge harmonization-chord chord)
                  (assoc :key-of key-of
                         :interval-tones (music-theory/interval-tones
                                          chord-intervals
                                          key-of)))))))))

(defmethod calc-harmonization :generated
  [{:keys [key-of harmonization scale]}]
  (let [scale-indexes        (get scale :scale/indexes)
        scale-intervals      (get scale :scale/intervals)
        chord-fn             (get harmonization :function)
        scale-interval-tones (music-theory/scale-interval-tones key-of scale-intervals)
        scale-index-tones    (music-theory/tones-by-key-and-indexes key-of scale-indexes)
        found-chords         (map (fn [tone]
                                    (let [index-tones-in-chord (music-theory/rotate-until
                                                                #(% tone)
                                                                scale-index-tones)]
                                      (-> (music-theory/find-chord
                                             music-theory/chords
                                             (chord-fn index-tones-in-chord))
                                          (assoc :key-of tone))))
                                  scale-interval-tones)
        first-is-major? ((-> found-chords first :chord/categories) :major)]
    (mapv
     #(assoc %1
             :idx %2
             :symbol %3
             :mode  %4
             :family %5)
     found-chords ;; 1
     (iterate inc 1) ;; 2
     (if first-is-major? ;; 3
       ["I" "ii" "iii" "IV" "V" "vi" "vii"]
       ["i" "ii" "III" "iv" "v" "VI" "VII"])
     (if first-is-major? ;; 4
       [:ionian  :dorian  :phrygian :lydian :mixolydian :aeolian :locrian]
       [:aeolian :locrian :ionian   :dorian :phrygian   :lydian  :mixolydian])
     (if first-is-major?
       [:tonic :subdominant :tonic :subdominant :dominant :tonic :dominant]
       [:tonic :subdominant :tonic :subdominant :dominant :subdominant :dominant]))))

(re-frame/reg-flow
 {:id     ::harmonization-chords
  :inputs {:harmonization (re-frame/flow<- ::events/harmonization)
           :scale         (re-frame/flow<- ::events/harmonization-scale)
           :key-of        [:path-params :key-of]}
  :output (fn [m]
            (calc-harmonization m))
  :path   [:harmonization-chords]})

(re-frame/reg-sub :harmonization-chords (fn [db [n']] (get db n')))

(defn table []
  (let [ms           @(re-frame/subscribe [:harmonization-chords])
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
        harmonization-chords                   @(re-frame/subscribe [:harmonization-chords])
        harmonization-scale                    @(re-frame/subscribe [:harmonization-scale])
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
     [:br]

     [common/select-harmonization]

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
      instrument
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
          harmonization-chord
          instrument
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
                      (events/do-on-url-change route-name p q))}]}]))
