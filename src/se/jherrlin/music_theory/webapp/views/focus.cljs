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


(doseq [{:keys [n s e]}
        [{:n ::entity}]]
  (re-frame/reg-sub n (or s (fn [db [n']] (get db n'))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))

(defn focus-view [{:keys [play-tone] :as deps}]
  (let [{:keys [id instrument key-of] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                                   @(re-frame/subscribe [:query-params])
        definition                                     (music-theory/get-definition id)
        instrument'                                    (music-theory/get-instrument instrument)
        entity                                         (music-theory/entity key-of instrument id)]
    [:<>
     #_[common/menu]
     #_[:br]
     #_[common/definition-info-for-focus definition instrument' path-params query-params]
     [common/instrument-view entity path-params query-params deps]]))

(re-frame/reg-event-fx
 ::start
 (fn [{:keys [db]} [_event-id entity]]
   (let [query-params     (events/query-params db)
         fretboard-matrix (music-theory/instrument-data-structure entity query-params)]
     {:fx [[:dispatch [::entity entity]]
           [:dispatch [:add-entity-instrument-data-structure entity fretboard-matrix]]]})))

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
                      (events/do-on-url-change route-name p q)
                      (re-frame/dispatch [::start p]))}]}]))
