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
     definition instrument path-params query-params intervals]))

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



(defmulti definition-view-detailed (fn [definition instrument path-params query-params]
                            (get definition :type)))

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
   [:h2 (str "Chord: " (-> key-of name str/capitalize) suffix)]
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
        interval-tones (music-theory/interval-tones intervals key-of)
        interval->tone (music-theory/intervals->tones intervals interval-tones)]
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
  [definition instrument path-params query-params]
  [:<>
   [:h2 "[:scale]"]
   [debug-view definition]
   [debug-view instrument]])

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
