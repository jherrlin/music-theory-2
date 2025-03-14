(ns se.jherrlin.music-theory.webapp.views.dev.learn-harmonizations
  (:require
   [re-frame.alpha :as rf-alpha]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard2 :as fretboard2]
   [se.jherrlin.music-theory.fretboard :as fretboard]
   [se.jherrlin.music-theory.webapp.components.tonejs]
   [re-frame.db :as db]
   [se.jherrlin.music-theory.webapp.views.common :as views.common]))


(rf-alpha/reg-cofx :now #(assoc % :now (js/Date.)))

(def app-db-path ::learn-harmonizations)

(defn path [x]
  (-> [app-db-path x] flatten vec))

;; `app-db` paths
(def click-history-path (path ::click-history))
(def yxs-path (path ::yxs))
(def chord-path (path ::chord))
(def chords-path (path ::chords))
(def key-of-path (path ::key-of))
(def instrument-path (path ::instrumentkey-of))  ;; Currently not used
(def nr-of-frets-path (path ::nr-of-frets))
(def game-state-path (path ::game-state))
(def chord-idx-path (path ::chord-number))
(def facit-fretboard-matrix-path (path ::facit-fretboard-matrix))
(def facit-yxs-path (path ::facit-yxs))

(def events-
  [{:n ::fretboard-size
    :d 1.0}
   {:n ::message}
   {:n ::show-facit?}
   {:n ::chord
    :p chord-path}
   {:n ::chords
    :p chords-path}
   {:n ::key-of
    :p key-of-path}
   {:n ::nr-of-frets
    :p nr-of-frets-path}
   {:n ::game-state
    :p game-state-path}
   {:n ::chord-idx
    :p chord-idx-path}
   {:n ::facit-fretboard-matrix
    :p facit-fretboard-matrix-path}
   {:n ::facit-yxs
    :p facit-yxs-path}])

(doseq [{:keys [n s e d p]} events-]
  (rf-alpha/reg-sub n      (or s (fn [db [_]]     (get-in db (or p (path n)) d))))
  (rf-alpha/reg-event-db n (or e (fn [db [_ e]] (assoc-in db (or p (path n)) e)))))

(rf-alpha/reg-flow
 (let [inputs {:key-of key-of-path}]
   {:doc         "Calculate the chords in the harmonization"
    :id          ::chords
    :live-inputs inputs
    :live?       :key-of
    :inputs      inputs
    :output      (fn [{:keys [key-of]}]
                   (music-theory/calc-harmonization-chords
                    {:instrument          :mandolin,
                     :key-of              key-of,
                     :harmonization-id    :triads,
                     :harmonization-scale :major}))
    :path        chords-path}))

(rf-alpha/reg-flow
 (let [inputs {:chords    (rf-alpha/flow<- ::chords)
               :chord-idx chord-idx-path}]
   {:doc         "Get the current chord"
    :id          ::chord
    :live-inputs inputs
    :live?       :chords
    :inputs      inputs
    :output      (fn [{:keys [chords chord-idx]}]
                   (let [chord (nth chords chord-idx)
                         interval-tones (music-theory/interval-tones (:key-of chord) (:chord/intervals chord))]
                     (assoc chord :interval-tones interval-tones)))
    :path        chord-path}))

(rf-alpha/reg-flow
 (let [inputs {:chord (rf-alpha/flow<- ::chord)
               :nr-of-frets nr-of-frets-path}]
   {:doc         "Facit fretboard matrix"
    :id          ::facit-fretboard-matrix
    :live-inputs inputs
    :live?       :chord
    :inputs      inputs
    :output      (fn [{:keys [nr-of-frets chord]}]
                   (let [{:keys [key-of id]} chord]
                     (music-theory/instrument-data-structure
                      {:id         id
                       :instrument :mandolin
                       :key-of     key-of}
                      {:nr-of-frets nr-of-frets})))
    :path        facit-fretboard-matrix-path}))

(rf-alpha/reg-flow
 (let [inputs {:facit-fretboard-matrix (rf-alpha/flow<- ::facit-fretboard-matrix)}]
   {:doc         "Facit yxs"
    :id          ::facit-yxs
    :live-inputs inputs
    :live?       :facit-fretboard-matrix
    :inputs      inputs
    :output      (fn [{:keys [facit-fretboard-matrix]}]
                   (->> facit-fretboard-matrix
                        (apply concat)
                        (filter :match?)
                        (map :yx)
                        (into #{})))
    :path        facit-yxs-path}))

(rf-alpha/reg-event-db
  ::click-history
  (fn [db [_event-id click]]
    (update-in db click-history-path conj click)))

(rf-alpha/reg-event-db
 ::yxs
 (fn [db [_event-id yx]]
   (let [yxs (get-in db yxs-path #{})]
     (if (yxs yx)
       (update-in db yxs-path disj yx)
       (update-in db yxs-path (fnil conj #{}) yx)))))

(rf-alpha/reg-sub
  ::yxs
  (fn [db _]
    (get-in db yxs-path #{})))

(rf-alpha/reg-event-fx
 ::on-click
 [(rf-alpha/inject-cofx :now)]
 (fn [{:keys [db now]} [_event-id {:keys [yx tone-str octave] :as m}]]
   (js/console.log m)
   {:fx [[:dispatch [::click-history (assoc m :timestamp now)]]
         [:dispatch [::yxs yx]]
         [:dispatch [:tonejs/play-tone {:tone tone-str :octave octave}]]]}))

(def facit
  (->> (music-theory/instrument-data-structure
        {:id         #uuid "1cd72972-ca33-4962-871c-1551b7ea5244"
         :instrument :mandolin
         :key-of     :d}
        {:nr-of-frets 10})
    (apply concat)
    (filter :match?)))

(comment
  (count facit)
  (->> facit
    (map :yx)
    (into #{}))
  :-)

(rf-alpha/reg-event-fx
 ::success
 (fn [{:keys [db]} [_event-id]]
   {:db (-> db
            (assoc-in yxs-path #{})
            (update-in chord-idx-path inc))
    :fx [[:dispatch [::message "All correct"]]]}))

(rf-alpha/reg-event-fx
 ::failure
 (fn [{:keys [db]} [_event-id]]
   {:fx [[:dispatch [::message "Failure"]]
         [:dispatch [::show-facit? true]]]}))

(rf-alpha/reg-event-fx
 ::submit
 (fn [{:keys [db]} [_event-id]]
   (let [selected-yxs (get-in db yxs-path)
         correct-yxs  (get-in db facit-yxs-path)]
     {:fx [(if (= selected-yxs correct-yxs)
             [:dispatch [::success]]
             [:dispatch [::failure]])]})))

(defn view [deps]
  (let [yxs (<sub [::yxs])
        facit-yxs (<sub [::facit-yxs])
        react-key "bSZ4ppJTu8xhNmZYCoc8uW"
        message (<sub [::message])
        show-facit? (<sub [::show-facit?])
        chord (<sub [::chord])
        nr-of-frets (<sub [::nr-of-frets])
        facit-fretboard-matrix (<sub [::facit-fretboard-matrix])]
    [:div
     [:h1 "Find all the notes in the chord!"]

     [:<>
      [views.common/chord-name (:key-of chord) (:chord/suffix chord) (:chord/explanation chord)]
      [:p message]
      [:p "Total:" (count facit-yxs)]
      [:p "Selected:" (count yxs)]
      [:p "Find tones: " [views.common/intervals (:interval-tones chord)]]
      [fretboard2/styled-view
       {:id               (str "hehe" react-key)
        :fretboard-matrix (->> (music-theory/fretboard-matrix->fretboard2
                                {}
                                (music-theory/create-fretboard-matrix-for-instrument
                                 :d nr-of-frets :mandolin))
                               (music-theory/map-matrix
                                (comp
                                 (music-theory/circle-color
                                  (fn [{:keys [yx]}]
                                    (yxs yx))
                                  "green")
                                 (music-theory/center-text
                                  (fn [{:keys [yx]}]
                                    (yxs yx))
                                  (fn [{:keys [out tone-str interval-tone]}]
                                    (or out tone-str interval-tone)))
                                 (fn [m]
                                   (assoc m :on-click
                                          (fn [_]
                                            (>evt [::on-click m])))))))
        :entity-str     react-key
        :fretboard-size 1}]
      [:button {:on-click #(>evt [::submit])} "Submit"]
      (when show-facit?
        [:<>
         [:h2 "Facit:"]
         [fretboard2/styled-view
          {:id               (str "hehe" react-key "123")
           :fretboard-matrix (music-theory/fretboard-matrix->fretboard2
                               {}
                               facit-fretboard-matrix)
           :entity-str     (str react-key "askldjalsd")
           :fretboard-size 1}]
         [:hr]])]]))

(rf-alpha/reg-event-fx
 ::start
 (fn [{:keys [db]} [_event-id]]
   (js/console.log ::start)
   {:db (-> db
            (assoc-in key-of-path :d)
            (assoc-in nr-of-frets-path 13)
            (assoc-in game-state-path :lobby)
            (assoc-in chord-idx-path 0))}))

(defn routes [deps]
  (let [route-name :learn-harmonizations]
    ["/dev/learn"
     {:name  route-name
      :view  [view deps]
      :controllers
      [{:start (fn [_]
                 (>evt [::start]))}]}]))


(comment

  (music-theory/calc-harmonization-chords
   {:instrument          :mandolin,
    :key-of              :d,
    :harmonization-id    :triads,
    :harmonization-scale :major})

  (>evt [::show-facit? true])
  (>evt [::success])

  (get-in @re-frame.db/app-db key-of-path)
  (get-in @re-frame.db/app-db nr-of-frets-path)
  (get-in @re-frame.db/app-db chords-path)
  (get-in @re-frame.db/app-db chord-path)
  (get-in @re-frame.db/app-db chord-idx-path)

  (get-in @re-frame.db/app-db chord-idx-path)
  (>evt [::chord-idx 1])

  (get-in @re-frame.db/app-db facit-fretboard-matrix-path)
  (get-in @re-frame.db/app-db facit-yxs-path)

  :-)
