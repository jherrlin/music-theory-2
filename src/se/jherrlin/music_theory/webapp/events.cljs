(ns se.jherrlin.music-theory.webapp.events
  (:require
   [reagent.dom :as rd]
   [re-frame.core :as re-frame]
   [re-frame.alpha]
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
                        :scale               :major
                        :harmonization-scale :major
                        :chord               :major
                        :harmonization-fn    :triad}
   :query-params       {:nr-of-frets    15
                        :nr-of-octavs   2
                        :as-intervals   false
                        :as-text        false
                        :trim-fretboard true
                        :debug          false}
   :mat nil})

(def events-
  [{:n :key-of
    :s (fn [db [k]] (get db k :c))}
   {:n :current-route}
   {:n :current-route-name
    :s (fn [db [k]] (get db k :home))}
   {:n :path-params
    :e merge'
    :s (fn [db [k]] (get db k))}
   {:n :instrument
    :s (fn [db [k]] (get-in db [:path-params k]))
    :e (fn [db [k v]] (assoc-in db [:path-params k] v))}
   {:n :scale
    :s (fn [db [k]] (get-in db [:path-params k]))
    :e (fn [db [k v]] (assoc-in db [:path-params k] v))}
   {:n :harmonization-scale
    :s (fn [db [k]] (get-in db [:path-params k]))
    :e (fn [db [k v]] (assoc-in db [:path-params k] v))}
   {:n :chord
    :s (fn [db [k]] (get-in db [:path-params k]))
    :e (fn [db [k v]] (assoc-in db [:path-params k] v))}
   {:n :harmonization-fn
    :s (fn [db [k]] (get-in db [:path-params k]))
    :e (fn [db [k v]] (assoc-in db [:path-params k] v))}
   {:n :query-params
    :e merge'
    :s (fn [db [k]]
         (get db k))}
   {:n :nr-of-frets
    :s (fn [db [k]] (get-in db [:query-params k]))
    :e (fn [db [k v]] (assoc-in db [:query-params k] v))}
   {:n :nr-of-octavs
    :s (fn [db [k]] (get-in db [:query-params k]))
    :e (fn [db [k v]] (assoc-in db [:query-params k] v))}
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

(re-frame/reg-sub
 :derp
 (fn [db _]
   (get db :derp)))

(re-frame/reg-sub
 :general-data
 (fn [db _]
   (when db
     (let [{type        :instrument/type
            tuning      :instrument/tuning
            :as         instrument}
           (-> (get-in db [:path-params :instrument])
               (music-theory/instrument))

           {nr-of-frets :query-params/nr-of-frets
            :as query-params}
           (-> (get-in db [:query-params])
               (utils/add-qualified-ns :query-params))]
       (merge
        instrument
        query-params
        (some-> (get-in db [:path-params])
                (utils/add-qualified-ns :path-params))

        (when (= type :fretboard)
          {:fretboard-matrix (music-theory/fretboard-strings tuning nr-of-frets)}))))))

(re-frame/reg-sub
 :data-for-id
 (fn [db [_ id]]
   (some-> id
           (music-theory/by-id)
           (utils/add-qualified-ns :definition))))

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
  [new-route-name
   {:keys [key-of instrument scale chord id]
    :as   path-params}
   {:keys [nr-of-frets] :as query-params}]
  (re-frame/dispatch [:current-route-name new-route-name])
  (re-frame/dispatch [:path-params path-params])
  (re-frame/dispatch [:query-params query-params])
  (when key-of
    (re-frame/dispatch [:key-of key-of]))
  (when scale
    (re-frame/dispatch [:scale scale]))
  (when chord
    (re-frame/dispatch [:chord chord])))
