(ns se.jherrlin.music-theory.webapp.views.abcjs.examples.editor-component
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


(def app-db-path ::derp)

(defn path [x]
  (vec (flatten [app-db-path x])))

(defn derived-data [k component-identifier]
  (path [:derived component-identifier k]))

(def components-path (path ::components))
(def components-index-path (path ::components-index))
(def editors-path (path ::editors))

(def key-of-path           (partial derived-data ::key-of))
(def signature-path        (partial derived-data ::signature))
(def editor-path           (partial derived-data ::editor))
(def editor-loaded?-path   (partial derived-data ::editor-loaded?))
(def index-tones-used-in-abc-path (partial derived-data ::index-tones-used-in-abc))


(rf/reg-sub
 ::abc-str
 (fn [db [_sub-id component-identifier]]
   (let [component-idx (get-in db (conj components-index-path component-identifier))]
     (get-in db (conj components-path component-idx :abc-str)))))

(rf/reg-sub
 ::abc-editor-rows
 (fn [db [_sub-id component-identifier]]
   (let [component-idx (get-in db (conj components-index-path component-identifier))
         rows          (some-> (get-in db (conj components-path component-idx :abc-str))
                               (str/split-lines)
                               (count))]
     (if (< rows 5) 5 (inc rows)))))

(rf/reg-event-db
 ::abc-str
 (fn [db [_event-id component-identifier abc-str]]
   (let [component-idx (get-in db (conj components-index-path component-identifier))]
     (assoc-in db (conj components-path component-idx :abc-str) abc-str))))

(rf/reg-sub
 ::editor
 (fn [db [_sub-id component-identifier]]
   (get-in db (editor-path component-identifier))))

(rf/reg-sub
 ::editor-is-loaded?
 (fn [db [_sub-id component-identifier]]
   (get-in db (editor-loaded?-path component-identifier))))

(rf/reg-event-db
 ::editor
 (fn [db [_event-id component-identifier editor]]
   (let [is-loaded? (.-isLoaded (.-synthControl (.-synth editor)))]
     (-> db
         (assoc-in (editor-path component-identifier) editor)
         (assoc-in (editor-loaded?-path component-identifier) is-loaded?)))))

(rf/reg-fx
 ::editor-go!
 (fn [{:keys [component-identifier editor]}]
   (-> (.runWhenReady
        (.-synthControl (.-synth editor))
        #(js/Promise.resolve
          (>evt [::editor component-identifier editor]))
        true)
       (.then #(js/console.log "loaded"))
       (.catch #(js/console.log "error")))))

(def audio-context
  (or js/window.AudioContext
      js/window.webkitAudioContext
      js/navigator.mozAudioContext
      js/navigator.msAudioContext))

(defn set-audio-context! [audio-context]
  (set! js/window.AudioContext audio-context))

(set-audio-context! audio-context)

(rf/reg-event-fx
 ::activate-editor!
 (fn [{:keys [db]} [_event-id component-identifier]]
   (js/console.log ::activate-editor!)
   (let [editor (get-in db (editor-path component-identifier))]
     {::editor-go! {:component-identifier component-identifier
                    :editor               editor}})))

(def whiskey-abc
  "X: 1                                                                       \nT: Wagon Wheel, mandolin solo                                              \nM: 4/4                                                                     \nL: 1/8                                                                     \nK: A                                                                       \nP: A                                                                       \n|: \"A\" A,2A,A, B,CEF | \"E\" E2EE GFEF | \"F#m\" F2FF ABcB  | \"D\" d2ef abaf |  \n|  \"A\" a2aa    fecB  | \"E\" B2BB cBGF | \"D\"   D2F2 dBAB  | \"D\" dABA A4 |    ")


(defn move-cursor! [cursor {:keys [x1 x2 y1 y2]}]
  (.setAttribute cursor "x1" x1)
  (.setAttribute cursor "x2" x2)
  (.setAttribute cursor "y1" y1)
  (.setAttribute cursor "y2" y2))

(defn create-cursor [canvas-cursor-dom-id]
  (let [cursor (js/document.createElementNS "http://www.w3.org/2000/svg" "line")]
    (.setAttribute cursor "class" canvas-cursor-dom-id)
    (.setAttributeNS cursor nil, "x1", 0)
    (.setAttributeNS cursor nil, "x1", 0)
    (.setAttributeNS cursor nil, "y1", 0)
    (.setAttributeNS cursor nil, "x2", 0)
    (.setAttributeNS cursor nil, "y2", 0)
    cursor))

(def fretboard-checked-state (atom []))

(defn unmark-and-mark-fretboard-tone! [component-identifier pitch-nr]
  ;; Remove old markers
  (let [elements @fretboard-checked-state]
    (.forEach
     (js/Array.from elements)
     (fn [el]
       (set! (.-backgroundColor (.-style el)) "rgb(255, 165, 0)"))))

  ;; Add new markers
  (reset!
   fretboard-checked-state
   (.map
    (js/Array.from
     (.querySelectorAll
      (js/document.querySelector
       (str "#" component-identifier))
      (str "." component-identifier "-" pitch-nr "-center-text-div")))
    (fn [el]
      (set! (.-backgroundColor (.-style el)) "green")
      el))))

(defn ->cursor-control-obj [{:keys [cursor canvas-dom-id component-identifier]}]
  (js-obj
   "onReady" (fn [synthController]
               (let [svg (js/document.querySelector (str "#" canvas-dom-id " svg"))]
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
                                    :y2 (+ (.-top position) (.-height position))}))
   "onEvent" (fn [event]
               (js/console.log "onEvent" event)
               (doseq [n (->> (js->clj (.-midiPitches event) :keywordize-keys true)
                              (map :pitch))]
                 (unmark-and-mark-fretboard-tone! component-identifier n)))))

(rf/reg-event-fx
 ::abc-after-parsing
 (fn [{:keys [db]} [_event-id {:keys [tune tune-number abc-string component-identifier]}]]
   (let [signature (-> (.getKeySignature tune)
                       (js->clj  :keywordize-keys true)
                       (select-keys [:accidentals :root :acc :mode]))
         key-of    (some-> signature :root clojure.string/lower-case keyword)]
     {:db (-> db
              (assoc-in (key-of-path    component-identifier) key-of)
              (assoc-in (signature-path component-identifier) signature))
      :fx [[:dispatch [::index-tones-used-in-abc component-identifier]]]})))

(rf/reg-event-fx
 ::index-tones-used-in-abc
 (fn [{:keys [db]} [_event-id component-identifier]]
   (js/console.log ::index-tones-used-in-abc)
   (let [editor             (get-in db (editor-path component-identifier))
         index-tones+octave (some->> (js->clj (.-noteTimings (first (.-tunes editor))) :keywordize-keys true)
                                     (mapcat :midiPitches)
                                     (filter (comp #{"note"} :cmd))
                                     (map :pitch)
                                     (into #{})
                                     (mapv music-theory/->midi-pitch->index-tone-with-octave))]
     {:db (cond-> db
            index-tones+octave (assoc-in (index-tones-used-in-abc-path component-identifier) index-tones+octave))})))

(rf/reg-sub
 ::index-tones-used-in-abc
 (fn [db [_sub-id component-identifier]]
   (get-in db (index-tones-used-in-abc-path component-identifier))))

(defn new-editor!
  [{:keys [component-identifier
           editor-dom-id canvas-dom-id warnings-dom-id synth-controller-dom-id
           synth-controller
           tab-instrument]}]
  (new
   abcjs/Editor
   editor-dom-id
   (clj->js
    {:canvas_id               canvas-dom-id
     :warnings_id             warnings-dom-id
     :generate_warnings       true
     :onchange                (fn [new-abc-str]
                                (js/console.log "editor onchange" new-abc-str))
     :selectionChangeCallback (fn [selection-start selection-end]
                                (js/console.log "selectionChangeCallback" selection-start selection-end))
     :synth                   {:el            (str "#" synth-controller-dom-id)
                               :cursorControl synth-controller
                               :options
                               {:displayRestart  true
                                :displayPlay     true
                                :displayProgress true
                                :displayWarp     true
                                :displayLoop     true}}
     :abcjsParams             {:responsive    "resize"
                               :tablature     tab-instrument
                               :afterParsing  (fn [tune, tuneNumber, abcString]
                                                (js/console.log
                                                 "afterParsing"
                                                 tune, tuneNumber, abcString)
                                                (>evt [::abc-after-parsing
                                                       {:tune                 tune
                                                        :tune-number          tuneNumber
                                                        :abc-string           abcString
                                                        :component-identifier component-identifier}]))
                               :clickListener (fn [abcelem tuneNumber classes analysis drag]
                                                (js/console.log
                                                 "clickListener"
                                                 abcelem tuneNumber classes analysis drag))}})))

(defn ->component [{:keys [component-identifier component-version]}]
  (case (:type component-version)
    :abc-editor
    (let [canvas-cursor-dom-id (str component-identifier "-canvas-cursor-dom-id")
          canvas-dom-id        (str component-identifier "-canvas-dom-id")
          canvas-cursor        (create-cursor canvas-cursor-dom-id)]
      {:synth-controller        (new ->cursor-control-obj
                                     {:cursor               canvas-cursor
                                      :canvas-dom-id        canvas-dom-id
                                      :component-identifier component-identifier})
       :editor-onchange         (fn [new-abc-str]
                                  ;; This is not how ABC string is updated. But
                                  ;; could be a good place for callbacks.
                                  (js/console.log "new-abc-str:" new-abc-str))
       :canvas-cursor           canvas-cursor
       :editor-dom-id           (str component-identifier "-editor-dom-id")
       :canvas-dom-id           canvas-dom-id
       :canvas-cursor-dom-id    canvas-cursor-dom-id
       :warnings-dom-id         (str component-identifier "-warnings-dom-id")
       :synth-controller-dom-id (str component-identifier "-audio-control-dom-id")})))

(rf/reg-fx
 ::new-editor!
 (fn [{:keys [component-identifier] :as args}]
   (let [editor (new-editor! args)]
     ;; If user have clicked somewhere on the page it's possible to create a
     ;; sound context. ABCjs needs that sound context for providing the correct
     ;; pitches and we use those to visualize the fretboard data.
     (>evt [::editor component-identifier editor])
     (when-not (= (.-state (new js/window.AudioContext)) "suspended")
       (-> (.runWhenReady
            (.-synthControl (.-synth editor))
            #(js/Promise.resolve
              (>evt [::editor component-identifier editor]))
            nil)
           (.then #(js/console.log "then" %))
           (.catch #(js/console.log "error" %)))))))

(rf/reg-event-fx
 ::create-new-editor!
 (fn [{:keys [db]} [_event-id args]]
   {:fx [[::new-editor! args]]}))

(def mandolin-fretboard-matrix
  (music-theory/create-fretboard-matrix-for-instrument
   :d 13 :mandolin))

(defn fretboard2-matrix [index-tones-used-in-abc]
  (music-theory/fretboard-matrix->fretboard2
   {}
   (let [m         (->> mandolin-fretboard-matrix
                        (apply concat)
                        (group-by
                         (juxt :index-tone :octave))
                        (map (fn [[[index-tone octave] frets]]
                               [{:index-tone index-tone :octave octave}
                                (->> frets (sort-by :x) first)]))
                        (into {}))
         frets     (->> index-tones-used-in-abc
                        (map #(get m % (assoc % :not-found true))))
         not-found (filter :not-found frets)
         found     (remove :not-found frets)]
  (reduce
   (fn [acc {:keys [x y interval-tone]}]
     (-> acc
         (assoc-in [y x :out] interval-tone)
         (assoc-in [y x :match?] true)))
   mandolin-fretboard-matrix
   found))))

(defn v1-abc-editor-component
  [{:keys [component-identifier component-version
           editor-dom-id canvas-dom-id warnings-dom-id synth-controller-dom-id]
    :as   args}]
  (r/create-class
   {:component-did-mount #(>evt [::create-new-editor! args])
    :display-name        (str "v1-abc-editor-component" component-identifier)
    :reagent-render
    (fn []
      (let [abc-str                 (<sub [::abc-str component-identifier])
            abc-editor-rows         (<sub [::abc-editor-rows component-identifier])
            editor-is-loaded?       (<sub [::editor-is-loaded? component-identifier])
            index-tones-used-in-abc (<sub [::index-tones-used-in-abc component-identifier])]
        [:div
         [:p (if editor-is-loaded? "Active" "Not active")]
         [:button {:on-click #(>evt [::activate-editor! component-identifier])}
          "Activate"]
         [:div {:style {:display "flex"}}
          [:textarea {:style      {:flex "1"}
                      :id         editor-dom-id
                      :rows       abc-editor-rows
                      :spellCheck false
                      :value      (or abc-str "")
                      :onChange   (fn [e]
                                    (let [value (.. e -target -value)]
                                      (>evt [::abc-str component-identifier value])))}]]

         [:div {:id warnings-dom-id}]

         [:div {:id canvas-dom-id}]

         [:div {:id synth-controller-dom-id}]

         [fretboard2/styled-view
          {:id               component-identifier
           :fretboard-matrix (fretboard2-matrix index-tones-used-in-abc)
           :fretboard-size   1}]]))}))

(comment
  (def db @re-frame.db/app-db)
  )

(rf/reg-sub
 ::components
 (fn [db [_sub-id]]
   (->> (get-in db components-path)
        (mapv (fn [{:keys [component-version] :as c}]
                (if (= component-version {:version 1 :type :abc-editor})
                  (merge c (->component c))
                  c))))))

(rf/reg-event-db
 ::components
 (fn [db [_event-id components]]
   (let [components-index (->> components
                               (map-indexed vector)
                               (map (fn [[idx {:keys [component-identifier]}]]
                                      [component-identifier idx]))
                               (into {}))]
     (-> db
         (assoc-in components-path components)
         (assoc-in components-index-path components-index)))))

(defn v1-h2-title
  [{:keys [text]}]
  [:h2 text])

(defn v1-textarea
  [{:keys [text]}]
  [:div {:style {:flex 1}}
   [:textarea {:defaultValue text}]])

(defn view []
  [:<>
   (for [component (<sub [::components])]
     (case (:component-version component)
       {:version 1, :type :abc-editor}
       (with-meta
         [v1-abc-editor-component component]
         {:key (str (:component-identifier component) "-parent")})

       {:version 1 :type :h2-title}
       (with-meta
         [v1-h2-title component]
         {:key (str (:component-identifier component) "-parent")})

       {:version 1 :type :textarea}
       (with-meta
         [v1-textarea component]
         {:key (str (:component-identifier component) "-parent")})

       [:div "Unsupported component"]))])

(rf/reg-event-fx
 ::start
 (fn [{:keys [db]} [_event-id]]
   (let [components [
                     ;; {:component-version    {:version 1 :type :h2-title}
                     ;;  :component-identifier (random-uuid)
                     ;;  :text                 "Whiskey before breakfast lesson"}
                     ;; {:component-version    {:version 1 :type :textarea}
                     ;;  :component-identifier (random-uuid)
                     ;;  :text                 "hejsan"}
                     {:component-version    {:version 1 :type :abc-editor}
                      :component-identifier #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"
                      :tab-instrument       [{:instrument "mandolin"
                                              :tuning     ["G,", "D", "A", "e"]
                                              :capo       0}]
                      :abc-str              whiskey-abc}
                     #_{:component-version    {:version 1 :type :abc-editor}
                        :component-identifier #uuid "3527b219-92fc-45ee-b950-22a88746d36d"
                        :tab-instrument       []
                        :abc-str              whiskey-abc}]]
     {:fx [[:dispatch
            [::components components]]]})))

(defn routes [deps]
  (let [route-name :abcjs-example/editor-component]
    ["/abcjs/examples/editor-component"
     {:name        route-name
      :view        [view]
      :top-nav     :abcjs-examples
      :controllers [{:start #(>evt [::start])}]}]))

(comment
  (>evt [::abc-str whiskey-abc])
  (<sub [::abc-str])
  :-)



(comment



  (get @re-frame.db/app-db app-db-path)
  (get-in @re-frame.db/app-db components-path)
  (get-in @re-frame.db/app-db components-index-path)

  (<sub [::components])

  (>evt [::abc-str #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270" "a"])


  (let [editor (<sub [::editor #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])]
    (-> (.play (.-synthControl (.-synth editor)))
        (.then (fn [_]
                 (.pause (.-synthControl (.-synth editor)))))))


  (let [editor (<sub [::editor #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])]
    (-> (.play (.-synthControl (.-synth editor)))
        (.then (fn []
                 (js/console.log "Playing")))))

  (let [editor (<sub [::editor #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])]
    (.pause (.-synthControl (.-synth editor))))

  (let [editor (<sub [::editor #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])]
    (->> (js->clj (.-noteTimings (first (.-tunes editor))) :keywordize-keys true)
         (mapcat :midiPitches)
         (filter (comp #{"note"} :cmd))
         (map :pitch)
         (into #{})
         (mapv music-theory/->midi-pitch->index-tone-with-octave)))

  (let [editor (<sub [::editor #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])]
    (-> (.getKeySignature (first (.-tunes editor)))
        (js->clj  :keywordize-keys true)
        :root
        clojure.string/lower-case
        keyword))
  {:accidentals [], :root "A", :acc "", :mode "m", :el_type "keySignature"}


  (let [editor (<sub [::editor #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])]
    (js/console.log (.getKeySignature (first (.-tunes editor)))))

  (let [editor (<sub [::editor #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])]
    (js/console.log (.-noteTimings (first (.-tunes editor)))))


  (let [editor (<sub [::editor #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])]
    (.seek (.-synthControl (.-synth editor)) 1 "beats"))

  (let [editor (<sub [::editor #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])]
    (js/console.log (.-synth editor)))

  (let [editor (<sub [::editor #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])]
    (js/console.log (.-synthControl (.-synth editor))))

  (let [editor (<sub [::editor #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])]
    (.go (.-synthControl (.-synth editor))))


  (let [editor (<sub [::editor #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])]
    (->> (.-selected (.-engraver (first (.-tunes editor))))
         (filter #(= "note" (.-type %)))
         (map #(js->clj (.-abcelem %) :keywordize-keys true))
         (mapcat :pitches)))


  )
