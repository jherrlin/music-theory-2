(ns se.jherrlin.music-theory.webapp.views.abcjs.examples.editor-component
  (:require ["abcjs" :as abcjs]
            ["functions/fns" :as js-fns]
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

(defn derived-data [k component-id]
  (path [:derived component-id k]))

(def components-path (path ::components))
(def components-index-path (path ::components-index))
(def editors-path (path ::editors))

(def key-of-path                  (partial derived-data ::key-of))
(def signature-path               (partial derived-data ::signature))
(def editor-path                  (partial derived-data ::editor))
(def editor-loaded?-path          (partial derived-data ::editor-loaded?))
(def index-tones-used-in-abc-path (partial derived-data ::index-tones-used-in-abc))
(def selected-in-abc-path         (partial derived-data ::selected-in-abc))
(def all-or-selected-path         (partial derived-data ::all-or-selected))
(def fretboard-matrix-path        (partial derived-data ::fretboard-matrix))
(def current-beat-number-path     (partial derived-data ::current-beat-number))
(def loop-start-at-beat-path      (partial derived-data ::loop-start-at-beat))
(def loop-ends-at-beat-path       (partial derived-data ::loop-ends-at-beat))
(def loop?-path                   (partial derived-data ::loop?))

(rf/reg-event-db
 ::loop-start-at-beat
 (fn [db [_event-id component-id]]
   (let [beat (get-in db (current-beat-number-path component-id) 0)]
     (assoc-in db (loop-start-at-beat-path component-id) beat))))

(rf/reg-sub
 ::loop-start-at-beat
 (fn [db [_sub-id component-id]]
   (get-in db (loop-start-at-beat-path component-id))))

(rf/reg-event-db
 ::loop-ends-at-beat
 (fn [db [_event-id component-id beat]]
   (let [beat (get-in db (current-beat-number-path component-id) 0)]
     (assoc-in db (loop-ends-at-beat-path component-id) beat))))

(rf/reg-sub
 ::loop-ends-at-beat
 (fn [db [_sub-id component-id]]
   (get-in db (loop-ends-at-beat-path component-id))))

(rf/reg-event-db
 ::loop?
 (fn [db [_event-id component-id loop?]]
   (assoc-in db (loop?-path component-id) loop?)))

(rf/reg-sub
 ::loop?
 (fn [db [_sub-id component-id]]
   (get-in db (loop?-path component-id) false)))

(rf/reg-sub
 ::abc-str
 (fn [db [_sub-id component-id]]
   (let [component-idx (get-in db (conj components-index-path component-id))]
     (get-in db (conj components-path component-idx :component/data :abc-str)))))

(comment
  (<sub [::abc-str #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])
  )

(rf/reg-sub
 ::abc-editor-rows
 (fn [db [_sub-id component-id]]
   (let [component-idx (get-in db (conj components-index-path component-id))
         rows          (count
                        (some-> (get-in db (conj components-path component-idx :component/data :abc-str))
                                (str/split-lines)))]
     (if (< rows 5) 5 (inc rows)))))

(rf/reg-event-db
 ::abc-str
 (fn [db [_event-id component-id abc-str]]
   (let [component-idx (get-in db (conj components-index-path component-id))]
     (assoc-in db (conj components-path component-idx :component/data :abc-str) abc-str))))


(rf/reg-event-db
 ::fretboard-matrix
 (fn [db [_event-id component-id fretboard]]
   (assoc-in db (fretboard-matrix-path component-id) fretboard)))

(rf/reg-sub
 ::fretboard-matrix
 (fn [db [_sub-id component-id]]
   (get-in db (fretboard-matrix-path component-id))))

(rf/reg-sub
 ::editor
 (fn [db [_sub-id component-id]]
   (get-in db (editor-path component-id))))

(rf/reg-sub
 ::editor-is-loaded?
 (fn [db [_sub-id component-id]]
   (get-in db (editor-loaded?-path component-id))))

(rf/reg-event-db
 ::editor
 (fn [db [_event-id component-id editor]]
   (let [is-loaded? (.-isLoaded (.-synthControl (.-synth editor)))]
     (-> db
         (assoc-in (editor-path component-id) editor)
         (assoc-in (editor-loaded?-path component-id) is-loaded?)))))

(rf/reg-fx
 ::editor-go!
 (fn [{:keys [component-id editor]}]
   (-> (.runWhenReady
        (.-synthControl (.-synth editor))
        #(js/Promise.resolve
          (>evt [::editor component-id editor]))
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
 (fn [{:keys [db]} [_event-id component-id]]
   (js/console.log ::activate-editor!)
   (let [editor (get-in db (editor-path component-id))]
     {::editor-go! {:component-identifier component-id
                    :editor               editor}})))

(rf/reg-sub
 ::all-or-selected
 (fn [db [_sub-id component-id]]
   (get-in db (all-or-selected-path component-id) :all)))

(rf/reg-event-db
 ::all-or-selected
 (fn [db [_event-id component-id k]]
   (assoc-in db (all-or-selected-path component-id) k)))

(def whiskey-abc
  "X: 1                                                                       \nT: Wagon Wheel, mandolin solo                                              \nM: 4/4                                                                     \nL: 1/8                                                                     \nK: A                                                                       \nP: A                                                                       \n|: \"A\" A,2A,A, B,CEF | \"E\" E2EE GFEF | \"F#m\" F2FF ABcB  | \"D\" d2ef abaf |  \n|  \"A\" a2aa    fecB  | \"E\" B2BB cBGF | \"D\"   D2F2 dBAB  | \"D\" dABA A4 |    ")

;; (def whiskey-abc
;;   "X: 1 \nT: Wagon Wheel, mandolin solo \nM: 4/4 \nL: 1/8 \nK: A \nP: A
;; ")


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

(def fretboard-checked-state (atom {}))

(defn unmark-and-mark-fretboard-tone!
  [{:keys [component-id fretboard-id]}
   pitch-nr]
  ;; Remove old markers
  (when-let [elements (get @fretboard-checked-state component-id)]
    (.forEach
     (js/Array.from elements)
     (fn [el]
       (set! (.-backgroundColor (.-style el)) "rgb(255, 165, 0)"))))

  ;; Add new markers
  (swap!
   fretboard-checked-state
   assoc
   component-id
   (.map
    (js/Array.from
     (.querySelectorAll
      (js/document.querySelector
       (str "#" fretboard-id))
      (str "." fretboard-id "-" pitch-nr "-center-text-div")))
    (fn [el]
      (set! (.-backgroundColor (.-style el)) "green")
      el))))

(rf/reg-event-fx
 ::selected-in-abc
 (fn [{:keys [db]} [_event-id component-id]]
   (if-let [editor (get-in db (editor-path component-id))]
     (let [pitches (->> (.-selected (.-engraver (first (.-tunes editor))))
                        (filter #(= "note" (.-type %)))
                        (map #(js->clj (.-abcelem %) :keywordize-keys true))
                        (mapcat :pitches))]
       {:db (assoc-in db (selected-in-abc-path component-id) pitches)})
     {})))

(rf/reg-sub
 ::selected-in-abc
 (fn [db [_sub-id component-id]]
   (let [selected    (get-in db (selected-in-abc-path component-id))
         signature   (get-in db (signature-path component-id))
         accidentals (:accidentals signature)]
     (some->> (abc-utils/->pitches
               {:accidentals accidentals
                :pitches     selected})
              (map (fn [{:keys [octave tone]}]
                     {:octave     octave
                      :index-tone (music-theory/interval-tone->index-tone tone)}))
              (set)))))

(rf/reg-event-db
 ::current-beat-number
 (fn [db [_event-id component-id current-beat-number]]
   (assoc-in db (current-beat-number-path component-id) current-beat-number)))

(rf/reg-sub
 ::current-beat-number
 (fn [db [_sub-id component-id]]
   (get-in db (current-beat-number-path component-id) 0)))

(rf/reg-fx
 ::seek
 (fn [{:keys [editor value kind]}]
   (.seek (.-synthControl (.-synth editor)) value kind)))

(rf/reg-event-fx
 ::seek-beat
 (fn [{:keys [db]} [_event-id component-id f value]]
   (let [current-beat (get-in db (current-beat-number-path component-id))
         editor       (get-in db (editor-path component-id))
         new-beat     (f current-beat value)]
     {:db    (assoc-in db (current-beat-number-path component-id) new-beat)
      ::seek {:editor editor
              :value  new-beat
              :kind   "beats"}})))

(def lastEls (atom {}))

(defn clear-colored-notes [component-id]
  (js-fns/colorElements
   "red-note-color"
   (get @lastEls component-id #js [])
   #js [])
  (swap! lastEls assoc component-id #js []))

(defn ->cursor-control-obj [{:keys [cursor canvas-dom-id component-id] :as args}]
  (js-obj
   "onReady" (fn [synthController]
               (let [svg (js/document.querySelector (str "#" canvas-dom-id " svg"))]
                 (js/console.log "onReady" synthController)
                 (>evt [::index-tones-used-in-abc component-id])
                 (.appendChild svg cursor)))
   "onStart" (fn []
               (js/console.log "onStart")
               (>evt [::all-or-selected component-id :all])
               (>evt [::selected-in-abc component-id []]))
   "onFinished" (fn []
                  (js/console.log "onFinished")
                  (clear-colored-notes component-id))
   "onBeat" (fn [beatNumber totalBeats totalTime position]
              (let [current-beat beatNumber ]
                (js/console.log "onBeat" current-beat)
                (>evt [::current-beat-number component-id current-beat])
                ;; (when-let [left-pos (.-left position)]
                ;;   (move-cursor! cursor {:x1 (- left-pos 2)
                ;;                         :x2 (- left-pos 2)
                ;;                         :y1 (.-top position)
                ;;                         :y2 (+ (.-top position) (.-height position))}))
                (when (and (get-in @re-frame.db/app-db (loop?-path component-id))
                           (> current-beat (get-in @re-frame.db/app-db (loop-ends-at-beat-path component-id))))
                  (let [editor (get-in @re-frame.db/app-db (editor-path component-id))
                        beat   (get-in @re-frame.db/app-db (loop-start-at-beat-path component-id))]
                    (.seek (.-synthControl (.-synth editor)) beat "beats")))))
   "onEvent" (fn [event]
               (js/console.log "onEvent" event)
               (when-let [left-pos (.-left event)]
                 (move-cursor! cursor {:x1 (- left-pos 2)
                                       :x2 (- left-pos 2)
                                       :y1 (.-top event)
                                       :y2 (+ (.-top event) (.-height event))}))
               (let [midi-pitches (->> (js->clj (.-midiPitches event) :keywordize-keys true)
                                       (map :pitch))
                     elements     (.-elements event)]
                 (js-fns/colorElements "red-note-color" (get @lastEls component-id #js []) elements)
                 (swap! lastEls assoc component-id elements)
                 (doseq [n midi-pitches]
                   (unmark-and-mark-fretboard-tone! args n))))))

(rf/reg-event-fx
 ::abc-after-parsing
 (fn [{:keys [db]} [_event-id {:keys [tune tune-number abc-string component-id]}]]
   (let [component-idx (get-in db (conj components-index-path component-id))
         instrument    (get-in db (conj components-path component-idx :component/data :instrument))
         signature     (-> (.getKeySignature tune)
                           (js->clj  :keywordize-keys true)
                           (select-keys [:accidentals :root :acc :mode]))
         key-of        (some-> signature :root clojure.string/lower-case keyword)]
     {:db (-> db
              (assoc-in (key-of-path    component-id) key-of)
              (assoc-in (signature-path component-id) signature))
      :fx [[:dispatch [::index-tones-used-in-abc component-id]]
           [:dispatch [::fretboard-matrix component-id
                       (if key-of
                         (music-theory/create-fretboard-matrix-for-instrument
                          key-of 13 instrument)
                         (music-theory/create-fretboard-matrix-for-instrument
                          13 instrument))]]]})))

(rf/reg-event-fx
 ::index-tones-used-in-abc
 (fn [{:keys [db]} [_event-id component-id]]
   (js/console.log ::index-tones-used-in-abc)
   (let [editor             (get-in db (editor-path component-id))
         index-tones+octave (some->> (js->clj (.-noteTimings (first (.-tunes editor))) :keywordize-keys true)
                                     (mapcat :midiPitches)
                                     (filter (comp #{"note"} :cmd))
                                     (map :pitch)
                                     (into #{})
                                     (mapv music-theory/->midi-pitch->index-tone-with-octave))]
     {:db (cond-> db
            index-tones+octave (assoc-in (index-tones-used-in-abc-path component-id) index-tones+octave))})))

(rf/reg-sub
 ::index-tones-used-in-abc
 (fn [db [_sub-id component-id]]
   (get-in db (index-tones-used-in-abc-path component-id))))

(defn new-editor!
  [{component-id :component/id
    :keys        [editor-dom-id canvas-dom-id warnings-dom-id synth-controller-dom-id
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
                                (js/console.log "selectionChangeCallback")
                                (>evt [::all-or-selected component-id :selected])
                                (>evt [::selected-in-abc component-id]))
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
                                                       {:tune         tune
                                                        :tune-number  tuneNumber
                                                        :abc-string   abcString
                                                        :component-id component-id}]))
                               :clickListener (fn [abcelem tuneNumber classes analysis drag]
                                                (js/console.log
                                                 "clickListener"
                                                 abcelem tuneNumber classes analysis drag))}})))

(defn ->component
  [{component-id :component/id component-version :component/version}]
  (let [canvas-cursor-dom-id (str "canvas-cursor-dom-id-" component-id)
        canvas-dom-id        (str "canvas-dom-id-" component-id)
        canvas-cursor        (create-cursor canvas-cursor-dom-id)
        fretboard-id         (str "fretboard-id-" component-id)]
    {:synth-controller        (new ->cursor-control-obj
                                   {:cursor               canvas-cursor
                                    :canvas-dom-id        canvas-dom-id
                                    :component-id component-id
                                    :fretboard-id         fretboard-id})
     :editor-onchange         (fn [new-abc-str]
                                ;; This is not how ABC string is updated. But
                                ;; could be a good place for callbacks.
                                (js/console.log "new-abc-str:" new-abc-str))
     :canvas-cursor           canvas-cursor
     :editor-dom-id           (str "editor-dom-id-" component-id)
     :fretboard-id            fretboard-id
     :canvas-dom-id           canvas-dom-id
     :canvas-cursor-dom-id    canvas-cursor-dom-id
     :warnings-dom-id         (str "warnings-dom-id-" component-id)
     :synth-controller-dom-id (str "audio-control-dom-id-" component-id )}))

(rf/reg-fx
 ::new-editor!
 (fn [{component-id :component/id :as args}]
   (let [editor (new-editor! args)]
     ;; If user have clicked somewhere on the page it's possible to create a
     ;; sound context. ABCjs needs that sound context for providing the correct
     ;; pitches and we use those to visualize the fretboard data.
     (>evt [::editor component-id editor])
     (when-not (= (.-state (new js/window.AudioContext)) "suspended")
       (-> (.runWhenReady
            (.-synthControl (.-synth editor))
            #(js/Promise.resolve
              (>evt [::editor component-id editor]))
            nil)
           (.then #(js/console.log "then" %))
           (.catch #(js/console.log "error" %)))))))

(rf/reg-event-fx
 ::create-new-editor!
 (fn [{:keys [db]} [_event-id args]]
   {:fx [[::new-editor! args]]}))

(defn fretboard2-matrix [fretboard-matrix index-tones-used-in-abc]
  (music-theory/fretboard-matrix->fretboard2
   {}
   (let [m         (->> fretboard-matrix
                        (apply concat)
                        (group-by (juxt :index-tone :octave))
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
      fretboard-matrix
      found))))

(defn v1-abc-editor-component
  [{component-id :component/id component-version :component/version
    :keys        [editor-dom-id canvas-dom-id warnings-dom-id synth-controller-dom-id fretboard-id]
    :as          args}]
  (r/create-class
   {:component-did-mount #(>evt [::create-new-editor! args])
    :display-name        (str "v1-abc-editor-component-" component-id)
    :reagent-render
    (fn []
      (let [abc-str                 (<sub [::abc-str component-id])
            abc-editor-rows         (<sub [::abc-editor-rows component-id])
            editor-is-loaded?       (<sub [::editor-is-loaded? component-id])
            index-tones-used-in-abc (<sub [::index-tones-used-in-abc component-id])
            selected-in-abc         (<sub [::selected-in-abc component-id])
            all-or-selected         (<sub [::all-or-selected component-id])
            fretboard-matrix        (<sub [::fretboard-matrix component-id])
            current-beat-number     (<sub [::current-beat-number component-id])
            loop-start-at-beat      (<sub [::loop-start-at-beat component-id])
            loop-ends-at-beat       (<sub [::loop-ends-at-beat component-id])
            loop?                   (<sub [::loop? component-id])]
        [:div
         ;; [:p (if editor-is-loaded? "Active" "Not active")]
         ;; [:button {:on-click #(>evt [::activate-editor! component-id])}
         ;;  "Activate"]

         [:div {:id canvas-dom-id}]

         [:div {:style {:display #_ "flex" "none"}}
          [:textarea {:style      {:flex "1"}
                      :id         editor-dom-id
                      :rows       abc-editor-rows
                      :spellCheck false
                      :value      (or abc-str "")
                      :onChange   (fn [e]
                                    (let [value (.. e -target -value)]
                                      (>evt [::abc-str component-id value])))}]]

         [:div {:style {:display "none"}
                :id    warnings-dom-id}]

         [:div {:id synth-controller-dom-id}]

         [:div
          [:label {:for (str "current-beat-input-" component-id)}
           "Beat "]
          [:button {:on-click #(>evt [::seek-beat component-id - 1])} "<<<"]
          [:button {:on-click #(>evt [::seek-beat component-id - 0.5])} "<<"]
          [:button {:on-click #(>evt [::seek-beat component-id - 0.1])} "<"]

          [:input {:id    (str "current-beat-input-" component-id)
                   :value current-beat-number
                   :type  "number"
                   :min   "0"}]
          [:button {:on-click #(>evt [::seek-beat component-id + 0.1])} ">"]
          [:button {:on-click #(>evt [::seek-beat component-id + 0.5])} ">>"]
          [:button {:on-click #(>evt [::seek-beat component-id + 1])} ">>>"]

          [:label {:for (str "loop-" component-id)} "Loop?"]
          [:input {:id        (str "loop-" component-id)
                   :type      "checkbox"
                   :checked   loop?
                   :on-change #(>evt [::loop? component-id (not loop?)])}]
          [:button {:on-click #(>evt [::loop-start-at-beat component-id])}
           (if loop-start-at-beat
             (str "Loop start at beat: " loop-start-at-beat)
             "Start beat")]
          [:button {:on-click #(>evt [::loop-ends-at-beat component-id])}
           (if loop-start-at-beat
             (str "Loop ends at beat: " loop-ends-at-beat)
             "End beat")]]

         [fretboard2/styled-view
          {:id               fretboard-id
           :fretboard-matrix (fretboard2-matrix
                              fretboard-matrix
                              (if (= all-or-selected :selected)
                                selected-in-abc index-tones-used-in-abc))
           :fretboard-size   1}]]))}))

(comment
  (def db @re-frame.db/app-db)
  )

(rf/reg-sub
 ::components
 (fn [db [_sub-id]]
   (->> (get-in db components-path)
        (mapv (fn [{:component/keys [version type] :as c}]
                (if (= [version type] [1 :abc-editor])
                  (merge c (->component c))
                  c))))))

(rf/reg-event-db
 ::components
 (fn [db [_event-id components]]
   (let [components-index (->> components
                               (map-indexed vector)
                               (map (fn [[idx {:component/keys [id]}]]
                                      [id idx]))
                               (into {}))
         components       (->> components
                               (mapv (fn [{:component/keys [data] :as m}]
                                       (let [instrument     (get data :instrument)
                                             tab-instrument (get (music-theory/get-instrument instrument) :abc)]
                                         (cond-> m
                                           tab-instrument
                                           (assoc :tab-instrument [tab-instrument]))))))]
     (-> db
         (assoc-in components-path components)
         (assoc-in components-index-path components-index)))))

(defn v1-h2-title
  [{{:keys [text]} :component/data}]
  [:h2 text])

(defn v1-textarea
  [{{:keys [text]} :component/data}]
  [:div {:style {:flex 1}}
   [:textarea {:defaultValue text}]])

(defn view []
  [:<>
   (for [{:component/keys [version type id] :as component} (<sub [::components])]
     (case [version type]
       [1 :abc-editor]
       (with-meta
         [v1-abc-editor-component component]
         {:key (str id "-parent")})

       [1 :h2-title]
       (with-meta
         [v1-h2-title component]
         {:key (str id "-parent")})

       [1 :textarea]
       (with-meta
         [v1-textarea component]
         {:key (str id "-parent")})

       [:div "Unsupported component"]))])

(rf/reg-event-fx
 ::start
 (fn [{:keys [db]} [_event-id]]
   (let [components [{:component/version 1
                      :component/type    :h2-title
                      :component/id      #uuid "2e0d5b61-319e-488f-a1e7-690f1bbee468"
                      :component/data    {:text "Whiskey before breakfast lesson"}}
                     {:component/version 1
                      :component/type    :textarea
                      :component/id      #uuid "8d75e4e1-6963-4110-bca4-3a05bbffd863"
                      :component/data    {:text "hejsan"}}
                     ;; {:component/version 1
                     ;;  :component/type    :abc-editor
                     ;;  :component/id      #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"
                     ;;  :component/data    {:instrument :guitar
                     ;;                      :abc-str    whiskey-abc}}
                     {:component/version 1
                      :component/type    :abc-editor
                      :component/id      #uuid "3cf5d204-15b2-4e6c-9c1e-4094f54868ca"
                      :component/data    {:instrument :mandolin
                                          :abc-str    whiskey-abc}}]]
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

  (<sub [::index-tones-used-in-abc #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])
  (<sub [::selected-in-abc #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])
  (<sub [::all-or-selected #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])
  (>evt [::all-or-selected #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270" :selected])


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


  (let [editor (<sub [::editor "c-3cf5d204-15b2-4e6c-9c1e-4094f54868ca"])]
    (.seek (.-synthControl (.-synth editor)) 0.2 "beats"))

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
