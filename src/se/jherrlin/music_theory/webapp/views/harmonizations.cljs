(ns se.jherrlin.music-theory.webapp.views.harmonizations
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [re-frame.alpha :as re-frame]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.utils :as utils]
   [se.jherrlin.music-theory.webapp.views.common :as common]
   [re-frame.db :as db]))

(comment
  @re-frame.db/app-db
  )


(re-frame/reg-flow
 {:id     ::harmonization-chords
  :inputs {:instrument    (re-frame/flow<- ::events/instrument)
           :harmonization (re-frame/flow<- ::events/harmonization)
           :scale         (re-frame/flow<- ::events/harmonization-scale)
           :key-of        [:path-params :key-of]}
  :output (fn [{{harmonization-chords :chords}     :harmonization
                {scale-intervals :scale/intervals} :scale
                :keys
                [instrument harmonization scale key-of]}]
            (let [interval-tones (music-theory/interval-tones scale-intervals key-of)]
              (def harmonization-chords harmonization-chords)
              (->> harmonization-chords
                   (map (fn [{:keys [idx-fn] :as m}]
                          (assoc m :key-of (idx-fn interval-tones))))
                   (map
                    (fn [harmonization-chord]
                      (let [chord                              (music-theory/get-chord (get harmonization-chord :chord))
                            {chord-intervals :chord/intervals} chord]
                        (-> chord
                            (merge harmonization-chord)
                            (assoc :key-of key-of
                                   :interval-tones (music-theory/interval-tones
                                                    chord-intervals
                                                    key-of)))))))))
  :path   [:harmonization-chords]})

(re-frame/reg-sub :harmonization-chords (fn [db [n']] (get db n')))

(defn table []
  (let [ms           @(re-frame/subscribe [:harmonization-chords])
        path-params  @(re-frame/subscribe [:path-params])
        query-params @(re-frame/subscribe [:query-params])]
    [:<>
     [:p "T = Tonic (stable), S = Subdominant (leaving), D = Dominant (back home)"]
     [:table
      [:tr (map (fn [m]
                  [:th (:idx m)]) ms)]
      [:tr (map (fn [m] [:th (:symbol m)]) ms)]

      [:tr (map (fn [{mode   :mode
                      key-of :key-of
                      :as    m}]
                  [:th
                   [:a {:href (rfe/href :scale
                                        (assoc path-params :scale mode :key-of key-of)
                                        query-params)}
                    (-> mode name str/capitalize)]]) ms)]
      [:tr (map (fn [m] [:th (-> m :family name str/capitalize)]) ms)]
      [:tr (map (fn [{key-of :key-of
                      chord  :chord
                      suffix :chord/suffix}]
                  [:th
                   [:a
                    {:href (rfe/href
                            :chord
                            (assoc path-params :chord chord :key-of key-of)
                            query-params)}
                    (str (-> key-of name str/capitalize)
                         suffix)]]) ms)]]]))

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

     [common/select-harmonization]

     [common/settings
      {:as-text?        (= instrument-type :fretboard)
       :nr-of-frets?    (= instrument-type :fretboard)
       :trim-fretboard? (= instrument-type :fretboard)
       :nr-of-octavs?   (= instrument-type :keyboard)}]

     [table]

     [:br]

     [common/instrument-view
      harmonization-scale
      instrument
      path-params
      query-params
      deps]

     (for [{:keys [id] :as harmonization-chord} harmonization-chords]
       ^{:key (str "harmonization-chord-" id)}
       [common/instrument-view
        harmonization-chord
        instrument
        path-params
        query-params
        deps])]))


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
