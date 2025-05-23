(ns se.jherrlin.music-theory.webapp.components.reitit
  (:require
   [integrant.core :as ig]
   [clojure.data :refer [diff]]
   [se.jherrlin.music-theory.webapp.events :as events]
   [re-frame.core :as re-frame]
   [re-frame.db]
   [reitit.frontend.controllers :as rfc]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.schema :as rsc]
   [reitit.frontend :as rf]
   [taoensso.timbre :as timbre]))


(re-frame/reg-event-db
 ::initialize-db
 (fn [db _]
   (assoc db :current-route nil)))

(comment
  (-> @re-frame.db/app-db
      :current-route))

(re-frame/reg-event-fx
 :push-state
 (fn [_ [_ [route-name path-params query-params :as route]]]
   (let [qp (-> (diff
                 query-params
                 (events/default-query-params))
                (first))]
     {:dispatch [:query-params query-params]
      :push-state
      [route-name
       path-params
       qp]})))

(re-frame/reg-event-fx
 :href
 (fn [_ [_ [route-name path-params query-params :as route]]]
   (let [qp (-> (diff
                 query-params
                 (events/default-query-params))
                (first))]
     {:dispatch [:query-params query-params]
      :push-state
      [route-name
       path-params
       qp]})))

(re-frame/reg-event-db
 ::navigated
 (fn [db [_ new-match]]
   (let [old-match   (:current-route db)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (assoc db :current-route (assoc new-match :controllers controllers)))))

(re-frame/reg-sub
 :current-route
 (fn [db]
   (:current-route db)))

(re-frame/reg-fx
 :push-state
 (fn [route]
   (apply rfe/push-state route)))

(defn on-navigate [new-match]
  (when new-match
    (re-frame/dispatch [::navigated new-match])))

(defn router [routes]
  (rf/router
   routes
   {:data {:coercion rsc/coercion #_rss/coercion}}))

(defn start! [routes]
  (rfe/start!
   (router routes)
   on-navigate
   {:use-fragment true}))

(defmethod ig/init-key :webapp/router [_ {:keys [routes]}]
  (timbre/info "Starting Ritit router.")
  (start! routes))
