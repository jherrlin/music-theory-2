(ns se.jherrlin.music-theory.webapp.views.dev
  (:require
   [re-frame.alpha :as re-frame]
   [reitit.coercion.malli]
   [clojure.string :as str]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.views.common :as common]
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard2 :as fretboard2]
   [re-frame.db :as db]
   [se.jherrlin.music-theory.models.entity :as entity]
   [se.jherrlin.music-theory.instruments :as instruments]
   [se.jherrlin.music-theory.webapp.views.scale-calcs :as scale-calcs]))



(defn ^:dev/after-load view [deps]
  [fretboard2/styled-view
   {:id "deokedk"
    :size 0.73
    :fretboard-matrix
    [[{:x              0, :y 0
       :x-min          true
       :fret-color     "white"
       :circle-color   "green"
       :center-text ""
       :left-is-blank? true}
      {:x               1,
       :y               0
       :center-text     "A"
       :down-right-text "2"}
      {:x 2, :y 0}
      {:x 3, :y 0}
      {:x 4,
       :y 0
       :background-color "purple"}
      {:x 5, :y 0}]
     [{:x 0, :y 1}
      {:x 1, :y 1}
      {:x 2, :y 1}
      {:x            3,
       :y            1
       :center-text "G"
       :circle-color "white"
       :on-click (fn [{:keys [x y center-text]}]
                   (js/console.log x y center-text))}
      {:x 4, :y 1}
      {:x 5, :y 1}]
     [{:x 0, :y 2}
      {:x 1, :y 2}
      {:x 2, :y 2}
      {:x 3, :y 2}
      {:x 4, :y 2}
      {:x 5,
       :y 2
       :x-max? true}]
     [{:x      0,
       :y      3
       :blank? true}
      {:x      1,
       :y      3
       :blank? true}
      {:x              2,
       :y              3
       :left-is-blank? true}
      {:x            3,
       :y            3
       :circle-color "green"
       :center-text  "B"}
      {:x 4, :y 3}
      {:x 5, :y 3}]]}])


(defn ^:dev/after-load routes [deps]
  (let [route-name :dev]
    ["/dev"
     {:name route-name
      :view [view deps]}]))
