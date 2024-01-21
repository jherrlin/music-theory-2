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
   [se.jherrlin.music-theory.webapp.views.root-component :refer [root-component]]))


(def debug? ^boolean goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (println "dev mode")))

(def system-config
  {:webapp/tonejs nil
   :webapp/dom    {:root-component root-component
                   :play-tone      (ig/ref :webapp/tonejs)}
   :webapp/router {:routes (routes/routes)}})

(defn ^:dev/after-load init
  "This is the starting point of the web application."
  []
  (timbre/info "Starting webapp.")
  (ig/init system-config))
