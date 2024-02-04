(ns se.jherrlin.music-theory.webapp.routes
  (:require
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.webapp.views.home :as home]
   [se.jherrlin.music-theory.webapp.views.focus :as focus]
   [se.jherrlin.music-theory.webapp.views.chord :as chord]
   [se.jherrlin.music-theory.webapp.views.scale :as scale]
   [se.jherrlin.music-theory.webapp.views.table :as table]
   [se.jherrlin.music-theory.webapp.views.harmonizations :as harmonizations]
   [se.jherrlin.music-theory.webapp.views.bookmarks :as bookmarks]
   [se.jherrlin.music-theory.webapp.views.find-chord :as find-chord]))


(defn routes [{:keys [play-tone] :as deps}]
  (timbre/info "Collecting routes.")
  [(home/routes deps)
   (focus/routes deps)
   (chord/routes deps)
   (scale/routes deps)
   (table/routes deps)
   (harmonizations/routes deps)
   (bookmarks/routes deps)
   (find-chord/routes deps)])
