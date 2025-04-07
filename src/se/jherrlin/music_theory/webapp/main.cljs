(ns se.jherrlin.music-theory.webapp.main
  (:require
   [reagent.dom :as rd]
   [re-frame.alpha :as re-frame]
   [se.jherrlin.music-theory.webapp.websocket :as websocket]
   [reitit.coercion.spec :as rss]
   [reagent.dom.client :as rdc]
   [reitit.frontend :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.frontend.easy :as rfe]
   [clojure.string :as str]
   [clojure.set :as set]
   [integrant.core :as ig]
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.webapp.components.reitit :as reitit]
   [se.jherrlin.music-theory.webapp.routes :as routes]
   [se.jherrlin.music-theory.webapp.components.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.root-component :refer [root-component]]
   [se.jherrlin.music-theory.webapp.events]
   [se.jherrlin.music-theory.webapp.websocket :as webapp.websocket]
   [se.jherrlin.music-theory.webapp.ws-handlers :as ws-handlers]))


;; -- Entry Point -------------------------------------------------------------

(defonce root-container
  (rdc/create-root (js/document.getElementById "app")))

(defn mount-ui
  []
  (rdc/render root-container [root-component]))

(defn ^:dev/after-load clear-cache-and-render!
  []
  ;; The `:dev/after-load` metadata causes this function to be called
  ;; after shadow-cljs hot-reloads code. We force a UI update by clearing
  ;; the Reframe subscription cache.
  (let [app-el     (.getElementById js/document "app")
        csrf-token (.getAttribute app-el "data-csrf-token")]
    (re-frame/clear-subscription-cache!)
    (reitit/start! (routes/routes {:backend? (string? csrf-token)}))
    (mount-ui)))

(re-frame/reg-fx
 ::start-router!
 (fn [{:keys [backend?]}]
   (timbre/info "Router starting up...")
   (reitit/start! (routes/routes {:backend? backend?}))))

(re-frame/reg-event-fx
 ::ws-failed-to-start
 (fn [{:keys [db]} [_event-id]]
   (timbre/error "Failed to init websocket! Won't start router.")))

(re-frame/reg-event-fx
 ::start-router
 (fn [{:keys [db]} [_event-id {:keys [nr-of-calls backend?]
                               :or   {nr-of-calls 0}
                               :as   args}]]
   (timbre/info "Trying to start websocket...")
   (cond
     (or (false? backend?)
         (get-in db websocket/open?-path))
     {::start-router! {:backend? backend?}}

     (< nr-of-calls 10)
     {:fx [[:dispatch-later {:ms 100 :dispatch [::start-router (update args :nr-of-calls inc)]}]]}

     :else
     {:fx [[:dispatch [::ws-failed-to-start]]]})))

(defn init
  "This is the starting point of the web application."
  []
  (timbre/info "Starting webapp...")
  (let [app-el     (.getElementById js/document "app")
        csrf-token (.getAttribute app-el "data-csrf-token")]
    (when csrf-token
      (webapp.websocket/start! csrf-token #'ws-handlers/incoming-events-handler))

    (re-frame/clear-subscription-cache!)
    (re-frame/dispatch-sync [:initialize-db])
    (re-frame/dispatch-sync [::start-router
                             {:nr-of-calls 0
                              :backend?    (string? csrf-token)}])
    (mount-ui)))
