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
            [se.jherrlin.music-theory.webapp.views.instruments.fretboard2 :as fretboard2]))

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

;; (defn ->abcjs-editor
;;   [{:keys [editor-dom-id
;;            warnings-dom-id
;;            audio-control-dom-id
;;            on-change
;;            selection-change-callback

;;            ]}])

;; (defn abc-component
;;   [{:keys [dom-identifier
;;            instrument
;;            editor-selected-tones
;;            seek-start
;;            seek-end
;;            seek?
;;            current-beat
;;            abc-str-errors
;;            abc-str
;;            bpm]}]
;;   (r/with-let [abcjs-editor]
;;     (r/create-class
;;      {:component-did-mount  #()
;;       :component-did-update #(editor)
;;       :display-name         (str dom-identifier "abc-component")
;;       :reagent-render
;;       (fn []
;;         [])}))
;;   )


(declare editor')

(comment
  (js/console.log editor')
  )


(defonce seek-state (atom {:start nil
                           :end   nil}))

@seek-state

(defonce current-beat (atom 0))


(def whiskey-abc
  "X: 1
T: Whiskey Before Breakfast
M: 4/4
L: 1/8
K: D
P:A
DD |: \"D\" AEFG A2 FG | ^ABAG F_DEF  | \"G\" GABG \"D\" F2 AF | \"A\" EDEA cefe |
   |  \"D\" ae ge fe ed | dA =cA BA FD  | \"G\" GABG \"D\" F2 AF  \\
   |1 \"A\" EDEF \"D\" D2 DD :|2 \"A\" EDEF \"D\" D2 FE ||")

(def cherokee-abc
  "X: 1
T: Cherokee Shuffle
M: 4/4
L: 1/8
K: D
P:A
DD |: \"D\" AEFG A2 FG |")

(def events-
  [{:n ::abc-str}
   {:n ::selected-tones}])

(doseq [{:keys [n s e d]} events-]
  (rf/reg-sub n (or s (fn [db [n']] (get db n' d))))
  (rf/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))

(defn move-cursor! [cursor {:keys [x1 x2 y1 y2]}]
  (.setAttribute cursor "x1" x1)
  (.setAttribute cursor "x2" x2)
  (.setAttribute cursor "y1" y1)
  (.setAttribute cursor "y2" y2))

(defn create-cursor []
  (let [svg    (js/document.querySelector "#abc-canvas-id svg")
        cursor (js/document.createElementNS "http://www.w3.org/2000/svg" "line")]
    (.setAttribute cursor "class" "abcjs-cursor")
    (.setAttributeNS cursor nil, "x1", 0)
    (.setAttributeNS cursor nil, "x1", 0)
    (.setAttributeNS cursor nil, "y1", 0)
    (.setAttributeNS cursor nil, "x2", 0)
    (.setAttributeNS cursor nil, "y2", 0)
    ;; (.appendChild svg cursor)
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
              (if (= (:end @seek-state) beatNumber)
                (.seek (.-synthControl (.-synth editor')) (:start @seek-state) "beats"))
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

                 (->> (js->clj (.-midiPitches event) :keywordize-keys true)
                      (map :pitch))

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
              (js->clj (.-tunes editor) :keywordize-keys true))
             @found)
           (assoc :pitches (->> (.-selected (.-engraver (first (.-tunes editor))))
                                (filter (fn [m] (= "note" (.-type m))))
                                (map (fn [x] (js->clj (.-abcelem x) :keywordize-keys true)))
                                (mapcat :pitches)
                                (vec))))))))

(def add-selected-tones
  (debounce (fn []
              (>evt [::selected-tones (selected-tones)]))
            500))

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


(comment
  (js/console.log editor')

  (js->clj (first (.-tunes editor')) :keywordize-keys true)

  (js->clj (.-noteTimings (first (.-tunes editor'))) :keywordize-keys true)


  (-> (.play (.-synthControl (.-synth editor')))
      (.then (fn [_]
               (.pause (.-synthControl (.-synth editor'))))))

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
   8 :mandolin))

(defn hehe [derp]
  (let [root (:root derp)]
    (loop [[x & rst] (:pitches derp)
           acc       mandolin-fretboard-matrix]
      (if (nil? x)
        acc
        (let [{score-interval-tone :tone score-octav :octav} x]
          (recur
           rst
           (map-matrix-by-y
            (fn [{fretboard-index-tone :tone fretboard-octave :octave :as m}]
              (let [exit? (and
                           (= score-octav fretboard-octave)
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

(defn ui []
  (r/create-class
   {:component-did-mount  #(editor)
    :component-did-update #(editor)
    :display-name         "editor"
    :reagent-render
    (fn []
      (let [abc-str (<sub [::abc-str])]
        [:div
         [:p "Editor with play example!"]
         (let [url "https://github.com/paulrosen/abcjs/blob/main/examples/printable.html"]
           [:p
            [:a {:href url} url]])
         [:div {:style {:display "flex"}}
          [:textarea {:style      {:flex "1"}
                      :id         "abc-editor-id"
                      :cols       "80"
                      :rows       12
                      :spellCheck false
                      :value      (or abc-str "")
                      :onChange   (fn [e]
                                    (let [value (.. e -target -value)]
                                      (js/console.log "Editor change")
                                      (js/console.log value)
                                      (>evt [::abc-str (clojure.string/trim value)])))}]]
         [:div {:id "abc-editor-warnings-id"}]
         [:div {:id "audio"}]
         [:div {:id "abc-canvas-id"}]
         [:button {:on-click #(swap! seek-state assoc :start @current-beat)} "Seek start"]
         [:button {:on-click #(swap! seek-state assoc :end @current-beat)} "Seek end"]
         [:hr]
         [:button {:on-click #(>evt [::abc-str whiskey-abc])} "Whiskey"]
         [:button {:on-click #(>evt [::abc-str cherokee-abc])} "Cherokee"]
         [:hr]
         [:br]
         [fretboard2/styled-view
          {:id               "akxH4rw4Y682ySSDUo2AEm"
           :fretboard-matrix (fretboard2-matrix (<sub [::selected-tones]))
           :fretboard-size   1}]]))}))

(defn routes [deps]
  (let [route-name :abcjs-example/editor-with-play-and-fretboard]
    ["/abcjs/examples/editor-with-play-and-fretboard"
     {:name    route-name
      :view    [ui]
      :top-nav :abcjs-examples}]))

(comment
  (>evt [::abc-str whiskey-abc])
  (<sub [::abc-str])
  :-)
