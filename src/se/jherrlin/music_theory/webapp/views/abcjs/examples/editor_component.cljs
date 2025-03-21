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
  "X: 1
T: Whiskey Before Breakfast
M: 4/4
L: 1/8
K: D
P: A
DD |: \"D\" DEFG A2 FG | ABAG FDEF  | \"G\" GABG \"D\" F2 AF | \"A\" EDEF EDCE |
   |  \"D\" DEFG A2 FG | cBAG FDEF  | \"G\" GABG \"D\" F2 AF |
   |1 \"A\" EDEF \"D\" D2 DD :|2 \"A\" EDEF \"D\" D2 FE ||")


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
                               :tablature  tab-instrument}})))

(random-uuid)

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
        [:div
         [:div {:style {:display "flex"}}
          [:textarea {:style      {:flex "1"}
                      :id         editor-dom-id
                      ;; :cols       "80"
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
        (mapv (fn [c]
                (let [abc-str (get c :abc-str)]
                  (merge c (->component c))))))))

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

(defn view []
  [:div
   [:h2 "View"]
   (for [component (<sub [::components])]
     ^{:key (str (:component-identifier component) "-parent")}
     (case (:component-version component)
       {:version 1, :type :abc-editor}
       [v1-abc-editor-component component]

       [:div "Unsupported component"]))])

(rf/reg-event-fx
 ::start
 (fn [{:keys [db]} [_event-id]]
   (let [components [{:component-version    {:version 1 :type :abc-editor}
                      :component-identifier "OYRGxhOybwzwAzOMiNl0ds"
                      :tab-instrument       [{:instrument "mandolin"
                                              :tuning     ["G,", "D", "A", "e"]
                                              :capo       0}]
                      :abc-str              whiskey-abc}
                     {:component-version    {:version 1 :type :abc-editor}
                      :component-identifier "RS0X3skMhNlnFqz0QTXfLn"
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
  (->> (<sub [::abc-str "OYRGxhOybwzwAzOMiNl0ds"])
       (str/split-lines)
       (count)
       (inc))

  (>evt [::abc-str "OYRGxhOybwzwAzOMiNl0ds" "a"])


  (->> (get-in @re-frame.db/app-db components-path)
       (map ->component))


  )
