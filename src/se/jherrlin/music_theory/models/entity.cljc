(ns se.jherrlin.music-theory.models.entity
  "An entity is something, like a chord.
  It has a key, a specific instrument and an id."
  (:require
   [malli.core :as m]
   [clojure.string :as str]
   [se.jherrlin.music-theory.models.tone :as tone]
   [se.jherrlin.music-theory.instruments :as instruments]
   [se.jherrlin.utils :as utils]))


(def Entity
  [:map
   [:instrument  (into
                  [:enum]
                  (->> instruments/definitions
                       vals
                       (map :id)
                       set
                       vec))]
   [:key-of      tone/IntervalTone]
   [:id          uuid?]])

(def Entities
  [:vector
   {:min 1}
   Entity])

(def EntitiesSet
  [:set
   {:min 1}
   Entity])

(def valid-entity?       (partial m/validate Entity))
(def valid-entities?     (partial m/validate Entities))
(def valid-entities-set? (partial m/validate EntitiesSet))

(valid-entity?
 {:instrument :guitar,
  :key-of     :c,
  :id         #uuid "39af7096-b5c6-45e9-b743-6791b217a3df"})

(defn entity [key-of instrument id]
  (utils/validate
   Entity
   {:id         id
    :instrument instrument
    :key-of     key-of}))

(defn select-entity-keys [m]
  (utils/validate
   Entity
   (select-keys m [:id :instrument :key-of])))

(defn definitions-to-entities
  ([key-of instrument definitions]
   (definitions-to-entities key-of instrument :id definitions))
  ([key-of instrument id-fn definitions]
   {:pre [(keyword? instrument)]}
   (->> definitions
        (map (fn [m]
               (let [id (id-fn m)]
                 (entity key-of instrument id)))))))

(defn entity-to-str [{:keys [instrument key-of id]}]
  (str (-> instrument name) "," (-> key-of name) "," id))

(defn str-to-entity [s]
  (let [[instrument key-of id] (str/split s ",")]
    {:instrument (keyword instrument)
     :key-of     (keyword key-of)
     :id         (parse-uuid id)}))

(defn str-to-entities [s]
  (->> (str/split s "_")
       (map str-to-entity)))

(let [m {:instrument :guitar
         :key-of     :c
         :id         #uuid "c91cddfe-f776-4c0c-8125-4f4c5d074e77"}]
  (->> m
       (entity-to-str)
       (str-to-entity)
       (= m)))

(str-to-entities
 "guitar,c,94f5f7a4-d852-431f-90ca-9e99f89bbb9c")

(defn fretboard-entity? [{:keys [instrument] :as m}]
  {:pre [(valid-entity? m)]}
  (= (instruments/get-instrument-type instrument)
     :fretboard))
