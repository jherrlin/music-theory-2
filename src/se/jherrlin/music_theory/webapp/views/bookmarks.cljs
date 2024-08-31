(ns se.jherrlin.music-theory.webapp.views.bookmarks
  (:require
   [clojure.string :as str]
   [clojure.data :refer [diff]]
   [re-frame.alpha :as re-frame]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.common :as common]
   [se.jherrlin.music-theory.models.entity :as entity]))

(re-frame/reg-event-fx
 :add-bookmark
 (fn [{:keys [db]} [_ bookmark]]
   {:pre [(music-theory/valid-entity? bookmark)]}
   (let [current-route-name     (get db :current-route-name)
         path-params            (get db :path-params)
         query-params           (get db :query-params)
         existing-bookmarks-str (get-in db [:query-params :bookmarks])
         new-bookmark-str       (music-theory/entity-to-str bookmark)
         new-bookmarks-str      (if-not (seq existing-bookmarks-str)
                                  new-bookmark-str
                                  (str existing-bookmarks-str "_" new-bookmark-str))
         new-query-params       (assoc query-params :bookmarks new-bookmarks-str)]
     {:push-state [current-route-name
                   path-params
                   (-> (diff
                        new-query-params
                        (events/default-query-params))
                       (first))]})))

(comment
  (get-in @re-frame.db/app-db [:query-params])
  )

(re-frame/reg-event-fx
 :remove-bookmark
 (fn [{:keys [db]} [_ bookmark]]
   {:pre [(music-theory/valid-entity? bookmark)]}
   (let [current-route-name (get db :current-route-name)
         path-params        (get db :path-params)
         query-params       (get db :query-params)

         existing-bookmarks-str (get-in db [:query-params :bookmarks])
         new-bookmark-str       (music-theory/entity-to-str bookmark)
         new-bookmarks-str      (-> existing-bookmarks-str
                                    (str/replace new-bookmark-str "")
                                    (str/replace #"_$" "")
                                    (str/replace #"^_" ""))
         new-query-params       (assoc query-params :bookmarks new-bookmarks-str)]
     {:push-state [current-route-name
                   path-params
                   (-> (diff
                        new-query-params
                        (events/default-query-params))
                       (first))]})))

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
       (let [definition (music-theory/get-definition id)
             instrument (music-theory/get-instrument instrument)]
         ^{:key (str "bookmark-definition-" (music-theory/entity-to-str entity))}
         [:<>
          [common/definition-info-for-focus
           entity definition instrument path-params query-params]
          [:br]
          [common/instrument-view entity path-params query-params deps]
          [:br]
          [:br]]))]))

(re-frame/reg-event-fx
 ::start
 (fn [{:keys [db]} [_event-id bookmarks-str]]
   (let [query-params     (events/query-params db)]
     {:fx (->> bookmarks-str
               music-theory/str-to-entities
               (mapv (fn [entity]
                      (let [fretboard-matrix (music-theory/instrument-data-structure entity query-params)]
                        [:dispatch [:add-entity-instrument-data-structure entity fretboard-matrix]]))))})))

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
                      (let [bookmarks-str (get q :bookmarks)]
                        (events/do-on-url-change route-name {} q)
                        (when (seq bookmarks-str)
                          (re-frame/dispatch [::start bookmarks-str]))))}]}]))
