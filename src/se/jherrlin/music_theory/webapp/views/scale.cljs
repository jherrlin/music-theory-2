(ns se.jherrlin.music-theory.webapp.views.scale
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


(defn scale-component [deps]
  (let [{:keys [id scale instrument] :as path-params} @(re-frame/subscribe [:path-params])
        _                                             (def scale scale)
        _                                             (def instrument instrument)
        query-params                                  @(re-frame/subscribe [:query-params])
        fretboard-matrix                              @(re-frame/subscribe [:fretboard-matrix])
        scale-definition                              (music-theory/scale scale)
        _                                             (def scale-definition scale-definition)
        instrument'                                   (music-theory/instrument instrument)
        _                                             (def instrument' instrument')
        {scale-intervals :scale/intervals
         scale-names     :scale/scale-names
         :as             scale'}                      (music-theory/scale scale)
        _                                             (def scale' scale')
        scale-patterns                                (music-theory/scale-patterns-for-scale-and-instrument scale-names instrument)
        _                                             (def scale-patterns scale-patterns)
        ]
    [:<>
     [common/menu]
     [:br]
     [common/instrument-selection]
     [:br]
     [common/key-selection]
     [:br]
     [common/scale-selection]
     [:br]

     [common/definition-view-detailed
      scale-definition instrument' path-params query-params]
     [common/instrument-view
      scale-definition instrument' path-params query-params deps]

     (when (seq scale-patterns)
       [:<>
        [:h2 "Scale patterns"]
        (for [{:keys [id] :as pattern-definitions} scale-patterns]
          ^{:key (str "scale-patterns-" id)}
          [common/instrument-view
           pattern-definitions instrument' path-params query-params deps])])]))


(defn routes [deps]
  (let [route-name :scale]
    ["/scale/:instrument/:key-of/:scale"
     {:name       route-name
      :view       [scale-component deps]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:path  [:map
                           [:instrument      keyword?]
                           [:key-of          keyword?]
                           [:scale           keyword?]]
                   :query events/Query}
      :controllers
      [{:parameters {:path  [:instrument :key-of :scale]
                     :query events/query-keys}
        :start      (fn [{p :path q :query}]
                      (events/do-on-url-change route-name p q))}]}]))
