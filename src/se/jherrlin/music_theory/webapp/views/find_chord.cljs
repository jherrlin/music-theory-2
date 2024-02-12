(ns se.jherrlin.music-theory.webapp.views.find-chord
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [re-frame.core :as re-frame]
   [re-frame.alpha :as re-frame-alpha]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard :as instruments-fretboard]
   [se.jherrlin.music-theory.webapp.views.common :as common]))

(def events-
  [{:n ::fretboard-matrix
    :e (fn [db [n e]]
         (assoc db n e))}
   {:n ::select-tone
    :e (fn [db [n tone]]
         (let [tones (get db n)]
           (assoc
            db
            n
            (cond
              (not (seq tones))                    #{tone}
              (and (seq tones) (tones tone))       (disj tones tone)
              (and (seq tones) (not (tones tone))) (conj tones tone)))))
    :s (fn [db [n]] (get db n #{}))}])

(doseq [{:keys [n s e]} events-]
  (re-frame/reg-sub n (or s (fn [db [n']] (get db n'))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))

(re-frame/reg-sub
 ::selected-tones
 (fn [db [_]]
   (->> (get db ::select-tone)
        (map :tone)
        (set))))

(re-frame/reg-sub
 ::matching-chords
 (fn [db [_]]
   (let [xs (some->> (get db ::select-tone)
                     (map :tone)
                     (set))]
     (when (seq xs)
       (->> (music-theory/match-tones-with-chords xs)
            (sort-by :intersections-count #(compare %2 %1)))))))

(re-frame/reg-event-db
 ::fretboard-click
 (fn [db [_ x y]]
   (let [fretboard (get db ::fretboard-matrix)]
     (assoc db ::fretboard-matrix (music-theory/update-matrix
                                   x y
                                   (fn [{:keys [match?] :as fret}]
                                     (assoc fret :match? (not match?)))
                                   fretboard)))))
(defn table []
  (let [path-params     @(re-frame/subscribe [:path-params])
        query-params    @(re-frame/subscribe [:query-params])
        matching-chords @(re-frame/subscribe [::matching-chords])]
    (when matching-chords
      [:table
       [:thead
        [:tr
         [:th "Key of"]
         [:th "Suffix"]
         [:th "Intervals"]
         [:th "Tones"]
         [:th "Score"]]]
       [:tbody
        (for [{id           :id
               intervals    :chord/intervals
               suffix       :chord/suffix
               chord-name   :chord/chord-name
               display-text :chord/display-text
               key-of       :key-of
               intersections :intersections
               intersections-count :intersections-count}
              matching-chords]
          ^{:key (str "chord-list-" id intersections-count intersections key-of)}
          [:tr
           [:td (-> key-of name str/capitalize)]
           [:td
            [:a
             {:href
              (rfe/href
               :chord
               (assoc path-params :chord chord-name :key-of key-of)
               query-params)}
             (or display-text suffix)]]
           [:td
            (->> intervals
                 (str/join ", "))]
           [:td
            (->> (music-theory/tones-by-key-and-intervals key-of intervals)
                 (map (comp str/capitalize name))
                 (str/join ", "))]
           [:td intersections-count]])]])))

(defn find-chord-view [{:keys [play-tone] :as deps}]
  (let [{:keys [instrument] :as path-params}    @(re-frame/subscribe [:path-params])
        {:keys [surrounding-intervals surrounding-tones show-octave debug show-tones]}
        @(re-frame/subscribe [:query-params])
        {instrument-type :type :as instrument'} (music-theory/get-instrument instrument)
        fretboard-matrix                        @(re-frame/subscribe [::fretboard-matrix])
        select-tone                             @(re-frame/subscribe [::select-tone])
        selected-tones                          @(re-frame/subscribe [::selected-tones])]
    [:<>
     [common/menu]
     [:br]
     [common/instrument-selection]
     [:br]
     [common/instrument-details instrument']
     [:br]
     [:br]
     [common/settings
      {:show-tones?            true
       :as-intervals?          false
       :surrounding-intervals? false
       :surrounding-tones?     false
       :nr-of-frets?           (= instrument-type :fretboard)}]
     [:br]
     [:br]
     (when debug
       [:<>
        [common/debug-view fretboard-matrix]
        [common/debug-view select-tone]])
     [instruments-fretboard/styled-view
      (cond-> {:id            (random-uuid)
               :on-click       (fn [{:keys [tone octave x y] :as m} fretboard-matrix]
                                 (let [tone-str (-> tone first name str/capitalize)]
                                   (play-tone (str tone-str octave)))
                                 (re-frame/dispatch [::fretboard-click x y])
                                 (re-frame/dispatch [::select-tone (select-keys m [:tone :octave])]))
               :fretboard-matrix fretboard-matrix
               :orange-fn      (fn [{:keys [tone match?]}]
                                 (when (and show-tones (not match?))
                                   (some-> tone
                                           (music-theory/sharp-or-flat "b")
                                           name
                                           str/capitalize)))
               :dark-orange-fn (fn [{:keys [match? tone] :as m}]
                                 (when match?
                                   (some-> tone
                                           (music-theory/sharp-or-flat "b")
                                           name
                                           str/capitalize)))}
        show-octave           (assoc :show-octave? true)
        surrounding-intervals (assoc :grey-fn :interval)
        surrounding-tones     (assoc :grey-fn :tone-str))]
     [:br]
     [table]]))

(defn setup-view-data
  [{:keys [instrument] :as path-params}
   {:keys [nr-of-frets]
    :or {nr-of-frets 15}
    :as query-params}]
  (let [tuning (music-theory/get-instrument-tuning instrument)
        fretboard-matrix (music-theory/create-fretboard-matrix nr-of-frets tuning)]
    (re-frame/dispatch [::fretboard-matrix fretboard-matrix])))

(defn routes [deps]
  (let [route-name :find-chord]
    ["/find-chord/:instrument/"
     {:name       route-name
      :view       [find-chord-view deps]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:path  [:map
                           [:instrument keyword?]]
                   :query events/Query}
      :controllers
      [{:parameters {:path  [:instrument]
                     :query events/query-keys}
        :start      (fn [{p :path q :query}]
                      (setup-view-data p q)
                      (events/do-on-url-change route-name p q))}]}]))
