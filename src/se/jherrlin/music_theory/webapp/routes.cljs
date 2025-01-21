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
   [se.jherrlin.music-theory.webapp.views.find-chord :as find-chord]
   [se.jherrlin.music-theory.webapp.views.find-scale :as find-scale]
   [se.jherrlin.music-theory.webapp.views.dev :as dev]
   [se.jherrlin.music-theory.webapp.views.dev.fretboard2 :as dev.fretboard2]
   [se.jherrlin.music-theory.webapp.views.dev.learn-harmonizations :as dev.learn-harmonizations]
   [se.jherrlin.music-theory.webapp.views.learn.chord-tones :as learn.chord-tones]
   [se.jherrlin.music-theory.webapp.views.intersecting-tones :as intersecting-tones]))


(defn ^:dev/after-load routes [deps]
  (timbre/info "Collecting routes.")
  [(home/routes deps)
   (focus/routes deps)
   (chord/routes deps)
   (scale/routes deps)
   (table/routes deps)
   (harmonizations/routes deps)
   (bookmarks/routes deps)
   (find-chord/routes deps)
   (find-scale/routes deps)
   (learn.chord-tones/routes deps)
   (intersecting-tones/routes deps)


   (dev/routes deps)
   (dev.fretboard2/routes deps)
   (dev.learn-harmonizations/routes deps)])
