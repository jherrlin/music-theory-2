(ns se.jherrlin.music-theory.webapp.views.root-component
  (:require
   [re-frame.core :as re-frame]))


(defn root-component [{:keys [play-tone] :as m}]
  (let [current-route @(re-frame/subscribe [:current-route])]
    [:<>
     (when current-route
       [:div
        {:style {:height         "100%"
                 :display        "flex"
                 :flex-direction "column"}}
        [:div {:style {:height      "100%"
                       :overflow-y  "auto"
                       :overflow-x  "hidden"
                       :padding-top "0.5em"}}

         ;; This is the main location on the page.
         (when current-route
           (-> current-route :data :view))]])]))
