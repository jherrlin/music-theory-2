(ns se.jherrlin.music-theory.webapp.views.table
  (:require
   [clojure.string :as str]
   [re-frame.core :as re-frame]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub <sub-flow >evt]]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.common :as common]
   [re-frame.alpha :as rf-alpha]))


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

(def app-db-path ::learn-harmonizations)

(defn path [x]
  (-> [app-db-path x] flatten vec))

(def notes-to-play-path (path ::notes-to-play))

(defonce interval-handler         ;; notice the use of defonce
  (let [live-intervals (atom {})] ;; storage for live intervals
    (fn handler [{:keys [action id frequency event]}] ;; the effect handler
      (condp = action
        :clean   (doall ;; clean up all existing
                   (map #(handler {:action :end  :id  %1}) (keys @live-intervals)))
        :start   (swap! live-intervals assoc id
                   (js/setInterval #(>evt event) frequency))
        :end     (do (js/clearInterval (get @live-intervals id))
                   (swap! live-intervals dissoc id))))))

(rf-alpha/reg-fx ::tick interval-handler)

(rf-alpha/reg-event-fx
 ::on-tick
 (fn [{:keys [db]} [_event-id]]
   (js/console.log ::tick)
   (let [[x & rst] (get-in db notes-to-play-path)]
     (if x
       {:db (assoc-in db notes-to-play-path rst)
        :fx [[:dispatch [:tonejs/play-tone x]]]}
       {:db db}))))

(rf-alpha/reg-event-db
 ::add-notes-to-play
 (fn [db [_event-id notes]]
   (js/console.log ::add-notes-to-play)
   (assoc-in db notes-to-play-path notes)))

(comment
  (interval-handler {:action :clean})
  (interval-handler {:action    :start
                     :id        ::tick
                     :frequency 500
                     :event     [::on-tick]})

  (>evt [::add-notes-to-play
         (->> (music-theory/tones-by-key-and-intervals :d ["1" "3" "5"])
              (map (comp #(hash-map :tone % :octave 3) clojure.string/capitalize name)))])
  :-)

(defn table-component [deps]
  (let [{:keys [key-of] :as path-params} @(re-frame/subscribe [:path-params])
        query-params                     @(re-frame/subscribe [:query-params])]
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
            (->> (music-theory/tones-by-key-and-intervals key-of intervals)
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
            (->> (music-theory/tones-by-key-and-intervals key-of intervals)
              (map (comp str/capitalize name))
              (str/join ", "))]])]]]

     [:<>
      [:h3 "Intervals"]
      [:table
       [:thead
        [:tr
         [:th "From tone"]
         [:th "To tone"]
         [:th "Intervals"]]]
       [:tbody
        (for [{t1 :t1
               t2 :t2
               interval :interval
               :as m}
              music-theory/all-intervals]
          ^{:key (str "interval-" (hash m))}
          [:tr
           [:td (-> t1 name str/capitalize)]
           [:td (-> t2 name str/capitalize)]
           [:td interval]])]]]]))


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
