(ns se.jherrlin.music-theory.webapp.views.scale-calcs
  (:require
   [se.jherrlin.music-theory.music-theory :as music-theory]))


(defn scale-pattern-entities
  [key-of instrument-id scale-names]
  (->> (music-theory/scale-patterns-for-scale-and-instrument scale-names instrument-id)
       (music-theory/definitions-to-entities key-of instrument-id)))
