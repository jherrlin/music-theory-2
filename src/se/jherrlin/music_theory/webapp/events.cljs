(ns se.jherrlin.music-theory.webapp.events
  (:require
   [reagent.dom :as rd]
   [re-frame.alpha :as re-frame]
   [reitit.coercion.spec :as rss]
   [reitit.frontend :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.frontend.easy :as rfe]
   [clojure.string :as str]
   [clojure.set :as set]
   [clojure.data :refer [diff]]
   [se.jherrlin.music-theory.music-theory :as music-theory]))


(defn merge' [db [k m]]
  (assoc db k (merge (get db k) m)))

(def query-params :query-params)

(def init-db
  {:current-route      nil
   :current-route-name :home
   :path-params        {:key-of              :c
                        :instrument          :guitar
                        :chord               :major
                        :scale               :major
                        :harmonization-id    :triads
                        :harmonization-scale :major}
   query-params        {:nr-of-frets           15
                        :nr-of-octavs          2
                        :as-intervals          false
                        :as-text               false
                        :debug                 false
                        :surrounding-intervals false
                        :surrounding-tones     false
                        :show-octave           false
                        :show-tones            false
                        :bookmarks             ""}})


(def Query
  [:map
   [:nr-of-frets           {:optional true} int?]
   [:nr-of-octavs          {:optional true} int?]
   [:as-intervals          {:optional true} boolean?]
   [:as-text               {:optional true} boolean?]
   [:surrounding-intervals {:optional true} boolean?]
   [:surrounding-tones     {:optional true} boolean?]
   [:show-octave           {:optional true} boolean?]
   [:show-tones            {:optional true} boolean?]
   [:debug                 {:optional true} boolean?]
   [:bookmarks             {:optional true} any?]])

(def query-keys
  [:nr-of-frets :as-intervals :as-text :nr-of-octavs :debug :show-tones
   :surrounding-intervals :surrounding-tones :show-octave :bookmarks])

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
       (update ::fretboards assoc-in [entity :fretboard] fretboard))))

(re-frame/reg-sub
 :changed-query-params
 (fn [db _]
   (-> (diff
        (query-params db)
        (query-params init-db))
       (first))))

(comment
  @(re-frame/subscribe [:changed-query-params])
  )

(re-frame/reg-sub
 :fretboard-by-entity
 (fn [db [_ entity]]
   (get-in db [::fretboards entity :fretboard])))

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
    (get-in @re-frame.db/app-db [::fretboards entity :fretboard]))

  )

(re-frame/reg-flow
 {:id     ::instrument
  :inputs {:i [:path-params :instrument]}
  :output (fn [{:keys [i]}] (music-theory/get-instrument i))
  :path   [:instrument]})

(re-frame/reg-sub :instrument (fn [db [n']] (get db n')))

(re-frame/reg-flow
 {:id     ::scale
  :inputs {:s [:path-params :scale]}
  :output (fn [{:keys [s]}] (music-theory/get-scale s))
  :path   [:scale]})

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
 :navigated
 (fn [db [_ new-match]]
   (let [old-match   (:current-route db)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (assoc db :current-route (assoc new-match :controllers controllers)))))

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
