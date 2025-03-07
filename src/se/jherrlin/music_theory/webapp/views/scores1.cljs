(ns se.jherrlin.music-theory.webapp.views.scores1
  ;; [M:2/4] http://www.lesession.co.uk/abc/abc_notation.htm
  (:require ["abcjs" :as abcjs]
    [clojure.string :as str]
    [clojure.walk :as walk]
    ["abcjs/plugin" :as abcjs-plugs]
    [reagent.dom.client :as rdc]
    [clojure.edn :as edn]
    [reagent.core :as r]
    [re-frame.alpha :as rf]
    [se.jherrlin.music-theory.music-theory :as music-theory]
    [se.jherrlin.music-theory.webapp.views.instruments.fretboard2 :as fretboard2]))





(defonce state (r/atom nil))

(defonce visualObj (atom nil))


(defn render-abc [dom-id abc opt]
  (abcjs/renderAbc dom-id abc opt))

;; (def a (new (get-in (js->clj abcjs) ["synth" "CreateSynthControl"])
;;          "whiskey-score-controller" {}))

;; ((get-in (js->clj abcjs) ["synth" "CreateSynthControl"])
;;          "whiskey-score-controller" {})

;; (new abcjs/synth.CreateSynthControl)

;; (abcjs/synth.supportsAudio)  ;; => true
;; (new js/window.AudioContext) ;; => #object [AudioContext [object AudioContext]]

(def audio-context
  (or js/window.AudioContext
      js/window.webkitAudioContext
      js/navigator.mozAudioContext
      js/navigator.msAudioContext))

(defn set-audio-context! [audio-context]
  (set! js/window.AudioContext audio-context))

(comment
  (set-audio-context! audio-context)

  ;; Works: plays sound
  ;; https://github.com/paulrosen/abcjs/blob/main/examples/basic-synth.html
  (let [audio-context (new js/window.AudioContext)]
    (-> audio-context
        (.resume)
        (.then (fn []
                 (js/console.log "AudioContext resumed")
                 (let [midiBuffer (new abcjs/synth.CreateSynth)]
                   (-> (.init midiBuffer (clj->js {:visualObj              @visualObj
                                                   :audioContext           audio-context
                                                   :millisecondsPerMeasure (.millisecondsPerMeasure @visualObj)
                                                   :options                {:swing false}}))
                       (.then (fn [response]
                                (js/console.log "Note loaded" response)
                                (.prime midiBuffer)))
                       (.then (fn [response]
                                (js/console.log "Status" (.-status response))
                                (.start midiBuffer)
                                (js/Promise.resolve)))
                       (.catch (fn [e]
                                 (def e e)
                                 (js/console.log "error:" e)))
                       (.finally #(js/console.log "Done"))))))))

  :-)





(defn abc-click-listener [abcelem, tuneNumber, classes, analysis, drag]
  (js/console.log "In abc-click-listener")
  (def abcelem abcelem)
  (def tuneNumber tuneNumber)
  (def classes classes)
  (def analysis analysis)
  (def drag drag)

  )

(defn render [dom-id abc-string tab]
  (reset! visualObj (first (render-abc
                            dom-id
                            abc-string
                            (clj->js
                             (cond-> {:responsive "resize"
                                      :add_classes true
                                      :clickListener abc-click-listener}
                               tab (assoc :tablature [tab])))))))

(def instruments
  {"guitar"        {:instrument "guitar"
                    :label      "Guitar (%T)"
                    :tuning     ["E,", "A,", "D", "G", "B", "e"] ;; E2 A2 D3 G3 B3 E4
                    :capo       0}
   "guitar-dadgad" {:instrument "guitar"
                    :label      "Guitar (%T)"
                    :tuning     ["D,", "A,", "D", "G", "A", "d"]
                    :capo       0}
   "mandolin"      {:instrument "mandolin"
                    :label      "Mandolin (%T)"
                    :tuning     ["G,", "D", "A", "e"]
                    :capo       0}
   "mandola"       {:instrument "mandolin"
                    :label      "Mandola (%T)"
                    :tuning     ["C,", "G,", "D", "A"]
                    :capo       0}})

(def whiskey-abc
  "
X: 1
T: Whiskey Before Breakfast
T: David Grisman, Panhandle Country on beat 5 and 6
M: 4/4
L: 1/8
K: D
P:A
DD |: \"D\" AEFG A2 FG | ^ABAG F_DEF  | \"G\" GABG \"D\" F2 AF | \"A\" EDEA cefe |
   |  \"D\" ae ge fe ed | dA =cA BA FD  | \"G\" GABG \"D\" F2 AF  \\
   |1 \"A\" EDEF \"D\" D2 DD :|2 \"A\" EDEF \"D\" D2 FE ||
")


(defn scores []
  (render
    "whiskey-score"
    whiskey-abc
    (get instruments "mandolin")))

(defn ui []
  (r/create-class
    {:component-did-mount  #(scores)
     :component-did-update #(scores)

     ;; name your component for inclusion in error messages
     :display-name "scores"

     ;; note the keyword for this method
     :reagent-render
     (fn []
       [:div
        [:div {:id "whiskey-score"}]
        [:div {:id "whiskey-score-controller"}]])}))

(defn routes [deps]
  (let [route-name :scores1]
    ["/scores1"
     {:name route-name
      :view [ui]}]))
