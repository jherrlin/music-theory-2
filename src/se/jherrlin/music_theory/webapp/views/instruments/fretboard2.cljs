(ns se.jherrlin.music-theory.webapp.views.instruments.fretboard2)


(defn fret-number [n]
  [:div {:style {:width           "3.9rem"
                 :min-width       "3.9rem"
                 :height          "2rem"
                 :display         "flex"
                 :justify-content :center}}
   (if (= n 0)
     ""
     n)])

(defn fret-component
  [{:keys [background-color
           circle-color
           center-text
           down-right-text
           blank?
           y
           on-click
           x-max?
           x-min?
           left-is-blank?]
    :as   m}]
  (let [y                (/ y 10)
        fret-color       (cond
                           x-max? "linear-gradient(black, black, black)"
                           x-min? "linear-gradient(#000000d6, #000000d6, #000000d6)"
                           :else
                           "linear-gradient(to right, #FFFFFF , #706e68)")
        background-color (if left-is-blank?
                           "white"
                           "#000000d6")
        string-color     "linear-gradient(#737270 , #b9bab3, #737270)"
        circle-color     (or circle-color "orange")
        string-height    (str (+ 0.2 y) "rem")
        fret-width       3.9
        fret-height      2.7]
    [:div (cond-> {:style    {:width          (str fret-width "rem")
                              :height         (str fret-height "rem")
                              :display        "flex"
                              :flex-direction "row"}}
            on-click (assoc :on-click #(on-click m)))
     [:div (when-not blank?
             {:style {:background-color background-color
                      :width            (str (- fret-width 0.3) "rem")
                      :height           "100%"
                      :justify-content  :center
                      :display          "flex"
                      :flex-direction   "column"}})
      [:div (when-not blank?
              {:style {:display          "flex"
                       :align-items      :center
                       :justify-content  :center
                       :background-image string-color
                       :height           string-height
                       :width            (str fret-width "rem")
                       :z-index          100}})
       (when center-text
         [:div {:style {:display          "flex"
                        :align-items      :center
                        :justify-content  :center
                        :height           "2rem"
                        :width            "2rem"
                        :background-color circle-color
                        :border-radius    "50%"
                        :z-index          0}}
          [:<>
           center-text
           (when down-right-text
             [:div {:style {:font-size  "small"
                            :margin-top "0.5em"}}
              down-right-text])]])]]

     (when-not blank?
       [:div {:style {:background-image fret-color
                      :z-index          50
                      :width            "0.5rem"
                      :height           "100%"}}])]))

(defn left-is-blank? [x y matrix]
  (let [xy-map (->> matrix
                    (apply concat)
                    (map (fn [{:keys [x y] :as m}]
                           [[x y] m]))
                    (into {}))
        fret (get-in xy-map [[(dec x) y]])]
    (or (get fret :blank?)
        (nil? fret))))

(defn styled-view [{:keys [fretboard-matrix id]}]
  [:div {:style {:overflow-x "auto"}}
   [:div {:style {:display "flex"}}
    (for [{:keys [x]} (-> fretboard-matrix first)]
      ^{:key (str "fretboard-fret-nr-" x id)}
      [fret-number x])]
   (for [fretboard-string fretboard-matrix]
     ^{:key (str fretboard-string id)}
     [:div {:style {:display "flex"}}
      (for [{:keys [x y] :as fret} fretboard-string]
        ^{:key (str "fret-" x y id)}
        [fret-component fret])])])
