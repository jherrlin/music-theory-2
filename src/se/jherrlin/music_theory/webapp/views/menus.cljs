(ns se.jherrlin.music-theory.webapp.views.menus)


(defn select-instrument [instruments]
  [:div {:style {:display        "flow"
                 :flow-direction "column"
                 :overflow-x     "auto"
                 :white-space    "nowrap"}}
   (for [{instrument-type' :instrument-type
          tuning'          :tuning
          :keys            [title] :as m}
         (->> instruments)]
     ^{:key title}
     [:a {:style {:margin-right "10px"}
          :href  (rfe/href
                  current-route-name
                  (assoc path-params :instrument-type instrument-type' :tuning tuning')
                  query-params)}
         [:button
          {:disabled (and (= instrument-type instrument-type')
                          (= tuning tuning'))}
          title]])])
