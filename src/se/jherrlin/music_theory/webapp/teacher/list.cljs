(ns se.jherrlin.music-theory.webapp.teacher.list
  (:require
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [se.jherrlin.music-theory.webapp.websocket :as websocket]
   [re-frame.alpha :as rf]
   [taoensso.timbre :as timbre]))

(def app-db-path ::teacher-list)

(defn path [x]
  (vec (flatten [app-db-path x])))

(def documents-path (path ::documents))

(def events-
  [{:n ::documents
    :p documents-path}])

(doseq [{:keys [n s e p]} events-]
  (rf/reg-sub n      (or s (fn [db _]     (get-in   db p))))
  (rf/reg-event-db n (or e (fn [db [_ e]] (assoc-in db p e)))))

(defn view []
  (let [documents (<sub [::documents])]
    [:div
     [:h2 "teacher list view!"]
     [:div
      (for [[title id] documents]
        ^{:key (str id)}
        [:div title])]]))

(rf/reg-event-fx
 ::start
 (fn [{:keys [db]} [_event-id]]
   (timbre/debug _event-id)
   {:fx [[::websocket/send {:data       [:fetch/documents]
                            :on-success #(>evt [::documents %])
                            :on-failure #(timbre/info "on-failure" %)}]]}))

(defn routes [deps]
  (let [route-name :teacher/list]
    ["/teacher/list"
     {:name route-name
      :view [view]
      :controllers
      [{:start #(>evt [::start])}]}]))

(comment
  (>evt [::start])
  (get @re-frame.db/app-db app-db-path)
  )
