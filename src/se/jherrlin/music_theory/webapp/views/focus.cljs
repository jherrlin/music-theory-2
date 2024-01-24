(ns se.jherrlin.music-theory.webapp.views.focus
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
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard :as instruments-fretboard]))


(defn debug-view
  ([]
   (debug-view @re-frame.db/app-db))
  ([x]
   [:pre
    (with-out-str
      (cljs.pprint/pprint x))]))

(defmulti instrument-view
  (fn [definition instrument path-params query-params]
    [(get instrument :type) (get definition :type)]))

(defmethod instrument-view [:fretboard [:chord]]
  [definition instrument
   {:keys [key-of] :as path-params}
   {:keys [as-intervals trim-fretboard] :as query-params}]
  (let [fretboard-matrix @(re-frame/subscribe [:fretboard-matrix])
        intervals        (get definition :chord/intervals)
        interval-tones   (music-theory/interval-tones intervals key-of)
        fretboard (if as-intervals
                    (music-theory/with-all-intervals
                      (mapv vector interval-tones intervals)
                      fretboard-matrix)
                    (music-theory/with-all-tones
                      interval-tones
                      fretboard-matrix))]
    [:<>
     #_[debug-view definition]
     #_[debug-view fretboard]
     [instruments-fretboard/styled-view
      {:matrix         (cond->> fretboard
                         trim-fretboard (music-theory/trim-matrix
                                         #(every? nil? (map :out %))))
       :dark-orange-fn (fn [{:keys [root?] :as m}]
                         (and root? (get m :out)))
       :orange-fn      :out #_:pattern-found-tone #_:pattern-found-interval
                                        ;:grey-fn        nil #_:tone-str           #_:interval
       }]]))

(defmethod instrument-view [:fretboard [:chord :pattern]]
  [{pattern :fretboard-pattern/pattern :as definition}
   instrument
   {:keys [key-of] :as path-params}
   {:keys [as-intervals trim-fretboard] :as query-params}]
  (let [fretboard-matrix @(re-frame/subscribe [:fretboard-matrix])
        matrix           ((if as-intervals
                            music-theory/pattern-with-intervals
                            music-theory/pattern-with-tones)
                          key-of
                          pattern
                          fretboard-matrix)
        fretboard'       ((if as-intervals
                            music-theory/pattern-with-intervals
                            music-theory/pattern-with-tones)
                          key-of
                          pattern
                          fretboard-matrix)]
    [:<>
     #_[debug-view definition]
     #_[debug-view fretboard']
     [instruments-fretboard/styled-view
      {:matrix
       (cond->> fretboard'
         trim-fretboard (music-theory/trim-matrix #(every? nil? (map :out %))))
       :dark-orange-fn (fn [{:keys [root?] :as m}]
                         (and root? (get m :pattern-found-tone)))
       :orange-fn      :pattern-found-tone #_:pattern-found-interval
       :grey-fn        :tone-str #_:interval}]]))

(defmethod instrument-view [:fretboard [:scale]]
  [definition instrument
   {:keys [key-of] :as path-params}
   {:keys [as-intervals trim-fretboard] :as query-params}]
  (let [fretboard-matrix @(re-frame/subscribe [:fretboard-matrix])
        intervals        (get definition :scale/intervals)
        interval-tones   (music-theory/interval-tones intervals key-of)
        fretboard (if as-intervals
                    (music-theory/with-all-intervals
                      (mapv vector interval-tones intervals)
                      fretboard-matrix)
                    (music-theory/with-all-tones
                      interval-tones
                      fretboard-matrix))]
    [:<>
     #_[debug-view definition]
     #_[debug-view fretboard]
     [instruments-fretboard/styled-view
      {:matrix         (cond->> fretboard
                         trim-fretboard (music-theory/trim-matrix
                                         #(every? nil? (map :out %))))
       :dark-orange-fn (fn [{:keys [root?] :as m}]
                         (and root? (get m :out)))
       :orange-fn      :out #_:pattern-found-tone #_:pattern-found-interval
                                        ;:grey-fn        nil #_:tone-str           #_:interval
       }]]))





(defmethod instrument-view :default
  [definition instrument path-params query-params]
  [:div
   [:h2 "Not implemented"]
   [debug-view definition]])



(defmulti definition-view (fn [definition instrument path-params query-params]
                            (get definition :type)))


{:chord/intervals      ["1" "3" "5"],
 :chord/chord-name-str "major",
 :chord/chord-name     :major,
 :chord/order          1,
 :type                 [:chord],
 :chord/categories     #{:major},
 :chord/display-text   "major",
 :id                   #uuid "1cd72972-ca33-4962-871c-1551b7ea5244",
 :chord/intervals-str  "1, 3, 5",
 :chord/explanation    "major",
 :chord/suffix         "",
 :chord/indexes        [0 4 7]}

(defmethod definition-view [:chord]
  [definition instrument {:keys [key-of] :as path-params}
   {:keys [as-intervals] :as query-params}]
  (let [intervals      (get definition :chord/intervals)
        interval-tones (music-theory/interval-tones intervals key-of)]
    [:div
   [:h2 "[:chord]"]
     [:p (str intervals)]
     [:p (str interval-tones)]
     [instrument-view definition instrument path-params query-params]]
    )
  )

(defmethod definition-view [:chord :pattern]
  [definition instrument path-params query-params]
  [:div
   [:h2 "[:chord :pattern]"]
   [instrument-view definition instrument path-params query-params]])

(defmethod definition-view [:scale]
  [definition instrument path-params query-params]
  [:div
   [:h2 "[:scale]"]
   [instrument-view definition instrument path-params query-params]])

(defmethod definition-view [:scale :pattern]
  [definition instrument path-params query-params]
  [:div
   [:h2 "[:scale :pattern]"]
   [instrument-view definition instrument path-params query-params]])

(defmethod definition-view :default
  [definition instrument path-params query-params]
  [:div
   [:h2 ":default"]
   [instrument-view definition instrument path-params query-params]])





(comment
  @(re-frame/subscribe [:general-data])
  @(re-frame/subscribe [:data-for-id #uuid "1cd72972-ca33-4962-871c-1551b7ea5244"])
  (music-theory/by-id #uuid "1cd72972-ca33-4962-871c-1551b7ea5244")
  )



(defn focus-view []
  (let [{:keys [id instrument] :as path-params} @(re-frame/subscribe [:path-params])
        _                            (def path-params path-params)
        _                            (def id id)
        query-params                 @(re-frame/subscribe [:query-params])
        _                            (def query-params query-params)
        fretboard-matrix             @(re-frame/subscribe [:fretboard-matrix])
        ]
    [:<>
     [:div "focus"]
     [instrument-view
      (music-theory/by-id id)
      (music-theory/instrument instrument)
      path-params
      query-params]
     [debug-view]
     #_[instruments-fretboard/styled-view
      {:matrix fretboard-matrix
       :display-fn :interval}]
     #_[instruments-fretboard/styled-view
      {:matrix fretboard-matrix
       :display-fn :tone-str}]]))





(defn routes []
  (let [route-name :focus]
    ["/focus/:instrument/:key-of/:id"
     {:name       route-name
      :view       [focus-view]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:path  [:map
                           [:instrument      keyword?]
                           [:key-of          keyword?]
                           [:id              uuid?]]
                   :query [:map
                           [:nr-of-frets  {:optional true} int?]
                           [:nr-of-octavs {:optional true} int?]
                           [:as-intervals {:optional true} boolean?]
                           [:as-text      {:optional true} boolean?]]}
      :controllers
      [{:parameters {:path  [:instrument :key-of :id]
                     :query [:nr-of-frets :as-intervals :as-text :nr-of-octavs]}
        :start      (fn [{p :path q :query}]
                      (events/do-on-url-change route-name p q))}]}]))
