(ns se.jherrlin.music-theory.webapp.system
  (:require
   [reagent.dom :as rd]
   [re-frame.core :as re-frame]
   [reitit.coercion.spec :as rss]
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
   [se.jherrlin.music-theory.webapp.components.dom :as dom]
   [se.jherrlin.music-theory.webapp.components.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.root-component :refer [root-component]]
   [se.jherrlin.music-theory.webapp.events]))


(def debug? ^boolean goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (println "dev mode")))

(defn ^:dev/after-load system-config
  []
  {:webapp/tonejs       nil
   :webapp/music-theory nil
   :webapp/dom          {:root-component root-component
                         :play-tone      (ig/ref :webapp/tonejs)
                         :music-theory   (ig/ref :webapp/music-theory)}
   :webapp/router       {:routes (routes/routes
                                  {:play-tone (ig/ref :webapp/tonejs)})}})

(defn ^:dev/after-load init
  "This is the starting point of the web application."
  []
  (timbre/info "Starting webapp.")
  (re-frame/clear-subscription-cache!)
  (re-frame/dispatch-sync [:initialize-db])
  (ig/init (system-config)))
