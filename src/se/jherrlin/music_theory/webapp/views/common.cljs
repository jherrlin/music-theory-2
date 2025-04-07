(ns se.jherrlin.music-theory.webapp.views.common
  (:require
   [clojure.set :as set]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [re-frame.core :as re-frame]
   [re-frame.alpha :as re-frame-alpha]
   [reitit.frontend.easy :as rfe]
   [se.jherrlin.utils :as basic-utils]
   [reitit.coercion.malli]
   [cljs.pprint]
   [se.jherrlin.music-theory.webapp.views.instrument-render]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard :as instruments-fretboard]
   [se.jherrlin.music-theory.harmonizations :as harmonizations]
   [se.jherrlin.music-theory.webapp.views.intersecting-tones :as-alias intersecting-tones]
   [se.jherrlin.music-theory.webapp.views.learn.chord-tones :as-alias learn.chord-tones]))


(defn debug-view
  ([]
   (debug-view @re-frame.db/app-db))
  ([x]
   (when x
     [:pre
      (with-out-str
        (cljs.pprint/pprint x))])))

(defn menu []
  (let [current-route-name   @(re-frame/subscribe [:current-route-name])
        path-params          @(re-frame/subscribe [:path-params])
        changed-query-params @(re-frame/subscribe [:changed-query-params])
        key-of               @(re-frame/subscribe [:key-of])]
    [:div {:style {:display        "flow"
                   :flow-direction "column"
                   :overflow-x     "auto"
                   :white-space    "nowrap"}}
     [:a {:style {:margin-right "10px"}
          :href  (rfe/href :home path-params changed-query-params)}
      [:button
       {:disabled (= current-route-name :home)}
       "Home"]]

     [:a {:style {:margin-right "10px"}
          :href  (rfe/href :chord path-params changed-query-params)}
      [:button
       {:disabled (= current-route-name :chord)}
       "Chords"]]

     [:a {:style {:margin-right "10px"}
          :href  (rfe/href :scale path-params changed-query-params)}
      [:button
       {:disabled (= current-route-name :scale)}
       "Scales"]]

     [:a {:style {:margin-right "10px"}
          :href  (rfe/href :harmonizations path-params changed-query-params)}
      [:button
       {:disabled (= current-route-name :harmonizations)}
       "Harmonizations"]]

     [:a {:style {:margin-right "10px"}
          :href  (rfe/href :table path-params changed-query-params)}
      [:button
       {:disabled (= current-route-name :table)}
       "Table"]]

     (let [route-name ::intersecting-tones/intersecting-tones]
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href route-name
                             {:instrument :mandolin
                              :id-1 #uuid "0b675c5b-a6fe-44fe-b7cc-6596c6c570a4"
                              :id-2 #uuid "bac1ab62-34df-4232-b205-b197d25d8892"
                              :key-of-1 :d
                              :key-of-2 :e}
                             changed-query-params)}
        [:button
         {:disabled (= current-route-name route-name)}
         "Tone intersections"]])

     (let [route-name ::learn.chord-tones/chord-tones]
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href route-name
                             {:instrument :mandolin
                              :key-of :d
                              :harmonization-id :triads}
                             changed-query-params)}
        [:button
         {:disabled (= current-route-name route-name)}
         "Learn: chord tone locations"]])

     [:a {:style {:margin-right "10px"}
          :href  (rfe/href :bookmarks path-params changed-query-params)}
      [:button
       {:disabled (= current-route-name :bookmarks)}
       "Bookmarks"]]

     #_[:a {:style {:margin-right "10px"}
          :href  (rfe/href :teacher/list path-params changed-query-params)}
      [:button
       {:disabled (= current-route-name :teacher/list)}
       "Teacher list"]]

     #_(let [route-name :find-chord]
         [:a {:style {:margin-right "10px"}
              :href  (rfe/href route-name path-params changed-query-params)}
          [:button
           {:disabled (= current-route-name route-name)}
           "Find chord"]])

     #_(let [route-name :find-scale]
         [:a {:style {:margin-right "10px"}
              :href  (rfe/href route-name path-params changed-query-params)}
          [:button
           {:disabled (= current-route-name route-name)}
           "Find scale"]])

     [:a {:style  {:margin-right "10px"}
          :href   "https://github.com/jherrlin/music-theory-2"
          :target "_blank"}
      [:button
       "Source code"]]]))

(defn top-nav-abcjs-examples []
  (let [current-route-name   @(re-frame/subscribe [:current-route-name])
        path-params          @(re-frame/subscribe [:path-params])
        changed-query-params @(re-frame/subscribe [:changed-query-params])
        key-of               @(re-frame/subscribe [:key-of])]
    [:div {:style {:display        "flow"
                   :flow-direction "column"
                   :overflow-x     "auto"
                   :white-space    "nowrap"}}
     (let [k :abcjs-examples]
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href k {} {})}
        [:button
         {:disabled (= current-route-name k)}
         "Start"]])
     (let [k :abcjs-example/basic]
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href k {} {})}
        [:button
         {:disabled (= current-route-name k)}
         "Basic"]])
     (let [k :abcjs-example/editor]
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href k {} {})}
        [:button
         {:disabled (= current-route-name k)}
         "Editor"]])

     (let [k :abcjs-example/editor-with-play]
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href k {} {})}
        [:button
         {:disabled (= current-route-name k)}
         "Editor with play"]])

     (let [k :abcjs-example/editor-with-play-and-fretboard]
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href k {} {})}
        [:button
         {:disabled (= current-route-name k)}
         "Editor with play and fretboard"]])

     (let [k :abcjs-example/basic-synth]
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href k {} {})}
        [:button
         {:disabled (= current-route-name k)}
         "Basic synth"]])

     (let [k :abcjs-example/animation]
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href k {} {})}
        [:button
         {:disabled (= current-route-name k)}
         "Animation"]])

     (let [k :abcjs-example/instaparse]
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href k {} {})}
        [:button
         {:disabled (= current-route-name k)}
         "Instaparse"]])

     (let [k :abcjs-example/editor-component]
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href k {} {})}
        [:button
         {:disabled (= current-route-name k)}
         "Editor component"]])]))

(defn chord-selection []
  (let [{:keys [chord] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                    @(re-frame/subscribe [:query-params])
        changed-query-params            @(re-frame/subscribe [:changed-query-params])]
    [:div
     (for [{chord-name   :chord/chord-name
            display-text :chord/display-text
            suffix       :chord/suffix
            id           :id
            :as          m} music-theory/chords]
       ^{:key (str "chord-selection-" id)}
       [:div {:style {:margin-right "10px" :display "inline"}}
        [:a {:href (rfe/href
                    :chord
                    (assoc path-params :chord chord-name)
                    changed-query-params)}
         [:button
          {:disabled (= chord chord-name)}
          (if display-text
            (-> display-text str/capitalize)
            (-> suffix str/capitalize))]]])]))

(defn scale-selection []
  (let [{:keys [scale] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                    @(re-frame/subscribe [:query-params])
        changed-query-params            @(re-frame/subscribe [:changed-query-params])]
    [:div
     (for [{scale' :scale
            id     :id}
           music-theory/scales]
       ^{:key (str "scale-selection-" id scale')}
       [:div {:style {:margin-right "10px" :display "inline"}}
        [:a {:href (rfe/href :scale
                             (assoc path-params :scale scale')
                             changed-query-params)}
         [:button
          {:disabled (= scale scale')}
          (-> scale' name (str/replace "-" " ") str/capitalize)]]])]))

(defn harmonization-scale-selection []
  (let [{:keys [harmonization-scale] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                    @(re-frame/subscribe [:query-params])
        changed-query-params            @(re-frame/subscribe [:changed-query-params])]
    [:div
     (for [{scale  :scale
            id     :id} music-theory/scales-for-harmonization]
       ^{:key (str "scale-selection-" id scale)}
       [:div {:style {:margin-right "10px" :display "inline"}}
        [:a {:href (rfe/href :harmonizations
                             (assoc path-params :harmonization-scale scale)
                             changed-query-params)}
         [:button
          {:disabled (= harmonization-scale scale)}
          (-> scale name (str/replace "-" " ") str/capitalize)]]])]))

(defn scales-to-chord [path-params query-params chord-intervals]
  (let [scales-to-chord      (music-theory/scales-to-chord music-theory/scales chord-intervals)
        changed-query-params @(re-frame/subscribe [:changed-query-params])]
    (when (seq scales-to-chord)
      [:<>
       (for [{scale :scale/scale-names
              id    :id}
             scales-to-chord]
         ^{:key id}
         [:div {:style {:margin-right "10px" :display "inline"}}
          [:a {:href
               (rfe/href :scale (assoc path-params :scale (-> scale first)) changed-query-params)}
           [:button
            (->> scale
                 (map (comp str/capitalize #(str/replace % "-" "") name))
                 (str/join " / "))]]])])))

(defn chords-to-scale [path-params query-params scale-intervals]
  (let [cts                  (music-theory/chords-to-scale music-theory/chords scale-intervals)
        changed-query-params @(re-frame/subscribe [:changed-query-params])]
    (when (seq cts)
      [:<>
       (for [{:keys          [id]
              chord-name     :chord/chord-name
              chord-name-str :chord/chord-name-str
              :as            chord} cts]
         ^{:key (str "chords-to-scale-" id)}
         [:div {:style {:margin-right "10px" :display "inline"}}
          [:a {:href
               (rfe/href :scale (assoc path-params :chord chord-name) changed-query-params)}
           [:button (str/capitalize chord-name-str)]]])])))

(defn key-selection []
  (let [path-params          @(re-frame/subscribe [:path-params])
        query-params         @(re-frame/subscribe [:query-params])
        changed-query-params @(re-frame/subscribe [:changed-query-params])
        current-route-name   @(re-frame/subscribe [:current-route-name])
        key-of               @(re-frame/subscribe [:key-of])]
    [:div {:style {:display        "flow"
                   :flow-direction "column"
                   :overflow-x     "auto"
                   :white-space    "nowrap"}}
     (for [{:keys [title key]}
           (->> music-theory/tones
                (map (fn [x] {:key   x
                              :title (-> x name str/capitalize)})))]
       ^{:key title}
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href current-route-name (assoc path-params :key-of key) changed-query-params)}
        [:button
         {:disabled (= key-of key)}
         title]])]))

(defn bookmark-button [entity]
  (let [bookmark-exists? @(re-frame/subscribe [:bookmark-exists? entity])]
    (if bookmark-exists?
      [:button
       {:on-click #(re-frame/dispatch [:remove-bookmark entity])}
       "Remove bookmark"]
      [:button
       {:on-click #(re-frame/dispatch [:add-bookmark entity])}
       "Add bookmark"])))

(defn focus-button [entity]
  (let [current-route-name   @(re-frame/subscribe [:current-route-name])
        path-params          @(re-frame/subscribe [:path-params])
        query-params         @(re-frame/subscribe [:query-params])
        changed-query-params @(re-frame/subscribe [:changed-query-params])]
    #_(when-not (= current-route-name :focus)
      [:a {:href (rfe/href :focus (merge path-params entity) changed-query-params)}
       [:button "Focus"]])))

(defn instrument-selection []
  (let [{:keys [instrument] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                         @(re-frame/subscribe [:query-params])
        changed-query-params                 @(re-frame/subscribe [:changed-query-params])
        current-route-name                   @(re-frame/subscribe [:current-route-name])
        instruments                          music-theory/instruments]
    [:div {:style {:display        "flow"
                   :flow-direction "column"
                   :overflow-x     "auto"
                   :white-space    "nowrap"}}
     (for [{:keys [id text] :as m} instruments]
       ^{:key (str "instrument-selection-" id)}
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href
                    current-route-name
                    (assoc path-params :instrument id)
                    changed-query-params)}
        [:button
         {:disabled (= instrument id)}
         text]])]))

(re-frame/reg-event-fx
 ::play-tones
 (fn [_ [_event-id fretboard-matrix]]
   {:fx (music-theory/fretboard-matrix->tonejs-dispatches fretboard-matrix)}))

(defn instrument-view-fretboard-pattern
  [{:keys [entity
           definition
           instrument
           path-params
           query-params
           deps
           fretboard-matrix]}]
  (let [{id :id}            definition
        {:keys [trim-fretboard surrounding-intervals surrounding-tones show-octave
                debug]}
        query-params
        {:keys [play-tone]} deps]
    [:<>
     #_(when debug
       [:<>
        [debug-view entity]
        [debug-view fretboard-matrix]])
     [instruments-fretboard/styled-view
      (cond-> {:id             id
               :on-click       (fn [{:keys [tone-str octave]} fretboard-matrix]
                                 (play-tone (str tone-str octave)))
               :fretboard-matrix  fretboard-matrix
               :dark-orange-fn (fn [{:keys [root?] :as m}]
                                 (and root? (get m :pattern-found-tone)))
               :orange-fn      :out}
        show-octave           (assoc :show-octave? true)
        surrounding-intervals (assoc :grey-fn :interval)
        surrounding-tones     (assoc :grey-fn :tone-str))]
     [:br]
     [bookmark-button entity]
     [focus-button entity]
     [:button
      {:on-click #(>evt [::play-tones fretboard-matrix])}
      "Play"]]))

(defn instrument-view-fretboard-chord-and-scale
  [{id            :id
    instrument-id :instrument
    key-of        :key-of
    :as           entity}
   path-params
   {:keys
    [as-intervals trim-fretboard surrounding-intervals surrounding-tones
     show-octave debug]
    :as query-params}
   intervals
   {:keys [play-tone] :as deps}]
  (let [fretboard-matrix @(re-frame/subscribe [:fretboard-by-entity entity])]
    [:<>
     #_(when debug
       [debug-view entity]
       [debug-view fretboard-matrix])
     [instruments-fretboard/styled-view
      (cond-> {:id                id
               :on-click          (fn [{:keys [tone-str octave]} fretboard-matrix]
                                    (play-tone (str tone-str octave)))
               :fretboard-matrix  fretboard-matrix
               :dark-orange-fn    (fn [{:keys [root?] :as m}]
                                    (and root? (get m :out)))
               :orange-fn         :out}
        show-octave           (assoc :show-octave? true)
        surrounding-intervals (assoc :grey-fn :interval)
        surrounding-tones     (assoc :grey-fn :tone-str))]
     [:br]
     [bookmark-button entity]
     [focus-button entity]]))

(defmulti harmonizations-chord-details
  (fn [definition instrument path-params query-params]
    [(get instrument :type) (get definition :type)]))

(defmethod harmonizations-chord-details [:fretboard [:chord]]
  [{suffix :chord/suffix
    intervals :chord/intervals
    chord :chord/chord-name
    :keys  [symbol idx mode family interval-tones] :as definition}
   instrument
   {:keys [key-of] :as path-params}
   query-params]
  [debug-view [definition path-params]]
  [:div {:style {:display     :flex
                 :align-items :center}}
   [:h2 {:style {:margin-right "2em"}} (str idx " / " symbol)]

   [:a
    {:href (rfe/href
            :chord
            (assoc path-params :chord chord :key-of key-of)
            query-params)}
    [:p {:style {:margin-right "2em"}}
     (str (-> key-of name str/capitalize) suffix)]]

   [:a {:href (rfe/href :scale
                        (assoc path-params :scale mode :key-of key-of)
                        query-params)}
    [:p {:style {:margin-right "2em"}} (-> mode name str/capitalize)]]

   [:p {:style {:margin-right "2em"}} (-> family name str/capitalize)]

   [:div {:style {:margin-left "1em"}}
    (->> (map
          #(str/join " -> " [%1 (-> %2  name str/capitalize)])
          intervals
          (music-theory/interval-tones key-of intervals))
         (str/join ",  "))]])

(defmethod harmonizations-chord-details :default
  [definition instrument path-params query-params]
  [:h1 "not implemented"])


(defmulti instrument-view
  (fn [{:keys [id instrument key-of] :as entity} path-params query-params deps]
    (let [instrument'     (music-theory/get-instrument instrument)
          definition      (music-theory/get-definition id)
          instrument-type (get instrument' :type)]
      [instrument-type (get definition :type)])))

(defmethod instrument-view [:fretboard [:chord]]
  [{:keys [id instrument key-of] :as entity} path-params query-params deps]
  (let [definition  (music-theory/get-definition id)
        intervals   (get definition :chord/intervals)]
    [instrument-view-fretboard-chord-and-scale
     entity path-params query-params intervals deps]))

(defmethod instrument-view [:fretboard [:chord :pattern]]
  [{:keys [id instrument key-of] :as entity}
   path-params
   {:keys [as-intervals] :as query-params}
   {:keys [play-tone] :as deps}]
  (let [definition (music-theory/get-definition id)
        fretboard-matrix @(re-frame/subscribe [:fretboard-by-entity entity])]
    [:<>
     [instrument-view-fretboard-pattern
      {:entity           entity
       :definition       definition
       :instrument       instrument
       :path-params      path-params
       :query-params     query-params
       :deps             deps
       :fretboard-matrix fretboard-matrix}]
     [:button
      {:on-click (fn [_]
                   (doseq [{:keys [octave pattern-found-tone]} (->> fretboard-matrix
                                                                    (apply concat)
                                                                    (filter :match?)
                                                                    (sort-by :y #(compare %2 %1)))]
                     (play-tone pattern-found-tone octave)))}
      "Play"]]))

(defmethod instrument-view [:fretboard [:scale]]
  [{:keys [id instrument key-of] :as entity} path-params query-params deps]
  (let [definition (music-theory/get-definition id)
        intervals  (get definition :scale/intervals)]
    [instrument-view-fretboard-chord-and-scale
     entity path-params query-params intervals deps]))

(defn sort-fretboard-nr
  "Fretboards with patterns closer to head stock should go first.
  Returns a number."
  [intervals-count fretboard-matrix]
  (let [matches       (->> fretboard-matrix
                           (apply concat)
                           (filter :match?)
                           (take intervals-count))
        nr-of-matches (count matches)
        sum-of-xs     (->> matches (map :x) (reduce +))]
    (when (< 0 nr-of-matches)
      (/ sum-of-xs nr-of-matches))))

(defmethod instrument-view [:fretboard [:scale :pattern]]
  [{:keys [id instrument key-of] :as entity}
   path-params
   {:keys [as-intervals trim-fretboard] :as query-params}
   {:keys [play-tone] :as deps}]
  (let [definition (music-theory/get-definition id)
        fretboard-matrix @(re-frame/subscribe [:fretboard-by-entity entity])]
    (def fretboard-matrix fretboard-matrix)
    (def definition definition)
    [:<>
     #_[:p (sort-fretboard-nr ;; Use this for sorting
          (-> definition :fretboard-pattern/intervals count)
          fretboard-matrix)]
     [instrument-view-fretboard-pattern
      {:entity           entity
       :definition       definition
       :instrument       instrument
       :path-params      path-params
       :query-params     query-params
       :deps             deps
       :fretboard-matrix fretboard-matrix}]]))

(defmethod instrument-view :default
  [entity path-params query-params deps]
  [:div
   [:h2 "Not implemented"]
   [debug-view entity]])

(defn intervals-to-tones [intervals tones]
  [:pre {:style {:overflow-x "auto"}}
   (->> (map
         (fn [interval index]
           (str (basic-utils/fformat "%8s" interval) " -> " (-> index name str/capitalize)))
         intervals
         tones)
        (str/join "\n")
        (apply str)
        (str "Interval -> Tone\n"))])

(defn intervals [intervals']
  [:p (->> intervals'
           (map #(-> % name str/capitalize))
           (str/join ", "))])

(defn highlight-tones [tones key-of]
  [:div {:style {:margin-top  "1em"
                 :display     "flex"
                 :align-items "center"
                 :overflow-x  "auto"
                 :overflow-y  "auto"}}

   (for [{:keys [tone match?]}
         (let [tones-set (set tones)]
           (->> key-of
                (music-theory/tones-starting-at)
                (map (fn [tone]
                       (cond-> {:tone tone}
                         (seq (set/intersection tones-set tone))
                         (assoc :match? true))))))]
     ^{:key (str tone "something")}
     [:div {:style {:width     "4.5em"
                    :font-size "0.9em"}}
      (for [t' (sort-by (fn [x]
                          (let [x (str x)]
                            (cond
                              (and (= (count x) 3) (str/includes? x "#"))
                              1
                              (= (count x) 3)
                              2
                              :else 0))) tone)]
        ^{:key (str tone t')}
        [:div {:style {:margin-top "0em"}}
         [:div
          {:style {:color       (if-not match? "grey")
                   :font-weight (if match? "bold")}}
          (-> t' name str/capitalize)]])])])

(defn chord-name
  [key-of suffix explanation]
  [:div {:style {:margin-top      "1em"
                 :height          "100%"
                 :display         "inline-flex"
                 :justify-content :center
                 :align-items     :center}}
   [:h1 (str "Chord: " (-> key-of name str/capitalize) suffix)]
   (when explanation
     [:p {:style {:margin-left "2rem"}}
      (str "(" explanation ")")])])

(defn instrument-tuning [tuning]
  [:div {:style {:display "flex"}}
   [:<>
    "Instrument tuning: "
    (for [{:keys [tone octave] :as m} tuning]
      ^{:key (str m)}
      [:div
       [:div {:style {:margin-left "1em"}}
        (str (-> tone name str/capitalize) "(" octave ")")]])]])

(defn instrument-description [description]
  [:p description])

(defn scale-names [names]
  (->> names
       (map (fn [n]
              (-> n name (str/replace "-" " ") str/capitalize)))
       (str/join " / ")))

(defn instrument-details
  [{instrument-description' :description
    :keys                   [tuning] :as instrument}]
  [:<>
   [instrument-description instrument-description']
   [instrument-tuning tuning]])

(defn tuning-view [tuning]
  [:div {:style {:display "flex"}}
   (for [{:keys [tone octave start-index]} tuning]
     ^{:key (str "tuning-view" tone octave start-index)}
     [:div {:style {:margin-left "0.5em"
                    :display     "flex"}}
      (-> tone name str/capitalize)
      [:div {:style {:font-size "small"
                     :margin-top "0.5em"}}
       octave]])])


(defmulti definition-info-for-focus
  (fn [entity definition instrument path-params query-params]
    [(get instrument :type) (get definition :type)]))

(defmethod definition-info-for-focus [:fretboard [:chord :pattern]]
  [{:keys [key-of] :as entity}
   {chord-name :fretboard-pattern/belongs-to
    :as        definition}
   {:keys [tuning] :as instrument}
   path-params
   query-params]
  (let [{chord-intervals :chord/intervals
         suffix          :chord/suffix :as chord} (music-theory/get-chord chord-name)]
    [:<>
     [:div {:style {:display "flex"}}
      [:div (str "Key of: " (-> key-of name str/capitalize))]
      [:div {:style {:margin-left "1em"}}
       (str "Chord: " (-> key-of name str/capitalize) suffix)]

      [:div {:style {:margin-left "1em"}}
       (->> (map
             #(str/join " -> " [%1 (-> %2  name str/capitalize)])
             chord-intervals
             (music-theory/interval-tones key-of chord-intervals))
            (str/join ", "))]

      [:div {:style {:margin-left "1em"}}
       [tuning-view tuning]]]]))

(defmethod definition-info-for-focus [:fretboard [:scale :pattern]]
  [{:keys [key-of] :as entity}
   {scale-name :fretboard-pattern/belongs-to
    :as        definition}
   {:keys [tuning] :as instrument}
   path-params
   query-params]
  (let [{intervals :scale/intervals
         scale-names :scale/scale-names
         :as scale} (music-theory/get-scale scale-name)]
    [:<>
     [:div {:style {:display "flex"}}
      [:div (str "Key of: " (-> key-of name str/capitalize))]
      [:div {:style {:margin-left "1em"}}
       (str "Scale: " (->> scale-names (map (comp str/capitalize name)) sort (str/join " / ")))]

      [:div {:style {:margin-left "1em"}}
       (->> (map
             #(str/join " -> " [%1 (-> %2  name str/capitalize)])
             intervals
             (music-theory/interval-tones key-of intervals))
            (str/join ", "))]

      [:div {:style {:margin-left "1em"}}
       [tuning-view tuning]]]]))

(defmethod definition-info-for-focus [:fretboard [:chord]]
  [{:keys [key-of] :as entity}
   {chord-intervals :chord/intervals
    suffix          :chord/suffix
    :as             definition}
   {:keys [tuning] :as instrument}
   path-params
   query-params]
  [:<>
   [:div {:style {:display "flex"}}
    [:div (str "Key of: " (-> key-of name str/capitalize))]
    [:div {:style {:margin-left "1em"}}
     (str "Chord: " (-> key-of name str/capitalize) suffix)]

    [:div {:style {:margin-left "1em"}}
     (->> (map
           #(str/join "  ->  " [%1 (-> %2  name str/capitalize)])
           chord-intervals
           (music-theory/interval-tones key-of chord-intervals))
          (str/join ", "))]

    [:div {:style {:margin-left "1em"}}
     [tuning-view tuning]]]])

(defmethod definition-info-for-focus [:fretboard [:scale]]
  [{:keys [key-of] :as entity}
   {intervals   :scale/intervals
    scale-names :scale/scale-names
    :as         definition}
   {:keys [tuning] :as instrument}
   path-params
   query-params]
  [:<>
   [:div {:style {:display "flex"}}
    [:div (str "Key of: " (-> key-of name str/capitalize))]
    [:div {:style {:margin-left "1em"}}
     (str "Scale: " (->> scale-names (map (comp str/capitalize name)) sort (str/join " / ")))]

    [:div {:style {:margin-left "1em"}}
     (->> (map
           #(str/join " -> " [%1 (-> %2  name str/capitalize)])
           intervals
           (music-theory/interval-tones key-of intervals))
          (str/join ", "))]

    [:div {:style {:margin-left "1em"}}
     [tuning-view tuning]]]])

(defmethod definition-info-for-focus :default
  [entity definition instrument path-params query-params]
  [:<>
   [:h2 "No implementatin for `definition-info-for-focus`"]
   [debug-view definition]
   [debug-view instrument]
   [debug-view path-params]])

(defmulti definition-view-detailed (fn [definition instrument path-params query-params]
                                     (get definition :type)))

(defmethod definition-view-detailed [:chord]
  [{suffix      :chord/suffix
    explanation :chord/explanation
    :as         definition}
   instrument
   {:keys [key-of] :as path-params}
   query-params]
  (let [intervals      (get definition :chord/intervals)
        interval-tones (music-theory/interval-tones key-of intervals)]
    [:<>
     [chord-name key-of suffix explanation]
     [intervals-to-tones intervals interval-tones]
     [highlight-tones interval-tones key-of]]))

(defmethod definition-view-detailed [:chord :pattern]
  [definition instrument path-params query-params]
  [:<>
   [:h2 "[:chord :pattern]"]
   [debug-view definition]
   [debug-view instrument]])

(defmethod definition-view-detailed [:scale]
  [{scale-names' :scale/scale-names
    intervals    :scale/intervals
    :as          definition}
   {instrument-description' :description
    :keys                   [tuning] :as instrument}
   {:keys [key-of] :as path-params}
   query-params]
  (let [interval-tones (music-theory/interval-tones key-of intervals)]
    [:<>
     [:h1 (str "Scale: " (scale-names scale-names'))]
     [:br]
     [intervals-to-tones intervals interval-tones]
     [highlight-tones interval-tones key-of]]))

(defn select-harmonization []
  (let [{:keys [harmonization-id] :as path-params}  @(re-frame/subscribe [:path-params])
        query-params @(re-frame/subscribe [:query-params])]
    [:div {:style {:display        "flow"
                   :flow-direction "column"
                   :overflow-x     "auto"
                   :white-space    "nowrap"}}
     (for [{:keys [id description] :as harmonization} music-theory/harmonizations]
       ^{:key (str "select-harmonization-" id)}
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href :harmonizations (assoc path-params :harmonization-id id) query-params)}
        [:button
         {:disabled (= id harmonization-id)}
         description]])]))

(defmethod definition-view-detailed [:scale :pattern]
  [definition instrument path-params query-params]
  [:<>
   [:h2 "[:scale :pattern]"]
   [debug-view definition]
   [debug-view instrument]])

(defmethod definition-view-detailed :default
  [definition instrument path-params query-params]
  [:div
   [:h2 ":default"]
   [debug-view definition]
   [debug-view instrument]])

(defn settings
  [{:keys [as-text? as-intervals? nr-of-frets? nr-of-octavs? trim-fretboard?
           surrounding-intervals? surrounding-tones? octave? show-tones?]
    ;; If the menu option should be shown or not
    :or   {as-intervals?          true
           surrounding-intervals? true
           surrounding-tones?     true
           octave?                true
           show-tones?            false}
    :as   m}]
  (let [current-route-name @(re-frame/subscribe [:current-route-name])
        path-params        @(re-frame/subscribe [:path-params])
        {:keys
         [trim-fretboard nr-of-frets as-text nr-of-octavs as-intervals nr-of-octavs
          surrounding-intervals surrounding-tones show-octave show-tones bpm]
         :as query-params}
        @(re-frame/subscribe [:query-params])]
    [:div
     [:div {:style {:display "flex"}}
      (when as-intervals?
        [:div
         [:input {:on-click #(re-frame/dispatch [:href [current-route-name path-params (assoc query-params :as-intervals (not as-intervals))]])
                  :checked  as-intervals
                  :type     "checkbox" :id "as-intervals-checkbox" :name "as-intervals-checkbox"}]
         [:label {:for "as-intervals-checkbox"} "Show intervals?"]])

      #_(when as-text?
          [:div {:style {:margin-left "1rem"}}
           [:input {:on-click #(re-frame/dispatch [:href [current-route-name path-params (assoc query-params :as-text (not as-text))]])
                    :checked  as-text
                    :type     "checkbox" :id "as-text-checkbox" :name "as-text-checkbox"}]
           [:label {:for "as-text-checkbox"} "Fretboard in text?"]])

      ;; TODO: trim fretboard doesnt work correctly.
      (when trim-fretboard?
          [:div {:style {:margin-left "1rem"}}
           [:input {:on-click #(re-frame/dispatch [:href [current-route-name path-params (assoc query-params :trim-fretboard (not trim-fretboard))]])
                    :checked  trim-fretboard
                    :type     "checkbox" :id "trim-fretboard-checkbox" :name "trim-fretboard-checkbox"}]
           [:label {:for "trim-fretboard-checkbox"} "Trim fretboard?"]])

      (when show-tones?
        (let [id        "show-tones-checkbox"
              label     "Show tones?"
              key       :show-tones
              old-value show-tones
              new-value (not show-tones)]
          [:div {:style {:margin-left "1rem"}}
           [:input {:on-click #(re-frame/dispatch [:href [current-route-name path-params
                                                          (assoc query-params key new-value)]])
                    :checked  old-value
                    :type     "checkbox" :id id :name id}]
           [:label {:for id} label]]))

      (when surrounding-intervals?
        (let [id        "surrounding-intervals-checkbox"
              label     "Surrounding intervals?"
              key       :surrounding-intervals
              old-value surrounding-intervals
              new-value (not surrounding-intervals)]
          [:div {:style {:margin-left "1rem"}}
           [:input {:on-click #(re-frame/dispatch [:href [current-route-name path-params
                                                          (assoc query-params key new-value
                                                                 :surrounding-tones (if new-value
                                                                                      false
                                                                                      surrounding-tones))]])
                    :checked  old-value
                    :type     "checkbox"
                    :id id
                    :name id}]
           [:label {:for id} label]]))

      (when surrounding-tones?
        (let [id        "surrounding-tones-checkbox"
              label     "Surrounding tones?"
              key       :surrounding-tones
              old-value surrounding-tones
              new-value (not surrounding-tones)]
          [:div {:style {:margin-left "1rem"}}
           [:input {:on-click #(re-frame/dispatch [:href [current-route-name path-params
                                                          (assoc query-params key new-value
                                                                 :surrounding-intervals (if new-value
                                                                                          false
                                                                                          surrounding-intervals))]])
                    :checked  old-value
                    :type     "checkbox" :id id :name id}]
           [:label {:for id} label]]))

      (when octave?
        (let [id        "show-octave-checkbox"
              label     "Show octave?"
              key       :show-octave
              old-value show-octave
              new-value (not show-octave)]
          [:div {:style {:margin-left "1rem"}}
           [:input {:on-click #(re-frame/dispatch
                                [:href [current-route-name path-params
                                        (assoc query-params key new-value)]])
                    :checked  old-value
                    :type     "checkbox"
                    :id       id
                    :name     id}]
           [:label {:for id} label]]))]

     [:br]

     [:div {:style {:display "flex"}}
      (when nr-of-frets?
        [:div
         [:label {:for "nr-of-frets-input"} "Nr of frets:"]
         [:input {:style     {:width "3rem"}
                  :on-change #(re-frame/dispatch [:href [current-route-name path-params (assoc query-params :nr-of-frets (-> % .-target .-value))]])
                  :value     nr-of-frets
                  :max       37
                  :min       2
                  :type      "number" :id "nr-of-frets-input" :name "nr-of-frets-input"}]])

      (when nr-of-octavs?
        [:div
         [:label {:for "nr-of-octavs-input"} "Nr of octavs:"]
         [:input {:style     {:width "3rem"}
                  :on-change #(re-frame/dispatch [:href [current-route-name path-params (assoc query-params :nr-of-octavs (-> % .-target .-value))]])
                  :value     nr-of-octavs
                  :max       4
                  :min       1
                  :type      "number" :id "nr-of-octavs-input" :name "nr-of-octavs-input"}]])

      (when (= current-route-name :scale)
        [:div
         [:label {:for "bpm-input"} "Bpm:"]
         [:input {:style     {:width "3rem"}
                  :on-change #(re-frame/dispatch [:href [current-route-name path-params (assoc query-params :bpm (-> % .-target .-value))]])
                  :value     bpm
                  :max       200
                  :min       50
                  :type      "number"
                  :id        "bpm-input"
                  :name      "bpm-input"}]])]]))

(defn select-instrument []
  (let [current-route-name                   (<sub [:current-route-name])
        {:keys [instrument] :as path-params} (<sub [:path-params])
        changed-query-params                 (<sub [:changed-query-params])]
    [:select {:value     (prn-str instrument)
              :on-change (fn [evt]
                           (let [value  (-> evt .-target .-value)
                                 value' (edn/read-string value)]
                             (>evt [:href [current-route-name
                                           (assoc path-params :instrument value')
                                           changed-query-params]])))}
     (for [{:keys [id text]} music-theory/get-fretboard-instruments]
       ^{:key id}
       [:option {:value (prn-str id)} text])]))

(defn select-key
  ([]
   (select-key :key-of))
  ([k]
   (let [current-route-name   (<sub [:current-route-name])
         path-params          (<sub [:path-params])
         value                (get path-params k)
         changed-query-params (<sub [:changed-query-params])]
     [:select {:value     (prn-str value)
               :on-change (fn [evt]
                            (let [value  (-> evt .-target .-value)
                                  value' (edn/read-string value)]
                              (>evt [:href [current-route-name
                                            (assoc path-params k value')
                                            changed-query-params]])))}
      (for [key-of music-theory/tones]
        ^{:key (str k key-of)}
        [:option {:value (prn-str key-of)} (-> key-of name str/capitalize)])])))


(defn select-scale-or-chord [k]
  (let [current-route-name   (<sub [:current-route-name])
        path-params          (<sub [:path-params])
        value                (get path-params k)
        changed-query-params (<sub [:changed-query-params])]
    [:select {:value     (prn-str value)
              :on-change (fn [evt]
                           (let [value  (-> evt .-target .-value)
                                 value' (edn/read-string value)]
                             (>evt [:href [current-route-name
                                           (assoc path-params k value')
                                           changed-query-params]])))}
     [:optgroup {:label "Chords"}
      (for [{s :chord/chord-name-str :keys [id]} music-theory/chords]
        ^{:key (str k id)}
        [:option {:value (prn-str id)} (str/capitalize s)])]
     [:optgroup {:label "Scales"}
      (for [{:keys [id scale]} music-theory/scales]
        ^{:key (str k scale)}
        [:option {:value (prn-str id)} (-> scale name str/capitalize)])]]))
