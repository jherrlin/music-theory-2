(ns se.jherrlin.music-theory.webapp.components.dom
   (:require
   [integrant.core :as ig]
   [taoensso.timbre :as timbre]
    [re-frame.core :as re-frame]
   [reagent.dom :as rd]
   [reitit.frontend.easy :as rfe]))


(defn ^:dev/after-load render [root-component]
  (re-frame/clear-subscription-cache!)
  (rd/render [root-component] (.getElementById js/document "app")))

(defmethod ig/init-key :webapp/dom [_ {:keys [root-component]}]
  (timbre/info "Rendering root-component.")
  (render root-component))
