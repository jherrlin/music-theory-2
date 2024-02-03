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
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard :as instruments-fretboard]
   [se.jherrlin.music-theory.webapp.views.common :as common]))


(defn focus-view [{:keys [play-tone] :as deps}]
  (let [{:keys [id instrument key-of] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                                   @(re-frame/subscribe [:query-params])
        fretboard-matrix                               @(re-frame/subscribe [:fretboard-matrix])
        definition                                     (music-theory/by-id id)
        instrument'                                    (music-theory/get-instrument instrument)
        entity                                         (music-theory/entity key-of instrument id)]
    [:<>
     [common/menu]
     [:br]
     [common/definition-info-for-focus definition instrument' path-params query-params]
     [common/instrument-view entity path-params query-params deps]]))

(defn routes [deps]
  (let [route-name :focus]
    ["/focus/:instrument/:key-of/:id"
     {:name       route-name
      :view       [focus-view deps]
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
                      (let [entity p
                            fretboard-matrix (common/prepair-instrument-data-for-entity entity {} q)]
                        (re-frame/dispatch [:add-entity-with-fretboard entity fretboard-matrix]))
                      (events/do-on-url-change route-name p q))}]}]))
