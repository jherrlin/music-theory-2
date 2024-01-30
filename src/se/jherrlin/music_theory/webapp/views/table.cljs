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
        query-params                     @(re-frame/subscribe [:query-params])
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

     [:<>
      [:h3 "Chords"]
      [:table
       [:thead
        [:tr
         [:th "Suffix"]
         [:th "Intervals"]
         [:th "Tones"]]]
       [:tbody
        (for [{id           :id
               intervals    :chord/intervals
               suffix       :chord/suffix
               chord-name   :chord/chord-name
               display-text :chord/display-text}
              music-theory/chords]
          ^{:key (str "chord-list-" id)}
          [:tr
           [:td
            [:a
             {:href
              (rfe/href
               :chord
               (assoc path-params :chord chord-name)
               query-params)}
             (or display-text suffix)]]
           [:td
            (->> intervals
                 (str/join ", "))]
           [:td
            (->> (utils/tones-by-key-and-intervals key-of intervals)
                 (map (comp str/capitalize name))
                 (str/join ", "))]])]]]

     [:<>
      [:h3 "Scales"]
      [:table
       [:thead
        [:tr
         [:th "Name"]
         [:th "Intervals"]
         [:th "Tones"]]]
       [:tbody
        (for [{id         :id
               intervals  :scale/intervals
               scale-name :scale}
              music-theory/scales]
          ^{:key (str "scale-list-" id scale-name)}
          [:tr
           [:td
            [:a
             {:href
              (rfe/href
               :scale
               (assoc path-params :scale scale-name)
               query-params)}
             (-> scale-name name str/capitalize)]]
           [:td (str/join ", " intervals)]
           [:td
            (->> (utils/tones-by-key-and-intervals key-of intervals)
                 (map (comp str/capitalize name))
                 (str/join ", "))]])]]]]))


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
