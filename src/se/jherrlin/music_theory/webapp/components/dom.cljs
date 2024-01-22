(ns se.jherrlin.music-theory.webapp.components.dom
   (:require
   [integrant.core :as ig]
   [taoensso.timbre :as timbre]
    [re-frame.core :as re-frame]
   [reagent.dom :as rd]
   [reitit.frontend.easy :as rfe]
   ;;[reagent.dom.client :as rdc]
   ))

;; new way of doing it
;; (defonce garage-calculator-root
;;   (rdc/create-root (js/document.getElementById "garage-calculator")))

;; (rdc/render garage-calculator-root
;;             [app-container [room-calculator :garage]])

(defn render [root-component m]
  (re-frame/clear-subscription-cache!)
  (rd/render [root-component m] (.getElementById js/document "app")))

(defmethod ig/init-key :webapp/dom [_ {:keys [root-component] :as m}]
  (timbre/info "Rendering root-component.")
  (render root-component m))
