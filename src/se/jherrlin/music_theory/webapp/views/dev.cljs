(ns se.jherrlin.music-theory.webapp.views.dev
  (:require
   [re-frame.alpha :as re-frame]
   [reitit.coercion.malli]
   [clojure.string :as str]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.common :as common]
   [re-frame.db :as db]
   [se.jherrlin.music-theory.models.entity :as entity]
   [se.jherrlin.music-theory.instruments :as instruments]
   [se.jherrlin.music-theory.webapp.views.scale-calcs :as scale-calcs]))



(defn view [deps]
  [:<>
   [:div "dev"]])

(defn ^:dev/after-load routes [deps]
  (let [route-name :dev]
    ["/dev/dev"
     {:name route-name
      :view [view deps]}]))
