(ns se.jherrlin.music-theory.webapp.components.music-theory
  (:require
   [integrant.core :as ig]
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.music-theory :as music-theory]))


(defmethod ig/init-key :webapp/music-theory [_ {:keys []}]
  (timbre/info "Starting music theory component.")
  {:instruments music-theory/instruments})
