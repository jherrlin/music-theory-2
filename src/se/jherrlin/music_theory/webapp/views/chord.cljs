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
  (let [{:keys [id chord instrument] :as path-params}
        @(re-frame/subscribe [:path-params])
        query-params                            @(re-frame/subscribe [:query-params])
        fretboard-matrix                        @(re-frame/subscribe [:fretboard-matrix])
        chord-definition                        (music-theory/chord chord)
        {instrument-type :type :as instrument'} (music-theory/instrument instrument)
        chord-patterns                          (music-theory/chord-patterns-belonging-to chord instrument)
        chord-triad-patterns                    (music-theory/chord-pattern-triads-belonging-to chord instrument)
        {chord-intervals :chord/intervals}      (music-theory/chord chord)]
    [:<>
     [common/menu]
     [:br]
     [common/instrument-selection]
     [:br]
     [common/key-selection]
     [:br]
     [common/chord-selection]
     [:br]

     [common/settings
      {:as-text?        (= instrument-type :fretboard)
       :nr-of-frets?    (= instrument-type :fretboard)
       :trim-fretboard? (= instrument-type :fretboard)
       :nr-of-octavs?   (= instrument-type :keyboard)}]

     [common/definition-view-detailed
      chord-definition instrument' path-params query-params]
     [common/instrument-view
      chord-definition instrument' path-params query-params deps]

     (when (seq chord-patterns)
       [:h2 "Chord patterns"])

     (for [{:keys [id] :as pattern-definitions} chord-patterns]
       ^{:key (str "chord-patterns-" id)}
       [common/instrument-view
        pattern-definitions instrument' path-params query-params deps])

     (when (seq chord-triad-patterns)
       [:h2 "Triads"])

     (for [{:keys [id] :as pattern-definitions} chord-triad-patterns]
       ^{:key (str "chord-triad-patterns-" id)}
       [common/instrument-view
        pattern-definitions instrument' path-params query-params deps])

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
                   :query [:map
                           [:nr-of-frets    {:optional true} int?]
                           [:nr-of-octavs   {:optional true} int?]
                           [:as-intervals   {:optional true} boolean?]
                           [:as-text        {:optional true} boolean?]
                           [:trim-fretboard {:optional true} boolean?]]}
      :controllers
      [{:parameters {:path  [:instrument :key-of :chord]
                     :query [:nr-of-frets :as-intervals :as-text :nr-of-octavs :trim-fretboard]}
        :start      (fn [{p :path q :query}]
                      (events/do-on-url-change route-name p q))}]}]))