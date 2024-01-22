(ns se.jherrlin.music-theory.webapp.components.dom
  (:require
   [integrant.core :as ig]
   [taoensso.timbre :as timbre]
   [re-frame.core :as re-frame]
   [reagent.dom :as rd]
   [reitit.frontend.easy :as rfe]
   [reagent.dom.client :as rdc]))

(defonce root
  (rdc/create-root (js/document.getElementById "app")))

(defn render [root-component m]
  (re-frame/clear-subscription-cache!)
  (rd/render [root-component m] (.getElementById js/document "app")))

(defmethod ig/init-key :webapp/dom [_ {:keys [root-component] :as m}]
  (timbre/info "Rendering root-component.")
  (re-frame/clear-subscription-cache!)
  (rdc/render root [root-component]))
