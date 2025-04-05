(ns se.jherrlin.music-theory.server.server
  (:require [org.httpkit.server :as hk-server]
            [se.jherrlin.music-theory.server.routes :as server.routes]))


(def my-server (hk-server/run-server server.routes/handler {:port 8088}))

(comment
  ;; Graceful shutdown (wait <=100 msecs for existing reqs to complete):
  (my-server :timeout 100)
  )
