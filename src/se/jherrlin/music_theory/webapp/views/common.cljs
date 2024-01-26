(ns se.jherrlin.music-theory.webapp.views.common
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

(defn menu []
  (let [current-route-name @(re-frame/subscribe [:current-route-name])
        path-params        @(re-frame/subscribe [:path-params])
        query-params       @(re-frame/subscribe [:query-params])
        key-of             @(re-frame/subscribe [:key-of])]
    [:div {:style {:display        "flow"
                   :flow-direction "column"
                   :overflow-x     "auto"
                   :white-space    "nowrap"}}
     #_[:a {:style {:margin-right "10px"} :href (rfe/href :home)}
       [:button
        {:disabled (= current-route-name :home)}
        "Home"]]
     [:a {:style {:margin-right "10px"}
          :href  (rfe/href :chord path-params query-params)}
       [:button
        {:disabled (= current-route-name :chord)}
        "Chords"]]

     [:a {:style {:margin-right "10px"}
          :href  (rfe/href :scale path-params query-params)}
       [:button
        {:disabled (= current-route-name :scale)}
        "Scales"]]

     #_[:a {:style {:margin-right "10px"}
          :href  (rfe/href :harmonizations path-params query-params)}
       [:button
        {:disabled (= current-route-name :harmonizations)}
        "Harmonizations"]]

     #_[:a {:style {:margin-right "10px"}
          :href  (rfe/href :table path-params query-params)}
       [:button
        {:disabled (= current-route-name :table)}
        "Table"]]

     #_[:a {:style {:margin-right "10px"}
          :href  (rfe/href :bookmarks path-params query-params)}
      [:button
       {:disabled (= current-route-name :bookmarks)}
       "Bookmarks"]]]))

(defn chord-selection []
  (let [current-route-name              @(re-frame/subscribe [:current-route-name])
        {:keys [chord] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                    @(re-frame/subscribe [:query-params])]
    [:div
     (for [{chord-name     :chord/chord-name
            chord-name-str :chord/chord-name-str
            id             :id
            :as            m} music-theory/chords]
       ^{:key (str "chord-selection-" id)}
       [:div {:style {:margin-right "10px" :display "inline"}}
        [:a {:href (rfe/href
                    :chord
                    (assoc path-params :chord chord-name)
                    query-params)}
         [:button
          {:disabled (= chord chord-name)}
          chord-name-str]]])]))

(defn scale-selection []
  (let [current-route-name              @(re-frame/subscribe [:current-route-name])
        {:keys [scale] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                    @(re-frame/subscribe [:query-params])]
    [:div
     (for [{scale' :scale
            id     :id
            :as    m} music-theory/scales]
       ^{:key (str "scale-selection-" id scale')}
       [:div {:style {:margin-right "10px" :display "inline"}}
        [:a {:href (rfe/href :scale (assoc path-params :scale scale') query-params)}
         [:button
          {:disabled (= scale scale')}
          (-> scale' name (str/replace "-" " ") str/capitalize)]]])]))

(defn scales-to-chord [path-params query-params chord-intervals]
  (let [scales-to-chord (music-theory/scales-to-chord music-theory/scales chord-intervals)]
    (when (seq scales-to-chord)
      [:<>
       [:h3 "Scales to chord"]
       (for [{scale :scale/scale-names
              id    :id}
             scales-to-chord]
         ^{:key id}
         [:div {:style {:margin-right "10px" :display "inline"}}
          [:a {:href
               (rfe/href :scale (assoc path-params :scale (-> scale first)) query-params)}
           [:button
            (->> scale
                 (map (comp str/capitalize #(str/replace % "-" "") name))
                 (str/join " / "))]]])])))

(defn key-selection []
  (let [current-route      @(re-frame/subscribe [:current-route])
        path-params        @(re-frame/subscribe [:path-params])
        query-params       @(re-frame/subscribe [:query-params])
        current-route-name @(re-frame/subscribe [:current-route-name])
        key-of             @(re-frame/subscribe [:key-of])]
    [:div {:style {:display        "flow"
                   :flow-direction "column"
                   :overflow-x     "auto"
                   :white-space    "nowrap"}}
     (for [{:keys [title key]}
           (->> [:a :a# :b :c :c# :d :d# :e :f :f# :g :g#]
                (map (fn [x] {:key   x
                              :title (-> x name str/capitalize)})))]
       ^{:key title}
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href current-route-name (assoc path-params :key-of key) query-params)}
        [:button
         {:disabled (= key-of key)}
         title]])]))

(defn instrument-selection []
  (let [current-route                        @(re-frame/subscribe [:current-route])
        {:keys [instrument] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                         @(re-frame/subscribe [:query-params])
        current-route-name                   @(re-frame/subscribe [:current-route-name])
        key-of                               @(re-frame/subscribe [:key-of])
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
                    query-params)}
         [:button
          {:disabled (= instrument id)}
          text]])]))

(defn instrument-view-fretboard-pattern
  [{pattern :fretboard-pattern/pattern :as definition}
   instrument
   {:keys [key-of] :as path-params}
   {:keys [as-intervals trim-fretboard] :as query-params}
   {:keys [play-tone] :as deps}]
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
     [instruments-fretboard/styled-view
      {:on-click       (fn [{:keys [tone-str octave]}]
                         (play-tone (str tone-str octave)))
       :matrix
       (cond->> fretboard'
         trim-fretboard (music-theory/trim-matrix #(every? nil? (map :out %))))
       :dark-orange-fn (fn [{:keys [root?] :as m}]
                         (and root? (get m :pattern-found-tone)))
       :orange-fn      :pattern-found-tone #_:pattern-found-interval
       :grey-fn        :tone-str           #_:interval}]]))

(defn instrument-view-fretboard-chord-and-scale
  [definition instrument
   {:keys [key-of] :as path-params}
   {:keys [as-intervals trim-fretboard] :as query-params}
   intervals
   {:keys [play-tone] :as deps}]
  (let [fretboard-matrix @(re-frame/subscribe [:fretboard-matrix])
        interval-tones   (music-theory/interval-tones intervals key-of)
        fretboard        (if as-intervals
                           (music-theory/with-all-intervals
                             (mapv vector interval-tones intervals)
                             fretboard-matrix)
                           (music-theory/with-all-tones
                             interval-tones
                             fretboard-matrix))]
    [:<>
     [instruments-fretboard/styled-view
      {:on-click       (fn [{:keys [tone-str octave]}]
                         (play-tone (str tone-str octave)))
       :matrix         (cond->> fretboard
                         trim-fretboard (music-theory/trim-matrix
                                         #(every? nil? (map :out %))))
       :dark-orange-fn (fn [{:keys [root?] :as m}]
                         (and root? (get m :out)))
       :orange-fn      :out #_:pattern-found-tone #_:pattern-found-interval
       :grey-fn        :tone-str           #_:interval
       }]]))

(defmulti instrument-view
  (fn [definition instrument path-params query-params deps]
    [(get instrument :type) (get definition :type)]))

;; http://localhost:8080/#/focus/guitar/c/1cd72972-ca33-4962-871c-1551b7ea5244
(defmethod instrument-view [:fretboard [:chord]]
  [definition instrument path-params query-params deps]
  (let [intervals (get definition :chord/intervals)]
    [instrument-view-fretboard-chord-and-scale
     definition instrument path-params query-params intervals deps]))

;; http://localhost:8080/#/focus/guitar/c/4db09dd6-9a44-4a1b-8c0f-6ed82796c8b5
(defmethod instrument-view [:fretboard [:chord :pattern]]
  [{pattern :fretboard-pattern/pattern :as definition}
   instrument
   {:keys [key-of] :as path-params}
   {:keys [as-intervals trim-fretboard] :as query-params}
   deps]
  [instrument-view-fretboard-pattern definition instrument path-params query-params deps])

;; http://localhost:8080/#/focus/guitar/e/3df70e72-dd4c-4e91-85b5-13de2bb062ce
(defmethod instrument-view [:fretboard [:scale]]
  [definition instrument path-params query-params deps]
  (let [intervals (get definition :scale/intervals)]
    [instrument-view-fretboard-chord-and-scale
     definition instrument path-params query-params intervals deps]))

;; http://localhost:8080/#/focus/guitar/c/dbc69a09-b3dc-4bfa-a4df-6dd767b65d25
(defmethod instrument-view [:fretboard [:scale :pattern]]
  [{pattern :fretboard-pattern/pattern :as definition}
   instrument
   {:keys [key-of] :as path-params}
   {:keys [as-intervals trim-fretboard] :as query-params}
   deps]
  [instrument-view-fretboard-pattern definition instrument path-params query-params])

(defmethod instrument-view :default
  [definition instrument path-params query-params deps]
  [:div
   [:h2 "Not implemented"]
   [debug-view definition]])

(defn intervals-to-tones [intervals tones]
  [:pre {:style {:overflow-x "auto"}}
   (->> (map
         (fn [interval index]
           (str (utils/fformat "%8s" interval) " -> " (-> index name str/capitalize)))
         intervals
         tones)
        (str/join "\n")
        (apply str)
        (str "Interval -> Tone\n"))])

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
   [:h3 (str "Chord: " (-> key-of name str/capitalize) suffix)]
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
  (->> #{:natural-minor :minor :aeolian}
       (map (fn [n]
              (-> n name (str/replace "-" " ") str/capitalize)))
       (str/join " / ")))

(defmulti definition-view-detailed (fn [definition instrument path-params query-params]
                                     (get definition :type)))

;; http://localhost:8080/#/focus/guitar/c/1cd72972-ca33-4962-871c-1551b7ea5244
(defmethod definition-view-detailed [:chord]
  [{suffix      :chord/suffix
    explanation :chord/explanation
    :as         definition}
   {instrument-description' :description
    :keys                   [tuning] :as instrument}
   {:keys [key-of] :as path-params}
   {:keys [as-intervals] :as query-params}]
  (let [intervals      (get definition :chord/intervals)
        interval-tones (music-theory/interval-tones intervals key-of)]
    [:<>
     [instrument-description instrument-description']
     [instrument-tuning tuning]
     [intervals-to-tones intervals interval-tones]
     [highlight-tones interval-tones key-of]
     [chord-name key-of suffix explanation]]))

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
  (let [_              (def definition definition)
        _              (def instrument instrument)
        _              (def path-params path-params)
        _              (def query-params query-params)
        interval-tones (music-theory/interval-tones intervals key-of)]
    [:<>
     [:h3 (str "Scale: " (scale-names scale-names'))]
     [instrument-description instrument-description']
     [instrument-tuning tuning]
     [intervals-to-tones intervals interval-tones]
     [highlight-tones interval-tones key-of]
     #_[debug-view definition]
     #_[debug-view instrument]]))

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
  [{:keys [as-text? as-intervals? nr-of-frets? nr-of-octavs? trim-fretboard?]
    :or   {as-intervals? true}
    :as   m}]
  (let [current-route-name                                                                                   @(re-frame/subscribe [:current-route-name])
        path-params                                                                                          @(re-frame/subscribe [:path-params])
        {:keys [trim-fretboard nr-of-frets as-text nr-of-octavs as-intervals nr-of-octavs] :as query-params} @(re-frame/subscribe [:query-params])]
    [:div {:style {:display "flex"}}
     (when as-intervals?
       [:div
        [:input {:on-click #(re-frame/dispatch [:href [current-route-name path-params (assoc query-params :as-intervals (not as-intervals))]])
                 :checked  as-intervals
                 :type     "checkbox" :id "as-intervals-checkbox" :name "as-intervals-checkbox"}]
        [:label {:for "as-intervals-checkbox"} "Show intervals"]])

     (when as-text?
       [:div {:style {:margin-left "1rem"}}
        [:input {:on-click #(re-frame/dispatch [:href [current-route-name path-params (assoc query-params :as-text (not as-text))]])
                 :checked  as-text
                 :type     "checkbox" :id "as-text-checkbox" :name "as-text-checkbox"}]
        [:label {:for "as-text-checkbox"} "Fretboard in text"]])

     (when trim-fretboard?
       [:div {:style {:margin-left "1rem"}}
        [:input {:on-click #(re-frame/dispatch [:href [current-route-name path-params (assoc query-params :trim-fretboard (not trim-fretboard))]])
                 :checked  trim-fretboard
                 :type     "checkbox" :id "trim-fretboard-checkbox" :name "trim-fretboard-checkbox"}]
        [:label {:for "trim-fretboard-checkbox"} "Trim fretboard"]])

     (when nr-of-frets?
       [:div {:style {:margin-left "1rem"}}
        [:label {:for "nr-of-frets-input"} "Nr of frets:"]
        [:input {:style     {:width "3rem"}
                 :on-change #(re-frame/dispatch [:href [current-route-name path-params (assoc query-params :nr-of-frets (-> % .-target .-value))]])
                 :value     nr-of-frets
                 :max       37
                 :min       2
                 :type      "number" :id "nr-of-frets-input" :name "nr-of-frets-input"}]])

     (when nr-of-octavs?
       [:div {:style {:margin-left "1rem"}}
        [:label {:for "nr-of-octavs-input"} "Nr of octavs:"]
        [:input {:style     {:width "3rem"}
                 :on-change #(re-frame/dispatch [:href [current-route-name path-params (assoc query-params :nr-of-octavs (-> % .-target .-value))]])
                 :value     nr-of-octavs
                 :max       4
                 :min       1
                 :type      "number" :id "nr-of-octavs-input" :name "nr-of-octavs-input"}]])]))
