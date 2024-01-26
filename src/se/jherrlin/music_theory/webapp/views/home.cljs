(ns se.jherrlin.music-theory.webapp.views.home
  (:require
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.webapp.views.common :as common]))


(defn view []
  [:<>
   [common/menu]
   [:br]
   [:h2 "Welcome"]])

(defn routes []
  ["/"
   {:name      :home
    :view      [#'view]
    :controllers
    [{:start
      (fn [_]
        (timbre/info "Entering route :home"))
      :stop
      (fn [_]
        (timbre/info "Leaving route :home"))}]}])
