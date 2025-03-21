(ns se.jherrlin.music-theory.abc
  (:require [clojure.string :as str]))


(def all-pitches
  "https://github.com/paulrosen/abcjs/blob/947d3c8956847ca929deea5c33e1c2e44fc0a7fb/src/parse/all-notes.js#L3"
  {"C,,," {:octave 0 :tone "c"}
   "D,,," {:octave 0 :tone "d"}
   "E,,," {:octave 0 :tone "e"}
   "F,,," {:octave 0 :tone "f"}
   "G,,," {:octave 0 :tone "g"}
   "A,,," {:octave 0 :tone "a"}
   "B,,," {:octave 0 :tone "b"}
   "C,,"  {:octave 1 :tone "c"}
   "D,,"  {:octave 1 :tone "d"}
   "E,,"  {:octave 1 :tone "e"}
   "F,,"  {:octave 1 :tone "f"}
   "G,,"  {:octave 1 :tone "g"}
   "A,,"  {:octave 1 :tone "a"}
   "B,,"  {:octave 1 :tone "b"}
   "C,"   {:octave 2 :tone "c"}
   "D,"   {:octave 2 :tone "d"}
   "E,"   {:octave 2 :tone "e"}
   "F,"   {:octave 2 :tone "f"}
   "G,"   {:octave 2 :tone "g"}
   "A,"   {:octave 2 :tone "a"}
   "B,"   {:octave 2 :tone "b"}
   "C"    {:octave 3 :tone "c"}
   "D"    {:octave 3 :tone "d"}
   "E"    {:octave 3 :tone "e"}
   "F"    {:octave 3 :tone "f"}
   "G"    {:octave 3 :tone "g"}
   "A"    {:octave 3 :tone "a"}
   "B"    {:octave 3 :tone "b"}
   "c"    {:octave 4 :tone "c"}
   "d"    {:octave 4 :tone "d"}
   "e"    {:octave 4 :tone "e"}
   "f"    {:octave 4 :tone "f"}
   "g"    {:octave 4 :tone "g"}
   "a"    {:octave 4 :tone "a"}
   "b"    {:octave 4 :tone "b"}
   "c'"   {:octave 5 :tone "c"}
   "d'"   {:octave 5 :tone "d"}
   "e'"   {:octave 5 :tone "e"}
   "f'"   {:octave 5 :tone "f"}
   "g'"   {:octave 5 :tone "g"}
   "a'"   {:octave 5 :tone "a"}
   "b'"   {:octave 5 :tone "b"}
   "c''"  {:octave 6 :tone "c"}
   "d''"  {:octave 6 :tone "d"}
   "e''"  {:octave 6 :tone "e"}
   "f''"  {:octave 6 :tone "f"}
   "g''"  {:octave 6 :tone "g"}
   "a''"  {:octave 6 :tone "a"}
   "b''"  {:octave 6 :tone "b"}
   "c'''" {:octave 7 :tone "c"}
   "d'''" {:octave 7 :tone "d"}
   "e'''" {:octave 7 :tone "e"}
   "f'''" {:octave 7 :tone "f"}
   "g'''" {:octave 7 :tone "g"}
   "a'''" {:octave 7 :tone "a"}
   "b'''" {:octave 7 :tone "b"}})

(def regex-pitches
  (->> (keys all-pitches)
       (map #(str "[\\^=_]?" %))
       (clojure.string/join "|")
       (re-pattern)))

(defn re-seq-pitches [s]
  (re-seq regex-pitches s))

(defn flat? [s]
  (when (string? s)
    (str/includes? s "_")))

(defn sharp? [s]
  (when (string? s)
    (str/includes? s "^")))

(defn pitches-key-and-accidentals
  [{:keys [root accidentals pitches]}]
  (let [accidentals-map (->> accidentals
                             (mapv (fn [{:keys [note] :as accidental}]
                                     (assoc accidental :note (str/lower-case note))))
                             (map (juxt :note identity))
                             (into {}))]
    {:pitches (->> pitches
                   (mapv (fn [{:keys [accidental name] :as pitch}]
                           (let [cleaned                    (first (re-seq #"[A-Za-z,']{1,4}" name))
                                 {:keys [tone octav] :as m} (get all-pitches cleaned)]
                             {:octave octav
                              :tone   (let [acc (get-in accidentals-map [tone :acc])]
                                       (keyword
                                        (cond
                                          (= accidental "sharp")   (str tone "#")
                                          (= accidental "flat")    (str tone "b")
                                          (= accidental "natural") tone
                                          (= acc "sharp")          (str tone "#")
                                          (= acc "flat")           (str tone "b")
                                          (= acc "natural")        tone
                                          :else                    tone)))}))))
     :root    (-> root str/lower-case keyword)}))
