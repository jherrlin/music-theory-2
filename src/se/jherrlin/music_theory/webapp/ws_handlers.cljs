(ns se.jherrlin.music-theory.webapp.ws-handlers
  (:require [taoensso.timbre :as timbre]
            [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
            [re-frame.alpha :as re-frame]))




(defn incoming-events-handler [{:keys [id] :as event}]
  (timbre/debug "incoming-events-handler got event:" event)

  )
