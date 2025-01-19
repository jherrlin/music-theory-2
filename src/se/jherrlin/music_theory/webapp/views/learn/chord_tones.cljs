(ns se.jherrlin.music-theory.webapp.views.learn.chord-tones
  (:require
   [clojure.string :as str]
   [re-frame.alpha :as rf-alpha]
   [re-frame.db :as db]
   [re-frame.interop :refer [debug-enabled?]]
   [reitit.coercion.malli]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.components.tonejs]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [se.jherrlin.music-theory.webapp.views.common :as views.common]
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard2 :as fretboard2]))





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
(def harmonization-id-path (path ::harmonization-id))
(def game-start-inst-path (path ::game-start-inst))
(def game-end-inst-path (path ::game-end-inst))
(def game-duration-path (path ::game-duration))

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
    :p facit-yxs-path}
   {:n ::harmonization-id
    :p harmonization-id-path}
   {:n ::instrument
    :p instrument-path}
   {:n ::game-start-inst
    :p game-start-inst-path}
   {:n ::game-end-inst
    :p game-end-inst-path}
   {:n ::game-duration
    :p game-duration-path}])

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

(rf-alpha/reg-event-fx
 ::success
 (fn [{:keys [db]} [_event-id]]
   {:db (-> db
            (assoc-in yxs-path #{})
            (update-in chord-idx-path inc)
            (assoc-in game-state-path :success))
    :fx [[:dispatch [::message "All correct"]]
         [:dispatch [::show-facit? false]]]}))

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

(rf-alpha/reg-event-fx
 ::start-timer
 (fn [{:keys [db]} [_event-id]]
   (let [now (get db :time/now)]
     {:db (assoc-in db game-start-inst-path now)})))

(rf-alpha/reg-event-fx
  ::begin-game
  (fn [{:keys [db]} [_event-id]]
    {:db (assoc-in db game-state-path :game)
     :fx [[:dispatch [::start-timer]]]}))

(defn guide []
  (let [react-key "0s7CcdEsh0q7VXcVIsqXmK"]
    [:div
     [:h1 "Practice chord tone locations"]
     [:p "Practice chord tones by clicking on a virtual mandolin fretboard."]
     [:p "When you have selected all of the tones  all over the fretboard you submit"]
     [:p "and if you are correct you will be taken to the next chord in the harmonization."]

     [:br]

     [:p "When you click on a tone on the fretboard the tone will show and the"]
     [:p "tone will be played through your speakers."]

     [fretboard2/styled-view
      {:id               (str "hehe" react-key)
       :fretboard-matrix (->> (music-theory/fretboard-matrix->fretboard2
                                {}
                                (music-theory/create-fretboard-matrix-for-instrument
                                  :d 7 :mandolin))
                           (music-theory/map-matrix
                             (comp
                               (music-theory/circle-color
                                 (fn [{:keys [yx]}]
                                   (#{2 100 200 302} yx))
                                 "green")
                               (music-theory/center-text
                                 (fn [{:keys [yx]}]
                                   (#{2 100 200 302} yx))
                                 (fn [{:keys [out tone-str interval-tone]}]
                                   (or out tone-str interval-tone))))))
       :entity-str       react-key
       :fretboard-size   1}]

     [:br]

     [:p "The app currenly only have support for mandolin in the key of D major."]

     [:button {:on-click #(>evt [::begin-game])} "Start"]]))

(defn game-duration []
  (let [duration (<sub [::game-duration])]
    [:p "Duration: " duration]))

(defn success-view []
  [:div
   [:h1 "Good job!"]
   [:p "You found all of the tones"]
   [:button {:on-click #(>evt [::begin-game])} "Next chord in the harmonization"]])

(defn game-view []
  (let [yxs (<sub [::yxs])
        facit-yxs (<sub [::facit-yxs])
        react-key "bSZ4ppJTu8xhNmZYCoc8uW"
        show-facit? (<sub [::show-facit?])
        chord (<sub [::chord])
        nr-of-frets (<sub [::nr-of-frets])
        facit-fretboard-matrix (<sub [::facit-fretboard-matrix])]
    [:div
     [:h1 "Find all the notes in the chord!"]

     [:<>
      [views.common/chord-name (:key-of chord) (:chord/suffix chord) (:chord/explanation chord)]
      [:p "There are " (count facit-yxs) " tones for this chord, you have selected " (count yxs)]
      ;; [:p "Total:" (count facit-yxs)]
      ;; [:p "Selected:" (count yxs)]
      [:p "Find tones: " (->> (:interval-tones chord)
                              (map #(-> % name str/capitalize))
                              (str/join ", "))]
      [game-duration]
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

(defn ^:dev/after-load view [deps]
  (let [game-state (<sub [::game-state])]
    (case game-state
      :lobby [guide]
      :game [game-view]
      :success [success-view]
      [:p "Unknown `game-state`"])))

(rf-alpha/reg-event-fx
 ::start
 (fn [{:keys [db]} [_event-id {:keys [key-of harmonization-id instrument]}]]
   (js/console.log ::start)
   {:db (-> db
            (assoc-in key-of-path key-of)
            (assoc-in nr-of-frets-path 13)
            (assoc-in game-state-path :lobby)
            (assoc-in chord-idx-path 0)
            (assoc-in harmonization-id-path harmonization-id)
            (assoc-in instrument-path instrument))
    :fx [[:dispatch [::start-tick]]]}))

(defn routes [deps]
  (let [route-name ::chord-tones]
    ["/learn/chord-tones/:instrument/:key-of/:harmonization-id"
     {:name       route-name
      :view       [view deps]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:path  [:map
                           [:instrument       keyword?]
                           [:key-of           keyword?]
                           [:harmonization-id keyword?]]
                   :query events/Query}
      :controllers
      [{:parameters {:path  [:instrument :key-of :harmonization-id]
                     :query events/query-keys}
        :start      (fn [{p :path q :query}]
                      (>evt [::start (merge p q)]))
        :stop       (fn [{p :path q :query}]
                      (>evt [::stop-tick]))}]}]))

(defonce interval-handler         ;; notice the use of defonce
  (let [live-intervals (atom {})] ;; storage for live intervals
    (fn handler [{:keys [action id frequency event]}] ;; the effect handler
      (condp = action
        :clean   (doall ;; clean up all existing
                  (map #(handler {:action :end  :id  %1}) (keys @live-intervals)))
        :start   (swap! live-intervals assoc id
                        (js/setInterval #(>evt event) frequency))
        :end     (do (js/clearInterval (get @live-intervals id))
                     (swap! live-intervals dissoc id))))))

(rf-alpha/reg-fx ::tick interval-handler)

(comment
  (interval-handler {:action :clean})
  (interval-handler {:action    :start
                     :id        ::tick
                     :frequency 5000
                     :event     [::on-tick]})
  )

(rf-alpha/reg-event-fx
 :time/now
 [(rf-alpha/inject-cofx :now)]
 (fn [{:keys [now db]}]
   {:db (assoc db :time/now now)}))

(rf-alpha/reg-event-fx
  ::on-tick
  (fn [{:keys [db]} [_event-id]]
    (js/console.log ::on-tick)
    {:fx [[:dispatch [:time/now]]]}))

(rf-alpha/reg-flow
 (let [inputs {:time-now        [:time/now]
               :game-start-inst game-start-inst-path}]
   {:doc         "Calculate the game duration"
    :id          ::game-duration
    :live-inputs inputs
    :live?       :game-start-inst
    :inputs      inputs
    :output      (fn [{:keys [time-now game-start-inst]}]
                   (js/Math.floor (/ (- time-now game-start-inst) 1000)))
    :path        game-duration-path}))

(rf-alpha/reg-event-fx
 ::start-tick
 (fn [_ _]
   (js/console.log ::start-tick)
   {::tick {:action    :start
            :id        ::tick
            :frequency (if debug-enabled?
                          ;; Tick less to reduce noise.
                         (* 1000 5) ;; 5 seconds
                         1000)      ;; 1 second
            :event     [::on-tick]}
    :fx [[:dispatch [::on-tick]]]}))

(rf-alpha/reg-event-fx
 ::stop-tick
 (fn [_ _]
   {::tick {:action :clean}}))


(comment

  "http://localhost:8080/#/learn/chord-tones/mandolin/d/triads"

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
  (get-in @re-frame.db/app-db game-start-inst-path)
  (get-in @re-frame.db/app-db game-duration-path)
  (get @re-frame.db/app-db :time/now)

  (get-in @re-frame.db/app-db chord-idx-path)
  (>evt [::chord-idx 1])

  (get-in @re-frame.db/app-db facit-fretboard-matrix-path)
  (get-in @re-frame.db/app-db facit-yxs-path)

  debug-enabled?

  (>evt [::start-tick])
  (>evt [::stop-tick])

  :-)
