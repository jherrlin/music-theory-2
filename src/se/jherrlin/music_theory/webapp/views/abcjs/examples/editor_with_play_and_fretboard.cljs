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

(declare editor')

(->> (map
      (fn [mini-nr index-tone octave]
        [mini-nr {:index-tone index-tone :octave octave}])
      (range 24 (+ 24 96))
      (take 96 (cycle [#{:c}
                       #{:db :c#}
                       #{:d}
                       #{:d# :eb}
                       #{:e}
                       #{:f}
                       #{:gb :f#}
                       #{:g}
                       #{:g# :ab}
                       #{:a}
                       #{:bb :a#}
                       #{:b}]))
      (concat
       (take 12 (repeat 1))
       (take 12 (repeat 2))
       (take 12 (repeat 3))
       (take 12 (repeat 4))
       (take 12 (repeat 5))
       (take 12 (repeat 6))
       (take 12 (repeat 7))
       (take 12 (repeat 8))))
     (into {}))

(def pitch->tone
  {65  {:index-tone #{:f}, :octave 4},
   70  {:index-tone #{:bb :a#}, :octave 4},
   62  {:index-tone #{:d}, :octave 4},
   74  {:index-tone #{:d}, :octave 5},
   110 {:index-tone #{:d}, :octave 8},
   59  {:index-tone #{:b}, :octave 3},
   86  {:index-tone #{:d}, :octave 6},
   72  {:index-tone #{:c}, :octave 5},
   58  {:index-tone #{:bb :a#}, :octave 3},
   60  {:index-tone #{:c}, :octave 4},
   27  {:index-tone #{:d# :eb}, :octave 1},
   69  {:index-tone #{:a}, :octave 4},
   101 {:index-tone #{:f}, :octave 7},
   24  {:index-tone #{:c}, :octave 1},
   102 {:index-tone #{:gb :f#}, :octave 7},
   55  {:index-tone #{:g}, :octave 3},
   85  {:index-tone #{:db :c#}, :octave 6},
   39  {:index-tone #{:d# :eb}, :octave 2},
   88  {:index-tone #{:e}, :octave 6},
   46  {:index-tone #{:bb :a#}, :octave 2},
   77  {:index-tone #{:f}, :octave 5},
   106 {:index-tone #{:bb :a#}, :octave 7},
   119 {:index-tone #{:b}, :octave 8},
   95  {:index-tone #{:b}, :octave 6},
   54  {:index-tone #{:gb :f#}, :octave 3},
   92  {:index-tone #{:g# :ab}, :octave 6},
   104 {:index-tone #{:g# :ab}, :octave 7},
   48  {:index-tone #{:c}, :octave 3},
   50  {:index-tone #{:d}, :octave 3},
   116 {:index-tone #{:g# :ab}, :octave 8},
   75  {:index-tone #{:d# :eb}, :octave 5},
   99  {:index-tone #{:d# :eb}, :octave 7},
   31  {:index-tone #{:g}, :octave 1},
   113 {:index-tone #{:f}, :octave 8},
   32  {:index-tone #{:g# :ab}, :octave 1},
   40  {:index-tone #{:e}, :octave 2},
   91  {:index-tone #{:g}, :octave 6},
   117 {:index-tone #{:a}, :octave 8},
   108 {:index-tone #{:c}, :octave 8},
   56  {:index-tone #{:g# :ab}, :octave 3},
   33  {:index-tone #{:a}, :octave 1},
   90  {:index-tone #{:gb :f#}, :octave 6},
   109 {:index-tone #{:db :c#}, :octave 8},
   36  {:index-tone #{:c}, :octave 2},
   41  {:index-tone #{:f}, :octave 2},
   118 {:index-tone #{:bb :a#}, :octave 8},
   89  {:index-tone #{:f}, :octave 6},
   100 {:index-tone #{:e}, :octave 7},
   43  {:index-tone #{:g}, :octave 2},
   61  {:index-tone #{:db :c#}, :octave 4},
   29  {:index-tone #{:f}, :octave 1},
   44  {:index-tone #{:g# :ab}, :octave 2},
   93  {:index-tone #{:a}, :octave 6},
   111 {:index-tone #{:d# :eb}, :octave 8},
   28  {:index-tone #{:e}, :octave 1},
   64  {:index-tone #{:e}, :octave 4},
   103 {:index-tone #{:g}, :octave 7},
   51  {:index-tone #{:d# :eb}, :octave 3},
   25  {:index-tone #{:db :c#}, :octave 1},
   34  {:index-tone #{:bb :a#}, :octave 1},
   66  {:index-tone #{:gb :f#}, :octave 4},
   107 {:index-tone #{:b}, :octave 7},
   47  {:index-tone #{:b}, :octave 2},
   35  {:index-tone #{:b}, :octave 1},
   82  {:index-tone #{:bb :a#}, :octave 5},
   76  {:index-tone #{:e}, :octave 5},
   97  {:index-tone #{:db :c#}, :octave 7},
   57  {:index-tone #{:a}, :octave 3},
   68  {:index-tone #{:g# :ab}, :octave 4},
   115 {:index-tone #{:g}, :octave 8},
   112 {:index-tone #{:e}, :octave 8},
   83  {:index-tone #{:b}, :octave 5},
   45  {:index-tone #{:a}, :octave 2},
   53  {:index-tone #{:f}, :octave 3},
   78  {:index-tone #{:gb :f#}, :octave 5},
   26  {:index-tone #{:d}, :octave 1},
   81  {:index-tone #{:a}, :octave 5},
   79  {:index-tone #{:g}, :octave 5},
   38  {:index-tone #{:d}, :octave 2},
   98  {:index-tone #{:d}, :octave 7},
   87  {:index-tone #{:d# :eb}, :octave 6},
   30  {:index-tone #{:gb :f#}, :octave 1},
   73  {:index-tone #{:db :c#}, :octave 5},
   96  {:index-tone #{:c}, :octave 7},
   105 {:index-tone #{:a}, :octave 7},
   52  {:index-tone #{:e}, :octave 3},
   114 {:index-tone #{:gb :f#}, :octave 8},
   67  {:index-tone #{:g}, :octave 4},
   71  {:index-tone #{:b}, :octave 4},
   42  {:index-tone #{:gb :f#}, :octave 2},
   80  {:index-tone #{:g# :ab}, :octave 5},
   37  {:index-tone #{:db :c#}, :octave 2},
   63  {:index-tone #{:d# :eb}, :octave 4},
   94  {:index-tone #{:bb :a#}, :octave 6},
   49  {:index-tone #{:db :c#}, :octave 3},
   84  {:index-tone #{:c}, :octave 6}})

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
                      (map (comp #(get pitch->tone %) :pitch)))

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
            1000))

(def deb (debounce (fn []
                     (js/console.log "Hejsan"))
                   2000))
(comment
  (deb)
  :-)



(defn editor []
  (def editor'
    (new
     abcjs/Editor
     "abc-editor-id"
     (clj->js
      {:canvas_id               "abc-canvas-id"
       :warnings_id             "abc-editor-warnings-id"
       :generate_warnings       true
       :onchange (fn [a b c]
                   (js/console.log "editor onchange" a b c))
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
       :abcjsParams             {:tablature
                                 [{:instrument "mandolin"
                                   :tuning     ["G,", "D", "A", "e"]
                                   :capo       0}]}}))))


(comment
  (js/console.log editor')

  (js->clj (first (.-tunes editor')) :keywordize-keys true)

  (js->clj (.-noteTimings (first (.-tunes editor'))) :keywordize-keys true)


  (.play (.-synthControl (.-synth editor')))
  (.seek (.-synthControl (.-synth editor')) 7.0 "beats")
  (.pause (.-synthControl (.-synth editor')))
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
      (let [abc-str (<sub [::abc-str])
            ]
        [:div
         [:p "Editor with play example"]
         (let [url "https://github.com/paulrosen/abcjs/blob/main/examples/printable.html"]
           [:p
            [:a {:href url} url]])
         [:textarea {:id         "abc-editor-id"
                     :cols       "80"
                     :rows       12
                     :spellCheck false
                     :value      (or abc-str "")
                     :onChange   (fn [e]
                                   (let [value (.. e -target -value)]
                                     (js/console.log "Editor change")
                                     (js/console.log value)
                                     (>evt [::abc-str (clojure.string/trim value)])))}]
         [:div {:id "abc-editor-warnings-id"}]
         [:div {:id "audio"}]
         [:div {:id "abc-canvas-id"}]
         [:button {:on-click #(swap! seek-state assoc :start @current-beat)} "Seek start"]
         [:button {:on-click #(swap! seek-state assoc :end @current-beat)} "Seek end"]
         [:hr]
         [:button {:on-click #(>evt [::abc-str whiskey-abc])} "Whiskey"]
         [:button {:on-click #(>evt [::abc-str cherokee-abc])} "Cherokee"]
         [:button {:on-click (fn [_] (deb))} "Bounce"]
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
