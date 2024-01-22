(ns se.jherrlin.music-theory.webapp.views.menus)


(defn select-instrument []
  (let [instruments se.jherrlin.music-theory.music-theory/instruments]
    [:div {:style {:display        "flow"
                   :flow-direction "column"
                   :overflow-x     "auto"
                   :white-space    "nowrap"}}
     (for [{:instrument/keys [text id] :as m}
           instruments]
       ^{:key (str "instrument-selection-" id)}
       [:a {:style {:margin-right "10px"}
            ;; :href  (rfe/href
            ;;         current-route-name
            ;;         (assoc path-params :instrument-type instrument-type' :tuning tuning')
            ;;         query-params)
            }
        [:button
         ;; {:disabled (and (= instrument-type instrument-type')
         ;;                 (= tuning tuning'))}
         text]])]))
