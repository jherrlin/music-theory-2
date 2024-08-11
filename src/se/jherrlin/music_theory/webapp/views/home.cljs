(ns se.jherrlin.music-theory.webapp.views.home
  (:require
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.webapp.views.common :as common]
   [reagent.core :as r]
   #_["shadcn" :refer [
                     Button
                     ;; Label
                     Alert
                     AlertTitle
                     AlertDescription
                     ;; NavigationMenu,
                     ;; NavigationMenuContent,
                     ;; NavigationMenuIndicator,
                     ;; NavigationMenuItem,
                     ;; NavigationMenuLink,
                     ;; NavigationMenuList,
                     ;; NavigationMenuTrigger,
                     ;; NavigationMenuViewport
                     ;; Accordion,
                     ;; AccordionContent,
                     ;; AccordionItem,
                     ;; AccordionTrigger
                     ]]
   ["shadcn" :as shadcn]
   ))

(js/console.log "shadcn" shadcn)
(js/console.log "Alert" shadcn/Alert)
(js/console.log "Button" shadcn/Button)
(js/console.log "Label" shadcn/Label)
;; (js/console.log "Accordion" Accordion)



(defn home-view [deps]
  [:<>
   [common/menu]
   [:br]
   [:> shadcn/Alert
    [:> shadcn/AlertTitle "Hej"]
    [:> shadcn/AlertDescription "Hopp"]
    ]
   [:> shadcn/Label {:htmlFor "email"} "derp@derp.se"]
   [:> shadcn/Button {:variant "outline"
                      :on-click (fn [_]
                                  (js/console.log "In here"))} "Button"]
   #_[:> Accordion {:type "single" :collapsible true}
      [:> AccordionItem {:value "item-1"}
       [:> AccordionTrigger "Hej"]
       [:> AccordionContent
        [:div "Hejsan"]]]]
   #_[:> NavigationMenu
      [:> NavigationMenuList
       [:> NavigationMenuItem
        [:> NavigationMenuTrigger "Item one"]
        [:> NavigationMenuContent
         [:div "hejsan"]]]]]
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
