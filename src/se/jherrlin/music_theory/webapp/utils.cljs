(ns se.jherrlin.music-theory.webapp.utils
  (:require [re-frame.alpha :as re-frame]))


(def <sub (comp deref re-frame/subscribe))
(def >evt re-frame/dispatch)
