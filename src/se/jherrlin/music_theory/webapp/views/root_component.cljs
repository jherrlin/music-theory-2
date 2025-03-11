(ns se.jherrlin.music-theory.webapp.views.root-component
  (:require
   [re-frame.core :as re-frame]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub <sub-flow >evt]]
   [se.jherrlin.music-theory.webapp.views.common :as views.common]))


(defn ^:dev/after-load root-component [{:keys [play-tone] :as m}]
  (let [current-route @(re-frame/subscribe [:current-route])
        top-nav       (get-in (<sub [:current-route]) [:data :top-nav])]
    (when current-route
      [:div
       {:style {:height         "100%"
                :display        "flex"
                :flex-direction "column"}}
       [:div {:style {:height      "100%"
                      :overflow-y  "auto"
                      :overflow-x  "hidden"
                      :padding-top "0.5em"}}

        (when (= top-nav :abcjs-examples)
          [views.common/top-nav-abcjs-examples])

        ;; This is the main location on the page.
        (when current-route
          (-> current-route :data :view))]])))
