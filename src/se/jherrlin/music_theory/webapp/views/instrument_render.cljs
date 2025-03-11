(ns se.jherrlin.music-theory.webapp.views.instrument-render
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [re-frame.core :as re-frame]
   [re-frame.alpha :as re-frame-alpha]
   [reitit.frontend.easy :as rfe]
   [se.jherrlin.utils :as basic-utils]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.models.entity :as entity]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard :as instruments-fretboard]
   [se.jherrlin.music-theory.harmonizations :as harmonizations]))


(defn basic-data [{:keys [definition-id instrument-id key-of query-params]}]
  (let [instrument (music-theory/get-instrument instrument-id)
        definition (music-theory/get-definition definition-id)]
    {:definition      definition
     :instrument      instrument
     :key-of          key-of
     :definition-type (:type definition)
     :instrument-type (:type instrument)
     :entity          (music-theory/entity key-of instrument-id definition-id)
     :query-params    query-params}))

(defn remove-scale-patterns-starts-on [m path entity interval]
  (update-in m (flatten [path entity :query-params]) (fnil disj #{}) interval))

(defn add-scale-patterns-starts-on
  [m path entity interval]
  (update m (flatten [path entity :query-params]) (fnil conj #{}) interval))


(defmulti instrument-render-data :instrument-type)

(defmethod instrument-render-data :fretboard
  [{:keys [instrument key-of query-params]
    :as m}]
  (let [nr-of-frets       (:nr-of-frets query-params)
        instrument-tuning (:tuning instrument)]
    (assoc m :fretboard-matrix (music-theory/create-fretboard-matrix key-of nr-of-frets instrument-tuning))))

(defn ->entity
  [{:keys [definition key-of instrument]}]
  (entity/entity key-of (:id instrument) (:id definition)))

;; (->> (basic-data
;;      {:key-of        :d
;;       :instrument-id :mandolin
;;       :definition-id #uuid "e99e0f40-93df-4524-b1f8-e6c70b12972f"
;;       :query-params  (<sub [:query-params])})
;;      instrument-render-data
;;      ;;->entity
;;      )
