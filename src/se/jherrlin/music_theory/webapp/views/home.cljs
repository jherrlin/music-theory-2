(ns se.jherrlin.music-theory.webapp.views.home
  (:require
   ;; ["components/ui/button" :refer [Button]]
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.webapp.views.common :as common]
   [re-com.core    :refer [at h-box v-box box gap line button label throbber hyperlink-href p p-span]]))



(defn home-view [deps]
  [:<>
   [common/menu]
   [:br]
   ;;[:> Button {:variant "outline"} "Button"]
   [button :label "hejsn"]
   [:div {:class "bg-green-200"}
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
