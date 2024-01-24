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
        _                (def path-params path-params)
        _                (def id id)
        query-params     @(re-frame/subscribe [:query-params])
        _                (def query-params query-params)
        fretboard-matrix @(re-frame/subscribe [:fretboard-matrix])
        _                (def fretboard-matrix fretboard-matrix)
        chord-definition (music-theory/chord chord)
        _                (def chord-definition chord-definition)
        instrument'      (music-theory/instrument instrument)
        _                (def instrument' instrument')
        chord-patterns   (music-theory/chord-patterns-belonging-to chord instrument)
        _                (def chord-patterns chord-patterns)]
    [:<>
     [common/definition-view-detailed
      chord-definition instrument' path-params query-params]
     [common/instrument-view
      chord-definition instrument' path-params query-params deps]

     (for [{:keys [id] :as pattern-definitions} chord-patterns]
       ^{:key (str id)}
       [common/instrument-view
        pattern-definitions instrument' path-params query-params deps])]))

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
