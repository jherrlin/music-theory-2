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
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard :as instruments-fretboard]))


(defmulti type-view :type)
(defmethod type-view [:chord]
  [_] 25)
(defmethod type-view [:chord :pattern]
  [_] 25)
(defmethod type-view [:scale]
  [_] 25)
(defmethod type-view [:scale :pattern]
  [_] 25)
(defmethod type-view :default
  [_] 25)

(defn debug-view
  ([]
   (debug-view @re-frame.db/app-db))
  ([x]
   [:pre
    (with-out-str
      (cljs.pprint/pprint
       (-> x
           (dissoc :current-route))))]))

(comment
  @(re-frame/subscribe [:general-data])
  @(re-frame/subscribe [:data-for-id #uuid "1cd72972-ca33-4962-871c-1551b7ea5244"])
  )

(defn focus-view []
  (let [{:keys [id] :as path-params} @(re-frame/subscribe [:path-params])
        _                            (def path-params path-params)
        _                            (def id id)
        query-params                 @(re-frame/subscribe [:query-params])
        _                            (def query-params query-params)
        fretboard-matrix             @(re-frame/subscribe [:fretboard-matrix])
        _                            (def fretboard-matrix fretboard-matrix)]
    [:<>
     [:div "focus"]
     [debug-view]
     [instruments-fretboard/styled-view
      {:matrix fretboard-matrix}]
     ]
    ))











(-> (music-theory/by-id #uuid "1cd72972-ca33-4962-871c-1551b7ea5244")
     (utils/add-qualified-ns :chord))



(defn routes []
  (let [route-name :focus]
    ["/focus/:instrument/:key-of/:id"
     {:name       route-name
      :view       [focus-view]
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
                      (events/do-on-url-change route-name p q))}]}]))
