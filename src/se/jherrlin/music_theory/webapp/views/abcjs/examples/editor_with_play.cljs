(ns se.jherrlin.music-theory.webapp.views.abcjs.examples.editor-with-play
  (:require ["abcjs" :as abcjs]
            [reagent.core :as r]
            [re-frame.alpha :as rf]
            [clojure.walk :as walk]
            [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
            [goog.functions :refer [debounce]]))


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
  [{:n ::abc-str}])

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

(defn ->cursor-control-obj [{:keys [cursor]}]
  (js-obj
   "onReady" (fn [synthController]
               (let [svg (js/document.querySelector "#abc-canvas-id svg")]
                 (def synthController synthController)
                 (js/console.log "onReady" synthController)
                 (.appendChild svg cursor)))
   "onStart" (fn []
               (js/console.log "onStart"))
   "onFinished" (fn []
                  (js/console.log "onFinished"))
   "onBeat" (fn [beatNumber totalBeats totalTime position]
              (move-cursor! cursor {:x1 (- (.-left position) 2)
                                    :x2 (- (.-left position) 2)
                                    :y1 (.-top position)
                                    :y2 (+ (.-top position) (.-height position))})
              (js/console.log "onBeat"))
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
                 (js/console.log "onEvent" event)))))


(def the-cursor (create-cursor))

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
                                  (def selection-end selection-end))
       :synth                   {:el            "#audio"
                                 :cursorControl (new ->cursor-control-obj
                                                     {:cursor the-cursor})
                                 :options
                                 {:displayRestart  true
                                  :displayPlay     true
                                  :displayProgress true
                                  :displayWarp     true}}
       :abcjsParams             {:tablature
                                 [{:instrument "mandolin"
                                   :tuning     ["G,", "D", "A", "e"]
                                   :capo       0}]}}))))

(comment
  (js->clj editor')

  ;; Find marked pitches
  (->> (.-selected (.-engraver (first (.-tunes editor'))))
       (filter (fn [m] (= "note" (.-type m))))
       (map (fn [x] (js->clj (.-abcelem x) :keywordize-keys true)))
       (mapcat :pitches))

  ;; Find root and accidentals
  ;; This only works for songs that doesn't change key
  (let [found (atom nil)]
    (walk/prewalk
     (fn [m]
       (when (and (map? m) (= (get m :el_type) "keySignature"))
         (reset! found (select-keys m [:accidentals :root])))
       m)
     (js->clj (.-tunes editor') :keywordize-keys true))
    @found)

  (.play (.-synthControl (.-synth editor')))
  (.pause (.-synthControl (.-synth editor')))
  (.restart (.-synthControl (.-synth editor')))
  (js/console.log (new ->cursor-control-obj))
  :-)


(def deb (debounce (fn []
                     (js/console.log "Hejsan"))
                   2000))
(comment
  (deb)
  :-)

(defn ui []
  (r/create-class
   {:component-did-mount  #(editor)
    :component-did-update #(editor)
    :display-name         "editor"
    :reagent-render
    (fn []
      (let [abc-str (<sub [::abc-str])]
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
         [:hr]
         [:button {:on-click #(>evt [::abc-str whiskey-abc])} "Whiskey"]
         [:button {:on-click #(>evt [::abc-str cherokee-abc])} "Cherokee"]
         [:button {:on-click (fn [_] (deb))} "Bounce"]]))}))

(defn routes [deps]
  (let [route-name :abcjs-example/editor-with-play]
    ["/abcjs/examples/editor-with-play"
     {:name    route-name
      :view    [ui]
      :top-nav :abcjs-examples}]))

(comment
  (>evt [::abc-str whiskey-abc])
  (<sub [::abc-str])
  :-)
