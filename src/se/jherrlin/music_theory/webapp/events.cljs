(ns se.jherrlin.music-theory.webapp.events
  (:require
   [reagent.dom :as rd]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [re-frame.alpha :as re-frame]
   [reitit.coercion.spec :as rss]
   [reitit.frontend :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.frontend.easy :as rfe]
   [clojure.string :as str]
   [clojure.set :as set]
   [clojure.data :refer [diff]]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [re-frame.db :as db]))


(defn merge' [db [k m]]
  (assoc db k (merge (get db k) m)))

(def app-db-path ::events)

(defn path [x]
  (-> [app-db-path x] flatten vec))

(def query-params :query-params)

(def init-db
  {:current-route      nil
   :current-route-name :home
   :scale-patterns-starts-on #{"1"}
   :path-params        {:key-of              :c
                        :instrument          :guitar
                        :chord               :major
                        :scale               :major
                        :harmonization-id    :triads
                        :harmonization-scale :major}
   query-params        {:nr-of-frets              15
                        :nr-of-octavs             2
                        :bpm                      80
                        :trim-fretboard           false
                        :as-intervals             false
                        :as-text                  false
                        :debug                    false
                        :surrounding-intervals    false
                        :surrounding-tones        false
                        :show-octave              false
                        :show-tones               false
                        :bookmarks                ""}})

(defn default-query-params []
  (get init-db query-params))


(def Query
  [:map
   [:nr-of-frets           {:optional true} int?]
   [:nr-of-octavs          {:optional true} int?]
   [:bpm                   {:optional true} int?]
   [:as-intervals          {:optional true} boolean?]
   [:as-text               {:optional true} boolean?]
   [:surrounding-intervals {:optional true} boolean?]
   [:surrounding-tones     {:optional true} boolean?]
   [:show-octave           {:optional true} boolean?]
   [:show-tones            {:optional true} boolean?]
   [:debug                 {:optional true} boolean?]
   [:trim-fretboard        {:optional true} boolean?]
   [:bookmarks             {:optional true} any?]])

(def query-keys (->> Query rest (mapv first)))

(def events-
  [{:n :current-route-name}
   {:n :path-params
    :e merge'
    :s (fn [db [k]] (get db k))}
   {:n :query-params
    :e merge'
    :s (fn [db [k]] (get db k))}
   {:n :key-of
    :s (fn [db [k]] (get-in db [:path-params k]))}
   {:n :as-intervals
    :s (fn [db [k]] (get-in db [:query-params k]))
    :e (fn [db [k v]] (assoc-in db [:query-params k] v))}
   {:n :as-text
    :s (fn [db [k]] (get-in db [:query-params k]))
    :e (fn [db [k v]] (assoc-in db [:query-params k] v))}
   {:n :trim-fretboard
    :s (fn [db [k]] (get-in db [:query-params k]))
    :e (fn [db [k v]] (assoc-in db [:query-params k] v))}
   {:n :debug
    :s (fn [db [k]] (get-in db [:query-params k]))
    :e (fn [db [k v]] (assoc-in db [:query-params k] v))}])

(doseq [{:keys [n s e]} events-]
  (re-frame/reg-sub n (or s (fn [db [n']] (get db n'))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))



(re-frame/reg-event-db
 :add-entity-instrument-data-structure
 (fn [db [_ entity fretboard]]
   (-> db
       (update ::fretboards assoc-in [entity :id] entity)
       (update ::fretboards assoc-in [entity :instrument-data-structure] fretboard))))

(re-frame/reg-sub
 :changed-query-params
 (fn [db _]
   (-> (diff
        (query-params db)
        (query-params init-db))
       (first))))

(re-frame/reg-sub
 :scale-patterns-starts-on
 (fn [db _]
   (let [query-params                 (get db :query-params)
         scale-patterns-starts-on-str (get query-params :scale-patterns-starts-on)]
     (when (seq scale-patterns-starts-on-str)
       (str/split scale-patterns-starts-on-str #",")))))

(comment
  @(re-frame/subscribe [:changed-query-params])
  (<sub [:scale-patterns-starts-on])
  )

(re-frame/reg-sub
 :fretboard-by-entity
 (fn [db [_ entity]]
   (get-in db [::fretboards entity :instrument-data-structure])))

(re-frame/reg-sub
 :fretboards
 (fn [db [_]]
   (get db ::fretboards)))

(comment
  (get @re-frame.db/app-db ::fretboards)
  )

(re-frame/reg-event-db
 :add-query-params-with-fretboard
 (fn [db [_ entity query-params]]
   (-> db
       (update ::fretboards assoc-in [entity :id] entity)
       (update ::fretboards assoc-in [entity :query-params] query-params))))

(re-frame/reg-sub
 :query-params-for-entity
 (fn [db [_ entity]]
   (get-in db [::fretboards entity :query-params])))




(comment
  (let [entity {:id #uuid "1cd72972-ca33-4962-871c-1551b7ea5244",
                :instrument :guitar,
                :key-of :c}]
    (get-in @re-frame.db/app-db [::fretboards entity :instrument-data-structure]))

  )

(re-frame/reg-flow
 {:id     ::key-of
  :inputs {:key-of [:path-params :key-of]}
  :output :key-of
  :path   (path ::key-of)})

(re-frame/reg-flow
 (let [inputs {:instrument-id (re-frame/flow<- ::instrument)}
       id     ::instrument-id]
   {:id          id
    :live-inputs inputs
    :live?       :instrument-id
    :inputs      inputs
    :output      (comp :id :instrument-id)
    :path        (path id)}))

(re-frame/reg-flow
 (let [inputs {:scale (re-frame/flow<- ::scale)}]
   {:id          ::scale-id
    :live-inputs inputs
    :inputs      inputs
    :live?       :scale
    :output      (comp :id :scale)
    :path        (path ::scale-id)}))

(re-frame/reg-flow
 (let [inputs {:scale (re-frame/flow<- ::scale)}]
   {:id          ::scale-names
    :live-inputs inputs
    :inputs      inputs
    :live?       :scale
    :output      (comp :scale/scale-names :scale)
    :path        (path :scale/scale-names)}))

(comment
  (get-in @re-frame.db/app-db (path ::scale-id))
  @(re-frame/subscribe [:flow {:id ::scale-id}])
  )


;;(re-frame/reg-sub ::ttl-max :-> (comp ::ttl-max app-db-path-))

;;
;; Old ones, not using `path` fn
;;

(re-frame/reg-flow
 (let [inputs {:instrument-id [:path-params :instrument]}]
   {:id          ::instrument
    :live-inputs inputs
    :live?       :instrument-id
    :inputs      inputs
    :output      (comp music-theory/get-instrument :instrument-id)
    :path        [:instrument]}))

(re-frame/reg-sub :instrument (fn [db [n']] (get db n')))

(re-frame/reg-flow
 (let [inputs {:s [:path-params :scale]}]
   {:id     ::scale
    :inputs inputs
    :live-inputs inputs
    :live? :s
    :output (comp music-theory/get-scale :s)
    :path   [:scale]}))

(re-frame/reg-flow
 {:id     ::chord
  :inputs {:c [:path-params :chord]}
  :output (fn [{:keys [c]}] (music-theory/get-chord c))
  :path   [:chord]})

(re-frame/reg-flow
 {:id     ::harmonization
  :inputs {:h [:path-params :harmonization-id]}
  :output (fn [{:keys [h]}] (music-theory/get-harmonization h))
  :path   [:harmonization]})

(re-frame/reg-flow
 {:id     ::harmonization-scale
  :inputs {:s [:path-params :harmonization-scale]}
  :output (fn [{:keys [s]}] (music-theory/get-scale s))
  :path   [:harmonization-scale]})

(re-frame/reg-sub :harmonization-scale (fn [db [n']] (get db n')))

(re-frame/reg-event-db
 :initialize-db
 (fn [db _]
   (if (get db :path-params)
     db
     init-db)))

(defn do-on-url-change
  [new-route-name path-params query-params]
  (re-frame/dispatch [:current-route-name new-route-name])
  (re-frame/dispatch [:path-params path-params])
  (re-frame/dispatch [:query-params query-params]))


(re-frame/reg-sub
 :scale-patterns-starts-on
 (fn [db [_event-id]]
   (get db :scale-patterns-starts-on)))

(re-frame/reg-event-db
 :scale-patterns-starts-on
 (fn [db [_event-id intervals]]
   (assoc db :scale-patterns-starts-on intervals)))

(re-frame/reg-event-db
 :remove-scale-patterns-starts-on
 (fn [db [_event-id interval]]
   (update db :scale-patterns-starts-on (fnil disj #{}) interval)))

(re-frame/reg-event-db
 :add-scale-patterns-starts-on
 (fn [db [_event-id interval]]
   (update db :scale-patterns-starts-on (fnil conj #{}) interval)))
