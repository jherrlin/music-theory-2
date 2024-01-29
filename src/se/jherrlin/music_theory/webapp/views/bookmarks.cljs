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
   {:pre [(music-theory/valid-unit? bookmark)]}
   (let [bookmarks    (get-in db [:query-params :bookmarks])
         bookmark-str (music-theory/unit-to-str bookmark)]
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
   {:pre [(music-theory/valid-unit? bookmark)]}
   (let [bookmarks (get-in db [:query-params :bookmarks])
         bookmark-str (music-theory/unit-to-str bookmark)
         new-bookmarks (-> bookmarks
                           (str/replace bookmark-str "")
                           (str/replace #"_$" "")
                           (str/replace #"^_" ""))]
     (assoc-in db [:query-params :bookmarks] new-bookmarks))))

(re-frame/reg-sub
 :bookmark-exists?
 (fn [db [_ bookmark]]
   {:pre [(music-theory/valid-unit? bookmark)]}
   (when (seq (get-in db [:query-params :bookmarks]))
     (let [bookmarks     (->> (get-in db [:query-params :bookmarks])
                              (music-theory/str-to-units))
           bookmarks-set (set bookmarks)]
       (boolean
        (bookmarks-set bookmark))))))

(re-frame/reg-flow
 {:id     ::bookmarks
  :inputs {:s [:query-params :bookmarks]}
  :output (fn [{:keys [s]}]
            (def s s)
            (when (seq s)
              (try
                (->> (music-theory/str-to-units s)
                     (map (fn [{:keys [instrument key-of id]}]
                            {:instrument (music-theory/get-instrument instrument)
                             :definition (music-theory/by-id id)
                             :key-of     key-of})))
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
     (for [{:keys [instrument definition key-of]} bookmarks]
       (let [id (get definition :id)]
         ^{:key (str "bookmark-definition-" id)}
         [:<>
          [common/definition-info-for-focus
           definition instrument (assoc path-params :key-of key-of) query-params]
          [:br]
          [common/instrument-view
           definition instrument (assoc path-params :key-of key-of) query-params deps]
          [:br]
          [:br]]))]))

(defn routes [deps]
  (let [route-name :bookmarks]
    ["/bookmarks"
     {:name       route-name
      :view       [bookmarks-component deps]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:query events/Query}
      :controllers
      [{:parameters {:path  [:instrument :key-of :chord]
                     :query events/query-keys}
        :start      (fn [{p :path q :query}]
                      (events/do-on-url-change route-name p q))}]}]))
