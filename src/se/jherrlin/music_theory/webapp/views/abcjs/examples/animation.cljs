(ns se.jherrlin.music-theory.webapp.views.abcjs.examples.animation
  (:require ["abcjs" :as abcjs]
            ["functions/fns" :as js-fns]
            [reagent.core :as r]))

(comment

  :-)

(defonce visualObjs (r/atom {}))
;; Store abcjs/TimingCallbacks instance
(def tc (atom nil))

(def whiskey-abc
  "X: 1
T: Whiskey Before Breakfast
M: 4/4
L: 1/8
K: D
P: A
DD |: \"D\" [Dd]EFG A2 FG | ABAG FDEF  | \"G\" GABG \"D\" F2 AF | \"A\" EDEF EDCE |
   |  \"D\" DEFG A2 FG | ABAG FDEF  | \"G\" GABG \"D\" F2 AF  \\
   |1 \"A\" EDEF \"D\" D2 DD :|2 \"A\" EDEF \"D\" D2 FE ||")



(defn render-abc [{:keys [dom-id abc ops id]}]
  (let [visualObj (abcjs/renderAbc dom-id abc ops)]
    (swap! visualObjs assoc id (first visualObj))))

(defn whiskey-score []
  (render-abc
   {:abc    whiskey-abc
    :dom-id "whiskey-score"
    :id     :whiskey
    :ops    (clj->js
             {:responsive "resize"})}))

(defn create-cursor []
  (let [svg    (js/document.querySelector "#whiskey-score svg")
        cursor (js/document.createElementNS "http://www.w3.org/2000/svg" "line")]
    (.setAttribute cursor "class" "abcjs-cursor")
    (.setAttributeNS cursor nil, "x1", 0)
    (.setAttributeNS cursor nil, "x1", 0)
    (.setAttributeNS cursor nil, "y1", 0)
    (.setAttributeNS cursor nil, "x2", 0)
    (.setAttributeNS cursor nil, "y2", 0)
    ;; (.appendChild svg cursor)
    cursor))

(defn beatCallback [cursor currentBeat totalBeats lastMoment position debugInfo]
  (when (.-left position)
    (let [x1 (- (.-left position) 2)
          x2 (- (.-left position) 2)
          y1 (.-top position)
          y2 (+ (.-top position) (.-height position))]
      (js/console.log "beatCallback")
      (js/console.log "position:" position)
      (js/console.log "currentBeat:" currentBeat)
      (js/console.log "debugInfo:" debugInfo)
      (.setAttribute cursor "x1" x1)
      (.setAttribute cursor "x2" x2)
      (.setAttribute cursor "y1" y1)
      (.setAttribute cursor "y2" y2))))

(def lastEls (atom (clj->js [])))

(defn clear-colored-notes []
  (js-fns/colorElements "red-note-color" @lastEls #js [])
  (reset! lastEls #js []))

(defn eventCallback [event]
  (def event event)
  (js/console.log "event:" event)
  (js->clj event)
  (if event
    (let [elements (.-elements event)]
      (js/console.log "eventCallback, elements:")
      (js/console.log elements)
      (def elements elements)
      (js-fns/colorElements "red-note-color" @lastEls elements)
      (reset! lastEls elements))
    (clear-colored-notes)))

(defn start-cursor! [cursor]
  (let [svg             (js/document.querySelector "#whiskey-score svg")
        timingCallbacks (new abcjs/TimingCallbacks
                             (get @visualObjs :whiskey)
                             (clj->js {:qpm           50 ;; bpm
                                       :beatCallback  (partial beatCallback cursor)
                                       :eventCallback eventCallback}))]
    (.appendChild svg cursor)
    (reset! tc timingCallbacks)
    (.start timingCallbacks)))

(defonce cursor (create-cursor))

(defn stop-cursor! [cursor]
  (let [svg (js/document.querySelector "#whiskey-score svg")]
    (.removeChild svg cursor)
    (.stop @tc)
    (clear-colored-notes)))

(defn ui []
  (r/create-class
   {:component-did-mount  #(whiskey-score)
    :component-did-update #(whiskey-score)
    :display-name         "editor"
    :reagent-render
    (fn []
      [:div
       [:h2 "Animation"]
       (let [url "https://github.com/paulrosen/abcjs/blob/main/examples/animation.html"]
         [:p
          [:a {:href url} url]])
       (let [url "https://paulrosen.github.io/abcjs/examples/animation.html"]
         [:p
          [:a {:href url} url]])

       [:div {:id "whiskey-score"}]
       [:button {:style    {:margin-right "1em"}
                 :on-click (fn [_] (start-cursor! cursor))} "Start"]
       [:button {:on-click (fn [_] (stop-cursor! cursor))} "Stop"]])}))

(defn routes [deps]
  (let [route-name :abcjs-example/animation]
    ["/abcjs/examples/animation"
     {:name    route-name
      :view    [ui]
      :top-nav :abcjs-examples}]))
