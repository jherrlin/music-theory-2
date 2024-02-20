(ns se.jherrlin.music-theory.webapp.views.focus
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [re-frame.alpha :as re-frame]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard :as instruments-fretboard]
   [se.jherrlin.music-theory.webapp.views.common :as common]))


(defn focus-view [{:keys [play-tone] :as deps}]
  (let [{:keys [id instrument key-of] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                                   @(re-frame/subscribe [:query-params])
        fretboard-matrix                               @(re-frame/subscribe [:fretboard-matrix])
        definition                                     (music-theory/get-definition id)
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
                   :query events/Query}
      :controllers
      [{:parameters {:path  [:instrument :key-of :id]
                     :query events/query-keys}
        :start      (fn [{p :path q :query}]
                      (let [entity p
                            fretboard-matrix (music-theory/instrument-data-structure entity q)]
                        (re-frame/dispatch [:add-entity-instrument-data-structure entity fretboard-matrix]))
                      (events/do-on-url-change route-name p q))}]}]))
