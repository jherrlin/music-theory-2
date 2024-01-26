(ns se.jherrlin.music-theory.webapp.views.chord
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
   [se.jherrlin.music-theory.webapp.views.common :as common]))


(defn chord-component [deps]
  (let [{:keys [chord instrument] :as path-params}
        @(re-frame/subscribe [:path-params])
        query-params                            @(re-frame/subscribe [:query-params])
        fretboard-matrix                        @(re-frame/subscribe [:fretboard-matrix])
        {:keys [id] :as chord-definition}       (music-theory/chord chord)
        {instrument-type :type :as instrument'} (music-theory/instrument instrument)
        chord-patterns                          (music-theory/chord-patterns-belonging-to chord instrument)
        chord-triad-patterns                    (music-theory/chord-pattern-triads-belonging-to chord instrument)
        {chord-intervals :chord/intervals}      (music-theory/chord chord)]
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
                      (events/do-on-url-change route-name p q))}]}]))
