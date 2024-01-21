(ns se.jherrlin.music-theory.webapp.views.home
  (:require
   [taoensso.timbre :as timbre]))


(defn view []
  [:div "Welcome home!"])

(defn routes []
  ["/"
   {:name      :route/home
    :view      [#'view]
    :controllers
    [{:start
      (fn [_]
        (timbre/info "Entering route :route/home"))
      :stop
      (fn [_]
        (timbre/info "Leaving route :route/home"))}]}])
