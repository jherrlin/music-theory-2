(ns se.jherrlin.music-theory.webapp.views.home
  (:require
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.webapp.views.common :as common]
   ["@shadcn/ui/button.js" :refer [Button]]))





(defn home-view [deps]
  [:<>
   [common/menu]
   [:br]
   [:> Button {:variant "outline"} "Button"]
   ;;[button :label "hejsn"]
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
