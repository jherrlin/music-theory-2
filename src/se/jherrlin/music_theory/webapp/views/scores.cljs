(ns se.jherrlin.music-theory.webapp.views.scores
  ;; [M:2/4] http://www.lesession.co.uk/abc/abc_notation.htm
  (:require ["abcjs" :as abcjs]
    [clojure.string :as str]
    [clojure.walk :as walk]
    ["abcjs/plugin" :as abcjs-plugs]
    [reagent.dom.client :as rdc]
    [clojure.edn :as edn]
    [reagent.core :as r]
    [se.jherrlin.music-theory.music-theory :as music-theory]
    [se.jherrlin.music-theory.webapp.views.instruments.fretboard2 :as fretboard2]))


(defn flat? [s]
  (when (string? s)
    (str/includes? s "_")))

(defn sharp? [s]
  (when (string? s)
    (str/includes? s "^")))

(def mandolin-fretboard-matrix
  (music-theory/create-fretboard-matrix-for-instrument
   8 :mandolin))

(defonce state (r/atom nil))

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

(defn ->tablature [s]
  (when s
    (get instruments s)))

(def render-abc (get (js->clj abcjs) "renderAbc"))



(defn abc-click-listener [abcelem, tuneNumber, classes, analysis, drag]
  (js/console.log "In abc-click-listener")
  (def abcelem abcelem)
  (def tuneNumber tuneNumber)
  (def classes classes)
  (def analysis analysis)
  (def drag drag)

  )

(defn render [dom-id abc-string tab]
  (render-abc
    dom-id
    abc-string
    (clj->js
      (cond-> {:responsive "resize"
               :clickListener abc-click-listener}
        tab (assoc :tablature [tab])))))

(def whiskey-abc-1
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
P:B
|: \"Em\" EAGD |
")

(def all-pitches
  {"C,,," {:octav 0 :tone "c"}
   "D,,," {:octav 0 :tone "d"}
   "E,,," {:octav 0 :tone "e"}
   "F,,," {:octav 0 :tone "f"}
   "G,,," {:octav 0 :tone "g"}
   "A,,," {:octav 0 :tone "a"}
   "B,,," {:octav 0 :tone "b"}
   "C,,"  {:octav 1 :tone "c"}
   "D,,"  {:octav 1 :tone "d"}
   "E,,"  {:octav 1 :tone "e"}
   "F,,"  {:octav 1 :tone "f"}
   "G,,"  {:octav 1 :tone "g"}
   "A,,"  {:octav 1 :tone "a"}
   "B,,"  {:octav 1 :tone "b"}
   "C,"   {:octav 2 :tone "c"}
   "D,"   {:octav 2 :tone "d"}
   "E,"   {:octav 2 :tone "e"}
   "F,"   {:octav 2 :tone "f"}
   "G,"   {:octav 2 :tone "g"}
   "A,"   {:octav 2 :tone "a"}
   "B,"   {:octav 2 :tone "b"}
   "C"    {:octav 3 :tone "c"}
   "D"    {:octav 3 :tone "d"}
   "E"    {:octav 3 :tone "e"}
   "F"    {:octav 3 :tone "f"}
   "G"    {:octav 3 :tone "g"}
   "A"    {:octav 3 :tone "a"}
   "B"    {:octav 3 :tone "b"}
   "c"    {:octav 4 :tone "c"}
   "d"    {:octav 4 :tone "d"}
   "e"    {:octav 4 :tone "e"}
   "f"    {:octav 4 :tone "f"}
   "g"    {:octav 4 :tone "g"}
   "a"    {:octav 4 :tone "a"}
   "b"    {:octav 4 :tone "b"}
   "c'"   {:octav 5 :tone "c"}
   "d'"   {:octav 5 :tone "d"}
   "e'"   {:octav 5 :tone "e"}
   "f'"   {:octav 5 :tone "f"}
   "g'"   {:octav 5 :tone "g"}
   "a'"   {:octav 5 :tone "a"}
   "b'"   {:octav 5 :tone "b"}
   "c''"  {:octav 6 :tone "c"}
   "d''"  {:octav 6 :tone "d"}
   "e''"  {:octav 6 :tone "e"}
   "f''"  {:octav 6 :tone "f"}
   "g''"  {:octav 6 :tone "g"}
   "a''"  {:octav 6 :tone "a"}
   "b''"  {:octav 6 :tone "b"}
   "c'''" {:octav 7 :tone "c"}
   "d'''" {:octav 7 :tone "d"}
   "e'''" {:octav 7 :tone "e"}
   "f'''" {:octav 7 :tone "f"}
   "g'''" {:octav 7 :tone "g"}
   "a'''" {:octav 7 :tone "a"}
   "b'''" {:octav 7 :tone "b"}})



(defn ->interval-tone [s]
  (let [cleaned              (first (re-seq #"[A-Za-z,']{1,4}" s))
        {:keys [tone] :as m} (get all-pitches cleaned)]
    (assoc m
           :interval-tone
           (cond-> tone
             (sharp? s) (str "#")
             (flat? s)  (str "b")
             :always    keyword))))

(->interval-tone "C,,")   ;; => {:octav 1, :tone "c", :id "C,,", :interval-tone :c}
(->interval-tone "_C,,")  ;; => {:octav 1, :tone "c", :id "C,,", :interval-tone :cb}

(defn calc [{:keys [name] :as m}]
  (merge m (->interval-tone name)))

(defn pitches-key-and-accidentals
  [abc-data]
  (let [acc (atom {})]
    (walk/postwalk
     (fn [m]
       (when (and (map? m) (= (get m :el_type) "note"))
         (swap! acc update :pitches concat (get m :pitches)))
       (when (and (map? m) (= (get m :el_type) "keySignature"))
         (swap! acc assoc :accidentals (get m :accidentals))
         (swap! acc assoc :key-of (get m :root)))
       m)
     abc-data)
    (update @acc :pitches set)))

(defn pitches-key-and-accidentals-2
  [{:keys [key-of accidentals pitches]}]
  (let [accidentals'    (->> accidentals
                             (mapv (fn [{:keys [note] :as accidental}]
                                     (assoc accidental :note (str/lower-case note)))))
        accidentals-map (->> accidentals'
                             (map (juxt :note identity))
                             (into {}))]
    {:pitches (->> pitches
                   (mapv (fn [{:keys [accidental name] :as pitch}]
                           (let [cleaned              (first (re-seq #"[A-Za-z,']{1,4}" name))
                                 {:keys [tone] :as m} (get all-pitches cleaned)]
                             (-> (merge pitch m)
                                 (dissoc :name :pitch :verticalPos :highestVert)
                                 (assoc :abc-tone name)
                                 (assoc :note-inc-acc (let [acc (get-in accidentals-map [tone :acc])]
                                                        (cond-> tone
                                                          (= acc "sharp")          (str "#")
                                                          (= accidental "sharp")   (str "#")
                                                          (= acc "flat")           (str "b")
                                                          (= accidental "flat")    (str "b")
                                                          (= acc "natural")        identity
                                                          (= accidental "natural") identity
                                                          :always                  keyword))))))))
     :key-of  (-> key-of str/lower-case keyword)}))

(comment
  (def a (render "whiskey-score-1" whiskey-abc-1 (->tablature "mandolin")))

  (->> (js->clj (nth a 0) :keywordize-keys true)
       pitches-key-and-accidentals
       pitches-key-and-accidentals-2)

  (->> (-> (walk/prewalk
            (fn [m]
              (if (and (map? m) (= (get-in m [:clef :type]) "TAB"))
                nil
                m))
            (js->clj (nth a 0) :keywordize-keys true))
           (get :lines)
           rest))

  (def b (let [state (atom [])]
           (walk/prewalk
            (fn [m]
              (when (and (map? m) (= (get m :el_type) "note"))
                (swap! state concat (get m :pitches)))
              m)
            (js->clj (nth a 0) :keywordize-keys true))
           @state))

  (->> (set b)
       (mapv calc))

  (def c (let [state (atom nil)]
           (walk/postwalk
            (fn [m]
              (when (and (map? m) (= (get m :el_type) "keySignature"))
                (reset! state m))
              m)
            (js->clj (nth a 0) :keywordize-keys true))
           @state))

  :-)

(def derp
  (get {:pitches
        [{:octav 3, :tone "a", :abc-tone "A", :note-inc-acc :a}
         {:octav 3, :tone "d", :abc-tone "D", :note-inc-acc :d}
         {:octav 4, :tone "d", :abc-tone "d", :note-inc-acc :d}
         {:octav 3, :tone "g", :abc-tone "G", :note-inc-acc :g}
         {:octav 3, :tone "e", :abc-tone "E", :note-inc-acc :e}
         {:octav 3, :tone "a", :abc-tone "A", :note-inc-acc :a}
         {:octav 4, :tone "e", :abc-tone "e", :note-inc-acc :e}
         {:octav 4, :tone "c", :abc-tone "c", :note-inc-acc :c#}
         {:octav 3, :tone "f", :abc-tone "F", :note-inc-acc :f#}
         {:accidental "natural",
          :octav 4,
          :tone "c",
          :abc-tone "=c",
          :note-inc-acc :c#}
         {:octav 4, :tone "a", :abc-tone "a", :note-inc-acc :a}
         {:accidental "flat", :octav 3, :tone "d", :abc-tone "_D", :note-inc-acc :db}
         {:accidental "sharp", :octav 3, :tone "a", :abc-tone "^A", :note-inc-acc :a#}
         {:octav 3, :tone "b", :abc-tone "B", :note-inc-acc :b}
         {:octav 4, :tone "f", :abc-tone "f", :note-inc-acc :f#}
         {:octav 4, :tone "g", :abc-tone "g", :note-inc-acc :g}],
        :key-of :d}
    :pitches))

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


(def hehe
  (loop [[x & rst] derp
         acc       mandolin-fretboard-matrix]
    (if (nil? x)
      acc
      (let [{score-interval-tone :note-inc-acc score-octav :octav} x]
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
                  :out    (-> score-interval-tone name str/capitalize)))]))
          acc))))))

(defn scores [state']
  (render "whiskey-score-1" whiskey-abc-1 (->tablature state')))

(def fretboard2-matrix
  (music-theory/fretboard-matrix->fretboard2
    {}
    hehe))

(defn ui []
  (r/create-class
    {:component-did-mount  #(scores @state)
     :component-did-update #(scores @state)

     ;; name your component for inclusion in error messages
     :display-name "scores"

     ;; note the keyword for this method
     :reagent-render
     (fn []
       [:div
        [:select {:value (prn-str (or @state "Select instrument"))
                  :on-change (fn [evt]
                               (let [value (-> evt .-target .-value edn/read-string)]
                                 (reset! state (if (= value "Select instrument")
                                                 nil
                                                 value))))}
         (for [t (concat ["Select instrument"] (keys instruments))]
           ^{:key t}
           [:option {:value (prn-str t)} t])]
        #_[:div {:id "whiskey-original-score"}]
        [:div {:id "whiskey-score-1"}]

        [fretboard2/styled-view
         {:id               "akxH4rw4Y682ySSDUo2AEm"
          :fretboard-matrix fretboard2-matrix
          :fretboard-size   1}]

        #_[:div {:id "cherokee-score"}]
        #_[:div {:id "jerusalem-score"}]])}))


(defn routes [deps]
  (let [route-name :scores]
    ["/scores"
     {:name route-name
      :view [ui]}]))
