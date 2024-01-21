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
   [se.jherrlin.music-theory.webapp.components.tonejs :as tonejs]))


(def debug? ^boolean goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (println "dev mode")))



(defn main []
  [:<>
   [:div "hejsan"]
   [:button {:on-click (fn [_]

                         (tonejs/play-tone :c 4)
                         )}
    "play"]])

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (rd/render
   [main]
   (.getElementById js/document "app")))


(def system-config
  {:webapp/tonejs nil
   ;; :website.websocket/sente {:csrf-token (when-let [el (.getElementById js/document "app")]
   ;;                                         (.getAttribute el "data-csrf-token"))
   ;;                           :handler    websocket-events/handler}
   ;; :website.routing/reitit  {:handler   routes/router
   ;;                           :websocket (ig/ref :website.websocket/sente)}
   ;; :website.dom/mount       {:router (ig/ref :website.routing/reitit)
   ;;                           :mount  views/mount-root}
   })

(defn init
  "This is the starting point of the web application."
  []
  (timbre/info "Starting webapp.")
  (ig/init system-config))
