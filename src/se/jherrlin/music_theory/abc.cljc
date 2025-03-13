(ns se.jherrlin.music-theory.abc
  (:require [clojure.string :as str]))


(def all-pitches
  "https://github.com/paulrosen/abcjs/blob/947d3c8956847ca929deea5c33e1c2e44fc0a7fb/src/parse/all-notes.js#L3"
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

(defn flat? [s]
  (when (string? s)
    (str/includes? s "_")))

(defn sharp? [s]
  (when (string? s)
    (str/includes? s "^")))

(defn pitches-key-and-accidentals
  [{:keys [root accidentals pitches]}]
  (let [accidentals'    (->> accidentals
                             (mapv (fn [{:keys [note] :as accidental}]
                                     (assoc accidental :note (str/lower-case note)))))
        accidentals-map (->> accidentals'
                             (map (juxt :note identity))
                             (into {}))]
    {:pitches (->> pitches
                   (mapv (fn [{:keys [accidental name] :as pitch}]
                           (let [cleaned                    (first (re-seq #"[A-Za-z,']{1,4}" name))
                                 {:keys [tone octav] :as m} (get all-pitches cleaned)]
                             {:octav octav
                              :tone  (let [acc (get-in accidentals-map [tone :acc])]
                                      (cond-> tone
                                        (= acc "sharp")          (str "#")
                                        (= accidental "sharp")   (str "#")
                                        (= acc "flat")           (str "b")
                                        (= accidental "flat")    (str "b")
                                        (= acc "natural")        identity
                                        (= accidental "natural") identity
                                        :always                  keyword))}))))
     :root    (-> root str/lower-case keyword)}))
