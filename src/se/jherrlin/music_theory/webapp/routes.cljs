(ns se.jherrlin.music-theory.webapp.routes
  (:require
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.webapp.views.home :as home]
   [se.jherrlin.music-theory.webapp.views.focus :as focus]))


(defn routes []
  (timbre/info "Collecting routes.")
  [(home/routes)
   (focus/routes)])
