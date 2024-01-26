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
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.utils :as utils]))


(defn merge' [db [k m]]
  (assoc db k (merge (get db k) m)))

(def init-db
  {:current-route      nil
   :current-route-name :home
   :path-params        {:key-of              :c
                        :instrument          :guitar
                        :chord               :major
                        :scale               :major
                        :harmonization-scale :major}
   :query-params       {:nr-of-frets           15
                        :nr-of-octavs          2
                        :as-intervals          false
                        :as-text               false
                        :trim-fretboard        false
                        :debug                 false
                        :surrounding-intervals false
                        :surrounding-tones     false}})

(def Query
  [:map
   [:nr-of-frets           {:optional true} int?]
   [:nr-of-octavs          {:optional true} int?]
   [:as-intervals          {:optional true} boolean?]
   [:as-text               {:optional true} boolean?]
   [:trim-fretboard        {:optional true} boolean?]
   [:surrounding-intervals {:optional true} boolean?]
   [:surrounding-tones     {:optional true} boolean?]])

(def query-keys
  [:nr-of-frets :as-intervals :as-text :nr-of-octavs :trim-fretboard
   :surrounding-intervals :surrounding-tones])

(def events-
  [{:n :current-route}
   {:n :current-route-name}
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

(re-frame/reg-flow
 {:id     ::instrument
  :inputs {:i [:path-params :instrument]}
  :output (fn [{:keys [i]}] (music-theory/instrument i))
  :path   [:instrument]})

(re-frame/reg-flow
 {:id     ::fretboard-matrix
  :inputs {:instrument  (re-frame/flow<- ::instrument)
           :nr-of-frets [:query-params :nr-of-frets]
           :key-of      [:path-params :key-of]}
  :output (fn [{:keys [instrument nr-of-frets key-of]}]
            (let [{:keys [type tuning]} instrument]
              (when (and (= type :fretboard) instrument nr-of-frets key-of)
                (let [fretboard (music-theory/fretboard-strings tuning nr-of-frets)
                      tones-matched-with-intervals
                      (mapv
                       vector
                       (->> (music-theory/tones-starting-at key-of)
                            (map first))
                       ["1" "b2" "2" "b3" "3" "4" "b5" "5" "b6" "6" "b7" "7"])]
                  (music-theory/add-intervals-to-fretboard-matrix ;; TODO: rename
                   fretboard
                   tones-matched-with-intervals)))))
  :path [:fretboard-matrix]})

(re-frame/reg-sub
 :fretboard-matrix
 (fn [db [n']] (get db n')))

(re-frame/reg-event-db
 :navigated
 (fn [db [_ new-match]]
   (let [old-match   (:current-route db)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (assoc db :current-route (assoc new-match :controllers controllers)))))

(re-frame/reg-fx
 :push-state
 (fn [route]
   (apply rfe/push-state route)))

(re-frame/reg-event-fx
 :href
 (fn [_ [_ route]]
   {:push-state route}))

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
