(ns se.jherrlin.music-theory.webapp.routes
  (:require
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.webapp.views.home :as home]
   [se.jherrlin.music-theory.webapp.views.focus :as focus]
   [se.jherrlin.music-theory.webapp.views.chord :as chord]
   [se.jherrlin.music-theory.webapp.views.scale :as scale]
   [se.jherrlin.music-theory.webapp.views.table :as table]))


(defn routes [{:keys [play-tone] :as deps}]
  (timbre/info "Collecting routes.")
  [(home/routes)
   (focus/routes deps)
   (chord/routes deps)
   (scale/routes deps)
   (table/routes deps)])
