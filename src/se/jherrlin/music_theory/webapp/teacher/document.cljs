(ns se.jherrlin.music-theory.webapp.teacher.document
  (:require
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [se.jherrlin.music-theory.webapp.websocket :as websocket]
   [re-frame.alpha :as rf]
   [taoensso.timbre :as timbre]))


(def app-db-path ::document)

(defn path [x]
  (vec (flatten [app-db-path x])))

(def document-id-path (path ::document-id))
(def document-path    (path ::document))

(def events-
  [{:n ::document-id
    :p document-id-path}
   {:n ::document
    :p document-path}])

(doseq [{:keys [n s e p]} events-]
  (rf/reg-sub n      (or s (fn [db _]     (get-in   db p))))
  (rf/reg-event-db n (or e (fn [db [_ e]] (assoc-in db p e)))))

(defn view []
  [:div
   [:h2 "Document by id"]])

(rf/reg-event-db
 ::fetch-document-success
 (fn [db [_event-id document]]
   (assoc-in db document-path document)))

(rf/reg-event-fx
 ::start
 (fn [{:keys [db]} [_event-id {:keys [document-id]}]]
   (timbre/debug _event-id document-id)
   {:db (-> db
            (assoc-in document-id-path document-id))
    :fx [[::websocket/send {:data       [:fetch/document document-id]
                            :on-success #(>evt [::fetch-document-success %])
                            :on-failure #(timbre/info "on-failure" %)}]]}))

(defn routes [deps]
  (let [route-name :document/by-id]
    ["/document/:id"
     {:name       route-name
      :view       [view]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:path [:map
                          [:id uuid?]]}
      :controllers
      [{:parameters {:path [:id]}
        :start      (fn [{{:keys [id]} :path :as m}]
                      (>evt [::start {:document-id id}]))}]}]))

(comment
  (>evt [::start {:document-id #uuid "3378e81a-0c56-4a2d-a2c2-cbff03829eae"}])

  (get @re-frame.db/app-db app-db-path)

  ;; http://localhost:8088/#/document/3378e81a-0c56-4a2d-a2c2-cbff03829eae
  )
