(ns se.jherrlin.music-theory.webapp.views.abcjs.examples.editor-with-play-and-fretboard
  (:require ["abcjs" :as abcjs]
            [se.jherrlin.music-theory.abc :as abc-utils]
            [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.alpha :as rf]
            [clojure.walk :as walk]
            [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
            [goog.functions :refer [debounce]]
            [se.jherrlin.music-theory.music-theory :as music-theory]
            [se.jherrlin.music-theory.webapp.views.instruments.fretboard2 :as fretboard2]
            [se.jherrlin.music-theory.webapp.views.instruments.fretboard3 :as fretboard3]))

;; Inputs and state:
;; dom-identifier
;; Instrument
;; Editor obj
;; Seek start and end
;; Loop over section?
;; ABC str
;; Current beat
;; Selected tones in editor
;; Bpm
;; ABC str errors





(declare editor')

(comment
  (js/console.log editor')
  )


(defonce seek-state (atom {:start nil
                           :end   nil}))

@seek-state

(defonce current-beat (atom 0))


;; (def whiskey-abc
;;   "X: 1
;; T: En tur over Staglaberget
;; M: 4/4
;; L: 1/8
;; K: D
;; P:A
;; |: \"D\" DEGF DE{=F}FA | \"D\" BdBA BdB2 | \"G\" GBdB ecdB | \"G\" GBGF GBA=F |
;; |\"C7\" | \"C7\" | \"F#m\" | \"F#m\" |
;; |  \"A\" AB{=c}c efaf |")

(def whiskey-abc
  "X: 1
T: Whiskey Before Breakfast
M: 4/4
L: 1/8
K: D
P: A
DD |: \"D\" D,,EFG A2 FG | ABAG FDEF  | \"G\" GABG \"D\" F2 AF | \"A\" EDEF EDCE |
   |  \"D\" DEFG A2 FG | c'''BAG FDEF  | \"G\" GABG \"D\" F2 AF |
   |1 \"A\" EDE,,,F \"D\" D2 DD :|2 \"A\" EDEF \"D\" D2 FE ||")



(def fretboard-checked-state (atom []))

(defn unmark-and-mark-fretboard-tone [pitch-nr]
  ;; Remove old markers
  (let [elements @fretboard-checked-state]
    (.forEach
     (js/Array.from elements)
     (fn [el]
       (set! (.-visibility (.-style el)) "collapse"))))

  ;; Add new markers
  (reset!
   fretboard-checked-state
   (.map
    (js/Array.from
     (.querySelectorAll
      (js/document.querySelector
       "#akxH4rw4Y682ySSDUo2AEm")
      (str ".akxH4rw4Y682ySSDUo2AEm-" pitch-nr "-center-text-div")))
    (fn [item]
      (set! (.-visibility (.-style item)) "visible")
      item))))

(defn unmark-and-mark-fretboard-tone-clean []
  ;; Remove old markers
  (let [elements @fretboard-checked-state]
    (.forEach
     (js/Array.from elements)
     (fn [el]
       (set! (.-visibility (.-style el)) "collapse")))))

(comment
  (unmark-and-mark-fretboard-tone 65)
  (unmark-and-mark-fretboard-tone 67)
  (unmark-and-mark-fretboard-tone-clean)
  )

(def events-
  [{:n ::abc-str}
   {:n ::selected-tones}
   {:n ::loop-section-start}
   {:n ::loop-section-end}
   {:n ::loop-section?}])

(comment
  (abc-utils/re-seq-pitches
   (<sub [::abc-str]))

  (println (<sub [::abc-str]))

  (<sub [::selected-tones])
  )

(doseq [{:keys [n s e d]} events-]
  (rf/reg-sub n (or s (fn [db [n']] (get db n' d))))
  (rf/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))

(defn move-cursor! [cursor {:keys [x1 x2 y1 y2]}]
  (.setAttribute cursor "x1" x1)
  (.setAttribute cursor "x2" x2)
  (.setAttribute cursor "y1" y1)
  (.setAttribute cursor "y2" y2))

(defn create-cursor []
  (let [cursor (js/document.createElementNS "http://www.w3.org/2000/svg" "line")]
    (.setAttribute cursor "class" "abcjs-cursor")
    (.setAttributeNS cursor nil, "x1", 0)
    (.setAttributeNS cursor nil, "x1", 0)
    (.setAttributeNS cursor nil, "y1", 0)
    (.setAttributeNS cursor nil, "x2", 0)
    (.setAttributeNS cursor nil, "y2", 0)
    cursor))

(def the-cursor (create-cursor))

(defn ->cursor-control-obj [{:keys [cursor]}]
  (js-obj
   "onReady" (fn [synthController]
               (let [svg (js/document.querySelector "#abc-canvas-id svg")]
                 (js/console.log "onReady" synthController)
                 (.appendChild svg cursor)))
   "onStart" (fn []
               (js/console.log "onStart"))
   "onFinished" (fn []
                  (js/console.log "onFinished"))
   "onBeat" (fn [beatNumber totalBeats totalTime position]
              ;; (js/console.log "onBeat")
              (reset! current-beat beatNumber)
              (if (and (get @re-frame.db/app-db ::loop-section?)
                       (= (get @re-frame.db/app-db ::loop-section-end) beatNumber))
                (.seek (.-synthControl (.-synth editor')) (get @re-frame.db/app-db ::loop-section-start) "beats"))
              (move-cursor! cursor {:x1 (- (.-left position) 2)
                                    :x2 (- (.-left position) 2)
                                    :y1 (.-top position)
                                    :y2 (+ (.-top position) (.-height position))}))
   "onEvent" (fn [event]
               (let [measureStart (.-measureStart event)
                     _            (def measureStart measureStart)
                     elements     (.-elements event)
                     _            (def elements elements)
                     left         (.-left event)
                     _            (def left left)
                     top          (.-top event)
                     _            (def top top)
                     height       (.-height event)
                     _            (def height height)
                     width        (.-width event)
                     _            (def width width)]
                 (js/console.log "onEvent" event)
                 (def event event)

                 (doseq [n (->> (js->clj (.-midiPitches event) :keywordize-keys true)
                                (map :pitch))]
                   (unmark-and-mark-fretboard-tone n))
                 (js->clj event :keywordize-keys true)))))

(defn selected-tones []
  (let [editor editor']
    (when editor
      (abc-utils/pitches-key-and-accidentals
       (-> (let [found (atom nil)]
             (walk/prewalk
              (fn [m]
                (when (and (map? m) (= (get m :el_type) "keySignature"))
                  (reset! found (select-keys m [:accidentals :root])))
                m)
              (js->clj (.-tunes editor') :keywordize-keys true))
             @found)
           ;; Move this over to use tone name and accidental instread of pitch as pitch seems to be wrong
           (assoc :pitches (->> (.-selected (.-engraver (first (.-tunes editor'))))
                                (filter (fn [m] (= "note" (.-type m))))
                                (map (fn [x] (js->clj (.-abcelem x) :keywordize-keys true)))
                                (mapcat :pitches)
                                (vec))))))))

[{:pitch 1, :name "D", :verticalPos 1, :highestVert 7}]

(def add-selected-tones
  (debounce (fn []
              (>evt [::selected-tones (selected-tones)]))
            250))

(defn editor []
  (def editor'
    (new
     abcjs/Editor
     "abc-editor-id"
     (clj->js
      {:canvas_id               "abc-canvas-id"
       :warnings_id             "abc-editor-warnings-id"
       :generate_warnings       true
       :onchange (fn [new-abc-str]
                   (js/console.log "editor onchange" new-abc-str))
       :selectionChangeCallback (fn [selection-start selection-end]
                                  (js/console.log "selectionChangeCallback" selection-start selection-end)
                                  (def selection-start selection-start)
                                  (def selection-end selection-end)
                                  (add-selected-tones))
       :synth                   {:el            "#audio"
                                 :cursorControl (new ->cursor-control-obj
                                                     {:cursor the-cursor})
                                 :options
                                 {:displayRestart  true
                                  :displayPlay     true
                                  :displayProgress true
                                  :displayWarp     true
                                  :displayLoop     true}}
       :abcjsParams             {:responsive "resize"
                                 :tablature
                                 [{:instrument "mandolin"
                                   :tuning     ["G,", "D", "A", "e"]
                                   :capo       0}]}}))))


(defn mark-note-timings []
  (let [pitches (atom #{})]
    (walk/prewalk
     (fn [x]
       (when (and (map? x) (= "note" (get x :cmd)))
         (swap! pitches conj (get x :pitch)))
       x)
     (js->clj (.-noteTimings (first (.-tunes editor'))) :keywordize-keys true))
    (->> @pitches
         (mapv music-theory/->midi-pitch->index-tone-with-octave))))


(comment
  (js/console.log editor')
  (js/console.log (.-lines (first (.-tunes editor'))))
  (js->clj (.-lines (first (.-tunes editor'))) :keywordize-keys true)

  (mark-note-timings)

  (.-noteTimings (first (.-tunes editor')))

  (js->clj (first (.-tunes editor')) :keywordize-keys true)







  (-> (.play (.-synthControl (.-synth editor')))
      (.then (fn [_]
               (.pause (.-synthControl (.-synth editor'))))))

  (js-keys (.-synthControl (.-synth editor')))

  (.-isLoaded (.-synthControl (.-synth editor')))

  (.-tunes editor')

  (.play (.-synthControl (.-synth editor')))
  (.pause (.-synthControl (.-synth editor')))
  (.seek (.-synthControl (.-synth editor')) 0.75 "beats")
  (.seek (.-synthControl (.-synth editor')) 7.0 "beats")
  (.restart (.-synthControl (.-synth editor')))
  (js/console.log (new ->cursor-control-obj))
  :-)


(defn map-matrix-by-y
  "Map over `matrix` applying `f` on each item.
  Going from through the matrix by `y`."
  [f matrix]
  (let [x-length (-> matrix first count)
        y-length (-> matrix count)]
    (loop [matrix' matrix
           x       0
           y       0
           exit?   false]
      (if exit?
        matrix'
        (let [item (get-in matrix [y x])
              [exit? res] (f item)]
          (recur
           (assoc-in matrix' [y x] res)
           (if (= y (dec y-length)) (inc x) x)
           (if (= y (dec y-length)) 0 (inc y))
           (or exit?
               (and (= x (dec x-length))
                    (= y (dec y-length))))))))))


(def mandolin-fretboard-matrix
  (music-theory/create-fretboard-matrix-for-instrument
   :d 13 :mandolin))

(comment




  )

(defn hehe [derp]
  (let [root (:root derp)]
    (loop [[x & rst] (:pitches derp)
           acc       mandolin-fretboard-matrix]
      (if (nil? x)
        acc
        (let [{score-interval-tone :tone score-octave :octave} x]
          (recur
           rst
           (map-matrix-by-y
            (fn [{fretboard-index-tone :tone fretboard-octave :octave :as m}]
              (let [exit? (and
                           (= score-octave fretboard-octave)
                           (contains? fretboard-index-tone score-interval-tone))]
                [exit?
                 (cond-> m
                   exit?
                   (assoc
                    :match? true
                    :root?  (fretboard-index-tone root)
                    :out    (-> score-interval-tone name str/capitalize)))]))
            acc)))))))

(defn fretboard2-matrix [selected-tones]
  (music-theory/fretboard-matrix->fretboard2
   {}
   (hehe selected-tones)))

(comment
  (fretboard2-matrix nil)
  )

(defn abc-editor+canvas+fretboard-component
  [])

(defn ui []
  (r/create-class
   {:component-did-mount  #(editor)
    :component-did-update #(editor)
    :display-name         "editor"
    :reagent-render
    (fn []
      (let [abc-str            (<sub [::abc-str])
            loop-section?      (<sub [::loop-section?])
            loop-section-start (<sub [::loop-section-start])
            loop-section-end   (<sub [::loop-section-end])]
        [:div
         [:div {:style {:border  "dashed #00000026"
                        :display "flex"}}
          [:textarea {:spellCheck false
                      :rows       9
                      :style      {:flex "1"}}
           "Hej och välkommen till din första lektion - Whiskey before breakfast på mandolin.

Detta är en interaktivt läromedel med olika komponenter som kan beskriva teori, instrument och spela media.

En lärare kan komponerar dessa komponenter och sedan publiserar så att en student kan ta del av materialet.

I dagsläget finns det inte så många komponenter men"]]
         [:br]
         [:div {:style {:border "dashed #00000026"}}
          [:div {:style {:display "flex"}}
           [:textarea {:style      {:flex "1"}
                       :id         "abc-editor-id"
                       :cols       "80"
                       :rows       9
                       :spellCheck false
                       :value      (or abc-str "")
                       :onChange   (fn [e]
                                     (let [value (.. e -target -value)]
                                       (js/console.log "Editor change")
                                       (js/console.log value)
                                       (>evt [::abc-str value])))}]]
          [:div {:id "abc-editor-warnings-id"}]

          [:div {:id "abc-canvas-id"}]

          [:br]
          [:div {:id "audio"}]
          [:input {:type     "checkbox"
                   :checked  loop-section?
                   :on-click #(>evt [::loop-section? (not loop-section?)])}]
          [:label "Loop?"]
          [:input {:type      "number" :placeholder "Start"
                   :value     loop-section-start
                   :on-change (fn [e]
                                (>evt [::loop-section-start (.. e -target -value)]))}]
          [:button {:on-click #(>evt [::loop-section-start @current-beat])} "Set start"]
          [:input {:type      "number" :placeholder "End"
                   :value     loop-section-end
                   :on-change (fn [e]
                                (>evt [::loop-section-end (.. e -target -value)]))}]
          [:button {:on-click #(>evt [::loop-section-end @current-beat])} "Set end"]

          [:br]
          [fretboard2/styled-view
           {:id               "akxH4rw4Y682ySSDUo2AEm"
            :fretboard-matrix (fretboard2-matrix (<sub [::selected-tones]))
            :fretboard-size   1}]]


         [:br]

         [:div {:style {:border "dashed #00000026"}}
          [:div {:style {:display         "flex"
                         :align-items     "center"
                         :justify-content "center"}}
           [:div
            [:p "Här finns en inspelning som lärraren gjort"]
            [:audio {:controls true
                     :src      "https://staglaberget-string-band.se/resources/media/whiskey-2025-02-19.mp3"}]]]]

         [:br]

         [:div {:style {:border "dashed #00000026"}}
          [:div {:style {:display         "flex"
                         :align-items     "center"
                         :justify-content "center"}}
           [:div
            [:p "Här är en länk till en YouTube video"]
            [:iframe {:src             "https://www.youtube.com/watch?v=VtxdaAui4tw"
                      :frameborder     "0"
                      :allow           "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                      :allowFullScreen true}]]]]

         [:br]


         [:div {:style {:border "dashed #00000026"}}
          [:div {:style {:display         "flex"
                         :align-items     "center"
                         :justify-content "center"}}
           [:div
            [:p "Läraren kan lägga till en bild och skriva en text till"]
            [:img {:style {:max-width  "100%"
                           :max-height "100%"}
                   :src   "https://online.berklee.edu/takenote/wp-content/uploads/2023/02/circle_of_fifths_article_image_2023.png"}]]]]]))}))



(rf/reg-fx
 ::play-and-pause
 (fn []
   #_(js/console.log "::play-and-pause")
   #_(-> (.play (.-synthControl (.-synth editor')))
       (.then (fn [_]
                (.pause (.-synthControl (.-synth editor'))))))))

(rf/reg-event-fx
 ::start
 (fn [{:keys [db]} [_event-id]]
   {::play-and-pause nil
    :fx [[:dispatch [::abc-str whiskey-abc]]
         [:dispatch [::loop-section-start 0.75]]
         [:dispatch [::loop-section-end 8]]]}))

(defn routes [deps]
  (let [route-name :abcjs-example/editor-with-play-and-fretboard]
    ["/abcjs/examples/editor-with-play-and-fretboard"
     {:name    route-name
      :view    [ui]
      :top-nav :abcjs-examples
      :controllers [{:start #(>evt [::start])}]
      }]))

(comment
  (>evt [::abc-str whiskey-abc])
  (<sub [::abc-str])
  :-)



(comment
  (let [canvas-cursor (create-cursor)]
    {:component-version                {:version 1 :type :abc-editor}
     :component-identifier             "OYRGxhOybwzwAzOMiNl0ds"
     :tab-instrument                   {:instrument "mandolin"
                                        :tuning     ["G,", "D", "A", "e"]
                                        :capo       0}
     :synth-controller                 (->cursor-control-obj {:cursor canvas-cursor})
     :editor-onchange                  (fn [new-abc-str]
                                         (js/console.log "new-abc-str:" new-abc-str))
     :editor-selection-change-callback (fn [selection-start selection-end]
                                         (js/console.log "selectionChangeCallback" selection-start selection-end)
                                         (def selection-start selection-start)
                                         (def selection-end selection-end))
     :editor-selected-tones            []
     :loop-section-start               nil
     :loop-section-end                 nil
     :loop-section?                    false
     :current-beat                     0
     :abc-str-errors                   nil
     :abc-str                          whiskey-abc
     :bpm                              50
     :canvas-cursor                    canvas-cursor
     :editor-dom-id                    "OYRGxhOybwzwAzOMiNl0ds-editor-dom-id"
     :canvas-dom-id                    "OYRGxhOybwzwAzOMiNl0ds-canvas-dom-id"
     :warnings-dom-id                  "OYRGxhOybwzwAzOMiNl0ds-warnings-dom-id"
     :synth-controller-dom-id          "OYRGxhOybwzwAzOMiNl0ds-audio-control-dom-id"})
  )
