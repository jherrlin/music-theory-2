(ns se.jherrlin.music-theory.webapp.components.tonejs
  (:require
   [integrant.core :as ig]
   [taoensso.timbre :as timbre]
   [clojure.string :as str]
   [re-frame.alpha :as rf-alpha]
   ["tone/build/Tone" :as tone]))


(defn play-tone [{:keys [tone octave lenght]
                  :or {lenght "8n"}}]
  {:pre [(string? tone)]}
  (let [synth (.toDestination (tone/Synth.))
        tone  (str tone octave)]
    (timbre/debug "Playing tone" tone)
    (.triggerAttackRelease synth tone lenght)))

(rf-alpha/reg-fx
 :tonejs/play-tone!
 play-tone)

(rf-alpha/reg-event-fx
 :tonejs/play-tone
 (fn [_ [_ tone]]
   {:tonejs/play-tone! tone}))

(rf-alpha/reg-event-fx
 ::play
 (fn [_ _]
   {:tonejs/play-tone! {:tone "A" :octave "4"}}
   ))

(comment
  (play-tone {:tone "A" :octave "4"})
  (re-frame.alpha/dispatch [::play])
  )

(defmethod ig/init-key :webapp/tonejs [_ {:keys []}]
  (timbre/info "Starting ToneJs object.")
  play-tone)
