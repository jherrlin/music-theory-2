(ns se.jherrlin.music-theory.webapp.views.home
  (:require
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.webapp.views.common :as common]
   ["shadcn/button" :refer [Button]]
   ["shadcn/alert" :refer [Alert AlertTitle AlertDescription]]
   ["shadcn/label" :refer [Label]]))


(js/console.log "Label" Label)
(js/console.log "Button" Button)
(js/console.log "Alert" Alert)

;; (js/console.log "Accordion" Accordion)



(defn home-view [deps]
  [:<>
   [common/menu]
   [:br]
   [:> Alert
    [:> AlertTitle "Hej"]
    [:> AlertDescription "Hopp"]]
   [:> Label {:htmlFor "email"} "derp@derp.se"]
   [:> Button {:variant "outline"
                      :on-click (fn [_]
                                  (js/console.log "In here"))} "Button"]
   [:div {:class "bg-green-100"}
    [:h2 "Welcome"]]])





(defn routes [deps]
  (let [route-name :home]
    ["/"
     {:name       route-name
      :view       [home-view deps]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:query events/Query}
      :controllers
      [{:parameters {:query events/query-keys}
        :start      (fn [{q :query}]
                      (events/do-on-url-change route-name {} q))}]}]))
