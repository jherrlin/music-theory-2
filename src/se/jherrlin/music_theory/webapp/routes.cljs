(ns se.jherrlin.music-theory.webapp.routes
  (:require
   [taoensso.timbre :as timbre]
   [reitit.frontend :as rf]
   [reitit.core :as r]
   [reitit.coercion.schema :as rsc]
   [se.jherrlin.music-theory.webapp.views.home :as home]
   [se.jherrlin.music-theory.webapp.views.focus :as focus]
   [se.jherrlin.music-theory.webapp.views.chord :as chord]
   [se.jherrlin.music-theory.webapp.views.scale :as scale]
   [se.jherrlin.music-theory.webapp.views.table :as table]
   [se.jherrlin.music-theory.webapp.views.harmonizations :as harmonizations]
   [se.jherrlin.music-theory.webapp.views.bookmarks :as bookmarks]
   [se.jherrlin.music-theory.webapp.views.find-chord :as find-chord]
   [se.jherrlin.music-theory.webapp.views.find-scale :as find-scale]
   [se.jherrlin.music-theory.webapp.views.dev :as dev]
   [se.jherrlin.music-theory.webapp.views.dev.fretboard2 :as dev.fretboard2]
   [se.jherrlin.music-theory.webapp.views.dev.learn-harmonizations :as dev.learn-harmonizations]
   [se.jherrlin.music-theory.webapp.views.learn.chord-tones :as learn.chord-tones]
   [se.jherrlin.music-theory.webapp.views.intersecting-tones :as intersecting-tones]
   [se.jherrlin.music-theory.webapp.views.scores :as scores]
   [se.jherrlin.music-theory.webapp.views.scores1 :as scores1]
   [se.jherrlin.music-theory.webapp.views.abcjs.examples :as abcjs.examples]
   [se.jherrlin.music-theory.webapp.views.abcjs.examples.basic :as abcjs.examples.basic]
   [se.jherrlin.music-theory.webapp.views.abcjs.examples.editor :as abcjs.examples.editor]
   [se.jherrlin.music-theory.webapp.views.abcjs.examples.editor-with-play :as examples.editor-with-play]
   [se.jherrlin.music-theory.webapp.views.abcjs.examples.basic-synth :as abcjs.examples.basic-synth]
   [se.jherrlin.music-theory.webapp.views.abcjs.examples.animation :as abcjs.examples.animation]
   [se.jherrlin.music-theory.webapp.views.abcjs.examples.editor-with-play-and-fretboard :as abcjs.examples.editor-with-play-and-fretboard]
   [se.jherrlin.music-theory.webapp.views.abcjs.examples.parser :as abcjs.examples.parser]
   [se.jherrlin.music-theory.webapp.views.abcjs.examples.editor-component :as abcjs.examples.editor-component]

   [se.jherrlin.music-theory.webapp.teacher.list :as teacher.list]
   [se.jherrlin.music-theory.webapp.teacher.document :as teacher.document]))


(defn routes [{:keys [backend?] :as deps}]
  (timbre/info "Collecting routes.")
  (->> [(home/routes deps)
        (focus/routes deps)
        (chord/routes deps)
        (scale/routes deps)
        (table/routes deps)
        (harmonizations/routes deps)
        (bookmarks/routes deps)
        (find-chord/routes deps)
        (find-scale/routes deps)
        (learn.chord-tones/routes deps)
        (intersecting-tones/routes deps)
        (scores/routes deps)
        (scores1/routes deps)

        ;; ABCjs examples
        (abcjs.examples/routes deps)
        (abcjs.examples.basic/routes deps)
        (abcjs.examples.editor/routes deps)
        (examples.editor-with-play/routes deps)
        (abcjs.examples.basic-synth/routes deps)
        (abcjs.examples.animation/routes deps)
        (abcjs.examples.editor-with-play-and-fretboard/routes deps)
        (abcjs.examples.parser/routes deps)
        (abcjs.examples.editor-component/routes deps)

        (when backend?
          (teacher.list/routes deps))
        (when backend?
          (teacher.document/routes deps))

        (dev/routes deps)
        (dev.fretboard2/routes deps)
        (dev.learn-harmonizations/routes deps)]
       (remove nil?)))

(comment
  (r/routes
   (rf/router
    (routes {})
    {:data {:coercion rsc/coercion}}))
  :-)
