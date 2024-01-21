(ns se.jherrlin.music-theory.webapp.components.tonejs
  (:require
   [integrant.core :as ig]
   [taoensso.timbre :as timbre]
   [clojure.string :as str]
   ["tone/build/Tone" :as tone]))


(defn play-tone [tone octave]
  (let [synth (.toDestination (tone/Synth.))
        t (str (-> tone name str/capitalize) octave)]
    (timbre/debug "Playing tone" t)
    (.triggerAttackRelease synth t "8n")))


(defmethod ig/init-key :webapp/tonejs [_ {:keys []}]
  (timbre/info "Starting ToneJs object.")
  {:tonejs/play-tone play-tone})
