(ns se.jherrlin.music-theory.webapp.views.dev.fretboard2
  (:require
   [re-frame.alpha :as re-frame]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard2 :as fretboard2]
   [se.jherrlin.music-theory.fretboard :as fretboard]))


(def app-db-path ::fretboard2)

(defn path [x]
  (-> [app-db-path x] flatten vec))

(def events-
  [{:n ::fretboard-size
    :d 1.0}])

(doseq [{:keys [n s e d]} events-]
  (re-frame/reg-sub n      (or s (fn [db [_]]     (get-in db (path n) d))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc-in db (path n) e)))))

(re-frame/reg-event-db
 ::inc-fretboard-size
 (fn [db _]
   (let [current-size (get-in db (path ::fretboard-size) 1.0)]
     (assoc-in db (path ::fretboard-size) (+ current-size 0.1)))))

(re-frame/reg-event-db
 ::dec-fretboard-size
 (fn [db _]
   (let [current-size (get-in db (path ::fretboard-size) 1.0)]
     (assoc-in db (path ::fretboard-size) (- current-size 0.1)))))

(defn octave-in-down-right-text
  [f {:keys [octave] :as m}]
  (cond-> m
    (f m) (assoc :down-right-text (str octave))))

(defn root-circle-color
  [f m]
  (cond-> m
    (f m) (assoc :circle-color "#ff7600")))

(defn center-text
  [pred k m]
  (cond-> m
    (pred m) (assoc :center-text (k m))))

(def fretboard-matrix
  [[{:x 0, :tone #{:e}, :octave 5, :y 0, :yx 0, :interval "2", :tone-str "E"}
    {:x 1, :tone #{:f}, :octave 5, :y 0, :yx 1, :interval "b3", :tone-str "F"}
    {:y 0,
     :octave 5,
     :tone-str "Gb",
     :yx 2,
     :tone #{:gb :f#},
     :flat "Gb",
     :sharp "F#",
     :x 2,
     :interval "3"}
    {:x 3, :tone #{:g}, :octave 5, :y 0, :yx 3, :interval "4", :tone-str "G"}
    {:y 0,
     :octave 5,
     :tone-str "Ab",
     :yx 4,
     :tone #{:g# :ab},
     :flat "Ab",
     :sharp "G#",
     :x 4,
     :interval "b5"}
    {:x 5, :tone #{:a}, :octave 5, :y 0, :yx 5, :interval "5", :tone-str "A"}]
   [{:y 1,
     :octave 4,
     :pattern-found-tone "A",
     :tone-str "A",
     :yx 100,
     :pattern-found-interval "5",
     :tone #{:a},
     :out "A",
     :x 0,
     :interval "5",
     :match? true}
    {:y 1,
     :octave 4,
     :tone-str "Bb",
     :yx 101,
     :tone #{:bb :a#},
     :flat "Bb",
     :sharp "A#",
     :x 1,
     :interval "b6"}
    {:y 1,
     :octave 4,
     :pattern-found-tone "B",
     :tone-str "B",
     :yx 102,
     :pattern-found-interval "6",
     :tone #{:b},
     :out "B",
     :x 2,
     :interval "6",
     :match? true}
    {:x 3,
     :tone #{:c},
     :octave 5,
     :y 1,
     :yx 103,
     :interval "b7",
     :tone-str "C"}
    {:y 1,
     :octave 5,
     :tone-str "Db",
     :yx 104,
     :tone #{:db :c#},
     :flat "Db",
     :sharp "C#",
     :x 4,
     :interval "7"}
    {:y 1,
     :octave 5,
     :root? true,
     :pattern-found-tone "D",
     :tone-str "D",
     :yx 105,
     :pattern-found-interval "1",
     :tone #{:d},
     :out "D",
     :x 5,
     :interval "1",
     :match? true}]
   [{:y 2,
     :octave 4,
     :root? true,
     :pattern-found-tone "D",
     :tone-str "D",
     :yx 200,
     :pattern-found-interval "1",
     :tone #{:d},
     :out "D",
     :x 0,
     :interval "1",
     :match? true}
    {:y 2,
     :octave 4,
     :tone-str "Eb",
     :yx 201,
     :tone #{:d# :eb},
     :flat "Eb",
     :sharp "D#",
     :x 1,
     :interval "b2"}
    {:y 2,
     :octave 4,
     :pattern-found-tone "E",
     :tone-str "E",
     :yx 202,
     :pattern-found-interval "2",
     :tone #{:e},
     :out "E",
     :x 2,
     :interval "2",
     :match? true}
    {:x 3,
     :tone #{:f},
     :octave 4,
     :y 2,
     :yx 203,
     :interval "b3",
     :tone-str "F"}
    {:y 2,
     :octave 4,
     :pattern-found-tone "F#",
     :tone-str "Gb",
     :yx 204,
     :pattern-found-interval "3",
     :tone #{:gb :f#},
     :out "F#",
     :flat "Gb",
     :sharp "F#",
     :x 4,
     :interval "3",
     :match? true}
    {:x 5,
     :tone #{:g},
     :octave 4,
     :y 2,
     :yx 205,
     :interval "4",
     :tone-str "G"}]
   [{:x 0, :tone #{:g}, :octave 3, :y 3, :yx 300, :interval "4", :tone-str "G"}
    {:y 3,
     :octave 3,
     :tone-str "Ab",
     :yx 301,
     :tone #{:g# :ab},
     :flat "Ab",
     :sharp "G#",
     :x 1,
     :interval "b5"}
    {:x 2, :tone #{:a}, :octave 3, :y 3, :yx 302, :interval "5", :tone-str "A"}
    {:y 3,
     :octave 3,
     :tone-str "Bb",
     :yx 303,
     :tone #{:bb :a#},
     :flat "Bb",
     :sharp "A#",
     :x 3,
     :interval "b6"}
    {:x 4, :tone #{:b}, :octave 3, :y 3, :yx 304, :interval "6", :tone-str "B"}
    {:x 5,
     :tone #{:c},
     :octave 4,
     :y 3,
     :yx 305,
     :interval "b7",
     :tone-str "C"}]])

(def f2
  [[{:x              0, :y 0
            :x-min          true
            :fret-color     "white"
            :circle-color   "green"
            :center-text ""
            :left-is-blank? true}
           {:x               1,
            :y               0
            :center-text     "A"
            :down-right-text "2"}
           {:x 2, :y 0}
           {:x 3, :y 0}
           {:x 4,
            :y 0
            :background-color "purple"}
           {:x 5, :y 0}]
          [{:x 0, :y 1}
           {:x 1, :y 1}
           {:x 2, :y 1}
           {:x            3,
            :y            1
            :center-text "G"
            :circle-color "white"
            :on-click (fn [{:keys [x y center-text]}]
                        (js/console.log x y center-text))}
           {:x 4, :y 1}
           {:x 5, :y 1}]
          [{:x 0, :y 2}
           {:x 1, :y 2}
           {:x 2, :y 2}
           {:x 3, :y 2}
           {:x 4, :y 2}
           {:x 5,
            :y 2
            :x-max? true}]
          [{:x      0,
            :y      3
            :blank? true}
           {:x      1,
            :y      3
            :blank? true}
           {:x              2,
            :y              3
            :left-is-blank? true}
           {:x            3,
            :y            3
            :circle-color "green"
            :center-text  "B"}
           {:x 4, :y 3}
           {:x 5, :y 3}]])

(re-frame/reg-event-fx
 ::play-tones
 (fn [_ [_event-id fretboard-matrix]]
   {:fx (music-theory/fretboard-matrix->tonejs-dispatches fretboard-matrix)}))

(comment
  (re-frame/dispatch [::play-tones fretboard-matrix])
  )

(comment
  (music-theory/fretboard-matrix->tonejs-dispatches fretboard-matrix)

  )

(defn ^:dev/after-load view [deps]
  (let [fretboard-size (<sub [::fretboard-size])]
    [:<>
     [:button
      {:on-click #(>evt [::inc-fretboard-size])}
      "inc"]
     [:button
      {:on-click #(>evt [::dec-fretboard-size])}
      "dec"]
     [fretboard2/styled-view
      {:id "deokedk"
       :fretboard-size fretboard-size
       :fretboard-matrix
       #_f2
       (->> fretboard-matrix
            (music-theory/map-matrix
             (comp
              #(select-keys % music-theory/fretboard2-keys)
              (partial center-text :match? :out)
              (partial root-circle-color :root?)
              (partial octave-in-down-right-text :match?)
              (partial music-theory/left-is-blank? fretboard-matrix))))}]]))

(defn ^:dev/after-load routes [deps]
  (let [route-name :fretboard2]
    ["/dev/fretboard2"
     {:name route-name
      :view [view deps]}]))
