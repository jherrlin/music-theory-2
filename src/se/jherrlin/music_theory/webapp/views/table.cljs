(ns se.jherrlin.music-theory.webapp.views.table
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


{:chord/intervals      ["1" "3" "5"],
 :chord/chord-name-str "major",
 :chord/chord-name     :major,
 :chord/order          1,
 :type                 [:chord],
 :chord/categories     #{:major},
 :chord/display-text   "major",
 :id                   #uuid "1cd72972-ca33-4962-871c-1551b7ea5244",
 :chord/intervals-str  "1, 3, 5",
 :chord/explanation    "major",
 :chord/suffix         "",
 :chord/indexes        [0 4 7]}

{:id                #uuid "39af7096-b5c6-45e9-b743-6791b217a3df",
 :type              [:scale],
 :scale/scale-names #{:ionian :major},
 :scale/intervals   ["1" "2" "3" "4" "5" "6" "7"],
 :scale/indexes     [0 2 4 5 7 9 11],
 :scale/categories  #{:major},
 :scale/order       1,
 :scale             :ionian}

(defn table-component [deps]
  (let [{:keys [key-of] :as path-params} @(re-frame/subscribe [:path-params])
        _                                (def key-of key-of)
        chords                           (->> music-theory/chords
                                              (map (fn [{intervals :chord/intervals :as m}]
                                                     (assoc m :tones (music-theory/tones-by-key-and-intervals key-of intervals)))))
        scales                           (->> music-theory/scales
                                              (map (fn [{intervals :scale/intervals :as m}]
                                                     (assoc m :tones (music-theory/tones-by-key-and-intervals key-of intervals)))))]
    [:<>
     [common/menu]
     [:br]
     [common/key-selection]

     [:div "table"]]))


(defn routes [deps]
  (let [route-name :table]
    ["/table/:key-of"
     {:name       route-name
      :view       [table-component deps]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:path  [:map
                           [:key-of keyword?]]
                   :query events/Query}
      :controllers
      [{:parameters {:path  [:key-of]
                     :query events/query-keys}
        :start      (fn [{p :path q :query}]
                      (events/do-on-url-change route-name p q))}]}]))
