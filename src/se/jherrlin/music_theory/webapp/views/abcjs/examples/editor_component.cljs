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

(def components-path (path ::components))
(def components-index-path (path ::components-index))
(def editors-path (path ::editors))

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
   (get-in db (conj editors-path component-identifier))))

(rf/reg-event-db
 ::editor
 (fn [db [_event-id component-identifier editor]]
   (assoc-in db (conj editors-path component-identifier) editor)))

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

(defn ->cursor-control-obj [{:keys [cursor canvas-dom-id]}]
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
               (js/console.log "onEvent" event))))

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
     :abcjsParams             {:responsive "resize"
                               :tablature  tab-instrument
                               :afterParsing (fn [tune, tuneNumber, abcString]
                                               (js/console.log
                                                "afterParsing"
                                                tune, tuneNumber, abcString))
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
                                     {:cursor        canvas-cursor
                                      :canvas-dom-id canvas-dom-id})
       :editor-onchange         (fn [new-abc-str]
                                  ;; This is not how ABC string is updated.
                                  ;; But could be a good place for callbacks
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
   (>evt [::editor component-identifier (new-editor! args)])))

(rf/reg-event-fx
 ::create-new-editor!
 (fn [{:keys [db]} [_event-id args]]
   {:fx [[::new-editor! args]]}))

(defn v1-abc-editor-component
  [{:keys [component-identifier component-version
           editor-dom-id canvas-dom-id warnings-dom-id synth-controller-dom-id]
    :as   args}]
  (r/create-class
   {:component-did-mount  #(>evt [::create-new-editor! args])
    :display-name         (str "v1-abc-editor-component" component-identifier)
    :reagent-render
    (fn []
      (let [abc-str         (<sub [::abc-str component-identifier])
            abc-editor-rows (<sub [::abc-editor-rows component-identifier])]
        [:<>
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

         [:div {:id synth-controller-dom-id}]]))}))

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
   [:textarea {:value text}]])

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
   (let [components [{:component-version    {:version 1 :type :h2-title}
                      :component-identifier (random-uuid)
                      :text                 "Whiskey before breakfast lesson"}
                     {:component-version    {:version 1 :type :textarea}
                      :component-identifier (random-uuid)
                      :text                 "hejsan"}
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
  (get-in @re-frame.db/app-db editors-path)

  (<sub [::components])
  (->> (<sub [::abc-str #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"])
       (str/split-lines)
       (count)
       (inc))

  (>evt [::abc-str #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270" "a"])


  (->> (get-in @re-frame.db/app-db components-path)
       (map ->component))




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
         (map music-theory/->midi-pitch->index-tone-with-octave)))

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


;; X: 1
;; T: Wagon Wheel, mandolin solo
;; M: 4/4
;; L: 1/8
;; K: A
;; P: A
;; |: "A" A,2A,A, B,CEF | "E" E2EE GFEF | "F#m" F2FF ABcB  | "D" d2ef abaf |
;; |  "A" a2aa    fecB  | "E" B2BB cBGF | "D"   D2F2 dBAB  | "D" dABA A4 |



;; Sjön Suger - Nere på stan
;;
;; Vers 1
;; A            D              A
;; Jag tror jag var på mitt 20 år just den dan
;;             D             E            A
;; Jag sa till mamma nu vill jag ensam gå ut
;;           D                     A
;; Du brukar alltid va med mej och hålla mig i hand
;;    D               E
;; då detta måste bli ett slut

;; Ref 1
;; A     D                    A
;; Fööör nere på stan har man roligt som fan
;;     D                          A
;; där får man sig ett glas eller två
;;     D                      A
;; Där träffar man tjejer med finfina grejer
;;         E                   A
;; som man ibland kan få smaka på

;; Banjo solo

;; Vers 2
;; Men hon svarade mig min stackars lilla pojk
;; ska du gå ifrån din kära mor
;; Så jag ilskna väll till och med min manligaste röst
;; sa jag - ser du inte att jag blivit stor

;; Vers 3
;; Hon sa det ser jag nog och jag ser något mer
;; som får kvinnfolket att springa efter dig
;; Sen på kvällen påväg ut tänkte jag på vad hon sagt
;; och det kändes något pirrigt inom mig

;; Ref 1

;; Mandolin solo

;; Vers 4
;; Jag gick in på en pub slog mig ner vid ett bord
;; Fick in en öl av en fin servitris
;; Hon stack till mig en lapp som sa jag slutar om en kvar
;; sen kan du och jag ha skönt på många vis

;; Vers 5
;; Men jag sprang där ifrån skrämd av den vänliga tant
;; och sökte skydd i dunklet på en krog
;; Men där tog fyra flickor hand om mig och dom kladdade runt
;; och bjöd på drinkar från mitt vinesnabbedog(?)

;; Ref 1

;; Fiol solo

;; Vers 6
;; När jag vakna ur mitt rus naken i en dubbelsäng
;; med dessa ladies och jag undrar vad som hänt
;; Åå när svaret stog klart fick jag brått där ifrån
;; och inom mig kändes något som förvrängt

;; Ref 2
;; Men nere på stan ser man en dam
;; en tjej jag aldrig vågat mig på
;; nu stannar jag hemma nu får mamma bestämma
;; För hon är nog den som är bäst ändå

;; Banjo

;; För hon är nog den som är bäst ändå
