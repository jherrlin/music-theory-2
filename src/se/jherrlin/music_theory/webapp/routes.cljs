(ns se.jherrlin.music-theory.webapp.routes
  (:require
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.webapp.views.home :as home]))


(defn routes []
  (timbre/info "Collecting routes.")
  [(home/routes)])
