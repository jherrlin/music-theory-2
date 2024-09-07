(ns se.jherrlin.music-theory.webapp.views.dev.fretboard2
  (:require
   [re-frame.alpha :as re-frame]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard2 :as fretboard2]))


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
         {:x 5, :y 3}]]}]]))

(defn ^:dev/after-load routes [deps]
  (let [route-name :fretboard2]
    ["/dev/fretboard2"
     {:name route-name
      :view [view deps]}]))
