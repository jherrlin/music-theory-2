(ns se.jherrlin.music-theory.webapp.views.bookmarks
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [re-frame.alpha :as re-frame]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.utils :as utils]
   [se.jherrlin.music-theory.webapp.views.common :as common]))

(re-frame/reg-event-db
 :add-bookmark
 (fn [db [_ bookmark]]
   {:pre [(music-theory/valid-entity? bookmark)]}
   (let [bookmarks    (get-in db [:query-params :bookmarks])
         bookmark-str (music-theory/entity-to-str bookmark)]
     (assoc-in db [:query-params :bookmarks]
               (if-not (seq bookmarks)
                 bookmark-str
                 (str bookmarks "_" bookmark-str))))))

(comment
  (get-in @re-frame.db/app-db [:query-params])
  )

(re-frame/reg-event-db
 :remove-bookmark
 (fn [db [_ bookmark]]
   {:pre [(music-theory/valid-entity? bookmark)]}
   (let [bookmarks (get-in db [:query-params :bookmarks])
         bookmark-str (music-theory/entity-to-str bookmark)
         new-bookmarks (-> bookmarks
                           (str/replace bookmark-str "")
                           (str/replace #"_$" "")
                           (str/replace #"^_" ""))]
     (assoc-in db [:query-params :bookmarks] new-bookmarks))))

(re-frame/reg-sub
 :bookmark-exists?
 (fn [db [_ bookmark]]
;;   {:pre [(music-theory/valid-entity? bookmark)]}
   (when (seq (get-in db [:query-params :bookmarks]))
     (let [bookmarks     (->> (get-in db [:query-params :bookmarks])
                              (music-theory/str-to-entities))
           bookmarks-set (set bookmarks)]
       (boolean
        (bookmarks-set bookmark))))))

(re-frame/reg-flow
 {:id     ::bookmarks
  :inputs {:s [:query-params :bookmarks]}
  :output (fn [{:keys [s]}]
            (when (seq s)
              (try
                (music-theory/str-to-entities s)
                (catch js/Error e
                  (println e)
                  nil))))
  :path   [:bookmarks]})

(re-frame/reg-sub :bookmarks (fn [db [n']] (get db n')))

(defn bookmarks-component [deps]
  (let [path-params  @(re-frame/subscribe [:path-params])
        query-params @(re-frame/subscribe [:query-params])
        bookmarks    @(re-frame/subscribe [:bookmarks])]
    [:<>
     [common/menu]
     [:br]
     (for [{:keys [instrument key-of id] :as entity} bookmarks]
       (let [definition (music-theory/by-id id)
             instrument (music-theory/get-instrument instrument)]
         ^{:key (str "bookmark-definition-" (music-theory/entity-to-str entity))}
         [:<>
          [common/definition-info-for-focus
           entity definition instrument path-params query-params]
          [:br]
          [common/instrument-view entity path-params query-params deps]
          [:br]
          [:br]]))]))

(defn prepair-fretboard-entities [s nr-of-frets]
  (let [fretboard-entities (->> s
                                (music-theory/str-to-entities)
                                (filter music-theory/fretboard-entity?))]
    (doseq [{:keys [id key-of instrument] :as fretboard-entity} fretboard-entities]
      (let [{:keys [tuning]} (music-theory/get-instrument instrument)
            fretboard-matrix (music-theory/create-fretboard-matrix key-of nr-of-frets tuning)]
        (re-frame/dispatch [:add-entity-with-fretboard fretboard-entity fretboard-matrix])))))

(defn routes [deps]
  (let [route-name :bookmarks]
    ["/bookmarks"
     {:name       route-name
      :view       [bookmarks-component deps]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:query events/Query}
      :controllers
      [{:parameters {:query events/query-keys}
        :start      (fn [{q :query}]
                      (let [bookmarks   (get q :bookmarks)
                            nr-of-frets (get q :nr-of-frets)]
                        (when (seq bookmarks)
                          (prepair-fretboard-entities bookmarks nr-of-frets))
                        (def q q))
                      (events/do-on-url-change route-name {} q))}]}]))
