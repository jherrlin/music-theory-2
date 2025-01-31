(ns se.jherrlin.music-theory.webapp.views.instruments.fretboard
  (:require [se.jherrlin.music-theory.music-theory :as music-theory]))


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
  [{:keys [on-click circle-text background-color fret-color y circle-color blank?
           octave show-octave?]
    :or   {y                0
           background-color "#000000d6"
           fret-color       "linear-gradient(to right, #FFFFFF , #706e68)"
           show-octave?     false}}]
  (let [string-color  "linear-gradient(#737270 , #b9bab3, #737270)"
        circle-color  (or circle-color "orange")
        string-height (str (+ 0.2 y) "rem")
        fret-width    3.9
        fret-height   2.7]
    [:div {:on-click on-click
           :style    {:width          (str fret-width "rem")
                      :height         (str fret-height "rem")
                      :display        "flex"
                      :flex-direction "row"}}
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
       (when circle-text
         [:div {:style {:display          "flex"
                        :align-items      :center
                        :justify-content  :center
                        :height           "2rem"
                        :width            "2rem"
                        :background-color circle-color
                        :border-radius    "50%"
                        :z-index          0}}
          [:<>
           circle-text
           (when show-octave?
             [:div {:style {:font-size  "small"
                            :margin-top "0.5em"}}
              octave])]])]]

     (when-not blank?
       [:div {:style {:background-image fret-color
                      :z-index          50
                      :width            "0.5rem"
                      :height           "100%"}}])]))

(defn styled-view [{:keys [id
                           fretboard-matrix
                           on-click
                           orange-fn
                           grey-fn
                           dark-orange-fn
                           show-octave?]
                    :or   {orange-fn      :out
                           grey-fn        (constantly false)
                           dark-orange-fn (constantly false)
                           on-click       (fn [fret fretboard-matrix]
                                            (js/console.log fret)
                                            (js/console.log fretboard-matrix))}}]
  (let [max-x (->> fretboard-matrix first (map :x) (apply max))]
    [:div {:style {:overflow-x "auto"}}
     [:div {:style {:display "flex"}}
      (for [{:keys [x y]} (-> fretboard-matrix first)]
        ^{:key (str "fretboard-fret-nr-" x id)}
        [fret-number x])]
     (for [fretboard-string fretboard-matrix]
       ^{:key (str fretboard-string id)}
       [:div {:style {:display "flex"}}
        (for [{:keys [x y tone out root? blank? octave] :as fret} fretboard-string]
          (let [orange-fn'      (orange-fn fret)
                dark-orange-fn' (dark-orange-fn fret)
                grey-fn'        (grey-fn fret)
                left-is-blank?  (music-theory/first-fret? fretboard-matrix fret)]
            ^{:key (str "fret-" x y id)}
            [fret-component
             {:show-octave?     show-octave?
              :octave           octave
              :blank?           blank?
              :y                (/ y 10)
              :circle-color     (cond
                                  dark-orange-fn' "#ff7600"
                                  orange-fn'      "orange"
                                  grey-fn'        "grey")
              :on-click         (fn [_] (on-click fret fretboard-matrix))
              :circle-text      (cond
                                  dark-orange-fn' dark-orange-fn'
                                  orange-fn'      orange-fn'
                                  grey-fn'        grey-fn'
                                  :else           nil)
              :background-color (if left-is-blank?
                                  "white"
                                  "#000000d6")
              :fret-color       (cond
                                  left-is-blank? "white"
                                  (= x 0)        "linear-gradient(black, black, black)"
                                  (= x max-x)    "linear-gradient(#000000d6, #000000d6, #000000d6)"
                                  :else
                                  "linear-gradient(to right, #FFFFFF , #706e68)")}]))])]))
