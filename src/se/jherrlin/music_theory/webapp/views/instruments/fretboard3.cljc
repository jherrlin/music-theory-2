(ns se.jherrlin.music-theory.webapp.views.instruments.fretboard3
  "The main difference between fretboard3 and fretboard2 is that fretboard3
  appends all the data and hides elements with CSS to make it simple to
  interact with the DOM directly."
  (:require
   [se.jherrlin.music-theory.music-theory :as music-theory]))


(defn fret-number [fretboard-size n]
  [:div {:style {:width           (str (* fretboard-size 3.9) "rem")
                 :min-width       (str (* fretboard-size 3.9) "rem")
                 :height          (str (* fretboard-size 2) "rem")
                 :display         "flex"
                 :justify-content :center}}
   (if (= n 0) "" n)])

(def root-note-color "#ff7600")
(def note-color "#ffa500") ;; orange
(def highlight-color "green")
(def grey "#808080")

(def colors
  {:root-note-color root-note-color
   :color/root-note root-note-color
   :note-color      note-color
   :color/note      note-color
   :color/highlight highlight-color
   :color/grey      grey})

(defn fret-component
  [{:as   m
    :keys [background-color
           circle-color
           center-text
           midi-pitch
           down-right-text
           blank?
           id
           y
           x
           on-click
           x-max?
           x-min?
           left-is-blank?
           fretboard-size
           entity-str
           circle-background
           interval-tone
           interval]
    :or   {circle-color   note-color
           fretboard-size 1}}]
  (let [string-hight     (/ y 10)
        fret-color       (cond
                           x-max? "linear-gradient(black, black, black)"
                           x-min? "linear-gradient(#000000d6, #000000d6, #000000d6)"
                           :else
                           "linear-gradient(to right, #FFFFFF , #706e68)")
        background-color (cond
                           background-color background-color
                           left-is-blank?   "white"
                           :else            "#000000d6")
        string-color     "linear-gradient(#737270 , #b9bab3, #737270)"
        string-height    (str (* fretboard-size (+ 0.2 string-hight)) "rem")
        fret-width       (* fretboard-size 3.9)
        fret-height      (* fretboard-size 2.7)]
    [:div (cond-> {:style    {:width          (str fret-width "rem")
                              :height         (str fret-height "rem")
                              :display        "flex"
                              :flex-direction "row"}}
            on-click (assoc :on-click #(on-click m)))
     [:div {:class              (str id "-" midi-pitch "-div")
            :data-interval-tone interval-tone
            :data-interval      interval
            :data-x             x
            :data-y             y
            :style              {:background-color (if blank?
                                                     "white"
                                                     background-color)
                                 :width            (str (- fret-width 0.3) "rem")
                                 :height           "100%"
                                 :justify-content  :center
                                 :display          "flex"
                                 :flex-direction   "column"}}
      [:div {:style {:display          "flex"
                     :align-items      :center
                     :justify-content  :center
                     :background-image (if blank?
                                         "white"
                                         string-color)
                     :height           string-height
                     :width            (str fret-width "rem")
                     :z-index          100}}
       (when interval-tone
         [:div {:id    (music-theory/circle-dom-id entity-str x y)
                :class (str id "-" midi-pitch "-center-text-div")

                :style {:display          "flex"
                        :align-items      :center
                        :justify-content  :center
                        :visibility       "collapse"
                        :height           (str (* fretboard-size 2) "rem")
                        :width            (str (* fretboard-size 2) "rem")
                        :background-color (if (keyword? circle-color)
                                            (get colors circle-color)
                                            circle-color)
                        :border-radius    "50%"
                        :z-index          0
                        :background       "#ffa500"}}
          [:<>
           interval-tone
           (when down-right-text
             [:div {:style {:font-fretboard-size "small"
                            :margin-top          (str (* fretboard-size 0.5) "rem")}
                    :class (str id "-" midi-pitch "-down-right-text-div")}
              down-right-text])]])]]

     [:div {:style {:background-image (if blank?
                                        "white"
                                        fret-color)
                    :z-index          50
                    :width            (str (* fretboard-size 0.5) "rem")
                    :height           "100%"}}]]))

(defn styled-view [{:keys [fretboard-matrix
                           id
                           fretboard-size
                           entity-str]
                    :or   {fretboard-size 1}}]
  [:div {:style {:overflow-x "auto"}
         :id    id}
   [:div {:style {:display "flex"}}
    (for [{:keys [x]} (-> fretboard-matrix first)]
      ^{:key (str "fretboard-fret-nr-" x id)}
      [fret-number fretboard-size x])]
   (for [fretboard-string fretboard-matrix]
     ^{:key (str fretboard-string id)}
     [:div {:style {:display "flex"}}
      (for [{:keys [x y] :as fret} fretboard-string]
        ^{:key (str "fret-" x y id)}
        [fret-component (assoc fret
                               :id id
                               :fretboard-size fretboard-size
                               :entity-str entity-str)])])])
