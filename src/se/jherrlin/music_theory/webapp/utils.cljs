(ns se.jherrlin.music-theory.webapp.utils
  (:require [re-frame.alpha :as re-frame]))


(def <sub (comp deref re-frame/subscribe))
(defn <sub-flow [flow-id]
  (deref (re-frame/sub :flow {:id flow-id})))
(def >evt re-frame/dispatch)
