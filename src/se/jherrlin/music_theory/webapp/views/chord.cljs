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
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard :as instruments-fretboard]))


(defn chord-component []
  (let [{:keys [id] :as path-params} @(re-frame/subscribe [:path-params])
        _                            (def path-params path-params)
        _                            (def id id)
        query-params                 @(re-frame/subscribe [:query-params])
        _                            (def query-params query-params)
        fretboard-matrix             @(re-frame/subscribe [:fretboard-matrix])
        _                            (def fretboard-matrix fretboard-matrix)]
    [:<>
     [instruments-fretboard/styled-view
      {:matrix fretboard-matrix
       :display-fn :interval}]
     ]))

(def routes
  (let [route-name :chord]
    ["/chord/:instrument/:key-of/:chord"
     {:name       route-name
      :view       [chord-component]
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
