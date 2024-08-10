(ns se.jherrlin.music-theory.webapp.components.tonejs
  (:require
   [integrant.core :as ig]
   [taoensso.timbre :as timbre]
   [clojure.string :as str]
   [re-frame.alpha :as re-frame]
   ["tone/build/Tone" :as tone]))


(defn play-tone [tone octave]
  {:pre [(string? tone)]}
  (let [synth (.toDestination (tone/Synth.))
        tone  (str tone octave)]
    (timbre/debug "Playing tone" tone)
    (.triggerAttackRelease synth tone "8n")))

(re-frame/reg-fx
 ::play-tone
 (fn [[tone octave]]
   (play-tone tone octave)))

(re-frame/reg-event-fx
 :tonejs/play-tone
 (fn [_ [_ tone octave]]
   {::play-tone [tone octave]}))

(defmethod ig/init-key :webapp/tonejs [_ {:keys []}]
  (timbre/info "Starting ToneJs object.")

  play-tone)
