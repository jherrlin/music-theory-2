(ns se.jherrlin.music-theory.webapp.views.abcjs.examples.basic-synth
  (:require ["abcjs" :as abcjs]
            [reagent.core :as r]))

(defonce visualObjs (r/atom {}))
(defonce midiBuffer (atom nil))

(def whiskey-abc
  "X: 1
T: Whiskey Before Breakfast
M: 4/4
L: 1/8
K: D
P: A
DD |: \"D\" DEFG A2 FG | ABAG FDEF  | \"G\" GABG \"D\" F2 AF | \"A\" EDEF EDCE |
   |  \"D\" DEFG A2 FG | ABAG FDEF  | \"G\" GABG \"D\" F2 AF  \\
   |1 \"A\" EDEF \"D\" D2 DD :|2 \"A\" EDEF \"D\" D2 FE ||")

(def audio-context
  (or js/window.AudioContext
      js/window.webkitAudioContext
      js/navigator.mozAudioContext
      js/navigator.msAudioContext))

(defn set-audio-context! [audio-context]
  (set! js/window.AudioContext audio-context))

(defn play-sound! [id]
  (let [_             (set-audio-context! audio-context)
        audio-context (new js/window.AudioContext)
        visualObj     (get @visualObjs id)]
    (when visualObj
      (-> audio-context
          (.resume)
          (.then (fn []
                   (let [_ (swap! midiBuffer (fn [buffer]
                                               (try
                                                 (.stop buffer)
                                                 (catch js/Error _))
                                               (new abcjs/synth.CreateSynth)))]
                     (-> (.init @midiBuffer (clj->js {:visualObj              visualObj
                                                      :audioContext           audio-context
                                                      :millisecondsPerMeasure (.millisecondsPerMeasure visualObj)
                                                      :options                {:swing false}}))
                         (.then (fn [_]
                                  (.prime @midiBuffer)))
                         (.then (fn [_]
                                  (.start @midiBuffer)
                                  (js/Promise.resolve)))
                         (.catch (fn [e]
                                   (js/console.log "error:" e)))))))))))

(defn stop-sound! []
  (let [buffer @midiBuffer]
    (when buffer (.stop buffer))))

(comment
  (play-sound! :whiskey)
  (stop-sound!)
  :-)

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

(defn ui []
  (r/create-class
   {:component-did-mount  #(whiskey-score)
    :component-did-update #(whiskey-score)
    :display-name         "editor"
    :reagent-render
    (fn []
      [:div
       [:p "Basic synth"]
       (let [url "https://paulrosen.github.io/abcjs/examples/basic-synth.html"]
           [:p
            [:a {:href url} url]])
       (let [url "https://github.com/paulrosen/abcjs/blob/main/examples/basic-synth.html"]
           [:p
            [:a {:href url} url]])
       [:div {:id "whiskey-score"}]
       [:div {:style {:display "flex"}}
        [:button {:style    {:margin-right "1em"}
                  :on-click (fn [_] (play-sound! :whiskey))} "Play"]
        [:button {:on-click (fn [_] (stop-sound!))} "Stop"]]])}))

(defn routes [deps]
  (let [route-name :abcjs-example/basic-synth]
    ["/abcjs/examples/basic-synth"
     {:name    route-name
      :view    [ui]
      :top-nav :abcjs-examples}]))
