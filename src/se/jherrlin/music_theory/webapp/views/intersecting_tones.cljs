(ns se.jherrlin.music-theory.webapp.views.intersecting-tones
  (:require
   [re-frame.alpha :as rf-alpha]
   [reitit.coercion.malli]
   [clojure.string :as str]
   [reitit.frontend.easy :as rfe]
   [se.jherrlin.music-theory.webapp.events :as events]
   [se.jherrlin.music-theory.music-theory :as music-theory]
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [se.jherrlin.music-theory.webapp.views.instruments.fretboard2 :as fretboard2]
   [se.jherrlin.music-theory.fretboard :as fretboard]
   [se.jherrlin.music-theory.webapp.components.tonejs]
   [re-frame.db :as db]
   [se.jherrlin.music-theory.webapp.views.common :as views.common]
   [se.jherrlin.music-theory.webapp.views.common :as common]))


(def app-db-path ::intersecting-tones)

(defn path [x]
  (-> [app-db-path x] flatten vec))

(def key-of-1-path (path ::key-of-1))
(def key-of-2-path (path ::key-of-2))
(def id-1-path (path ::id-1))
(def id-2-path (path ::id-2))
(def instrument-path (path ::instrument))

(def events-
  [{:n ::key-of-1
    :p key-of-1-path}
   {:n ::key-of-2
    :p key-of-2-path}
   {:n ::id-1
    :p id-1-path}
   {:n ::id-2
    :p id-2-path}
   {:n ::instrument
    :p instrument-path}])

(doseq [{:keys [n s e d p]} events-]
  (rf-alpha/reg-sub n      (or s (fn [db [_]]     (get-in db (or p (path n)) d))))
  (rf-alpha/reg-event-db n (or e (fn [db [_ e]] (assoc-in db (or p (path n)) e)))))

(defn mmap-matrix
  [f matrix-1 matrix-2]
  (let [matrix-width (-> matrix-1 first count)
        m1 (apply concat matrix-1)
        m2 (apply concat matrix-2)]
    (->> (map f m1 m2)
         (partition matrix-width)
         (mapv #(mapv identity %)))))

(defn key-selection
  [k]
  (let [path-params          (<sub [:path-params])
        changed-query-params (<sub [:changed-query-params])
        current-route-name   (<sub [:current-route-name])]
    [:div {:style {:display        "flow"
                   :flow-direction "column"
                   :overflow-x     "auto"
                   :white-space    "nowrap"}}
     (for [{:keys [title key]}
           (->> [:a :a# :b :c :c# :d :d# :e :f :f# :g :g#]
                (map (fn [x] {:key   x
                              :title (-> x name str/capitalize)})))]
       ^{:key title}
       [:a {:style {:margin-right "10px"}
            :href  (rfe/href current-route-name (assoc path-params k key) changed-query-params)}
        [:button
         {:disabled (= (get path-params k) key)}
         title]])]))

(defn view [_deps]
  (let [react-key            "B8xw8F83Ci5vnPpVI2CXVy"
        nr-of-frets          16
        instrument           (<sub [::instrument])
        color-1              "rgb(252, 94, 3)"
        color-2              "rgb(252, 173, 3)"
        key-of-1             (<sub [::key-of-1])
        key-of-2             (<sub [::key-of-2])
        id-1                 (<sub [::id-1])
        id-2                 (<sub [::id-2])
        current-route-name   (<sub [:current-route-name])
        path-params          (<sub [:path-params])
        changed-query-params (<sub [:changed-query-params])]
    [:<>

     (when (and instrument key-of-1 key-of-2 id-1 id-2)
       [:div
        [common/menu]
        [:br]

        [:h1 "Intersecting tones"]

        [:p "This view shows intersecting tones between chords or scales."]
        [:p "Select a instrument, chords or scales and keys and study the fretboard."]

        [:h3 "Instrument:"]
        [common/select-instrument]

        (when id-1
          [:div {:style {:background color-1}}
           [:h3 "Chord or scale:"]
           [common/select-scale-or-chord :id-1]
           [:h3 "In the key of:"]
           [key-selection :key-of-1]
           [:code
            [:pre "Key of: " (-> key-of-1 name str/capitalize)]
            [:pre
             (with-out-str
               (cljs.pprint/pprint
                 (-> (music-theory/get-definition id-1)
                   (select-keys [:type :scale/scale-names :scale/intervals]))))]]])

        (when id-2
          [:div {:style {:background color-2}}
           [:h3 "Chord or scale:"]
           [common/select-scale-or-chord :id-2]
           [:h3 "In the key of:"]
           [key-selection :key-of-2]
           [:code
            [:pre "Key of: " (-> key-of-2 name str/capitalize)]
            [:pre
             (with-out-str
               (cljs.pprint/pprint
                 (-> (music-theory/get-definition id-2)
                   (select-keys [:type :scale/scale-names :scale/intervals]))))]]])

        [:br]

        [fretboard2/styled-view
         {:id             (str "hehe" react-key)
          :fretboard-matrix
          (music-theory/fretboard-matrix->fretboard2
           {}
           (mmap-matrix
            (fn [{m1-match? :match? :as m1} {m2-match? :match? :as m2}]
              (cond-> (merge m1 m2)
                m1-match?                 (assoc :circle-background color-1)
                m2-match?                 (assoc :circle-background color-2)
                (and m1-match? m2-match?) (assoc :circle-background (str "linear-gradient(45deg, " color-1 " 50%, " color-2 " 50%)"))))
            (music-theory/instrument-data-structure
             {:id         id-1 ;; #uuid "0b675c5b-a6fe-44fe-b7cc-6596c6c570a4" ;; Pentatonic bluegrass scale
              :instrument instrument
              :key-of     key-of-1}
             {:nr-of-frets nr-of-frets})
            (music-theory/instrument-data-structure
             {:id         id-2 ;;#uuid "bac1ab62-34df-4232-b205-b197d25d8892" ;; Pentatonic blues
              :instrument instrument
              :key-of     key-of-2}
             {:nr-of-frets nr-of-frets})))
          :entity-str     react-key
          :fretboard-size 1}]])]))

(rf-alpha/reg-event-fx
 ::start
 (fn [{:keys [db]} [_event-id {:keys [key-of-1 key-of-2 id-1 id-2 instrument]}]]
   {:db (-> db
            (assoc-in key-of-1-path key-of-1)
            (assoc-in key-of-2-path key-of-2)
            (assoc-in id-1-path id-1)
            (assoc-in id-2-path id-2)
            (assoc-in instrument-path instrument))}))

(defn ^:dev/after-load routes [deps]
  (let [route-name ::intersecting-tones]
    ["/intersecting-tones/:instrument/:key-of-1/:key-of-2/:id-1/:id-2"
     {:name  route-name
      :view  [view deps]
      :coercion   reitit.coercion.malli/coercion
      :parameters {:path [:map
                          [:instrument    keyword?]
                          [:key-of-1      keyword?]
                          [:key-of-2      keyword?]
                          [:id-1          uuid?]
                          [:id-2          uuid?]]}
      :controllers
      [{:identity (comp :path :parameters)
        :start (fn [params]
                 (events/do-on-url-change route-name params {})
                 (>evt [::start params]))}]}]))

(comment

  "http://localhost:8080/#/dev/intersecting-tones/mandolin/d/e/0b675c5b-a6fe-44fe-b7cc-6596c6c570a4/bac1ab62-34df-4232-b205-b197d25d8892"

  (mmap-matrix
    (fn [{m1-match? :match? :as m1} {m2-match? :match? :as m2}]
      (cond-> (merge m1 m2)
        m1-match?                 (assoc :circle-background "orange")
        m2-match?                 (assoc :circle-background "yellow")
        (and m1-match? m2-match?) (assoc :circle-background "linear-gradient(to right, orange 50%, yellow 50%)")))
    (music-theory/instrument-data-structure
      {:id         #uuid "e4db6b64-e5e1-46c1-a8ab-e1a99aa26a85" ;; Major chord pattern
       :instrument :mandolin
       :key-of     :d}
      {:nr-of-frets 10})
    (music-theory/instrument-data-structure
      {:id         #uuid "39af7096-b5c6-45e9-b743-6791b217a3df" ;; Major scale
       :instrument :mandolin
       :key-of     :d}
      {:nr-of-frets 10}))

  (music-theory/get-definition #uuid "0b675c5b-a6fe-44fe-b7cc-6596c6c570a4")
  (music-theory/get-definition #uuid "bac1ab62-34df-4232-b205-b197d25d8892")

  :fretboard-pattern/belongs-to
  :-)
