(ns se.jherrlin.music-theory.webapp.views.root-component
  (:require
   [re-frame.core :as re-frame]))


(defn ^:dev/after-load root-component [{:keys [play-tone] :as m}]
  (let [current-route @(re-frame/subscribe [:current-route])]
    (when current-route
      [:div {:style {:class "container mx-auto px-4"}}
        (when current-route
          (-> current-route :data :view))])))
