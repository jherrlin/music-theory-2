(ns se.jherrlin.music-theory.webapp.main
  (:require
   [reagent.dom :as rd]
   [re-frame.core :as re-frame]
   [reitit.coercion.spec :as rss]
   [reagent.dom.client :as rdc]
   [reitit.frontend :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.frontend.easy :as rfe]
   [clojure.string :as str]
   [clojure.set :as set]
   [integrant.core :as ig]
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.webapp.components.tonejs :as tonejs]
   [se.jherrlin.music-theory.webapp.components.reitit :as reitit]
   [se.jherrlin.music-theory.webapp.routes :as routes]
   [se.jherrlin.music-theory.webapp.components.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.root-component :refer [root-component]]
   [se.jherrlin.music-theory.webapp.events]))


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
  (re-frame/clear-subscription-cache!)
  (reitit/start! (routes/routes {}))
  (mount-ui))

(defn init
  "This is the starting point of the web application."
  []
  (timbre/info "Starting webapp.")
  (re-frame/clear-subscription-cache!)
  (re-frame/dispatch-sync [:initialize-db])
  (reitit/start! (routes/routes {}))
  (mount-ui))
